package main

import (
	"time"
	"cloud.google.com/go/datastore"
)

const (
	KindUser = "User"
	KindConversation = "Conversation"
	KindMessage = "Message"
)

type DSUser struct {
	// key: username, parent: nil
	Name			string				`json:"name,omitempty"`
	PassHash		[]byte				`json:"pass_hash,omitempty"`
	Email			string				`json:"email,omitempty"`
	Phone			*string				`json:"phone,omitempty"`
	Contacts		[]string			`json:"contacts,omitempty"`
	Conversations	[]*datastore.Key	`json:"conversations"`
}

type DSMessage struct {
	// type: KindMessage, key: random, parent: KindConversation
	Time		time.Time	`json:"time"`
	From		string		`json:"from"`
	Text		string		`json:"text"`
	Reactions	*[]Reaction	`json:"reactions"`
	// add additional fields for reactions
}

type DSConversation struct {
	// type: KindConversation, key: random, parent: nil
	Time	time.Time            `json:"time"`    // time of last message, used for ordering conversations in client
	Members	map[string]Status `json:"members"` // members of conversation and their read/typing statuses
}