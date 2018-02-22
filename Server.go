package main

import (
	"net/http"
	"github.com/gorilla/mux"
	"fmt"
	"log"
	"context"
	"cloud.google.com/go/datastore"
	"time"
	"io"
)

const ProjectID = "cen3031-192414"

// TODO: write tests
// TODO: implement a toggle switch for editing messages in the conversation
// TODO: implement authorization tokens

func socketClosed(err error) bool {
	return err == io.EOF || err == io.ErrUnexpectedEOF || err == io.ErrClosedPipe
}

func register(user *DSUser, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := "cannot register user:"

	// verify new username
	if message.Username == nil || message.Password == nil || message.Profile == nil {
		e := NewError("missing username, password and/or profile", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return ErrMissingParameter
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 || len(message.Profile.Name) == 0 || len(message.Profile.Email) == 0 {
		e := NewError("empty username, password, profile.name and/or profile.email", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return ErrEmptyParameter
	}

	errStr = fmt.Sprintf("cannot register %s as '%s':", message.Profile.Name, *message.Username)

	// create account
	user, err := createUserAccount(*message.Username, *message.Password, *message.Profile)
	if err != nil {
		if err == ErrExistingAccount {
			log.Println(ErrorTag, errStr, "account already exists")
			rsp.setError(ErrExistingAccount)
		} else {
			log.Println(ErrorTag, errStr, err)
			rsp.setError(ErrInternalServer)
		}

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return err
	}

	log.Printf("%s (%s) created an account\n", user.Profile.Name, user.username)

	return nil
}

func logIn(user *DSUser, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := "cannot log user in:"

	// verify credentials
	if message.Username == nil || message.Password == nil {
		e := NewError("missing username and/or password", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)

		err := sendServerMessage(user.connection.bufrw, rsp)
		if err != nil {
			return err
		}
		return ErrMissingParameter
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 {
		e := NewError("empty username and/or password", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)

		err := sendServerMessage(user.connection.bufrw, rsp)
		if err != nil {
			return err
		}
		return ErrEmptyParameter
	}

	errStr = fmt.Sprintf("cannot log in as '%s':", *message.Username)

	user, err := getUserAccountAuthenticated(*message.Username, *message.Password)
	if err != nil {
		return err
	}

	user.username = *message.Username

	contacts, err := getContacts(user)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get contacts:", err)
	} else {
		user.contacts = *contacts
	}

	log.Printf("%s (%s) logged in\n", user.Profile.Name, user.username)

	return nil
}

func updateOnlineStatus(user *DSUser, online bool) {
	// create notification message
	msg := new(ServerMessage)
	msg.Status = NotificationUserOnlineStatus
	msg.Online = &online
	msg.Username = &user.username

	// send notification to online contacts
	for _, contact := range user.contacts {
		sendServerMessageToUser(contact, msg)
	}

	log.Printf("%s (%s) is %s\n", user.Profile.Name, user.username, func() string {
		if online {
			return "online"
		} else {
			return "offline"
		}
	}())
}

func remove(s []string, r string) ([]string, bool) {
	for i, v := range s {
		if v == r {
			return append(s[:i], s[i+1:]...), true
		}
	}
	return s, false
}

func handleConnect(w http.ResponseWriter, _ *http.Request) {
	hj, ok := w.(http.Hijacker)
	if !ok {
		http.Error(w, "Hijacking not supported", http.StatusInternalServerError)
		return
	}

	s, bufrw, err := hj.Hijack()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	defer s.Close()

	var loggedIn bool
	sockClosed := false

	for !sockClosed {
		loggedIn = false
		usr := new(DSUser)
		rsp := new(ServerMessage)
		msg := new(ServerMessage)
		for !loggedIn {
			// first message received is either Login or Register
			err = getServerMessage(bufrw, msg)
			if err != nil {
				log.Println("cannot get message from client:", err)
				err = sendServerMessage(bufrw, rsp)
				if socketClosed(err) {
					sockClosed = true
					break
				}

				// if the socket is still open, the only other errors result from invalid JSON
				rsp.setError(ErrInvalidJSON)
				err = sendServerMessage(bufrw, rsp)
				if socketClosed(err) {
					sockClosed = true
					break
				}
			}

			switch msg.Status {
			case ActionLogIn:
				err = logIn(usr, msg)
				if err != nil {
					log.Println("cannot log user in:", err)
					sockClosed = socketClosed(err)
					break
				}
				loggedIn = true
				break
			case ActionRegister:
				err = register(usr, msg)
				if err != nil {
					sockClosed = socketClosed(err)
					break
				}
				loggedIn = true
				break
			default:
				// any other message requires the user to be logged in
				e := NewError("not logged in", ErrorUnauthorized)
				log.Println("cannot perform action:", e)
				rsp.setError(e)
				serr := sendServerMessage(bufrw, rsp)
				sockClosed = socketClosed(serr)
				break
			}

			if sockClosed {
				break
			}

			rsp.clear()
		}

		if sockClosed {
			break
		}

		// add connection to connections map
		connection := new(Connection)
		connection.time = time.Now()
		connection.bufrw = bufrw
		usr.connection = connection
		conns.add(usr)

		rsp.clear()
		rsp.Status = NotificationLoggedIn
		rsp.Username = &usr.username
		rsp.Profile = &usr.Profile
		rsp.Contacts = new([]Contact)
		for _, contact := range usr.contacts {
			*rsp.Contacts = append(*rsp.Contacts, Contact{Username:contact, Online:conns.contains(contact)})
		}
		rsp.Conversations, err = getConversations(usr)
		if err != nil {
			break
		}

		// return logged in message with user information
		err = sendServerMessage(bufrw, rsp)
		if err != nil {
			break
		}

		// event loop
		for loggedIn && !sockClosed {
			err = getServerMessage(bufrw, msg)
			if err != nil {
				if socketClosed(err) {
					sockClosed = true
					break
				}

				log.Println("error getting message from client:", err)
				continue
			}

			err = handleServerMessage(usr, bufrw, msg)
			if err != nil {
				if socketClosed(err) {
					sockClosed = true
					break
				}

				log.Println("error handling message:", err)
			}

			if msg.Status == ActionLogOut {
				loggedIn = false
				break
			}
		}
		conns.remove(usr)
	}

	log.Println("Socket closed")
}

var c = context.Background()
var client *datastore.Client

func main() {
	var err error
	client, err = datastore.NewClient(c, ProjectID)
	if err != nil {
		log.Fatal(ErrorTag, "cannot create datastore client:", err)
	}

	r := mux.NewRouter()
	r.HandleFunc("/connect", handleConnect).Methods("GET")

	log.Println("Server started")
	log.Fatal(http.ListenAndServe(":8675", r))
}
