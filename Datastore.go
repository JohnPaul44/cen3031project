package main

import (
	"time"
)

const (
	KindUser         = "User"
	KindConversation = "Conversation"
	KindMessage      = "Message"
)

type DSUser struct {
	// key: username, parent: nil
	username      string      // unexported, used internally
	connection    *Connection // unexported, used internally
	PassHash      []byte   `json:"passHash"`
	Profile       Profile  `json:"profile"`
	Contacts      []string `json:"contacts"`
	Conversations []string `json:"conversations"`
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
	Time         time.Time         `json:"time"`          // time of last message, used for ordering conversations in client
	MemberStatus map[string]Status `json:"memberStatus"` // members of conversation and their read/typing statuses
}
