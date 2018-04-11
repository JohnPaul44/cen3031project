package main

import (
	"log"
	"fmt"
	"cloud.google.com/go/datastore"
	"net"
	e "./Errors"
	ds "./Datastore"
	msg "./ServerMessage"
)

type ServerMessageHandler func(*ds.User, net.Conn, *msg.ServerMessage) error

var handlerMap = map[int]ServerMessageHandler{
	msg.ActionLogOut:                     handleLogOut,
	msg.ActionQueryUsers:                 handleQueryUsers,
	msg.ActionAddContact:                 handleAddContact,
	msg.ActionRemoveContact:              handleRemoveContact,
	msg.ActionUpdateProfile:              handleUpdateProfile,
	msg.ActionSendMessage:                handleSendMessage,
	msg.ActionUpdateMessage:              handleUpdateMessage,
	msg.ActionReactToMessage:             handleReactToMessage,
	msg.ActionAddUserToConversation:      handleAddUserToConversation,
	msg.ActionRemoveUserFromConversation: handleRemoveUserFromConversation,
	msg.ActionReadMessage:                handleReadMessage,
	msg.ActionSetTyping:                  handleSetTyping,
}

func handleServerMessage(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	handler, contains := handlerMap[message.Status]
	if !contains {
		rsp := new(msg.ServerMessage)
		err := e.ErrInvalidStatus
		log.Println(err)
		rsp.SetError(err)
		serr := sendServerMessage(conn, rsp)
		if serr != nil {
			return serr
		}
		return err
	}
	return handler(user, conn, message)
}

func handleLogOut(user *ds.User, conn net.Conn, _ *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	// connection removed in main event loop
	rsp.Status = msg.NotificationLoggedOut

	log.Println(user.Username, "logged out")

	return sendServerMessage(conn, rsp)
}

func handleQueryUsers(_ *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationQueryResults

	if message.Query == nil {
		err := e.New("missing query", e.MissingParameter)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Query) == 0 {
		err := e.New("empty query", e.EmptyParameter)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	// query datastore
	results, err := ds.QueryUserAccounts(*message.Query)
	if err != nil {
		log.Println("cannot query user accounts:", err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	rsp.QueryResults = &results

	return sendServerMessage(conn, rsp)
}

func handleAddContact(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationContactAdded
	errStr := user.Username + " cannot add contact:"

	if message.Username == nil {
		err := e.New("missing username", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Username) == 0 {
		err := e.New("empty username", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	errStr = fmt.Sprintf("%s cannot add %s as a contact:", user.Username, *message.Username)

	contact, err := ds.AddContact(user.Username, *message.Username)
	if err != nil {
		if err == e.ErrInvalidUsername {
			serr := e.ErrInvalidUsername
			log.Println(errStr, serr)
			rsp.SetError(serr)
			return sendServerMessage(conn, rsp)
		}

		if err == e.ErrUnauthorized {
			serr := e.ErrUnauthorized
			log.Println(errStr, serr)
			rsp.SetError(serr)
			return sendServerMessage(conn, rsp)
		}

		log.Printf("%s %s cannot get user %s from datastore: %s\n", e.Tag, errStr, user.Username, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	contactProfile, err := ds.GetUserProfile(*message.Username)
	if err != nil {
		serr := e.ErrInternalServer
		log.Println(errStr, serr)
		rsp.SetError(serr)
		return sendServerMessage(conn, rsp)
	}

	user.Contacts = append(user.Contacts, contact)

	rsp.Username = message.Username
	rsp.Profile = &contactProfile

	sendServerMessageToUser(user.Username, rsp)
	log.Printf("%s added %s as a contact\n", user.Username, *message.Username)

	// TODO: send notification to contact that user.Username has added him/her as a contact

	return nil
}

func handleRemoveContact(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationContactRemoved
	errStr := user.Username + " cannot remove contact:"

	if message.Username == nil {
		err := e.New("missing username", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Username) == 0 {
		err := e.New("empty username", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	errStr = fmt.Sprintf("%s cannot remove %s from contacts:", user.Username, *message.Username)

	err := ds.RemoveContact(user.Username, *message.Username)
	if err != nil {
		if err == e.ErrInvalidUsername {
			err := e.ErrInvalidUsername
			log.Println(errStr, err)
			rsp.SetError(err)
			return sendServerMessage(conn, rsp)
		}

		log.Println(e.Tag, errStr, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// remove contact from local copy
	for i, c := range user.Contacts {
		if c.Contact == *message.Username {
			user.Contacts = append(user.Contacts[:i], user.Contacts[i+1:]...)
			break
		}
	}

	rsp.Username = message.Username

	sendServerMessageToUser(user.Username, rsp)
	log.Printf("%s removed %s from contacts\n", user.Username, *message.Username)

	return nil
}

func handleUpdateProfile(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationProfileUpdated
	errStr := user.Username + " cannot update profile:"

	if message.Profile == nil {
		err := e.New("missing profile", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(message.Profile.FirstName) == 0 || len(message.Profile.LastName) == 0 || len(message.Profile.Email) == 0 ||
		len(message.Profile.Phone) == 0 || len(message.Profile.Color) == 0{
		err := e.New("empty profile.[firstName | lastName | email | phone | color]", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	// update local profile (for logging) for all connections with the same username
	ds.UpdateConnectionProfile(user, *message.Profile)

	err := ds.UpdateProfile(user.Username, *message.Profile)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot update profile in datastore:", err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	rsp.Profile = &user.Profile

	sendServerMessageToUser(user.Username, rsp)

	log.Printf("%s updated %s profile: %+v\n", user.Username, func() string {
		switch user.Profile.Gender {
		case msg.GenderFemale:
			return "her"
		case msg.GenderMale:
			return "his"
		default:
			return "their"
		}
	}(), *rsp.Profile)

	return nil
}

func handleSendMessage(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationMessageReceived
	errStr := user.Username + " cannot send message:"


	if message.Message == nil {
		err := e.New("missing Message", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if (message.Message.To == nil && message.Message.ConversationKey == nil) || message.Message.Text == nil || message.Message.ClientTime == nil {
		err := e.New("missing message.to, message.conversationKey, message.text and/or message.clientTime", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Message.Text) == 0 || len(*message.Message.ClientTime) == 0 {
		err := e.New("empty message.text and/or message.clientTime", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.To != nil {
		if len(*message.Message.To) == 0 || len((*message.Message.To)[0]) == 0 {
			err := e.New("empty message.to", e.EmptyParameter)
			log.Println(errStr, err)
			rsp.SetError(err)
			return sendServerMessage(conn, rsp)
		}
	} else {
		if len(*message.Message.ConversationKey) == 0 {
			err := e.New("empty message.conversationKey", e.EmptyParameter)
			log.Println(errStr, err)
			rsp.SetError(err)
			return sendServerMessage(conn, rsp)
		}
	}

	log.Printf("handling sendMessage: text: %s\n", *message.Message.Text)
	isNewConversation := message.Message.ConversationKey == nil

	message.Message.From = &user.Username
	m, err := ds.AddMessage(*message.Message)
	if err != nil {
		if err == e.ErrInvalidConversationKey {
			log.Println(errStr, err)
			rsp.SetError(e.ErrInvalidConversationKey)
			return sendServerMessage(conn, rsp)
		}

		log.Println(e.Tag, errStr, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	memberStatuses, err := ds.GetConversationMemberStatuses(m.Key.Parent)
	if err != nil {
		log.Println(e.Tag, errStr, "could not get member statuses:", err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	msgKeyString := new(string)
	*msgKeyString = fmt.Sprintf("%d", m.Key.ID)
	convKeyString := new(string)
	*convKeyString = fmt.Sprintf("%d", m.Key.Parent.ID)

	memberMsg := new(msg.Message)
	memberMsg.MessageKey = msgKeyString
	memberMsg.ConversationKey = convKeyString
	memberMsg.ServerTime = &m.Time
	memberMsg.ClientTime = &message.ClientTime
	memberMsg.From = &m.From.Name
	memberMsg.Text = &m.Text

	if isNewConversation {
		// move message to Conversation.Messages
		conv := new(msg.Conversation)
		conv.ConversationKey = *convKeyString
		conv.MemberStatus = memberStatuses
		conv.Messages = make(map[string]msg.Message)
		conv.Messages[m.Key.Name] = *memberMsg
		rsp.Conversations = new(map[string]msg.Conversation)
		*rsp.Conversations = make(map[string]msg.Conversation)
		(*rsp.Conversations)[*convKeyString] = *conv
	} else {
		rsp.Message = memberMsg
	}

	log.Printf("Sending message: from='%s', convKey='%s', msgKey='%s', text='%s'\n", *memberMsg.From, *memberMsg.ConversationKey, *memberMsg.MessageKey, *memberMsg.Text)

	// send message to all users in conversation, excluding sender
	for member := range memberStatuses {
		if member != user.Username {
			sendServerMessageToUser(member, rsp)
		}
	}

	// send message to all sender's connections, excluding the originating connection
	for t, connection := range ds.GetConnections(user.Username) {
		if t != user.Connection.Time {
			err = sendServerMessage(connection.Conn, rsp)
			if err != nil {
				log.Printf("socket closed for '%s'\n", user.Username)
			}
		}
	}

	if rsp.Message == nil {
		rsp.Message = new(msg.Message)
	}
	rsp.Message.ClientTime = &message.ClientTime

	return sendServerMessage(conn, rsp)
}

func handleUpdateMessage(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationMessageUpdated
	errStr := user.Username + " cannot update message:"

	if message.Message == nil {
		err := e.New("missing Message", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.ConversationKey == nil || message.Message.MessageKey == nil || message.Message.Text == nil {
		err := e.New("missing message.conversationKey, message.messageKey and/or message.text", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 || len(*message.Message.MessageKey) == 0 || len(*message.Message.Text) == 0 {
		err := e.New("empty message.conversationKey, message.messageKey and/or message.text", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	convKey := ds.GetConversationKey(*message.Message.ConversationKey)

	// get Message (MessageKey) from datastore
	dsMsgKey := ds.GetMessageKey(*message.Message.MessageKey, convKey)
	dsMsg, err := ds.GetMessage(dsMsgKey)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			err := e.ErrInvalidMessageKey
			log.Println(errStr, err)
			rsp.SetError(err)
		} else {
			log.Println(e.Tag, errStr, "cannot get message from datastore:", err)
			rsp.SetError(e.ErrInternalServer)
		}
		return sendServerMessage(conn, rsp)
	}

	// ensure that the user who wrote the message is the same one trying to update it
	if dsMsg.From.Name != user.Username {
		err := e.New(fmt.Sprintf("user can only update messages %s sent", func() string {
			switch user.Profile.Gender {
			case msg.GenderFemale:
				return "she has"
			case msg.GenderMale:
				return "he has"
			default:
				return "they have"
			}
		}()), e.Unauthorized)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	// update message
	dsMsg.Text = *message.Message.Text
	err = ds.UpdateMessage(dsMsg)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot update Message in datastore:", err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// notify all users in Conversation (NotificationMessageUpdated), including the sender
	rsp.Message = new(msg.Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.MessageKey = message.Message.MessageKey
	rsp.Message.Text = message.Message.Text

	memberStatuses, err := ds.GetConversationMemberStatuses(convKey)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get member statuses:", err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	for member := range memberStatuses {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s updated a message\n", user.Username)

	return nil
}

func handleReactToMessage(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	//if message.Message == nil || message.Message.Reactions == nil {
	//
	//}
	//
	//userReactions, contains := (*message.Message.Reactions)[user.Username]
	//if !contains {
	//
	//}
	//
	//// update datastore
	//msg, err := ds.GetMessage()
	//
	//// send notifications to online users
	//

	return nil
}

func handleAddUserToConversation(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationUserAddedToConversation
	errStr := user.Username + " cannot add user to conversation:"

	if message.Message == nil {
		err := e.New("missing Message", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if message.Username == nil || message.Message.ConversationKey == nil {
		err := e.New("missing username and/or message.conversationKey", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Username) == 0 || len(*message.Message.ConversationKey) == 0 {
		err := e.New("empty username and/or message.conversationKey", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	errStr = fmt.Sprintf("%s cannot add user %s to conversation:", user.Username, *message.Username)

	err := ds.AddUserToConversation(user.Username, *message.Username, *message.Message.ConversationKey)

	convKey := ds.GetConversationKey(*message.Message.ConversationKey)
	conv, err := ds.GetConversation(convKey)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get conversation from datastore:", err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// notify all users in Conversation
	rsp.Message = new(msg.Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	memberStatuses, err := ds.GetConversationMemberStatuses(convKey)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get member statuses:", err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	for member := range memberStatuses {
		sendServerMessageToUser(member, rsp)
	}

	messages, err := ds.GetMessages(convKey)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get conversation messages:", err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// send message to new member (message.Username) with all conversation data
	rsp.Conversations = new(map[string]msg.Conversation)
	*rsp.Conversations = make(map[string]msg.Conversation)
	conversation := msg.Conversation{MemberStatus: memberStatuses, LastMessage: conv.LastMessage, ConversationKey: convKey.Name, Messages: messages}
	(*rsp.Conversations)[convKey.Name] = conversation


	sendServerMessageToUser(*message.Username, rsp)
	log.Printf("%s added %s to conversation with: %s\n", user.Username, *message.Username, func() []string {
		members, err := ds.GetConversationMembers(convKey)
		if err != nil {
			log.Println(e.Tag, errStr, err)
		}
		return *members
	}())

	return nil
}

func handleRemoveUserFromConversation(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationUserRemovedFromConversation
	errStr := user.Username + " cannot remove user from conversation:"

	if message.Message == nil || message.Username == nil {
		err := e.New("missing message and/or username", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.ConversationKey == nil {
		err := e.New("missing message.conversationKey", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Username) == 0 || len(*message.Message.ConversationKey) == 0 {
		err := e.New("empty username and/or message.conversationKey", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	errStr = fmt.Sprintf("%s cannot remove %s from conversation:", user.Username, *message.Username)

	convKey := ds.GetConversationKey(*message.Message.ConversationKey)
	err := ds.RemoveUserFromConversation(user.Username, *message.Username, convKey)
	if err != nil {
		if err == e.ErrInvalidUsername {
			// intentionally not sending a response
			return err
		}
		log.Println(e.Tag, errStr, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// notify all users in Conversation (NotificationUserRemovedFromConversation)
	rsp.Message = new(msg.Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	members, err := ds.GetConversationMembers(convKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	for _, member := range *members {
		sendServerMessageToUser(member, rsp)
	}

	// only send notification to user if he/she didn't remove himself/herself
	if user.Username == *message.Username {
		log.Printf("'%s' removed %s from conversation with: %s\n", user.Username, func() string {
			switch user.Profile.Gender {
			case msg.GenderFemale:
				return "herself"
			case msg.GenderMale:
				return "himself"
			default:
				return "themself"
			}
		}(), members)
	} else {
		sendServerMessageToUser(*message.Username, rsp)
		log.Printf("'%s' was removed from conversation with: %s\n", *message.Username, members)
	}

	return nil
}

func handleReadMessage(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationMessageRead
	errStr := user.Username + " cannot read message:"

	if message.Message == nil {
		err := e.New("missing message", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.ConversationKey == nil {
		err := e.New("missing message.conversationKey", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 {
		err := e.New("empty message.conversationKey", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	convKey := ds.GetConversationKey(*message.Message.ConversationKey)

	err := ds.ReadConversation(user.Username, convKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	rsp.Message = new(msg.Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.Username

	memberStatuses, err := ds.GetConversationMemberStatuses(convKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// notify members, including the sender
	for member := range memberStatuses {
		sendServerMessageToUser(member, rsp)
	}

	log.Println(user.Username, "read a message")

	return nil
}

func handleSetTyping(user *ds.User, conn net.Conn, message *msg.ServerMessage) error {
	rsp := new(msg.ServerMessage)
	rsp.Status = msg.NotificationTyping
	errStr := user.Username + " cannot update typing status:"

	if message.Message == nil {
		err := e.New("missing message", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.ConversationKey == nil || message.Message.Typing == nil {
		err := e.New("missing message.conversationKey and/or message.typing", e.MissingParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 {
		err := e.New("empty message.conversationKey", e.EmptyParameter)
		log.Println(errStr, err)
		rsp.SetError(err)
		return sendServerMessage(conn, rsp)
	}

	convKey := ds.GetConversationKey(*message.Message.ConversationKey)

	err := ds.SetTypingStatus(user.Username, convKey, *message.Message.Typing)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.Username
	rsp.Message.Typing = message.Message.Typing

	// notify members of conversation
	members, err := ds.GetConversationMembers(convKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		rsp.SetError(e.ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	for _, member := range *members {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s %s typing\n", user.Username, func() string {
		if *message.Message.Typing {
			return "started"
		} else {
			return "stopped"
		}
	}())

	return nil
}
