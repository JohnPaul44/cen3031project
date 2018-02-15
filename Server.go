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
	"sync"
	"io"
	"errors"
)

const ProjectID = "cen3031-192414"

// TODO: write tests
// TODO: implement a toggle switch for editing messages in the conversation
// TODO: implement authorization tokens

type UserReaction struct {
	Reactions []int  `json:"type"`
	User      string `json:"user"`
}

type Message struct {
	ServerTime      *time.Time      `json:"serverTime,omitempty"`
	ClientTime      *string         `json:"clientTime,omitempty"`
	To              *[]string       `json:"to,omitempty"`
	MessageKey      *string         `json:"messageKey,omitempty"`
	ConversationKey *string         `json:"conversationKey,omitempty"`
	From            *string         `json:"from,omitempty"`
	Text            *string         `json:"text,omitempty"`
	Reactions       *[]UserReaction `json:"reactions,omitempty"`
	Typing          *bool           `json:"typing,omitempty"`
}

type Status struct {
	Read   bool `json:"read"`
	Typing bool `json:"typing"`
}

type Conversation struct {
	Time         time.Time         `json:"time"`
	MemberStatus map[string]Status `json:"memberStatus"`
	Messages     []Message         `json:"messages"`
}

type Contact struct {
	Username string `json:"username"`
	Online   bool   `json:"online"`
}

const (
	GenderFemale = iota
	GenderMale   = iota
	GenderOther  = iota
)

type Profile struct {
	Name   string  `json:"name"`
	Email  string  `json:"email"`
	Phone  *string `json:"phone,omitempty"`
	Gender *int    `json:"gender,omitempty"`
}

type ServerMessage struct {
	Status        int             `json:"status"`
	ErrorNumber   *int            `json:"errorNumber,omitempty"`
	ErrorString   *string         `json:"errorString,omitempty"`
	Username      *string         `json:"username,omitempty"`
	Password      *string         `json:"password,omitempty"`
	Profile       *Profile        `json:"profile,omitempty"`
	Contacts      *[]Contact      `json:"contacts,omitempty"`
	Online        *bool           `json:"online,omitempty"`
	Conversations *[]Conversation `json:"conversations,omitempty"`
	Message       *Message        `json:"message,omitempty"`
}

func (msg *ServerMessage) clear() {
	msg.Status = StatusUninitialized
	msg.ErrorNumber = nil
	msg.ErrorString = nil
	msg.Username = nil
	msg.Password = nil
	msg.Profile = nil
	msg.Contacts = nil
	msg.Online = nil
	msg.Conversations = nil
	msg.Message = nil
}

func (msg *ServerMessage) setError(err ServerError) {
	msg.Status = NotificationError
	msg.ErrorNumber = &err.id
	msg.ErrorString = &err.error
}

const (
	ReactionExclamation = iota
	ReactionQuestion    = iota
	ReactionHeart       = iota
	ReactionThumbsUp    = iota
	ReactionThumbsDown  = iota
)

const (
	// Default status
	StatusUninitialized = iota

	// Notifications are sent to devices
	NotificationError                       = iota // returns ErrorNumber and ErrorString
	NotificationLoggedIn                    = iota // returns Username, Profile, Contacts, Conversations
	NotificationUserOnlineStatus            = iota // returns Online, Username
	NotificationLoggedOut                   = iota // session has ended, returns nothing
	NotificationContactAdded                = iota // returns Username
	NotificationContactRemoved              = iota // returns Username
	NotificationProfileUpdated              = iota // returns Profile
	NotificationMessageReceived             = iota // returns Message.[ConversationKey, MessageKey, ServerTime, From, Text (, Reactions)] (Message is embedded in Conversations if it is the first message in a conversation)
	NotificationMessageUpdated              = iota // returns Message.[ConversationKey, MessageKey, Text]
	NotificationUserAddedToConversation     = iota // returns Username, Message.ConversationKey (, Message.Conversations (returned only to new user))
	NotificationUserRemovedFromConversation = iota // returns Username, Message.ConversationKey
	NotificationMessageRead                 = iota // returns Message.[ConversationKey, From]
	NotificationTyping                      = iota // returns Message.[ConversationKey, From, Typing]

	// Actions are received from client devices
	ActionRegister                   = iota // requires Username, Password, Name, Email
	ActionLogIn                      = iota // requires Username, Password
	ActionLogOut                     = iota // request to end session, requires nothing
	ActionAddContact                 = iota // requires Username
	ActionRemoveContact              = iota // requires Username
	ActionUpdateProfile              = iota // requires Profile
	ActionSendMessage                = iota // requires Message.[(To | ConversationKey), ClientTime, Text]
	ActionUpdateMessage              = iota // requires Message.[ConversationKey, MessageKey, Text]
	ActionAddUserToConversation      = iota // requires Username, Message.ConversationKey
	ActionRemoveUserFromConversation = iota // requires Username, Message.ConversationKey
	ActionReadMessage                = iota // requires ConversationKey
	ActionSetTyping                  = iota // requires Message.ConversationKey, Message.Typing
)

const (
	ErrorInternalServer         = iota
	ErrorInvalidJSON            = iota
	ErrorUnauthorized           = iota
	ErrorExistingAccount        = iota // returns username
	ErrorMissingParameter       = iota
	ErrorEmptyParameter         = iota
	ErrorInvalidUsername        = iota // returns username
	ErrorInvalidLogin           = iota
	ErrorInvalidConversationKey = iota
	ErrorInvalidMessageKey      = iota
	ErrorInvalidStatus          = iota
)

type ServerError struct {
	error string
	id    int
}

func NewError(error string, errType int) ServerError {
	return ServerError{error, errType}
}

func (err ServerError) Error() string {
	return err.error
}

func (err ServerError) Id() int {
	return err.id
}

const ErrorTag = "ERROR:"

var (
	ErrInternalServer         = NewError("internal server error", ErrorInternalServer)
	ErrInvalidJSON            = NewError("invalid JSON", ErrorInvalidJSON)
	ErrExistingAccount        = NewError("account already exists", ErrorExistingAccount)
	ErrMissingParameter       = NewError("missing parameter", ErrorMissingParameter)
	ErrEmptyParameter         = NewError("empty parameter", ErrorEmptyParameter)
	ErrInvalidUsername        = NewError("invalid username", ErrorInvalidUsername)
	ErrInvalidLogin           = NewError("invalid login credentials", ErrorInvalidLogin)
	ErrInvalidConversationKey = NewError("invalid conversationKey", ErrorInvalidConversationKey)
	ErrInvalidMessageKey      = NewError("invalid messageKey", ErrorInvalidMessageKey)
	ErrInvalidStatus          = NewError("invalid Status", ErrorInvalidStatus)

	ErrInvalidPassword = errors.New("invalid password")
)

func getServerMessage(bufrw *bufio.ReadWriter, message *ServerMessage) error {
	err := json.NewDecoder(bufrw).Decode(message)
	if err != nil {
		log.Println("cannot decode JSON message:", err)
	}
	return nil
}

func sendServerMessage(bufrw *bufio.ReadWriter, message *ServerMessage) error {
	bytes, err := json.Marshal(message)
	if err != nil {
		log.Println(ErrorTag, "cannot encode JSON message:", err)
		return err
	}
	_, err = bufrw.Write(bytes)
	return err
}

func socketClosed(err error) bool {
	return err == io.EOF || err == io.ErrUnexpectedEOF || err == io.ErrClosedPipe
}

func sendServerMessageToUser(username string, message *ServerMessage) {
	_, contains := conns[username]
	if contains {
		for _, conn := range conns[username] {
			err := sendServerMessage(conn.bufrw, message)
			if err != nil {
				if socketClosed(err) {
					log.Printf("socket closed for '%s'\n", username)
				} else {
					log.Println(ErrorTag, "error sending message to client:", err)
				}
			}
		}
	}
}

func usernameStringToKey(username string) *datastore.Key {
	return datastore.NameKey(KindUser, username, nil)
}

func conversationStringToKey(conversation string) *datastore.Key {
	return datastore.NameKey(KindConversation, conversation, nil)
}

func messageStringToKey(message string, conversationKey *datastore.Key) *datastore.Key {
	return datastore.NameKey(KindMessage, message, conversationKey)
}

func userExists(username string) (bool, error) {
	user := new(DSUser)
	err := client.Get(c, usernameStringToKey(username), user)
	if err == nil {
		return true, nil
	}
	if err == datastore.ErrNoSuchEntity {
		return false, nil
	}
	return false, err
}

type ServerMessageHandler func(*DSUser, *bufio.ReadWriter, *ServerMessage) error

var handlerMap = map[int]ServerMessageHandler{
	ActionLogOut:                     handleLogOut,
	ActionAddContact:                 handleAddContact,
	ActionRemoveContact:              handleRemoveContact,
	ActionUpdateProfile:              handleUpdateProfile,
	ActionSendMessage:                handleSendMessage,
	ActionReadMessage:                handleReadMessage,
	ActionSetTyping:                  handleSetTyping,
	ActionUpdateMessage:              handleUpdateMessage,
	ActionAddUserToConversation:      handleAddUserToConversation,
	ActionRemoveUserFromConversation: handleRemoveUserFromConversation,
}

func handleServerMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	handler, contains := handlerMap[message.Status]
	if !contains {
		rsp := new(ServerMessage)
		e := ErrInvalidStatus
		log.Println(e)
		rsp.setError(e)
		serr := sendServerMessage(bufrw, rsp)
		if serr != nil {
			return serr
		}
		return e
	}
	return handler(user, bufrw, message)
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

	userKey := usernameStringToKey(*message.Username)
	err := client.Get(c, userKey, user)
	if err == nil {
		// user account already exists
		e := NewError(fmt.Sprintf("username '%s' is already taken", *message.Username), ErrorExistingAccount)
		log.Println(errStr, e)
		rsp.setError(e)

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return ErrExistingAccount
	}

	if err != datastore.ErrNoSuchEntity {
		log.Println(ErrorTag, errStr, "cannot determine if user already exists:", err)
		rsp.setError(ErrInternalServer)

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return err
	}

	// create account
	user.username = *message.Username
	user.Profile.Name = message.Profile.Name
	user.Profile.Email = message.Profile.Email
	user.PassHash, err = bcrypt.GenerateFromPassword([]byte(*message.Password), 10)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot generate password hash:", err)
		rsp.setError(ErrInternalServer)

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return err
	}

	_, err = client.Put(c, userKey, user)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot add user to datastore:", err)
		rsp.setError(ErrInternalServer)

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return err
	}

	log.Printf("%s (%s) created an account\n", user.Profile.Name, user.username)

	return nil
}

func getContacts(user *DSUser) *[]Contact {
	contacts := new([]Contact)
	for _, dsContact := range user.Contacts {
		contact := new(Contact)
		contact.Username = dsContact
		contact.Online = conns.contains(dsContact)
		*contacts = append(*contacts, *contact)
	}

	return contacts
}

func getConversations(user *DSUser) (*[]Conversation, error) {
	errStr := "cannot get conversations:"

	conversations := new([]Conversation)
	for _, conversationKey := range user.Conversations {
		dsConversation := new(DSConversation)
		convKey := conversationStringToKey(conversationKey)
		err := client.Get(c, convKey, dsConversation)
		if err != nil {
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			return nil, err
		}

		conversation := new(Conversation)
		conversation.Time = dsConversation.Time
		conversation.MemberStatus = dsConversation.MemberStatus

		q := datastore.NewQuery(KindMessage).Ancestor(convKey).Order("time")
		it := client.Run(c, q)

		dsMessage := new(DSMessage)
		messageKey, err := it.Next(dsMessage)
		for err == nil {
			message := new(Message)
			message.MessageKey = &messageKey.Name
			message.ConversationKey = &conversationKey
			message.From = &dsMessage.From
			message.Text = &dsMessage.Text
			message.ServerTime = &dsMessage.Time
			message.Reactions = &dsMessage.Reactions

			conversation.Messages = append(conversation.Messages, )
			_, err = it.Next(dsMessage)
		}

		if err != iterator.Done {
			log.Println(ErrorTag, errStr, "cannot get message from datastore:", err)
			return nil, err
		}

		*conversations = append(*conversations, *conversation)
	}

	return conversations, nil
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

	userKey := usernameStringToKey(*message.Username)
	err := client.Get(c, userKey, user)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			// user doesn't exist
			log.Println(errStr, ErrInvalidUsername)
			rsp.setError(ErrInvalidLogin)
			rsp.Username = message.Username

			serr := sendServerMessage(user.connection.bufrw, rsp)
			if serr != nil {
				return serr
			}
			return ErrInvalidUsername
		}

		log.Println(ErrorTag, errStr, "cannot get user from datastore:", err)
		rsp.setError(ErrInternalServer)

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return err
	}

	err = bcrypt.CompareHashAndPassword(user.PassHash, []byte(*message.Password))
	if err != nil {
		// invalid password
		log.Println(errStr, ErrInvalidPassword)
		rsp.setError(ErrInvalidLogin)

		serr := sendServerMessage(user.connection.bufrw, rsp)
		if serr != nil {
			return serr
		}
		return err
	}

	user.username = *message.Username
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
	for _, contact := range user.Contacts {
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

func handleLogOut(user *DSUser, bufrw *bufio.ReadWriter, _ *ServerMessage) error {
	rsp := new(ServerMessage)
	conns.remove(user)
	rsp.Status = NotificationLoggedOut

	log.Printf("%s (%s) logged out\n", user.Profile.Name, user.username)

	return sendServerMessage(bufrw, rsp)
}

func handleAddContact(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot add contact:", user.Profile.Name, user.username)

	if message.Username == nil {
		e := NewError("missing username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Username) == 0 {
		e := NewError("empty username", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	errStr = fmt.Sprintf("%s (%s) cannot add '%s' as a contact:", user.Profile.Name, user.username, *message.Username)

	exists, err := userExists(*message.Username)
	if err != nil {
		log.Printf("%s %s cannot get user '%s' from datastore: %s\n", ErrorTag, errStr, *message.Username, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	if !exists {
		e := NewError(fmt.Sprintf("user '%s' does not exist", *message.Username), ErrorInvalidUsername)
		log.Println(errStr, e)
		rsp.setError(e)
		rsp.Username = message.Username
		return sendServerMessage(bufrw, rsp)
	}

	err = client.Get(c, usernameStringToKey(user.username), user)
	if err != nil {
		log.Printf("%s %s cannot get user '%s' from datastore: %s\n", ErrorTag, errStr, user.username, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	contactExists := false
	for _, u := range user.Contacts {
		if u == *message.Username {
			contactExists = true
			break
		}
	}
	if contactExists {
		log.Println(errStr, "user is already a contact")
		// intentionally not sending error to client if contact already exists
		return nil
	}

	user.Contacts = append(user.Contacts, *message.Username)
	_, err = client.Put(c, usernameStringToKey(user.username), user)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update contacts in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	rsp.Status = NotificationContactAdded
	rsp.Username = message.Username

	sendServerMessageToUser(user.username, rsp)
	log.Printf("%s (%s) added user '%s' as a contact\n", user.Profile.Name, user.username, *message.Username)

	return nil
}

func handleRemoveContact(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot remove contact:", user.Profile.Name, user.username)

	if message.Username == nil {
		e := NewError("missing username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Username) == 0 {
		e := NewError("empty username", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	errStr = fmt.Sprintf("%s (%s) cannot remove user '%s' from contacts:", user.Profile.Name, user.username, *message.Username)

	err := client.Get(c, usernameStringToKey(user.username), user)
	if err != nil {
		log.Printf("%s %s cannot get user '%s' from datastore: %s\n", ErrorTag, errStr, user.username, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	contacts, contains := remove(user.Contacts, *message.Username)
	if !contains {
		log.Println(errStr, "user is not a contact")
		// intentionally not sending error to client if user is not a contact
		return nil
	}

	user.Contacts = contacts

	rsp.Status = NotificationContactRemoved
	rsp.Username = message.Username

	sendServerMessageToUser(user.username, rsp)
	log.Printf("%s (%s) removed '%s' from contacts\n", user.Profile.Name, user.username, *message.Username)

	return nil
}

func handleUpdateProfile(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot update profile:", user.Profile.Name, user.username)

	if message.Profile == nil {
		e := NewError("missing profile", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(message.Profile.Name) == 0 || len(message.Profile.Email) == 0 {
		e := NewError("empty profile.name and/or profile.email", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	_, err := client.Put(c, usernameStringToKey(user.username), message.Profile)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update profile in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	user.Profile = *message.Profile

	rsp.clear()
	rsp.Status = NotificationProfileUpdated
	rsp.Profile = &user.Profile

	sendServerMessageToUser(user.username, rsp)
	log.Printf("%s (%s) updated %s profile\n", user.Profile.Name, func() string {
		if user.Profile.Gender == nil {
			return "his/her"
		}
		switch *user.Profile.Gender {
		case GenderFemale:
			return "her"
		case GenderMale:
			return "his"
		default:
			return "their"
		}
	}(), user.username)

	return nil
}

func handleSetTyping(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot update typing status:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Message.ConversationKey == nil || message.Message.Typing == nil {
		e := NewError("missing message.conversationKey and/or message.typing", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// get conversation from datastore
	convKey := conversationStringToKey(*message.Message.ConversationKey)
	conversation := new(DSConversation)
	err := client.Get(c, convKey, conversation)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	// update conversation status in datastore
	conversation.MemberStatus[user.username] = Status{conversation.MemberStatus[user.username].Read, *message.Message.Typing}
	_, err = client.Put(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	rsp.Status = NotificationTyping
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.username
	rsp.Message.Typing = message.Message.Typing

	// notify members of conversation
	for member := range conversation.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s (%s) %s typing\n", user.Profile.Name, user.username, func() string {
		if *message.Message.Typing {
			return "started"
		} else {
			return "stopped"
		}
	}())

	return nil
}

func handleSendMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot send message:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if (message.Message.To == nil && message.Message.ConversationKey == nil) || message.Message.Text == nil || message.Message.ClientTime == nil {
		e := NewError("missing message.to, message.conversationKey, message.text and/or message.clientTime", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if ((len(*message.Message.To) == 0 || len((*message.Message.To)[0]) == 0) && len(*message.Message.ConversationKey) == 0) || len(*message.Message.Text) == 0 || len(*message.Message.ClientTime) == 0 {
		e := NewError("empty message.to, message.conversationKey, message.text and/or message.clientTime", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	conversation := new(DSConversation)
	isNewConversation := message.Message.ConversationKey == nil

	if isNewConversation {
		// new conversation
		// create conversation entity and get key
		conversation.Time = time.Now()

		for _, member := range *message.Message.To {
			conversation.MemberStatus[member] = Status{false, conversation.MemberStatus[member].Typing} // reset user read status
		}
		conversation.MemberStatus[user.username] = Status{true, false} // the user who sent the message has already read it and is socketClosed typing

		conversationKey := datastore.IncompleteKey(KindConversation, nil)
		conversationKey, err := client.Put(c, conversationKey, conversation)
		if err != nil {
			log.Println(ErrorTag, errStr, "cannot add conversation to datastore:", err)
			rsp.setError(ErrInternalServer)
			return sendServerMessage(bufrw, rsp)
		}

		message.Message.ConversationKey = &conversationKey.Name

		// add conversation key to every member's conversation list (including this account)
		for member := range conversation.MemberStatus {
			usrKey := usernameStringToKey(member)
			u := new(DSUser)
			err = client.Get(c, usrKey, u)
			if err != nil {
				log.Println(ErrorTag, errStr, "cannot get user from datastore:", err)
				rsp.setError(ErrInternalServer)
				return sendServerMessage(bufrw, rsp)
			}

			u.Conversations = append(u.Conversations, conversationKey.Name)
			_, err = client.Put(c, usrKey, u)
			if err != nil {
				log.Println(ErrorTag, errStr, "cannot update user's conversations in datastore:", err)
				rsp.setError(ErrInternalServer)
				return sendServerMessage(bufrw, rsp)
			}
		}

		log.Printf("%s (%s) created new conversation with: %s\n", user.Profile.Name, user.username, *message.Message.To)
	}

	// add message to datastore and get key
	m := new(DSMessage)
	m.Time = time.Now()
	m.Text = *message.Message.Text
	m.From = user.username
	if message.Message.Reactions != nil {
		m.Reactions = *message.Message.Reactions
	}

	convKey := conversationStringToKey(*message.Message.ConversationKey)

	err := client.Get(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// update time in conversation to reflect most recent message
	conversation.Time = m.Time
	_, err = client.Put(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	mKey := datastore.IncompleteKey(KindMessage, convKey)
	mKey, err = client.Put(c, mKey, m)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot add message to datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// notify members that message was received
	rsp.Status = NotificationMessageReceived
	msg := new(Message)
	msg.MessageKey = &mKey.Name
	msg.ConversationKey = message.Message.ConversationKey
	msg.ServerTime = &m.Time
	msg.From = &m.From
	msg.Text = &m.Text
	msg.Reactions = message.Message.Reactions

	if isNewConversation {
		// move message to Conversation.Messages[0]
		conv := new(Conversation)
		for _, member := range *msg.To {
			conv.MemberStatus[member] = Status{false, false}
		}
		conv.MemberStatus[*msg.From] = Status{true, false}
		conv.Messages[0] = *msg
		rsp.Conversations = new([]Conversation)
		(*rsp.Conversations)[0] = *conv
	} else {
		rsp.Message = msg
	}

	// send message to all users in conversation, excluding sender
	for member := range conversation.MemberStatus {
		if member != user.username {
			sendServerMessageToUser(member, rsp)
		}
	}

	// send message to all sender's connections, excluding the originating connection
	for t := range conns[user.username] {
		if t != user.connection.time {
			conn := conns[user.username][t]
			err = sendServerMessage(conn.bufrw, rsp)
			if err != nil {
				if socketClosed(err) {
					log.Printf("socket closed for '%s'\n", user.username)
				} else {
					log.Println(ErrorTag, "error sending message to client:", err)
				}
			}
		}
	}

	// set ClientTime to the time sent by the client (for identifying message internally)
	rsp.Message.ClientTime = message.Message.ClientTime
	log.Printf("%s (%s) sent message to: %s\n", user.Profile.Name, user.username, *message.Message.To)

	return sendServerMessage(bufrw, rsp)
}

func handleUpdateMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot update message:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Message.ConversationKey == nil || message.Message.MessageKey == nil || message.Message.Text == nil {
		e := NewError("missing message.conversationKey, message.messageKey and/or message.text", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 || len(*message.Message.MessageKey) == 0 || len(*message.Message.Text) == 0 {
		e := NewError("empty message.conversationKey, message.messageKey and/or message.text", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	convKey := conversationStringToKey(*message.Message.ConversationKey)

	// get Message (MessageKey) from datastore
	dsMsgKey := messageStringToKey(*message.Message.MessageKey, convKey)
	dsMsg := new(DSMessage)
	err := client.Get(c, dsMsgKey, dsMsg)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidMessageKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get message from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	// ensure that the user who wrote the message is the same one trying to update it
	if dsMsg.From != user.username {
		e := NewError(fmt.Sprintf("user can only update messages %s sent", func() string {
			if user.Profile.Gender == nil {
				return "he/she has"
			}
			switch *user.Profile.Gender {
			case GenderFemale:
				return "she has"
			case GenderMale:
				return "he has"
			default:
				return "they have"
			}
		}()), ErrorUnauthorized)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// update message
	dsMsg.Text = *message.Message.Text

	// put Message (MessageKey) into datastore
	_, err = client.Put(c, dsMsgKey, dsMsg)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update Message in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// get Conversation (ConversationKey) from datastore
	conv := new(DSConversation)
	err = client.Get(c, convKey, conv)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidMessageKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	// notify all users in Conversation (NotificationMessageUpdated), including the sender
	rsp.Status = NotificationMessageUpdated
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.MessageKey = message.Message.MessageKey
	rsp.Message.Text = message.Message.Text

	for member := range conv.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s (%s) updated a message\n", user.Profile.Name, user.username)

	return nil
}

func handleAddUserToConversation(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot add user to conversation:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Username == nil || message.Message.ConversationKey == nil {
		e := NewError("missing username and/or message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Username) == 0 || len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty username and/or message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	errStr = fmt.Sprintf("%s (%s) cannot add user '%s' to conversation:", user.Profile.Name, user.username, *message.Username)

	// get Conversation from datastore (ConversationKey)
	conv := new(DSConversation)
	convKey := conversationStringToKey(*message.Message.ConversationKey)
	err := client.Get(c, convKey, conv)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	var members []string

	usr := false
	newUsr := false
	for member := range conv.MemberStatus {
		members = append(members, member)
		if member == user.username {
			usr = true
		} else if member == *message.Username {
			newUsr = true
		}
	}

	// ensure usr is in conversation
	if !usr {
		e := NewError(fmt.Sprintf("'%s' is not in conversation", user.username), ErrorUnauthorized)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// ensure newUsr is not already in Conversation
	if newUsr {
		// intentionally not sending response to client, since there is nothing to do if the user is already in the conversation
		return nil
	}

	// update Conversation (add message.Username to MemberStatus status={false, false})
	conv.MemberStatus[*message.Username] = Status{false, false}

	// put Conversation into datastore
	_, err = client.Put(c, convKey, conv)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// notify all users in Conversation
	rsp.Status = NotificationUserAddedToConversation
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	for member := range conv.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	// send message to new member (message.Username) with all conversation data
	rsp.Conversations = new([]Conversation)
	(*rsp.Conversations)[0].MemberStatus = conv.MemberStatus
	(*rsp.Conversations)[0].Time = conv.Time

	q := datastore.NewQuery(KindMessage).Ancestor(convKey).Order("ServerTime")
	dsMsgs := new([]DSMessage)
	_, err = client.GetAll(c, q, dsMsgs)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get messages from datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	var messages []Message

	for _, dsMsg := range *dsMsgs {
		msg := new(Message)
		msg.ServerTime = &dsMsg.Time
		msg.From = &dsMsg.From
		msg.Text = &dsMsg.Text
		msg.Reactions = &dsMsg.Reactions
		messages = append(messages, *msg)
	}

	(*rsp.Conversations)[0].Messages = messages

	sendServerMessageToUser(*message.Username, rsp)
	log.Printf("%s (%s) added '%s' to conversation with: %s\n", user.Profile.Name, user.username, *message.Username, members)

	return nil
}

func handleRemoveUserFromConversation(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot remove user from conversation:", user.Profile.Name, user.username)

	if message.Message == nil || message.Username == nil {
		e := NewError("missing message and/or username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Message.ConversationKey == nil {
		e := NewError("missing message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Username) == 0 || len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty username and/or message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	errStr = fmt.Sprintf("%s (%s) cannot remove '%s' from conversation:", user.Profile.Name, user.username, *message.Username)

	// get Conversation from datastore
	conv := new(DSConversation)
	convKey := conversationStringToKey(*message.Message.ConversationKey)
	err := client.Get(c, convKey, conv)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	// ensure user.username and *message.Username are in Conversation
	usr := false
	delUsr := false
	for member := range conv.MemberStatus {
		if member == user.username {
			usr = true
			break
		} else if member == *message.Username {
			delUsr = true
			break
		}
	}

	// ensure usr is in conversation
	if !usr {
		e := NewError(fmt.Sprintf("'%s' is not in conversation", user.username), ErrorUnauthorized)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// ensure delUsr is already in conversation
	if !delUsr {
		// intentionally not sending a response to client, since there is nothing to do if the user isn't in the conversation
		return nil
	}

	// put Conversation into datastore
	_, err = client.Put(c, convKey, conv)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// notify all users in Conversation (NotificationUserRemovedFromConversation)
	rsp.Status = NotificationUserRemovedFromConversation
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	var members []string

	for member := range conv.MemberStatus {
		members = append(members, member)
		sendServerMessageToUser(member, rsp)
	}

	// send notification to user that was removed
	sendServerMessageToUser(*message.Username, rsp)
	log.Printf("'%s' was removed from conversation with: %s\n", *message.Username, members)

	return nil
}

func handleReadMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot read message:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Message.ConversationKey == nil {
		e := NewError("missing message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// update conversation in datastore
	convKey := conversationStringToKey(*message.Message.ConversationKey)
	conversation := new(DSConversation)
	err := client.Get(c, convKey, conversation)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get user from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	conversation.MemberStatus[user.username] = Status{true, conversation.MemberStatus[user.username].Typing}
	_, err = client.Put(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update user in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	rsp.Status = NotificationMessageRead
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.username

	// notify members, including the sender
	for member := range conversation.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s (%s) read a message\n", user.Profile.Name, user.username)

	return nil
}

type Connection struct {
	bufrw        *bufio.ReadWriter // interface for reading and writing to the connection
	time         time.Time         // time that the connection was established (used for differentiating different connections with same username)
}

type Connections map[string]map[time.Time]*Connection

var conns = make(Connections)
var connsMutex = new(sync.Mutex)

func (conns Connections) contains(username string) bool {
	_, contains := conns[username]
	return contains
}

func (conns *Connections) add(user *DSUser) {
	_, contains := (*conns)[user.username]

	connsMutex.Lock()
	(*conns)[user.username][user.connection.time] = user.connection
	connsMutex.Unlock()

	if contains {
		updateOnlineStatus(user, true)
	}
}

func (conns *Connections) remove(user *DSUser) {
	connsMutex.Lock()
	delete((*conns)[user.username], user.connection.time)
	if len((*conns)[user.username]) == 0 {
		delete(*conns, user.username)
	}
	connsMutex.Unlock()

	_, contains := (*conns)[user.username]
	if !contains {
		updateOnlineStatus(user, false)
	}
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
		rsp.Contacts = getContacts(usr)
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
