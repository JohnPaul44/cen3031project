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
)

const ProjectID = "cen3031-192414"

// TODO: implement a toggle switch for editing messages in the conversation
// TODO: implement authorization tokens

type UserReaction struct {
	Reactions []int  `json:"type"`
	User      string `json:"user"`
}

type Message struct {
	ServerTime      *time.Time      `json:"serverTime,omitempty"`
	ClientTime      *string      	`json:"clientTime,omitempty"`
	To              *[]string       `json:"to,omitempty"`
	MessageKey      *datastore.Key  `json:"messageKey,omitempty"`
	ConversationKey *datastore.Key  `json:"conversationKey,omitempty"`
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
	Time     time.Time         `json:"time"`
	MemberStatus  map[string]Status `json:"memberStatus"`
	Messages []Message         `json:"messages"`
}

type Contact struct {
	Username string `json:"username"`
	Online   bool   `json:"online"`
}

type Profile struct {
	Name  string  `json:"name"`
	Email string  `json:"email"`
	Phone *string `json:"phone,omitempty"`
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

func (msg *ServerMessage) setError(err error) {
	msg.Status = NotificationError
	msg.ErrorNumber = errorToNumber(ErrorDefault)
	msg.ErrorString = new(string)
	*msg.ErrorString = err.Error()
}

func (msg *ServerMessage) setCustomError(errorNumber int, errorString string) {
	msg.Status = NotificationError
	msg.ErrorNumber = &errorNumber
	if len(errorString) != 0 {
		msg.ErrorString = &errorString
	} else {
		msg.ErrorString = errorToString(errorNumber)
	}
}

func (msg *ServerMessage) setDefaultCustomError(errorNumber int) {
	msg.Status = NotificationError
	msg.ErrorNumber = new(int)
	*msg.ErrorNumber = errorNumber
	msg.ErrorString = new(string)
	*msg.ErrorString = defaultErrorStrings[errorNumber]
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
	ErrorDefault          = iota
	ErrorNotLoggedIn      = iota
	ErrorExistingAccount  = iota // returns username
	ErrorMissingParameter = iota
	ErrorEmptyParameter   = iota
	ErrorInvalidUsername  = iota // returns username
	ErrorInvalidPassword  = iota
	ErrorUserDoesNotExist = iota
)

type ErrorStrings = map[int]string

var defaultErrorStrings = ErrorStrings{
	ErrorDefault:          "default",
	ErrorNotLoggedIn:      "not logged in",
	ErrorExistingAccount:  "account already exists",
	ErrorMissingParameter: "missing parameter",
	ErrorEmptyParameter:   "empty parameter",
	ErrorInvalidUsername:  "invalid username",
	ErrorInvalidPassword:  "invalid password",
	ErrorUserDoesNotExist: "user does not exist",
}

func errorToNumber(errorNumber int) *int {
	i := new(int)
	*i = errorNumber
	return i
}

func errorToString(errorNumber int) *string {
	errorString := new(string)
	*errorString = defaultErrorStrings[errorNumber]
	return errorString
}

func getServerMessage(bufrw *bufio.ReadWriter, message *ServerMessage) bool {
	err := json.NewDecoder(bufrw).Decode(message)
	if err != nil {
		fmt.Errorf("cannot decode JSON message: %s", err)
	}
	return err == nil
}

func sendServerMessage(bufrw *bufio.ReadWriter, message *ServerMessage) bool {
	bytes, err := json.Marshal(message)
	if err != nil {
		fmt.Errorf("cannot encode JSON message: %s", err)
	}
	_, err = bufrw.Write(bytes)
	return err == nil
}

func sendServerMessageToUser(username string, message *ServerMessage) bool {
	_, contains := conns[username]
	if contains {
		for _, bufrw := range conns[username] {
			if !sendServerMessage(bufrw, message) {
				return false
			}
		}
	}
	return true
}

func userToKey(user *DSUser) *datastore.Key {
	return datastore.NameKey(KindUser, user.username, nil)
}

func userExists(username string) (bool, error) {
	user := new(DSUser)
	err := client.Get(c, datastore.NameKey(KindUser, username, nil), user)
	if err == nil {
		return true, nil
	} else if err == datastore.ErrNoSuchEntity {
		return false, nil
	}

	return false, nil
}

type ServerMessageHandler func(*DSUser, *bufio.ReadWriter, *ServerMessage)

var handlerMap = map[int]ServerMessageHandler{
	ActionLogOut:        handleLogOut,
	ActionAddContact:    handleAddContact,
	ActionRemoveContact: handleRemoveContact,
	ActionUpdateProfile: handleUpdateProfile,
	ActionSendMessage:   handleSendMessage,
	ActionReadMessage:   handleReadMessage,
	ActionSetTyping:     handleSetTyping,
	ActionUpdateMessage: handleUpdateMessage,
	ActionAddUserToConversation: handleAddUserToConversation,
	ActionRemoveUserFromConversation: handleRemoveUserFromConversation,
}

func handleServerMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	handler, contains := handlerMap[message.Status]
	if !contains {
		fmt.Println("invalid ServerMessage.Status")
		return
	}
	handler(user, bufrw, message)
}

func register(user *DSUser, message *ServerMessage) bool {
	rsp := new(ServerMessage)
	// verify new username
	if message.Username == nil || message.Password == nil || message.Profile == nil {
		fmt.Println("cannot register: missing Username, Password and/or Profile")
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.[Username | Password | Profile]")
		sendServerMessage(user.connection.bufrw, rsp)
		return false
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 || len(message.Profile.Name) == 0 || len(message.Profile.Email) == 0 {
		fmt.Println("cannot register: empty Username, Password, Name, and/or Email")
		rsp.setCustomError(ErrorEmptyParameter, "empty ServerMessage.[Username | Password | Name | Email]")
		sendServerMessage(user.connection.bufrw, rsp)
		return false
	}

	userKey := datastore.NameKey(KindUser, *message.Username, nil)
	err := client.Get(c, userKey, user)
	if err == nil {
		// user account already exists
		fmt.Printf("cannot register as %s: username taken\n", *message.Username)
		rsp.setDefaultCustomError(ErrorExistingAccount)
		sendServerMessage(user.connection.bufrw, rsp)
		return false
	} else if err != datastore.ErrNoSuchEntity {
		fmt.Printf("cannot register as %s: %s\n", *message.Username, err)
		rsp.setError(err)
		sendServerMessage(user.connection.bufrw, rsp)
		return false
	}

	// create account
	user.username = *message.Username
	user.Profile.Name = message.Profile.Name
	user.Profile.Email = message.Profile.Email
	user.PassHash, err = bcrypt.GenerateFromPassword([]byte(*message.Password), 10)
	if err != nil {
		fmt.Printf("%s (%s) cannot register account: %s\n", user.Profile.Name, *message.Username, err)
		rsp.setError(err)
		sendServerMessage(user.connection.bufrw, rsp)
		return false
	}

	_, err = client.Put(c, userKey, user)
	if err != nil {
		fmt.Errorf("%s (%s) cannot register account: cannot add user to datastore\n", user.Profile.Name, *message.Username)
		rsp.setError(err)
		sendServerMessage(user.connection.bufrw, rsp)
		return false
	}

	fmt.Printf("%s (%s) created an account\n", user.Profile.Name, user.username)

	return true
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

func getConversations(user *DSUser) *[]Conversation {
	conversations := new([]Conversation)
	for _, conversationKey := range user.Conversations {
		dsConversation := new(DSConversation)
		err := client.Get(c, conversationKey, dsConversation)
		if err != nil {
			fmt.Errorf("cannot get conversation: %s\n", err)
			continue
		}

		conversation := new(Conversation)
		conversation.Time = dsConversation.Time
		conversation.MemberStatus = dsConversation.MemberStatus

		q := datastore.NewQuery(KindMessage).Ancestor(conversationKey).Order("time")
		it := client.Run(c, q)
		dsMessage := new(DSMessage)
		messageKey, err := it.Next(dsMessage)
		for err != nil {
			message := new(Message)
			message.MessageKey = messageKey
			message.ConversationKey = conversationKey
			message.From = &dsMessage.From
			message.Text = &dsMessage.Text
			message.ServerTime = &dsMessage.Time
			message.Reactions = &dsMessage.Reactions

			conversation.Messages = append(conversation.Messages, )
			_, err = it.Next(dsMessage)
		}

		if err != iterator.Done {
			fmt.Errorf("cannot get conversation: %s\n", err)
			continue
		}

		*conversations = append(*conversations, *conversation)
	}

	return conversations
}

func logIn(user *DSUser, message *ServerMessage) bool {
	rsp := new(ServerMessage)
	// verify credentials
	if message.Username == nil || message.Password == nil {
		fmt.Println("cannot log in: missing Username and/or Password")
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.[Username | Password]")
		sendServerMessage(user.connection.bufrw, rsp)
		return false
	}

	if len(*message.Username) == 0 || len(*message.Password) == 0 {
		fmt.Println("cannot log in: empty Username and/or Password")
		rsp.setCustomError(ErrorEmptyParameter, "empty ServerMessage.[Username | Password]")
	}

	userKey := datastore.NameKey(KindUser, *message.Username, nil)
	err := client.Get(c, userKey, user)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			// user doesn't exist, return invalid username
			fmt.Printf("cannot log in as %s: invalid username\n", *message.Username)
			rsp.setDefaultCustomError(ErrorInvalidUsername)
			rsp.Username = message.Username
			sendServerMessage(user.connection.bufrw, rsp)
			return false
		} else {
			fmt.Errorf("cannot log in as %s: %s\n", *message.Username, err)
			rsp.setError(err)
			sendServerMessage(user.connection.bufrw, rsp)
			return false
		}
	}

	err = bcrypt.CompareHashAndPassword(user.PassHash, []byte(*message.Password))
	if err != nil {
		// invalid password
		fmt.Printf("%s (%s) cannot log in: invalid password\n", user.Profile.Name, *message.Username)
		rsp.setDefaultCustomError(ErrorInvalidPassword)
		sendServerMessage(user.connection.bufrw, rsp)
		return false
	}

	user.username = *message.Username

	fmt.Printf("%s (%s) logged in\n", user.Profile.Name, user.username)

	return true
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

	if online {
		fmt.Printf("%s (%s) is online\n", user.Profile.Name, user.username)
	} else {
		fmt.Printf("%s (%s) is offline\n", user.Profile.Name, user.username)
	}
}

func handleLogOut(user *DSUser, bufrw *bufio.ReadWriter, _ *ServerMessage) {
	rsp := new(ServerMessage)
	conns.remove(user)
	rsp.Status = NotificationLoggedOut
	sendServerMessage(bufrw, rsp)
	fmt.Printf("%s (%s) logged out\n", user.Profile.Name, user.username)
}

func handleAddContact(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Username == nil {
		fmt.Printf("%s (%s) cannot add contact: missing Username\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Username")
		sendServerMessage(bufrw, rsp)
		return
	}

	exists, err := userExists(*message.Username)
	if err != nil {
		fmt.Errorf("datastore error: %s\n", err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	if !exists {
		fmt.Printf("%s (%s) cannot add contact: %s does not exist\n", user.Profile.Name, user.username, *message.Username)
		rsp.setCustomError(ErrorUserDoesNotExist, "")
		rsp.Username = message.Username
		sendServerMessage(bufrw, rsp)
		return
	}

	err = client.Get(c, userToKey(user), user)
	if err != nil {
		fmt.Errorf("%s (%s) cannot add contact: cannot get user from datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	contactExists := false
	for _, u := range user.Contacts {
		if u == *message.Username {
			contactExists = true
			break
		}
	}
	if contactExists {
		fmt.Printf("%s (%s) cannot add contact: user already has contact %s: %s\n", user.Profile.Name, user.username, *message.Username, *message.Username)
		return
	}

	user.Contacts = append(user.Contacts, *message.Username)
	_, err = client.Put(c, userToKey(user), user)
	if err != nil {
		fmt.Errorf("%s (%s) cannot add contact: cannot update user in datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}
	rsp.Status = NotificationContactAdded
	rsp.Username = message.Username
	sendServerMessageToUser(user.username, rsp)

	fmt.Printf("%s (%s) added contact: %s\n", user.Profile.Name, user.username, *message.Username)
}

func handleRemoveContact(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Username == nil {
		fmt.Printf("%s (%s) cannot remove contact: missing Username\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Username")
		sendServerMessage(bufrw, rsp)
		return
	}

	err := client.Get(c, userToKey(user), user)
	if err != nil {
		fmt.Errorf("%s (%s) cannot remove contact: cannot get user from datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	contacts, ok := remove(user.Contacts, *message.Username)
	if !ok {
		fmt.Printf("%s (%s) cannot remove contact: %s not in contacts\n", user.Profile.Name, user.username, *message.Username)
		rsp.Status = NotificationError
		rsp.ErrorString = new(string)
		*rsp.ErrorString = user.username + " cannot remove contact: " + *message.Username + " not in contacts"
		sendServerMessage(bufrw, rsp)
		return
	}

	user.Contacts = contacts

	rsp.Status = NotificationContactRemoved
	rsp.Username = message.Username
	sendServerMessageToUser(user.username, rsp)

	fmt.Printf("%s (%s) removed contact: %s\n", user.Profile.Name, user.username, *message.Username)
}

func handleUpdateProfile(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Profile == nil {
		fmt.Printf("%s (%s) cannot update profile: missing Profile\n", user.Profile.Name, user.Profile)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Profile")
		sendServerMessage(bufrw, rsp)
		return
	}

	if len(message.Profile.Name) == 0 || len(message.Profile.Email) == 0 {
		fmt.Printf("%s (%s) cannot update profile: empty ServerMessage.Profile.[Name | Email]\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorEmptyParameter, "empty ServerMessage.Profile.[Name | Email]")
		sendServerMessage(bufrw, rsp)
		return
	}

	_, err := client.Put(c, datastore.NameKey(KindUser, user.username, nil), message.Profile)
	if err != nil {
		fmt.Printf("%s (%s) cannot update profile: cannot update profile in datastore: %s", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	user.Profile = *message.Profile

	rsp.clear()
	rsp.Status = NotificationProfileUpdated
	rsp.Profile = &user.Profile

	sendServerMessageToUser(user.username, rsp)

	fmt.Printf("%s (%s) updated his/her profile\n", user.Profile.Name, user.username)
}

func handleSetTyping(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		fmt.Printf("%s (%s) cannot update typing status: missing Message\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Message")
		sendServerMessage(bufrw, rsp)
		return
	}

	if message.Message.ConversationKey == nil || message.Message.Typing == nil {
		fmt.Printf("%s (%s) cannot update typing status: missing ConversationKey and/or Typing\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Message.[ConversationKey | Typing]")
		sendServerMessage(bufrw, rsp)
		return
	}

	// get conversation from datastore
	conversation := new(DSConversation)
	err := client.Get(c, message.Message.ConversationKey, conversation)
	if err != nil {
		fmt.Printf("%s (%s) cannot update typing status: cannot get conversation from datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	// update conversation status in datastore
	conversation.MemberStatus[user.username] = Status{conversation.MemberStatus[user.username].Read, *message.Message.Typing}
	_, err = client.Put(c, message.Message.ConversationKey, conversation)
	if err != nil {
		fmt.Printf("%s (%s) cannot update typing status: cannot update conversation in datastore: %s", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	rsp.Status = NotificationTyping
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.username
	rsp.Message.Typing = message.Message.Typing

	// notify members
	for member := range conversation.MemberStatus {
		if member != user.username {
			sendServerMessageToUser(member, rsp)
		}
	}

	if *message.Message.Typing {
		fmt.Printf("%s (%s) started typing\n", user.Profile.Name, user.username)
	} else {
		fmt.Printf("%s (%s) stopped typing\n", user.Profile.Name, user.username)
	}
}

func handleSendMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		fmt.Printf("%s (%s) cannot send message: missing Message\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Message")
		sendServerMessage(bufrw, rsp)
		return
	}

	if (message.Message.To == nil && message.Message.ConversationKey == nil) || message.Message.Text == nil || message.Message.ClientTime == nil {
		fmt.Printf("%s (%s) cannot send message: missing To, ConversationKey, and/or Text\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Message.[To | ConversationKey | Text | ClientTime]")
		sendServerMessage(bufrw, rsp)
		return
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
		conversation.MemberStatus[user.username] = Status{true, false} // the user who sent the message has already read it and is done typing

		conversationKey := datastore.IncompleteKey(KindConversation, nil)
		conversationKey, err := client.Put(c, conversationKey, conversation)
		if err != nil {
			fmt.Errorf("%s (%s) cannot send message: cannot add conversation to datastore: %s\n", user.Profile.Name, user.username, err)
			rsp.setError(err)
			sendServerMessage(bufrw, rsp)
			return
		}

		message.Message.ConversationKey = conversationKey

		// add conversation key to every member's conversation list (including this account)
		for member := range conversation.MemberStatus {
			uKey := datastore.NameKey(KindUser, member, nil)
			u := new(DSUser)
			err = client.Get(c, uKey, u)
			if err != nil {
				fmt.Errorf("%s (%s) cannot send message: cannot get %s from datastore: %s\n", user.Profile.Name, user.username, member, err)
				rsp.setError(err)
				sendServerMessage(bufrw, rsp)
				return
			}

			u.Conversations = append(u.Conversations, conversationKey)
			_, err = client.Put(c, uKey, u)
			if err != nil {
				fmt.Errorf("%s (%s) cannot send message: cannot update %s's conversations: %s\n", user.Profile.Name, user.username, member, err)
				rsp.setError(err)
				sendServerMessage(bufrw, rsp)
				return
			}
		}
		fmt.Printf("%s (%s) created new conversation with %s\n", user.Profile.Name, user.username, *message.Message.To)
	}

	// add message to datastore and get key
	m := new(DSMessage)
	m.Time = time.Now()
	m.Text = *message.Message.Text
	m.From = user.username
	if message.Message.Reactions != nil {
		m.Reactions = *message.Message.Reactions
	}

	err := client.Get(c, message.Message.ConversationKey, conversation)
	if err != nil {
		fmt.Errorf("%s (%s) cannot send message: cannot get conversation from datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	// update time in conversation to reflect most recent message
	conversation.Time = m.Time
	_, err = client.Put(c, message.Message.ConversationKey, conversation)
	if err != nil {
		fmt.Errorf("%s (%s) cannot send message: cannot update conversation: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	mKey := datastore.IncompleteKey(KindMessage, message.Message.ConversationKey)
	mKey, err = client.Put(c, mKey, m)
	if err != nil {
		fmt.Errorf("%s (%s) cannot send message: cannot add message to datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}


	// notify members that message was received
	rsp.Status = NotificationMessageReceived
	msg := new(Message)
	msg.MessageKey = mKey
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
			sendServerMessage(conns[user.username][t], rsp)
		}
	}

	// set ClientTime to the time sent by the client (for identifying message internally)
	rsp.Message.ClientTime = message.Message.ClientTime
	sendServerMessage(bufrw, rsp)

	fmt.Printf("%s (%s) sent message to %s\n", user.Profile.Name, user.username, *message.Message.To)
}

func handleUpdateMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		fmt.Printf("%s (%s) cannot update message: missing Message\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing Message")
		sendServerMessage(bufrw, rsp)
		return
	}

	if message.Message.ConversationKey == nil || message.Message.MessageKey == nil || message.Message.Text == nil {
		fmt.Printf("%s (%s) cannot update message: missing ConversationKey, MessageKey, and/or Text\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ConversationKey, MessageKey, and/or Text")
		sendServerMessage(bufrw, rsp)
		return
	}

	if len(*message.Message.Text) == 0 {
		fmt.Printf("%s (%s) cannot update message: empty Text\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "empty Text")
		sendServerMessage(bufrw, rsp)
		return
	}

	// get Message (MessageKey) from datastore
	dsmsgKey := message.Message.MessageKey
	dsmsg := new(DSMessage)
	err := client.Get(c, dsmsgKey, dsmsg)
	if err != nil {
		fmt.Errorf("%s (%s) cannot update message: cannot get message from datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	// ensure that the user who wrote the message is the same one trying to update it
	if dsmsg.From != user.username {
		fmt.Printf("%s (%s) cannot update message: user can only update messages he/she has sent\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorDefault, "user can only update messages he/she has sent")
		sendServerMessage(bufrw, rsp)
		return
	}

	// update message
	dsmsg.Text = *message.Message.Text

	// put Message (MessageKey) into datastore
	_, err = client.Put(c, dsmsgKey, dsmsg)
	if err != nil {
		fmt.Errorf("%s (%s) cannot update message: cannot update Message in datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	// get Conversation (ConversationKey) from datastore
	convKey := message.Message.ConversationKey
	conv := new(DSConversation)
	err = client.Get(c, convKey, conv)
	if err != nil {
		fmt.Errorf("%s (%s) cannot update message: cannot get conversation from datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
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

	fmt.Printf("%s (%s) updated a message\n", user.Profile.Name, user.username)
}

func handleAddUserToConversation(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		fmt.Printf("%s (%s) cannot add %s to conversation: missing Message\n", user.Profile.Name, user.username, *message.Username)
		rsp.setCustomError(ErrorMissingParameter, "missing Message")
		sendServerMessage(bufrw, rsp)
		return
	}

	if message.Message.ConversationKey == nil || message.Username == nil {
		fmt.Printf("%s (%s) cannot add %s to conversation: missing ConversationKey and/or Username\n", user.Profile.Name, user.username, *message.Username)
		rsp.setCustomError(ErrorMissingParameter, "missing ConversationKey and/or Username")
		sendServerMessage(bufrw, rsp)
		return
	}

	if len(*message.Username) == 0 {
		fmt.Printf("%s (%s) cannot add %s to conversation: empty Username\n", user.Profile.Name, user.username, *message.Username)
		rsp.setCustomError(ErrorEmptyParameter, "empty Username")
		sendServerMessage(bufrw, rsp)
		return
	}

	// get Conversation from datastore (ConversationKey)
	conv := new(DSConversation)
	convKey := message.Message.ConversationKey
	err := client.Get(c, convKey, conv)
	if err != nil {
		fmt.Errorf("%s (%s) cannot add %s to conversation: cannot get conversation from datastore: %s\n", user.Profile.Name, user.username, *message.Username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	// ensure user.username is in Conversation
	contains := false
	for member := range conv.MemberStatus {
		if member == user.username {
			contains = true
			break
		}
	}

	if !contains {
		fmt.Printf("%s (%s) cannot add %s to conversation: %s not in conversation\n", user.Profile.Name, user.username, *message.Username, user.username)
		rsp.setCustomError(ErrorDefault, "user not in conversation")
		sendServerMessage(bufrw, rsp)
		return
	}

	// update Conversation (add message.Username to MemberStatus status={false, false})
	conv.MemberStatus[*message.Username] = Status{false, false}

	// put Conversation into datastore
	_, err = client.Put(c, convKey, conv)
	if err != nil {
		fmt.Errorf("%s (%s) cannot add %s to conversation: cannot update conversation in datastore: %s\n", user.Profile.Name, user.username, *message.Username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	// notify all users in Conversation (NotificationUserAddedToConversation)
	rsp.Status = NotificationUserAddedToConversation
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	for member := range conv.MemberStatus {
		if member != *message.Username {
			sendServerMessageToUser(member, rsp)
		}
	}

	// send message to new member (message.Username) with all conversation data
	rsp.Conversations = new([]Conversation)
	(*rsp.Conversations)[0].MemberStatus = conv.MemberStatus
	(*rsp.Conversations)[0].Time = conv.Time

	q := datastore.NewQuery(KindMessage).Ancestor(convKey).Order("ServerTime")
	dsMsgs := new([]DSMessage)
	_, err = client.GetAll(c, q, dsMsgs)
	if err != nil {
		fmt.Errorf("%s (%s) cannot add %s to conversation: cannot get messages from datastore: %s\n", user.Profile.Name, user.username, *message.Username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
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

	fmt.Printf("%s was added to a conversation\n", *message.Username)
}

func handleRemoveUserFromConversation(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		fmt.Printf("%s (%s) cannot remove %s from conversation: missing Message\n", user.Profile.Name, user.username, *message.Username)
		rsp.setCustomError(ErrorMissingParameter, "missing Message")
		sendServerMessage(bufrw, rsp)
		return
	}

	if message.Message.ConversationKey == nil || message.Username == nil {
		fmt.Printf("%s (%s) cannot remove %s from conversation: missing ConversationKey and/or Username\n", user.Profile.Name, user.username, *message.Username)
		rsp.setCustomError(ErrorMissingParameter, "missing ConversationKey and/or Username")
		sendServerMessage(bufrw, rsp)
		return
	}

	if len(*message.Username) == 0 {
		fmt.Printf("%s (%s) cannot remove %s from conversation: empty Username\n", user.Profile.Name, user.username, *message.Username)
		rsp.setCustomError(ErrorEmptyParameter, "empty Username")
		sendServerMessage(bufrw, rsp)
		return
	}

	// get Conversation from datastore
	conv := new(DSConversation)
	convKey := message.Message.ConversationKey
	err := client.Get(c, convKey, conv)
	if err != nil {
		fmt.Errorf("%s (%s) cannot remove %s from conversation: cannot get conversation from datastore: %s\n", user.Profile.Name, user.username, *message.Username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
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

	if usr {
		if delUsr || user.username == *message.Username {
			// update Conversation (remove message.Username from MemberStatus)
			delete(conv.MemberStatus, *message.Username)
		} else {
			// error delUsr not in conversation
			fmt.Printf("%s (%s) cannot remove %s from conversation: %s not in conversation\n", user.Profile.Name, user.username, *message.Username, user.username)
			rsp.setCustomError(ErrorDefault, "user not in conversation")
			sendServerMessage(bufrw, rsp)
			return
		}
	} else {
		// error usr not in conversation
		fmt.Printf("%s (%s) cannot remove %s from conversation: %s not in conversation\n", user.Profile.Name, user.username, user.username, user.username)
		rsp.setCustomError(ErrorDefault, "user not in conversation")
		sendServerMessage(bufrw, rsp)
		return
	}

	// put Conversation into datastore
	_, err = client.Put(c, convKey, conv)
	if err != nil {
		fmt.Errorf("%s (%s) cannot remove %s from conversation: cannot update conversation in datastore: %s\n", user.Profile.Name, user.username, *message.Username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	// notify all users in Conversation (NotificationUserRemovedFromConversation)
	rsp.Status = NotificationUserRemovedFromConversation
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	for member := range conv.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	// send notification to user that was removed
	sendServerMessageToUser(*message.Username, rsp)

	fmt.Printf("%s was removed from a conversation\n", *message.Username)
}

func handleReadMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) {
	rsp := new(ServerMessage)
	if message.Message == nil {
		fmt.Printf("%s (%s) cannot read message: missing Message\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Message")
		sendServerMessage(bufrw, rsp)
		return
	}

	if message.Message.ConversationKey == nil {
		fmt.Printf("%s (%s) cannot read message: missing ConversationKey\n", user.Profile.Name, user.username)
		rsp.setCustomError(ErrorMissingParameter, "missing ServerMessage.Message.ConversationKey")
		sendServerMessage(bufrw, rsp)
		return
	}

	// update conversation in datastore
	conversation := new(DSConversation)
	err := client.Get(c, message.Message.ConversationKey, conversation)
	if err != nil {
		fmt.Errorf("%s (%s) cannot read message: cannot get user from datastore: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	conversation.MemberStatus[user.username] = Status{true, conversation.MemberStatus[user.username].Typing}
	_, err = client.Put(c, message.Message.ConversationKey, conversation)
	if err != nil {
		fmt.Errorf("%s (%s) cannot read message: %s\n", user.Profile.Name, user.username, err)
		rsp.setError(err)
		sendServerMessage(bufrw, rsp)
		return
	}

	rsp.Status = NotificationMessageRead
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.username

	// notify members, including the sender
	for member := range conversation.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	fmt.Printf("%s (%s) read message\n", user.Profile.Name, user.username)
}

type Connection struct {
	bufrw *bufio.ReadWriter
	time  time.Time
}

type Connections map[string]map[time.Time]*bufio.ReadWriter

var conns = make(Connections)
var connsMutex = new(sync.Mutex)

func (conns Connections) contains(username string) bool {
	_, contains := conns[username]
	return contains
}

func (conns *Connections) add(user *DSUser) {
	_, contains := (*conns)[user.username]

	connsMutex.Lock()
	(*conns)[user.username][user.connection.time] = user.connection.bufrw
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

	for {
		loggedIn = false
		usr := new(DSUser)
		rsp := new(ServerMessage)
		msg := new(ServerMessage)
		for !loggedIn {
			// first message received is either Login or Register
			if !getServerMessage(bufrw, msg) {
				continue
			}

			switch msg.Status {
			case ActionLogIn:
				loggedIn = logIn(usr, msg)
				break
			case ActionRegister:
				loggedIn = register(usr, msg)
				break
			default:
				// any other message requires the user to be logged in
				fmt.Println("cannot perform action: not logged in")
				rsp.clear()
				rsp.Status = ErrorNotLoggedIn
				sendServerMessage(bufrw, rsp)
				break
			}
		}

		// add connection to connections map
		connection := new(Connection)
		connection.time = time.Now()
		connection.bufrw = bufrw
		usr.connection = connection
		conns.add(usr)

		// return logged in message with user information
		rsp.clear()
		rsp.Status = NotificationLoggedIn
		rsp.Username = &usr.username
		rsp.Profile = &usr.Profile
		rsp.Contacts = getContacts(usr)
		rsp.Conversations = getConversations(usr)

		sendServerMessage(bufrw, rsp)

		// event loop
		for {
			if !getServerMessage(bufrw, msg) {
				continue
			}
			handleServerMessage(usr, bufrw, msg)
			if msg.Status == ActionLogOut {
				break
			}
		}
	}
}

var c = context.Background()
var client *datastore.Client

func main() {
	var err error
	client, err = datastore.NewClient(c, ProjectID)
	if err != nil {
		fmt.Errorf("cannot create datastore client: %s", err)
		return
	}

	r := mux.NewRouter()
	r.HandleFunc("/connect", handleConnect).Methods("GET")

	fmt.Println("Server started")
	log.Fatal(http.ListenAndServe(":8675", r))
}
