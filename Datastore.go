package main

import (
	"time"
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
}

type DSMessage struct {
	// type: KindMessage, key: random, parent: KindConversation
	Time	time.Time	`json:"time"`
	From	string		`json:"from"`
	Text	string		`json:"text"`
}

type DSConversation struct {
	// type: KindConversation, key: random, parent: KindUser
	Time	time.Time	`json:"time"`
	Members	[]string	`json:"members"`
}