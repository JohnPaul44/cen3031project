package main

import (
	"time"
	"cloud.google.com/go/datastore"
	"log"
	"google.golang.org/api/iterator"
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
		conversation.ConversationKey = conversationKey

		q := datastore.NewQuery(KindMessage).Ancestor(convKey).Order("time")
		it := client.Run(c, q)

		dsMessage := new(DSMessage)
		messageKey, err := it.Next(dsMessage)
		for err == nil {
			message := new(Message)
			message.MessageKey = &messageKey.Name
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