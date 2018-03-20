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
)

// TODO: write tests
// TODO: implement a toggle switch for editing messages in the conversation
// TODO: implement authorization tokens

func register(user *ds.User, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	errStr := "cannot register user:"

	// verify new username
	if message.Username == nil || message.Password == nil || message.Profile == nil {
		err := e.New("missing username, password and/or profile", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)

		serr := sendServerMessage(user.Connection.Conn, rsp)
		if serr != nil {
			return serr
		}
		return e.ErrMissingParameter
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 || len(message.Profile.FirstName) == 0 || len(message.Profile.LastName) == 0 || len(message.Profile.Email) == 0 ||
		len(message.Profile.Phone) == 0 || len(message.Profile.SecurityQuestion) == 0 || len(message.Profile.SecurityAnswer) == 0 {
		err := e.New("empty profile.[firstName | lastName | email | phone | securityQuestion | securityAnswer", e.EmptyParameter)
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
	u, err := ds.CreateUserAccount(*message.Username, *message.Password, *message.Profile)
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
		user.Contacts = *contacts
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
		sendServerMessageToUser(contact, message)
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
	return json.NewEncoder(conn).Encode(message)
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
			// first message received is either Login or Register
			err = getServerMessage(conn, message)
			if err != nil {
				log.Println("cannot get message from client:", err)
				sockClosed = true
				break
			}

			switch message.Status {
			case msg.ActionLogIn:
				err = logIn(usr, message)
				if err != nil {
					log.Println("cannot log user in:", err)
					if err == e.ErrInvalidUsername || err == e.ErrInvalidPassword {
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
				err = register(usr, message)
				if err != nil {
					log.Println("cannot register user:", err)
					if err == e.ErrExistingAccount {
						rsp.SetError(e.ErrExistingAccount)
						err = sendServerMessage(conn, rsp)
						sockClosed = err != nil
					}
					break
				}
				loggedIn = true
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
		rsp.Profile = usr.Profile
		rsp.Contacts = new(map[string]msg.Contact)
		*rsp.Contacts = make(map[string]msg.Contact)

		for _, contact := range usr.Contacts {
			(*rsp.Contacts)[contact] = msg.Contact{
				Online:           ds.ConnectionsContains(contact),
				SentMessages:     0,
				ReceivedMessages: 0,
				/* TODO
				Games:,
				FriendshipLevel:,
				*/
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
				log.Println("cannot get message from client:", err)
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
