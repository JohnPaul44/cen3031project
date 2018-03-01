package main

import (
	"net/http"
	"github.com/gorilla/mux"
	"fmt"
	"log"
	"context"
	"cloud.google.com/go/datastore"
	"time"
	"encoding/json"
	"net"
)

const ProjectID = "cen3031-192414"

// TODO: write tests
// TODO: implement a toggle switch for editing messages in the conversation
// TODO: implement authorization tokens

func register(user *DSUser, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := "cannot register user:"

	// verify new username
	if message.Username == nil || message.Password == nil || message.Profile == nil {
		e := NewError("missing username, password and/or profile", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)

		serr := sendServerMessage(user.connection.conn, rsp)
		if serr != nil {
			return serr
		}
		return ErrMissingParameter
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 || len(message.Profile.FirstName) == 0 || len(message.Profile.LastName) == 0 || len(message.Profile.Email) == 0 ||
		len(message.Profile.Phone) == 0 || len(message.Profile.SecurityQuestion) == 0 || len(message.Profile.SecurityAnswer) == 0 {
		e := NewError("empty profile.[firstName | lastName | email | phone | securityQuestion | securityAnswer", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)

		serr := sendServerMessage(user.connection.conn, rsp)
		if serr != nil {
			return serr
		}
		return ErrEmptyParameter
	}

	errStr = fmt.Sprintf("cannot register %s %s as %s:", message.Profile.FirstName, message.Profile.LastName, *message.Username)

	// create account
	u, err := createUserAccount(*message.Username, *message.Password, *message.Profile)
	if err != nil {
		if err == ErrExistingAccount {
			log.Println(ErrorTag, errStr, "account already exists")
			rsp.setError(ErrExistingAccount)
		} else {
			log.Println(ErrorTag, errStr, err)
			rsp.setError(ErrInternalServer)
		}

		serr := sendServerMessage(user.connection.conn, rsp)
		if serr != nil {
			return serr
		}
		return err
	}

	*user = *u

	log.Printf("%s %s created an account with username %s\n", user.Profile.FirstName, user.Profile.LastName, user.username)

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

		err := sendServerMessage(user.connection.conn, rsp)
		if err != nil {
			return err
		}
		return ErrMissingParameter
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 {
		e := NewError("empty username and/or password", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)

		err := sendServerMessage(user.connection.conn, rsp)
		if err != nil {
			return err
		}
		return ErrEmptyParameter
	}

	errStr = fmt.Sprintf("cannot log in as '%s':", *message.Username)

	u, err := getUserAccountAuthenticated(*message.Username, *message.Password)
	if err != nil {
		return err
	}

	*user = *u

	contacts, err := getContacts(user)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get contacts:", err)
	} else {
		user.contacts = *contacts
	}

	log.Printf("%s logged in\n", user.username)

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

	log.Printf("%s is %s\n", user.username, func() string {
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

func getServerMessage(conn net.Conn, message *ServerMessage) error {
	return json.NewDecoder(conn).Decode(message)
}

func sendServerMessage(conn net.Conn, message *ServerMessage) error {
	return json.NewEncoder(conn).Encode(message)
}

func sendServerMessageToUser(username string, message *ServerMessage) {
	_, contains := conns[username]
	if contains {
		for _, conn := range conns[username].connections {
			err := sendServerMessage(conn.conn, message)
			if err != nil {
				log.Printf("socket closed for '%s'\n", username)
			}
		}
	}
}

func handleConnect(w http.ResponseWriter, _ *http.Request) {
	hj, ok := w.(http.Hijacker)
	if !ok {
		http.Error(w, "Hijacking not supported", http.StatusInternalServerError)
		return
	}

	conn, _, err := hj.Hijack()
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	defer conn.Close()

	var loggedIn bool
	sockClosed := false
	connection := new(Connection)
	connection.time = time.Now()
	connection.conn = conn

	for !sockClosed {
		loggedIn = false
		usr := new(DSUser)
		usr.connection = connection
		rsp := new(ServerMessage)
		msg := new(ServerMessage)

		for !loggedIn {
			// first message received is either Login or Register
			err = getServerMessage(conn, msg)
			if err != nil {
				log.Println("cannot get message from client:", err)
				sockClosed = true
				break
			}

			switch msg.Status {
			case ActionLogIn:
				err = logIn(usr, msg)
				if err != nil {
					log.Println("cannot log user in:", err)
					if err == ErrInvalidUsername || err == ErrInvalidPassword {
						rsp.setError(ErrInvalidLogin)
						err = sendServerMessage(conn, rsp)
						sockClosed = err != nil
					}
					break
				}
				log.Println("user logged in:", usr.username)
				loggedIn = true
				break
			case ActionRegister:
				err = register(usr, msg)
				if err != nil {
					log.Println("cannot register user:", err)
					if err == ErrExistingAccount {
						rsp.setError(ErrExistingAccount)
						err = sendServerMessage(conn, rsp)
						sockClosed = err != nil
					}
					break
				}
				loggedIn = true
				break
			default:
				// any other message requires the user to be logged in
				e := NewError("not logged in", ErrorUnauthorized)
				log.Println("cannot perform action:", e)
				rsp.setError(e)
				err = sendServerMessage(conn, rsp)
				sockClosed = err != nil
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
		connection.conn = conn
		usr.connection = connection
		conns.add(usr)

		rsp.clear()
		rsp.Status = NotificationLoggedIn
		rsp.Username = &usr.username
		rsp.Profile = usr.Profile
		rsp.Contacts = new([]Contact)
		for _, contact := range usr.contacts {
			*rsp.Contacts = append(*rsp.Contacts, Contact{Username:contact, Online:conns.contains(contact)})
		}
		rsp.Conversations, err = getConversations(usr)
		if err != nil {
			log.Println(ErrorTag, "cannot get conversations:", err)
			break
		}

		// return logged in message with user information
		err = sendServerMessage(conn, rsp)
		if err != nil {
			log.Println(ErrorTag, "cannot send response to client:", err)
			break
		}

		// event loop
		for loggedIn && !sockClosed {
			err = getServerMessage(conn, msg)
			if err != nil {
				log.Println("cannot get message from client:", err)
				sockClosed = true
				break
			}

			err = handleServerMessage(usr, conn, msg)
			if err != nil {
				log.Println("cannot handle message from client:", err)
				sockClosed = true
				break
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

func handleHome(w http.ResponseWriter, _ *http.Request) {
	w.Write([]byte("<H1>System.Out.Chat()</H1>"))
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
	r.HandleFunc("/", handleHome)
	r.HandleFunc("/connect", handleConnect).Methods("GET") // "GET /connect HTTP/1.0\r\n\r\n"

	log.Println("Server started")
	log.Fatal(http.ListenAndServe(":8675", r))
}
