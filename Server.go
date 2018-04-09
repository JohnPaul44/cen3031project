package main

import (
	"net/http"
	"github.com/gorilla/mux"
	"fmt"
	"log"
	"time"
	"encoding/json"
	"net"
	e "./Errors"
	ds "./Datastore"
	msg "./ServerMessage"
	"io"
)

// TODO: implement a toggle switch for editing messages in the conversation
// TODO: implement authorization tokens

func register(user *ds.User, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationLoggedIn
	errStr := "cannot register user:"

	// verify new username
	if message.Username == nil || message.Password == nil || message.Profile == nil || message.SecurityQuestion == nil || message.SecurityAnswer == nil {
		err := e.New("missing username, password, profile, and/or security question/answer", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)

		serr := sendServerMessage(user.Connection.Conn, rsp)
		if serr != nil {
			return serr
		}
		return e.ErrMissingParameter
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 || len(message.Profile.FirstName) == 0 || len(message.Profile.LastName) == 0 || len(message.Profile.Email) == 0 ||
		len(message.Profile.Phone) == 0 || len(message.Profile.Color) == 0 || len(*message.SecurityQuestion) == 0 || len(*message.SecurityAnswer) == 0 {
		err := e.New("empty username, password, profile.[firstName | lastName | email | phone | color], securityQuestion, and/or securityAnswer", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)

		serr := sendServerMessage(user.Connection.Conn, rsp)
		if serr != nil {
			return serr
		}
		return e.ErrEmptyParameter
	}

	errStr = fmt.Sprintf("cannot register %s %s as %s:", message.Profile.FirstName, message.Profile.LastName, *message.Username)

	// create account
	u, err := ds.CreateUserAccount(*message.Username, *message.Password, *message.Profile, *message.SecurityQuestion, *message.SecurityAnswer)
	if err != nil {
		if err == e.ErrExistingAccount {
			log.Println(e.Tag, errStr, "account already exists")
			rsp.SetError(e.ErrExistingAccount)
		} else {
			log.Println(e.Tag, errStr, err)
			rsp.SetError(e.ErrInternalServer)
		}

		serr := sendServerMessage(user.Connection.Conn, rsp)
		if serr != nil {
			return serr
		}
		return err
	}

	*user = *u

	log.Printf("%s %s created an account with username %s\n", user.Profile.FirstName, user.Profile.LastName, user.Username)

	return nil
}

func logIn(user *ds.User, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationLoggedIn
	errStr := "cannot log user in:"

	// verify credentials
	if message.Username == nil || message.Password == nil {
		err := e.New("missing username and/or password", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)

		serr := sendServerMessage(user.Connection.Conn, rsp)
		if serr != nil {
			return serr
		}
		return e.ErrMissingParameter
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 {
		err := e.New("empty username and/or password", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)

		serr := sendServerMessage(user.Connection.Conn, rsp)
		if serr != nil {
			return serr
		}
		return e.ErrEmptyParameter
	}

	errStr = fmt.Sprintf("cannot log in as '%s':", *message.Username)

	u, err := ds.GetUserAccountAuthenticated(*message.Username, *message.Password)
	if err != nil {
		return err
	}

	*user = *u

	contacts, err := ds.GetContacts(user)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get contacts:", err)
	} else {
		user.Contacts = contacts
	}

	log.Printf("%s logged in\n", user.Username)

	return nil
}

func updateOnlineStatus(user *ds.User, online bool) {
	// create notification message
	message := new(msg.ServerMessage)
	message.Status = msg.NotificationUserOnlineStatus
	message.Online = &online
	message.Username = &user.Username

	// send notification to online contacts
	for _, contact := range user.Contacts {
		sendServerMessageToUser(contact.Contact, message)
	}

	log.Printf("%s is %s\n", user.Username, func() string {
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

func getServerMessage(conn net.Conn, message *msg.ServerMessage) error {
	return json.NewDecoder(conn).Decode(message)
}

func sendServerMessage(conn net.Conn, message *msg.ServerMessage) error {
	bytes, err := json.Marshal(message)
	if err != nil {
		return err
	}

	log.Printf("Sending message: %s\n", bytes)
	_, err = conn.Write(bytes)
	if err != nil {
		log.Println("cannot send message", err)
	}
	return err
}

func sendServerMessageToUser(username string, message *msg.ServerMessage) {
	connections := ds.GetConnections(username)
	for _, conn := range connections {
		err := sendServerMessage(conn.Conn, message)
		if err != nil {
			log.Printf("socket closed for '%s'\n", username)
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
	connection := new(ds.Connection)
	connection.Time = time.Now()
	connection.Conn = conn

	for !sockClosed {
		loggedIn = false
		usr := new(ds.User)
		usr.Connection = connection
		rsp := new(msg.ServerMessage)
		message := new(msg.ServerMessage)

		for !loggedIn {
			err = getServerMessage(conn, message)
			if err != nil {
				if err != io.EOF {
					log.Println("cannot get message from client:", err)
				}

				sockClosed = true
				break
			}
			rsp.Status = message.Status

			switch message.Status {
			case msg.ActionLogIn:
				rsp.Status = msg.NotificationLoggedIn

				err = logIn(usr, message)
				if err != nil {
					log.Println("cannot log user in:", err)
					if err == e.ErrInvalidUsername || err == e.ErrInvalidPassword || err == e.ErrInvalidLogin {
						rsp.SetError(e.ErrInvalidLogin)
						err = sendServerMessage(conn, rsp)
						sockClosed = err != nil
					}
					break
				}
				log.Println("user logged in:", usr.Username)
				loggedIn = true
				break
			case msg.ActionRegister:
				rsp.Status = msg.NotificationLoggedIn

				err = register(usr, message)
				if err != nil {
					log.Println("cannot register user:", err)
					if err == e.ErrExistingAccount {
						rsp.SetError(e.ErrExistingAccount)
						sockClosed = sendServerMessage(conn, rsp) != nil
					}
					break
				}
				loggedIn = true
				break
			case msg.ActionRequestSecurityQuestion:
				rsp.Status = msg.NotificationSecurityQuestion

				if message.Username == nil {
					err := e.New("missing username", e.MissingParameter)
					rsp.SetError(err)
					sockClosed = sendServerMessage(conn, rsp) != nil
					break
				}

				if len(*message.Username) == 0 {
					err := e.New("empty username", e.EmptyParameter)
					rsp.SetError(err)
					sockClosed = sendServerMessage(conn, rsp) != nil
					break
				}

				user, err := ds.GetUserAccount(*message.Username)
				if err != nil {
					// handle error
					if err == e.ErrInvalidUsername {
						// invalid username
						rsp.SetError(e.ErrInvalidUsername)
						sockClosed = sendServerMessage(conn, rsp) != nil
					} else {
						log.Println("cannot get user account:", err)
						rsp.SetError(e.ErrInternalServer)
						sockClosed = sendServerMessage(conn, rsp) != nil
					}
					break
				}

				rsp.SecurityQuestion = &user.SecurityQuestion
				sockClosed = sendServerMessage(conn, rsp) != nil
				break
			case msg.ActionChangePassword:
				rsp.Status = msg.NotificationPasswordChanged

				if message.Username == nil || message.Password == nil || message.SecurityAnswer == nil || message.Phone == nil {
					// handle missing parameter
					err := e.New("missing username, password, securityAnswer and/or phone", e.MissingParameter)
					rsp.SetError(err)
					sockClosed = sendServerMessage(conn, rsp) != nil
					break
				}

				if len(*message.Username) == 0 || len(*message.Password) == 0 || len(*message.SecurityAnswer) == 0 || len(*message.Phone) == 0 {
					// handle empty parameter
					err := e.New("empty username, password, securityAnswer and/or phone", e.EmptyParameter)
					rsp.SetError(err)
					sockClosed = sendServerMessage(conn, rsp) != nil
					break
				}

				user, err := ds.GetUserAccount(*message.Username)
				if err != nil {
					// handle error
					if err == e.ErrInvalidUsername {
						// invalid username
						rsp.SetError(e.ErrInvalidUsername)
						sockClosed = sendServerMessage(conn, rsp) != nil
					} else {
						log.Println("cannot get user account:", err)
						rsp.SetError(e.ErrInternalServer)
						sockClosed = sendServerMessage(conn, rsp) != nil
					}
					break
				}

				if *message.SecurityAnswer != user.SecurityAnswer {
					// handle error
					err := e.New("security parameters do not match", e.Unauthorized)
					rsp.SetError(err)
					sockClosed = sendServerMessage(conn, rsp) != nil
					break
				}

				passHash, err := ds.GeneratePasswordHash(*message.Password)
				if err != nil {
					// handle error
					log.Println("cannot generate password hash:", err)
					rsp.SetError(e.ErrInternalServer)
					sockClosed = sendServerMessage(conn, rsp) != nil
					break
				}

				user.PassHash = passHash

				err = ds.UpdateUserAccount(user)
				if err != nil {
					// handle error
					log.Println("cannot update user account:", err)
					rsp.SetError(e.ErrInternalServer)
					sockClosed = sendServerMessage(conn, rsp) != nil
					break
				}

				log.Println("user changed password")

				// send msg.NotificationPasswordChanged
				sockClosed = sendServerMessage(conn, rsp) != nil

				break
			default:
				// any other message requires the user to be logged in
				err := e.New("not logged in", e.Unauthorized)
				log.Println("cannot perform action:", err)
				rsp.SetError(err)
				serr := sendServerMessage(conn, rsp)
				sockClosed = serr != nil
				break
			}

			if sockClosed {
				break
			}

			rsp.Clear()
		}

		if sockClosed {
			break
		}

		// add connection to connections map
		connection := new(ds.Connection)
		connection.Time = time.Now()
		connection.Conn = conn
		usr.Connection = connection
		firstOnline := ds.AddConnection(usr)
		if firstOnline {
			updateOnlineStatus(usr, true)
		}

		rsp.Clear()
		rsp.Status = msg.NotificationLoggedIn
		rsp.Username = &usr.Username
		rsp.Profile = new(msg.Profile)
		*rsp.Profile = usr.Profile
		rsp.Contacts = new(map[string]msg.Contact)
		*rsp.Contacts = make(map[string]msg.Contact)

		for _, contact := range usr.Contacts {
			contactProfile, err := ds.GetUserProfile(contact.Contact)
			if err != nil {
				continue
			}

			(*rsp.Contacts)[contact.Contact] = msg.Contact{
				Online: ds.ConnectionsContains(contact.Contact),
				Added: contact.Added,
				Profile: contactProfile,
				Statistics: contact.Statistics,
			}
		}

		rsp.Conversations, err = ds.GetConversations(usr)
		if err != nil {
			log.Println(e.Tag, "cannot get conversations:", err)
			break
		}

		// return logged in message with user information
		err = sendServerMessage(conn, rsp)
		if err != nil {
			log.Println(e.Tag, "cannot send response to client:", err)
			break
		}

		// event loop
		for loggedIn && !sockClosed {
			err = getServerMessage(conn, message)
			if err != nil {
				if err != io.EOF {
					log.Println("cannot get message from client:", err)
				}

				sockClosed = true
				break
			}

			err = handleServerMessage(usr, conn, message)
			if err != nil {
				log.Println("cannot handle message from client:", err)
				sockClosed = true
				break
			}

			if message.Status == msg.ActionLogOut {
				loggedIn = false
				break
			}
		}
		lastOnline := ds.RemoveConnection(usr)
		if lastOnline {
			updateOnlineStatus(usr, false)
		}
	}

	log.Println("Socket closed")
}

func handleHome(w http.ResponseWriter, _ *http.Request) {
	w.Write([]byte("<H1>System.Out.Chat()</H1>"))
}

func main() {
	err := ds.Connect()
	if err != nil {
		log.Fatal(e.Tag, "cannot create datastore client:", err)
	}

	r := mux.NewRouter()
	r.HandleFunc("/", handleHome)
	r.HandleFunc("/connect", handleConnect).Methods("GET") // "GET /connect HTTP/1.0\r\n\r\n"

	log.Println("Server started")
	log.Fatal(http.ListenAndServe(":8675", r))
}
