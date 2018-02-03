package main

import (
	"net/http"
	"github.com/gorilla/mux"
	"fmt"
	"log"
	"context"
	"cloud.google.com/go/datastore"
	"golang.org/x/crypto/bcrypt"
	"encoding/json"
	"bufio"
)

const ProjectID = "cen3031-192414"

type Message struct {
	To		*[]string	`json:"to,omitempty"`
	Group	*bool		`json:"group,omitempty"`
	From	*string		`json:"from,omitempty"`
	Text	*string		`json:"text,omitempty"`
}

type Conversation struct {
	Members		[]string	`json:"members"`
	Messages	*[]Message	`json:"messages"`
}

type ServerMessage struct {
	Status			int             `json:"status"`
	Username 		*string         `json:"username,omitempty"`
	Password		*string         `json:"password,omitempty"`
	Email			*string         `json:"email,omitempty"`
	Name			*string         `json:"name,omitempty"`
	Phone			*string         `json:"phone,omitempty"`
	Contacts		*[]string       `json:"contacts,omitempty"`
	Conversations	*[]Conversation	`json:"conversations,omitempty"`
	Message			*Message        `json:"message,omitempty"`
}

const (
	ErrorNotLoggedIn		= iota
	Error					= iota
	StatusRegister        	= iota 	// requires username, password, name, and email
	ErrorExistingAccount 	= iota 	// returns username
	StatusLogin           	= iota 	// requires username and password
	ErrorMissingParameter 	= iota 	// returns nothing
	ErrorInvalidUsername  	= iota 	// returns username
	ErrorInvalidPassword  	= iota 	// returns nothing
	StatusLoggedIn        	= iota 	// returns username, email, phone, contacts, conversations
	StatusLogOut		  	= iota	// request to end session, requires nothing
	StatusLoggedOut		  	= iota	// notification that the session has ended, returns nothing
	StatusAddContact	  	= iota 	// requires username
	StatusContactAdded		= iota	// returns username
	StatusRemoveContact		= iota	// requires username
	StatusContactRemoved	= iota	// returns username
	StatusUpdateProfile	  	= iota 	// requires profile fields
	StatusSendMessage	  	= iota	// requires Message
	StatusMessageDelivered	= iota	// returns Message
	StatusMessageRead		= iota	// returns Username, Message
	StatusTyping			= iota	// returns Username, Message
	StatusMessageReceived 	= iota 	// returns Message
	StatusSearchUser      	= iota 	// requires username, email, or phone
)

type Connections map[string] *bufio.ReadWriter

var conns = make(Connections)

func (conns Connections) contains(username string) bool {
	_, contains := conns[username]
	return contains
}

func (conns *Connections) add(username string, readWriter *bufio.ReadWriter) {
	(*conns)[username] = readWriter
}

func (conns *Connections) remove(username string) {
	delete(*conns, username)
}

func remove(s []string, r string) []string {
	for i, v := range s {
		if v == r {
			return append(s[:i], s[i+1:]...)
		}
	}
	return s
}

func handleConnect(w http.ResponseWriter, req *http.Request) {
	c := context.Background()
	client, err := datastore.NewClient(c, ProjectID)
	if err != nil {
		http.Error(w, "Could not create datastore client", http.StatusInternalServerError)
		return
	}

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

	d := json.NewDecoder(bufrw)
	e := json.NewEncoder(bufrw)

	usr := new(DSUser)
	rsp := new(ServerMessage)
	msg := new(ServerMessage)

	loggedIn := false

	for !loggedIn {
		// first message received is either a Login or Register
		m := new(ServerMessage)
		d.Decode(m)
		r := new(ServerMessage)

		switch m.Status {
		case StatusLogin:
			// verify credentials
			if m.Username == nil || m.Password == nil {
				r.Status = ErrorMissingParameter
				e.Encode(r)
				continue
			}

			userKey := datastore.NameKey(KindUser, *m.Username, nil)
			lu := new(DSUser)
			err := client.Get(c, userKey, lu)
			if err != nil {
				if err == datastore.ErrNoSuchEntity {
					// user doesn't exist, return invalid username
					r.Status = ErrorInvalidUsername
					*r.Username = *m.Username
					e.Encode(r)
					continue
				} else {
					r.Status = Error
					e.Encode(r)
					continue
				}
			}

			err = bcrypt.CompareHashAndPassword(lu.PassHash, []byte(*m.Password))
			if err != nil {
				// invalid password
				r.Status = ErrorInvalidPassword
				e.Encode(r)
				continue
			}

			msg = m
			usr = lu
			loggedIn = true
			break
		case StatusRegister:
			// verify new username
			if m.Username == nil || m.Password == nil || m.Email == nil || m.Name == nil {
				r.Status = ErrorMissingParameter
				e.Encode(r)
				continue
			}

			userKey := datastore.NameKey(KindUser, *m.Username, nil)
			lu := new(DSUser)
			err := client.Get(c, userKey, lu)
			if err == nil {
				// user account already exists
				r.Status = ErrorExistingAccount
				e.Encode(r)
				continue
			} else if err != datastore.ErrNoSuchEntity {
				r.Status = Error
				e.Encode(r)
				continue
			}

			// create account
			lu.Email = *m.Email
			lu.Name = *m.Name
			lu.PassHash, err = bcrypt.GenerateFromPassword([]byte(*m.Password), 10)
			if err != nil {
				r.Status = Error
				e.Encode(r)
				continue
			}

			_, err = client.Put(c, userKey, lu)
			if err != nil {
				r.Status = Error
				e.Encode(r)
				continue
			}

			msg = m
			usr = lu
			loggedIn = true
			break
		default:
			r.Status = ErrorNotLoggedIn
			e.Encode(r)
			continue
		}
	}

	// add connection to connections map
	conns[*msg.Username] = bufrw
	defer conns.remove(usr.Name)

	// return logged in message
	rsp.Status = StatusLoggedIn
	*rsp.Username = *msg.Username
	*rsp.Email = usr.Email
	if usr.Phone != nil {
		*rsp.Phone = *usr.Phone
	}
	*rsp.Contacts = usr.Contacts

	e.Encode(rsp)

	userKey := datastore.NameKey(KindUser, usr.Name, nil)

	// TODO: return ErrorMissingParameter if required parameter is not present
	for {
		d.Decode(msg)
		rsp = new(ServerMessage)

		switch msg.Status {
		case StatusLogOut:
			rsp.Status = StatusLoggedOut
			e.Encode(rsp)
			return
		case StatusAddContact:
			client.Get(c, userKey, usr)

			contactExists := false
			for _, u := range usr.Contacts {
				if u == *msg.Username {
					contactExists = true
					break
				}
			}
			if contactExists { break }

			usr.Contacts = append(usr.Contacts, *msg.Username)
			_, err = client.Put(c, userKey, usr)
			if err != nil {
				rsp.Status = Error
				e.Encode(rsp)
			}
			rsp.Status = StatusContactAdded
			*rsp.Username = *msg.Username
			e.Encode(rsp)
			break
		case StatusRemoveContact:
			err = client.Get(c, userKey, usr)
			if err != nil {
				rsp.Status = Error
				e.Encode(rsp)
				break
			}

			remove(usr.Contacts, *msg.Username)
			rsp.Status = StatusContactRemoved
			*rsp.Username = *msg.Username
			e.Encode(rsp)
			break
		case StatusUpdateProfile:
			// TODO
			break
		case StatusSendMessage:
			if msg.Message == nil {
				rsp.Status = ErrorMissingParameter
				e.Encode(rsp)
				break
			}

			if msg.Message.To == nil || msg.Message.From == nil {
				rsp.Status = ErrorMissingParameter
				e.Encode(rsp)
				break
			}

			if *msg.Message.Group {

			} else {
				for _, c := range *msg.Message.To {
					json.NewEncoder(conns[c]).Encode(msg.Message)
					// add message to datastore
				}
			}

			break
		case StatusSearchUser:
			// TODO
			break
		default:
			// some messages won't be received by the server (e.g. notifications)
			break
		}
	}

}

func main() {
	var r = mux.NewRouter()
	r.HandleFunc("/connect", handleConnect).Methods("GET")

	fmt.Println("Server started")
	log.Fatal(http.ListenAndServe(":80", r))
}
