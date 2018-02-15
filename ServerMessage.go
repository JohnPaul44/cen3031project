package main

import (
	"time"
	"bufio"
	"encoding/json"
	"log"
)

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

// TODO: add conversationKey to Conversation struct
// TODO: send conversationKey in Conversation on login and addUserToConversation

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
	Age	   *int		`json:"age,omitempty"`
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

func sendServerMessageToUser(username string, message *ServerMessage) {
	_, contains := conns[username]
	if contains {
		for _, conn := range conns[username].connections {
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