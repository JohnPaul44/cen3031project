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
	"time"
)

const ProjectID = "cen3031-192414"

type Reaction struct {
	Reactions	[]int	`json:"type"`
	User		string	`json:"user"`
}

type Message struct {
	To				*[]string		`json:"to,omitempty"`
	MessageKey		*datastore.Key	`json:"message_key,omitempty"`
	ConversationKey	*datastore.Key	`json:"conversation_key,omitempty"`
	From			*string			`json:"from,omitempty"`
	Text			*string			`json:"text,omitempty"`
	Reactions		*[]Reaction		`json:"reactions,omitempty"`
	Typing			*bool			`json:"typing,omitempty"`
}

type Conversation struct {
	Members		[]string	`json:"members"`
	Messages	[]Message	`json:"messages"`
}

type ServerMessage struct {
	Status			int             `json:"status"`
	Error			*int			`json:"error,omitempty"`
	ErrorDetails	*string			`json:"error_details,omitempty"`
	Username 		*string         `json:"username,omitempty"`
	Password		*string         `json:"password,omitempty"`
	Email			*string         `json:"email,omitempty"`
	Name			*string         `json:"name,omitempty"`
	Phone			*string         `json:"phone,omitempty"`
	Contacts		*[]string       `json:"contacts,omitempty"`
	Conversations	*[]Conversation	`json:"conversations,omitempty"`
	Message			*Message        `json:"message,omitempty"`
}

func (msg *ServerMessage) setError(err error) {
	msg.Status = StatusError
	*msg.ErrorDetails = err.Error()
}

func (msg *ServerMessage) setCustomError(err int) {
	msg.Status = StatusError
	*msg.Error = err
}

const (
	ReactionExclamation	= iota
	ReactionQuestion	= iota
	ReactionHeart		= iota
	ReactionThumbsUp	= iota
	ReactionThumbsDown	= iota
)

const (
	ErrorNotLoggedIn		= iota
	ErrorExistingAccount 	= iota 	// returns username
	ErrorMissingParameter 	= iota
	ErrorInvalidUsername  	= iota	// returns username
	ErrorInvalidPassword  	= iota
)

const (
	StatusError         	= iota
	StatusRegister      	= iota 	// requires username, password, name, and email
	StatusLogin         	= iota 	// requires username and password
	StatusLoggedIn      	= iota 	// returns username, email, phone, contacts, conversations
	StatusLogOut        	= iota	// request to end session, requires nothing
	StatusLoggedOut     	= iota	// notification that the session has ended, returns nothing
	StatusAddContact    	= iota 	// requires username
	StatusContactAdded  	= iota	// returns username
	StatusRemoveContact 	= iota	// requires username
	StatusContactRemoved	= iota	// returns username
	StatusUpdateProfile	  	= iota 	// requires profile fields
	StatusSendMessage	  	= iota	// requires Message
	StatusMessageDelivered	= iota	// returns Message
	StatusMessageRead		= iota	// returns Message
	StatusTyping			= iota	// returns Message
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
				r.setCustomError(ErrorMissingParameter)
				e.Encode(r)
				continue
			}

			userKey := datastore.NameKey(KindUser, *m.Username, nil)
			lu := new(DSUser)
			err = client.Get(c, userKey, lu)
			if err != nil {
				if err == datastore.ErrNoSuchEntity {
					// user doesn't exist, return invalid username
					r.setCustomError(ErrorInvalidUsername)
					*r.Username = *m.Username
					e.Encode(r)
				} else {
					r.setError(err)
					e.Encode(r)
				}
			}

			err = bcrypt.CompareHashAndPassword(lu.PassHash, []byte(*m.Password))
			if err != nil {
				// invalid password
				r.setCustomError(ErrorInvalidPassword)
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
				r.setCustomError(ErrorMissingParameter)
				e.Encode(r)
				continue
			}

			userKey := datastore.NameKey(KindUser, *m.Username, nil)
			lu := new(DSUser)
			err := client.Get(c, userKey, lu)
			if err == nil {
				// user account already exists
				r.setCustomError(ErrorExistingAccount)
				e.Encode(r)
				continue
			} else if err != datastore.ErrNoSuchEntity {
				r.setError(err)
				e.Encode(r)
				continue
			}

			// create account
			lu.Email = *m.Email
			lu.Name = *m.Name
			lu.PassHash, err = bcrypt.GenerateFromPassword([]byte(*m.Password), 10)
			if err != nil {
				r.setError(err)
				e.Encode(r)
				continue
			}

			_, err = client.Put(c, userKey, lu)
			if err != nil {
				r.setError(err)
				e.Encode(r)
				continue
			}

			msg = m
			usr = lu
			loggedIn = true
			break
		default:
			// any other message requires the user to be logged in
			r.Status = ErrorNotLoggedIn
			e.Encode(r)
			continue
		}
	}

	// add connection to connections map
	conns[usr.Name] = bufrw
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

	for {
		d.Decode(msg)
		rsp = new(ServerMessage)

		switch msg.Status {
		case StatusLogOut:
			rsp.Status = StatusLoggedOut
			e.Encode(rsp)
			return
		case StatusAddContact:
			if msg.Username == nil {
				rsp.setCustomError(ErrorMissingParameter)
				e.Encode(rsp)
				break
			}

			err = client.Get(c, userKey, usr)
			if err != nil {
				rsp.setError(err)
				e.Encode(rsp)
				break
			}

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
				rsp.setError(err)
				e.Encode(rsp)
				break
			}
			rsp.Status = StatusContactAdded
			*rsp.Username = *msg.Username
			e.Encode(rsp)
			break
		case StatusRemoveContact:
			if msg.Username == nil {
				rsp.setCustomError(ErrorMissingParameter)
				e.Encode(rsp)
				break
			}

			err = client.Get(c, userKey, usr)
			if err != nil {
				rsp.setError(err)
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
				rsp.setCustomError(ErrorMissingParameter)
				e.Encode(rsp)
				break
			}

			if msg.Message.To == nil && msg.Message.ConversationKey == nil || msg.Message.Text == nil {
				rsp.setCustomError(ErrorMissingParameter)
				e.Encode(rsp)
				break
			}

			// remove any extra parameters
			tmp := new(ServerMessage)
			tmp.Status = msg.Status
			tmp.Message = msg.Message
			msg = tmp

			*msg.Message.From = usr.Name
			conversation := new(DSConversation)

			if msg.Message.ConversationKey == nil {
				// new conversation
				// create conversation entity and get key
				conversation.Time = time.Now()
				conversation.Read[usr.Name] = true // the user who sent the message has already read it
				for _, member := range *msg.Message.To { conversation.Read[member] = false }

				conversationKey := datastore.IncompleteKey(KindConversation, nil)
				conversationKey, err = client.Put(c, conversationKey, conversation)
				if err != nil {
					rsp.setError(err)
					e.Encode(rsp)
					break
				}

				msg.Message.ConversationKey = conversationKey

				// add conversation key to every member's conversation list (including this account)

				for member := range conversation.Read {
					uKey := datastore.NameKey(KindUser, member, nil)
					u := new(DSUser)
					err = client.Get(c, uKey, u)
					if err != nil {
						rsp.setError(err)
						e.Encode(rsp)
						break
					}

					u.Conversations = append(u.Conversations, conversationKey)
					_, err = client.Put(c, uKey, u)
					if err != nil {
						rsp.setError(err)
						e.Encode(rsp)
						break
					}
				}
				if err != nil { break }
			}

			// add message to datastore and get key
			m := new(DSMessage)
			m.Time = time.Now()
			m.Text = *msg.Message.Text
			m.From = usr.Name
			if msg.Message.Reactions != nil {
				for _, r := range *msg.Message.Reactions {
					*m.Reactions = append(*m.Reactions, r)
				}
			}

			err = client.Get(c, msg.Message.ConversationKey, conversation)
			if err != nil {
				rsp.setError(err)
				e.Encode(rsp)
				break
			}

			// update time in conversation to reflect most recent message
			conversation.Time = m.Time
			_, err = client.Put(c, msg.Message.ConversationKey, conversation)
			if err != nil {
				rsp.setError(err)
				e.Encode(rsp)
				break
			}

			mKey := datastore.IncompleteKey(KindMessage, msg.Message.ConversationKey)
			mKey, err = client.Put(c, mKey, m)
			if err != nil {
				rsp.setError(err)
				e.Encode(rsp)
				break
			}

			msg.Message.MessageKey = mKey
			msg.Status = StatusMessageReceived

			// send message to all users in conversation
			for _, member := range *msg.Message.To {
				json.NewEncoder(conns[member]).Encode(msg)
			}

			// notify client that message was delivered
			msg.Status = StatusMessageDelivered
			e.Encode(msg)
			break
		case StatusSearchUser:
			// TODO
			break
		case StatusMessageRead:
			if msg.Message == nil {
				rsp.setCustomError(ErrorMissingParameter)
				e.Encode(rsp)
				break
			}

			if msg.Message.ConversationKey == nil {
				rsp.setCustomError(ErrorMissingParameter)
				e.Encode(rsp)
				break
			}

			// update conversation in datastore
			conversation := new(DSConversation)
			err = client.Get(c, msg.Message.ConversationKey, conversation)
			if err != nil {
				rsp.setError(err)
				e.Encode(rsp)
				break
			}

			conversation.Read[usr.Name] = true
			_, err = client.Put(c, msg.Message.ConversationKey, conversation)
			if err != nil {
				rsp.setError(err)
				e.Encode(rsp)
				break
			}

			*msg.Message.From = usr.Name

			// notify members
			for member := range conversation.Read {
				if member != usr.Name { json.NewEncoder(conns[member]).Encode(msg) }
			}
			break
		case StatusTyping:
			if msg.Message == nil {
				rsp.setCustomError(ErrorMissingParameter)
				e.Encode(rsp)
				break
			}

			if msg.Message.ConversationKey == nil || msg.Message.Typing == nil {
				rsp.setCustomError(ErrorMissingParameter)
				e.Encode(rsp)
				break
			}

			// get conversation from datastore
			conversation := new(DSConversation)
			err = client.Get(c, msg.Message.ConversationKey, conversation)
			if err != nil {
				rsp.setError(err)
				e.Encode(rsp)
				break
			}

			*msg.Message.From = usr.Name

			// notify members
			for member := range conversation.Read {
				if member != usr.Name { json.NewEncoder(conns[member]).Encode(msg) }
			}
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
	log.Fatal(http.ListenAndServe(":8675", r))
}
