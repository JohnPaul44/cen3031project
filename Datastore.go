package main

import (
	"time"
	"cloud.google.com/go/datastore"
)

const (
	KindUser         = "User"
	KindConversation = "Conversation"
	KindMessage      = "Message"
)

type DSUser struct {
	// key: username, parent: nil
	username      string      // unexported
	connection    *Connection // unexported
	PassHash      []byte           `json:"pass_hash,omitempty"`
	Profile       Profile          `json:"profile"`
	Contacts      []string         `json:"contacts,omitempty"`
	Conversations []*datastore.Key `json:"conversations"`
}

type DSMessage struct {
	// type: KindMessage, key: random, parent: KindConversation
	Time      time.Time      `json:"time"`
	From      string         `json:"from"`
	Text      string         `json:"text"`
	Reactions []UserReaction `json:"reactions"`
	// add additional fields for reactions
}

type DSConversation struct {
	// type: KindConversation, key: random, parent: nil
	Time    time.Time         `json:"time"`    // time of last message, used for ordering conversations in client
	Members map[string]Status `json:"members"` // members of conversation and their read/typing statuses
}
