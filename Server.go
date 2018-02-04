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
	"google.golang.org/api/iterator"
)

const ProjectID = "cen3031-192414"

// TODO: implement support for multiple connections on the same account - use a map of slices for the connections

type Reaction struct {
	Reactions	[]int	`json:"type"`
	User		string	`json:"user"`
}

type Message struct {
	Time			*time.Time		`json:"time,omitempty"`
	To				*[]string		`json:"to,omitempty"`
	MessageKey		*datastore.Key	`json:"message_key,omitempty"`
	ConversationKey	*datastore.Key	`json:"conversation_key,omitempty"`
	From			*string			`json:"from,omitempty"`
	Text			*string			`json:"text,omitempty"`
	Reactions		*[]Reaction		`json:"reactions,omitempty"`
	Typing			*bool			`json:"typing,omitempty"`
}

type Status struct {
	Read	*bool	`json:"read,omitempty"`
	Typing	*bool	`json:"typing,omitempty"`
}

type Conversation struct {
	Time		time.Time			`json:"time"`
	Members		map[string] Status	`json:"read"`
	Messages	[]Message			`json:"messages"`
}

type Contact struct {
	Username	string	`json:"username"`
	Online		bool	`json:"online"`
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
	Contacts		[]Contact       `json:"contacts,omitempty"`
	Online			*bool			`json:"online,omitempty"`
	Conversations	*[]Conversation	`json:"conversations,omitempty"`
	Message			*Message        `json:"message,omitempty"`
}

func (msg *ServerMessage) setError(err error) {
	msg.Status = NotificationError
	*msg.ErrorDetails = err.Error()
}

func (msg *ServerMessage) setCustomError(err int) {
	msg.Status = NotificationError
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
	// Notifications are sent to devices
	NotificationError     			 = iota	// returns Error
	NotificationLoggedIn             = iota // returns Username, Email, Phone, Contacts, Conversations
	NotificationLoggedOut            = iota // session has ended
	NotificationContactAdded         = iota // returns Username
	NotificationContactRemoved       = iota // returns Username
	NotificationMessageDelivered     = iota // returns Message.ConversationKey and Message.MessageKey
	NotificationMessageRead          = iota // returns Message.ConversationKey and Message.From
	NotificationTyping               = iota // returns Message.ConversationKey, Message.From and Message.Typing
	NotificationMessageReceived      = iota // returns Message
	NotificationUserOnlineStatus	 = iota	// returns

	// Actions are received from client devices
	ActionRegister                   = iota // requires Username, Password, Name, and Email
	ActionLogIn                      = iota // requires Username and Password
	ActionLogOut                     = iota // request to end session
	ActionAddContact                 = iota // requires Username
	ActionRemoveContact              = iota // requires Username
	ActionUpdateProfile              = iota // requires profile fields
	ActionSendMessage                = iota // requires Message
	ActionUpdateMessage              = iota // returns Message
	ActionAddUserToConversation      = iota // requires Username
	ActionRemoveUserFromConversation = iota // requires Username
	ActionReadMessage                = iota // requires ConversationKey
	ActionSetTyping                  = iota // requires Message.ConversationKey and Message.Typing
	ActionSearchUser                 = iota // requires Username, Email, or Phone
)

func userToKey(user *DSUser) *datastore.Key {
	return datastore.NameKey(KindUser, user.username, nil)
}

type ServerMessageHandler func(*DSUser, *json.Encoder, *ServerMessage)

var handlerMap = map[int]ServerMessageHandler {
	ActionLogOut:			handleLogOut,
	ActionAddContact:		handleAddContact,
	ActionRemoveContact:	handleRemoveContact,
	ActionSendMessage:		handleSendMessage,
	ActionReadMessage:		handleReadMessage,
	ActionSetTyping:		handleSetTyping,
}

func handleServerMessage(user *DSUser, encoder *json.Encoder, message *ServerMessage) {
	handlerMap[message.Status](user, encoder, message)
}

func handleLogOut(user *DSUser, encoder *json.Encoder, message *ServerMessage) {
	rsp := new(ServerMessage)
	conns.remove(user.username)
	rsp.Status = NotificationLoggedOut
	encoder.Encode(rsp)
}

func handleAddContact(user *DSUser, encoder *json.Encoder, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Username == nil {
		rsp.setCustomError(ErrorMissingParameter)
		encoder.Encode(rsp)
		return
	}

	err := client.Get(c, userToKey(user), user)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	contactExists := false
	for _, u := range user.Contacts {
		if u == *message.Username {
			contactExists = true
			break
		}
	}
	if contactExists { return }

	user.Contacts = append(user.Contacts, *message.Username)
	_, err = client.Put(c, userToKey(user), user)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}
	rsp.Status = NotificationContactAdded
	*rsp.Username = *message.Username
	encoder.Encode(rsp)
	return
}

func handleRemoveContact(user *DSUser, encoder *json.Encoder, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Username == nil {
		rsp.setCustomError(ErrorMissingParameter)
		encoder.Encode(rsp)
		return
	}

	err := client.Get(c, userToKey(user), user)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	remove(user.Contacts, *message.Username)
	rsp.Status = NotificationContactRemoved
	*rsp.Username = *message.Username
	encoder.Encode(rsp)
}

func handleSendMessage(user *DSUser, encoder *json.Encoder, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		rsp.setCustomError(ErrorMissingParameter)
		encoder.Encode(rsp)
		return
	}

	if message.Message.To == nil && message.Message.ConversationKey == nil || message.Message.Text == nil {
		rsp.setCustomError(ErrorMissingParameter)
		encoder.Encode(rsp)
		return
	}

	// remove any extra parameters
	tmp := new(ServerMessage)
	tmp.Status = message.Status
	tmp.Message = message.Message
	message = tmp

	*message.Message.From = user.username
	conversation := new(DSConversation)

	if message.Message.ConversationKey == nil {
		// new conversation
		// create conversation entity and get key
		conversation.Time = time.Now()

		*conversation.Members[user.username].Read = true // the user who sent the message has already read it and is done typing
		for _, member := range *message.Message.To {
			*conversation.Members[member].Read = false	// reset user read status
		}

		conversationKey := datastore.IncompleteKey(KindConversation, nil)
		conversationKey, err := client.Put(c, conversationKey, conversation)
		if err != nil {
			rsp.setError(err)
			encoder.Encode(rsp)
			return
		}

		message.Message.ConversationKey = conversationKey

		// add conversation key to every member's conversation list (including this account)

		for member := range conversation.Members {
			uKey := datastore.NameKey(KindUser, member, nil)
			u := new(DSUser)
			err = client.Get(c, uKey, u)
			if err != nil {
				rsp.setError(err)
				encoder.Encode(rsp)
				break
			}

			u.Conversations = append(u.Conversations, conversationKey)
			_, err = client.Put(c, uKey, u)
			if err != nil {
				rsp.setError(err)
				encoder.Encode(rsp)
				break
			}
		}
		if err != nil { return }
	}

	// add message to datastore and get key
	m := new(DSMessage)
	m.Time = time.Now()
	m.Text = *message.Message.Text
	m.From = user.username
	if message.Message.Reactions != nil {
		for _, r := range *message.Message.Reactions {
			*m.Reactions = append(*m.Reactions, r)
		}
	}

	err := client.Get(c, message.Message.ConversationKey, conversation)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	// update time in conversation to reflect most recent message
	conversation.Time = m.Time
	_, err = client.Put(c, message.Message.ConversationKey, conversation)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	mKey := datastore.IncompleteKey(KindMessage, message.Message.ConversationKey)
	mKey, err = client.Put(c, mKey, m)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	message.Message.MessageKey = mKey
	message.Status = NotificationMessageReceived

	// send message to all users in conversation
	for _, member := range *message.Message.To {
		json.NewEncoder(conns[member]).Encode(message)
	}

	// notify client that message was delivered
	message.Status = NotificationMessageDelivered
	encoder.Encode(message)
}

func handleReadMessage(user *DSUser, encoder *json.Encoder, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		rsp.setCustomError(ErrorMissingParameter)
		encoder.Encode(rsp)
		return
	}

	if message.Message.ConversationKey == nil {
		rsp.setCustomError(ErrorMissingParameter)
		encoder.Encode(rsp)
		return
	}

	// update conversation in datastore
	conversation := new(DSConversation)
	err := client.Get(c, message.Message.ConversationKey, conversation)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	*conversation.Members[user.username].Read = true
	_, err = client.Put(c, message.Message.ConversationKey, conversation)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	message.Status = NotificationMessageRead
	*message.Message.From = user.username

	// notify members
	for member := range conversation.Members {
		if member != user.username { json.NewEncoder(conns[member]).Encode(message) }
	}
}

func handleSetTyping(user *DSUser, encoder *json.Encoder, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		rsp.setCustomError(ErrorMissingParameter)
		encoder.Encode(rsp)
		return
	}

	if message.Message.ConversationKey == nil || message.Message.Typing == nil {
		rsp.setCustomError(ErrorMissingParameter)
		encoder.Encode(rsp)
		return
	}

	// get conversation from datastore
	conversation := new(DSConversation)
	err := client.Get(c, message.Message.ConversationKey, conversation)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	// update conversation status in datastore
	*conversation.Members[user.username].Typing = *message.Message.Typing
	_, err = client.Put(c, message.Message.ConversationKey, conversation)
	if err != nil {
		rsp.setError(err)
		encoder.Encode(rsp)
		return
	}

	message.Status = NotificationTyping
	*message.Message.From = user.username

	// notify members
	for member := range conversation.Members {
		if member != user.username { json.NewEncoder(conns[member]).Encode(message) }
	}
}

/*
func handleFunc(user *DSUser, encoder *json.Encoder, message *ServerMessage) {
	
}
*/

func sendOnlineNotificationToOnlineContacts(username string, online bool) {
	// get slice of user's contacts
	usrKey := datastore.NameKey(KindUser, username, nil)
	usr := new(DSUser)
	err := client.Get(c, usrKey, usr)
	if err != nil {
		fmt.Print(err)
	}

	// create notification message
	msg := new(ServerMessage)
	msg.Status = NotificationUserOnlineStatus
	*msg.Online = online
	*msg.Username = username

	// send notification to online contacts
	for _, contact := range usr.Contacts {
		bufrw, contains := conns[contact]
		if contains {
			err := json.NewEncoder(bufrw).Encode(msg)
			if err != nil {
				fmt.Print(err)
			}
		}
	}
}

type Connections map[string] *bufio.ReadWriter

var conns = make(Connections)

func (conns Connections) contains(username string) bool {
	_, contains := conns[username]
	return contains
}

func (conns *Connections) add(username string, readWriter *bufio.ReadWriter) {
	(*conns)[username] = readWriter
	sendOnlineNotificationToOnlineContacts(username, true)
}

func (conns *Connections) remove(username string) {
	delete(*conns, username)
	sendOnlineNotificationToOnlineContacts(username, false)
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
	c = context.Background()
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

	decoder := json.NewDecoder(bufrw)
	encoder := json.NewEncoder(bufrw)
	
	usr := new(DSUser)
	rsp := new(ServerMessage)
	msg := new(ServerMessage)

	loggedIn := false

	for !loggedIn {
		// first message received is either a Login or Register
		m := new(ServerMessage)
		decoder.Decode(m)

		r := new(ServerMessage)

		switch m.Status {
		case ActionLogIn:
			// verify credentials
			if m.Username == nil || m.Password == nil {
				r.setCustomError(ErrorMissingParameter)
				encoder.Encode(r)
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
					encoder.Encode(r)
				} else {
					r.setError(err)
					encoder.Encode(r)
				}
			}

			err = bcrypt.CompareHashAndPassword(lu.PassHash, []byte(*m.Password))
			if err != nil {
				// invalid password
				r.setCustomError(ErrorInvalidPassword)
				encoder.Encode(r)
				continue
			}

			lu.username = *msg.Username
			msg = m
			usr = lu
			loggedIn = true
			break
		case ActionRegister:
			// verify new username
			if m.Username == nil || m.Password == nil || m.Email == nil || m.Name == nil {
				r.setCustomError(ErrorMissingParameter)
				encoder.Encode(r)
				continue
			}

			userKey := datastore.NameKey(KindUser, *m.Username, nil)
			lu := new(DSUser)
			err := client.Get(c, userKey, lu)
			if err == nil {
				// user account already exists
				r.setCustomError(ErrorExistingAccount)
				encoder.Encode(r)
				continue
			} else if err != datastore.ErrNoSuchEntity {
				r.setError(err)
				encoder.Encode(r)
				continue
			}

			// create account
			lu.Email = *m.Email
			lu.Name = *m.Name
			lu.PassHash, err = bcrypt.GenerateFromPassword([]byte(*m.Password), 10)
			if err != nil {
				r.setError(err)
				encoder.Encode(r)
				continue
			}

			_, err = client.Put(c, userKey, lu)
			if err != nil {
				r.setError(err)
				encoder.Encode(r)
				continue
			}

			lu.username = *m.Username
			msg = m
			usr = lu
			loggedIn = true
			break
		default:
			// any other message requires the user to be logged in
			r.Status = ErrorNotLoggedIn
			encoder.Encode(r)
			continue
		}
	}

	// add connection to connections map
	conns.add(usr.username, bufrw)
	defer conns.remove(usr.username)

	// return logged in message with user
	rsp.Status = NotificationLoggedIn
	*rsp.Username = usr.username
	*rsp.Email = usr.Email
	if usr.Phone != nil {
		*rsp.Phone = *usr.Phone
	}

	for _, dsContact := range usr.Contacts {
		contact := new(Contact)
		contact.Username = dsContact
		contact.Online = conns.contains(dsContact)
		rsp.Contacts = append(rsp.Contacts, *contact)
	}

	// get conversations from datastore
	for _, conversationKey := range usr.Conversations {
		dsConversation := new(DSConversation)
		err = client.Get(c, conversationKey, dsConversation)
		if err != nil {
			// TODO
		}

		conversation := new(Conversation)
		conversation.Time = dsConversation.Time
		conversation.Members = dsConversation.Members

		q := datastore.NewQuery(KindMessage).Ancestor(conversationKey).Order("time")
		it := client.Run(c, q)
		dsMessage := new(DSMessage)
		messageKey, err := it.Next(dsMessage)
		for err != nil {
			message := new(Message)
			message.MessageKey = messageKey
			message.ConversationKey = conversationKey
			*message.From = dsMessage.From
			*message.Text = dsMessage.Text
			*message.Time = dsMessage.Time
			message.Reactions = dsMessage.Reactions

			conversation.Messages = append(conversation.Messages, )
			_, err = it.Next(dsMessage)
		}

		if err != iterator.Done {
			// TODO
		}

		*rsp.Conversations = append(*rsp.Conversations, *conversation)
	}

	encoder.Encode(rsp)

	// event loop
	for {
		decoder.Decode(msg)
		handleServerMessage(usr, encoder, msg)
	}

}

var c context.Context
var client datastore.Client

func main() {
	var r = mux.NewRouter()
	r.HandleFunc("/connect", handleConnect).Methods("GET")

	fmt.Println("Server started")
	log.Fatal(http.ListenAndServe(":8675", r))
}
