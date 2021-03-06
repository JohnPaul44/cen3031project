package ServerMessage

import (
	"time"
	e "../Errors"
)

type Reactions int

const (
	ReactionExclamation Reactions = 1 << iota
	ReactionQuestion              = 1 << iota
	ReactionHeart                 = 1 << iota
	ReactionThumbsUp              = 1 << iota
	ReactionThumbsDown            = 1 << iota
)

func (r *Reactions) clear() {
	*r = 0
}

func (r *Reactions) set(reaction Reactions) {
	*r |= reaction
}

type Message struct {
	ResponseKey     *string               `json:"responseKey,omitempty"`
	ServerTime      *time.Time            `json:"serverTime,omitempty"`
	ClientTime      *string               `json:"clientTime,omitempty"`
	To              *[]string             `json:"to,omitempty"`
	MessageKey      *string               `json:"messageKey,omitempty"`
	ConversationKey *string               `json:"conversationKey,omitempty"`
	From            *string               `json:"from,omitempty"`
	Text            *string               `json:"text,omitempty"`
	Reactions       *map[string]Reactions `json:"reactions,omitempty"`
	Typing          *bool                 `json:"typing,omitempty"`
}

type Status struct {
	Read   bool `json:"read",datastore:"Read"`
	Typing bool `json:"typing",datastore:"Typing"`
}

// TODO: implement Created time in handlers
type Conversation struct {
	Created         time.Time         `json:"created"`
	LastMessage     time.Time         `json:"lastMessage"`
	MemberStatus    map[string]Status `json:"memberStatus"`
	ConversationKey string            `json:"conversationKey"`
	Messages        []Message         `json:"messages"`
}

type ContactGame struct {
	Wins   int `json:"wins"`
	Losses int `json:"losses"`
	Ties   int `json:"ties"`
}

type Contact struct {
	Online  bool      `json:"online"`
	Added   time.Time `json:"added"` // when contact was first added
	Profile Profile   `json:"profile"`

	// TODO: implement in methods
	SentMessages     int                    `json:"sentMessages"`
	ReceivedMessages int                    `json:"receivedMessages"`
	Games            map[string]ContactGame `json:"games"`
	FriendshipLevel  int                    `json:"friendshipLevel"`

	/*
	FriendshipLevel = (ln(sent + received) + 15*ln(wins+losses+ties)) * 100
	*/
}

const (
	GenderFemale  = "female"
	GenderMale    = "male"
	GenderOther   = "other"
	GenderUnknown = "na"
)

// TODO: add field for profile picture color in Profile and in datastore
type Profile struct {
	// Required
	FirstName string `json:"firstName"`
	LastName  string `json:"lastName"`
	Email     string `json:"email"`
	Phone     string `json:"phone"`

	// Optional
	Gender   string `json:"gender,omitempty"`
	Birthday string `json:"birthday,omitempty"`
}

type ServerMessage struct {
	Status           int                 `json:"status"`
	ErrorNumber      *int                `json:"errorNumber,omitempty"`
	ErrorString      *string             `json:"errorString,omitempty"`
	Username         *string             `json:"username,omitempty"`
	Password         *string             `json:"password,omitempty"`
	Profile          *Profile            `json:"profile,omitempty"`
	Query            *string             `json:"query,omitempty"`
	QueryResults     *map[string]Profile `json:"queryResults,omitempty"`
	SecurityQuestion *string             `json:"securityQuestion,omitempty"`
	SecurityAnswer   *string             `json:"securityAnswer,omitempty"`
	Phone            *string             `json:"phone,omitempty"`
	Contacts         *map[string]Contact `json:"contacts,omitempty"`
	Online           *bool               `json:"online,omitempty"`
	Conversations    *[]Conversation     `json:"conversations,omitempty"`
	Message          *Message            `json:"message,omitempty"`
}

func (msg *ServerMessage) Clear() {
	msg.Status = StatusUninitialized
	msg.ErrorNumber = nil
	msg.ErrorString = nil
	msg.Username = nil
	msg.Password = nil
	msg.Profile = nil
	msg.Query = nil
	msg.QueryResults = nil
	msg.SecurityQuestion = nil
	msg.SecurityAnswer = nil
	msg.Phone = nil
	msg.Contacts = nil
	msg.Online = nil
	msg.Conversations = nil
	msg.Message = nil
}

func (msg *ServerMessage) SetError(err e.Error) {
	msg.Status = NotificationError
	msg.ErrorNumber = new(int)
	*msg.ErrorNumber = err.Id()
	msg.ErrorString = new(string)
	*msg.ErrorString = err.Error()
}

const (
	// Default status
	StatusUninitialized = iota

	// Notifications are sent to devices
	NotificationError                       = iota // returns ErrorNumber and ErrorString
	NotificationLoggedIn                    = iota // returns Username, Profile, Contacts, ConversationKeys
	NotificationUserOnlineStatus            = iota // returns Online, Username
	NotificationSecurityQuestion            = iota // returns SecurityQuestion
	NotificationPasswordChanged             = iota
	NotificationLoggedOut                   = iota // session has ended, returns nothing
	NotificationQueryResults                = iota // returns QueryResults
	NotificationContactAdded                = iota // returns Username
	NotificationContactRemoved              = iota // returns Username
	NotificationProfileUpdated              = iota // returns Profile
	NotificationMessageReceived             = iota // returns Message.[ConversationKey, MessageKey, ServerTime, From, Text (, Reactions)] (Message is embedded in ConversationKeys if it is the first message in a conversation)
	NotificationMessageUpdated              = iota // returns Message.[ConversationKey, MessageKey, Text]
	NotificationMessageReaction             = iota // returns Message.[ConversationKey, MessageKey, Reactions]
	NotificationUserAddedToConversation     = iota // returns Username, Message.ConversationKey (, Message.ConversationKeys (returned only to new user))
	NotificationUserRemovedFromConversation = iota // returns Username, Message.ConversationKey
	NotificationMessageRead                 = iota // returns Message.[ConversationKey, From]
	NotificationTyping                      = iota // returns Message.[ConversationKey, From, Typing]
	NotifiactionContactUpdated              = iota // returns Contacts[Username]

	// Actions are received from client devices
	ActionRegister                   = iota // requires Username, Password, First Name, Last Name, Email, Security Question & Answer, phone; optionally DOB, gender
	ActionLogIn                      = iota // requires Username, Password
	ActionRequestSecurityQuestion    = iota // requires Username
	ActionChangePassword             = iota // requires Username, Password, SecurityAnswer, Phone
	ActionLogOut                     = iota // request to end session, requires nothing
	ActionQueryUsers                 = iota // requires Query
	ActionAddContact                 = iota // requires Username
	ActionRemoveContact              = iota // requires Username
	ActionUpdateProfile              = iota // requires Profile
	ActionSendMessage                = iota // requires Message.[(To | ConversationKey), ClientTime, Text]
	ActionUpdateMessage              = iota // requires Message.[ConversationKey, MessageKey, Text]
	ActionReactToMessage             = iota // requires Message.[ConversationKey, MessageKey, Reactions]
	ActionAddUserToConversation      = iota // requires Username, Message.ConversationKey
	ActionRemoveUserFromConversation = iota // requires Username, Message.ConversationKey
	ActionReadMessage                = iota // requires ConversationKey
	ActionSetTyping                  = iota // requires Message.ConversationKey, Message.Typing
	ActionGetContact                 = iota // requires Username
)
