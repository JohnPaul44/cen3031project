package main

import (
	"log"
	"fmt"
	"cloud.google.com/go/datastore"
	"net"
)

type ServerMessageHandler func(*DSUser, net.Conn, *ServerMessage) error

// TODO: implement NotificationMessageReaction and ActionReactToMessage
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

func handleServerMessage(user *DSUser, conn net.Conn, message *ServerMessage) error {
	handler, contains := handlerMap[message.Status]
	if !contains {
		rsp := new(ServerMessage)
		e := ErrInvalidStatus
		log.Println(e)
		rsp.setError(e)
		serr := sendServerMessage(conn, rsp)
		if serr != nil {
			return serr
		}
		return e
	}
	return handler(user, conn, message)
}

func handleLogOut(user *DSUser, conn net.Conn, _ *ServerMessage) error {
	rsp := new(ServerMessage)
	// connection removed in main event loop
	rsp.Status = NotificationLoggedOut

	log.Println(user.username, "logged out")

	return sendServerMessage(conn, rsp)
}

func handleAddContact(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot add contact:"

	if message.Username == nil {
		e := NewError("missing username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Username) == 0 {
		e := NewError("empty username", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	errStr = fmt.Sprintf("%s cannot add %s as a contact:", user.username, *message.Username)


	err := addContact(user.username, *message.Username)
	if err != nil {
		if err == ErrInvalidUsername {
			e := ErrInvalidUsername
			log.Println(errStr, e)
			rsp.setError(e)
			return sendServerMessage(conn, rsp)
		}

		log.Printf("%s %s cannot get user %s from datastore: %s\n", ErrorTag, errStr, user.username, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	user.contacts = append(user.contacts, *message.Username)

	rsp.Status = NotificationContactAdded
	rsp.Username = message.Username

	sendServerMessageToUser(user.username, rsp)
	log.Printf("%s added %s as a contact\n", user.username, *message.Username)

	return nil
}

func handleRemoveContact(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot remove contact:"

	if message.Username == nil {
		e := NewError("missing username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Username) == 0 {
		e := NewError("empty username", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	errStr = fmt.Sprintf("%s cannot remove %s from contacts:", user.username, *message.Username)

	err := removeContact(user.username, *message.Username)
	if err != nil {
		if err == ErrInvalidUsername {
			e := ErrInvalidUsername
			log.Println(errStr, e)
			rsp.setError(e)
			return sendServerMessage(conn, rsp)
		}

		log.Println(ErrorTag, errStr, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	user.contacts, _ = remove(user.contacts, *message.Username)

	rsp.Status = NotificationContactRemoved
	rsp.Username = message.Username

	sendServerMessageToUser(user.username, rsp)
	log.Printf("%s removed %s from contacts\n", user.username, *message.Username)

	return nil
}

func handleUpdateProfile(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot update profile:"

	if message.Profile == nil {
		e := NewError("missing profile", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if len(message.Profile.FirstName) == 0 || len(message.Profile.LastName) == 0 || len(message.Profile.Email) == 0 ||
		len(message.Profile.Phone) == 0 || len(message.Profile.SecurityQuestion) == 0 || len(message.Profile.SecurityAnswer) == 0 {
		e := NewError("empty profile.[firstName | lastName | email | phone | securityQuestion | securityAnswer", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	// update local profile (for logging) for all connections with the same username
	conns.updateProfile(user, *message.Profile)

	err := updateProfile(user.username, *message.Profile)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update profile in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}
	rsp.clear()
	rsp.Status = NotificationProfileUpdated
	rsp.Profile = &user.Profile

	sendServerMessageToUser(user.username, rsp)

	log.Printf("%s updated %s profile\n", user.username, func() string {
		switch user.Profile.Gender {
		case GenderFemale:
			return "her"
		case GenderMale:
			return "his"
		default:
			return "their"
		}
	}())

	return nil
}

func handleSetTyping(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot update typing status:"

	if message.Message == nil {
		e := NewError("missing message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.ConversationKey == nil || message.Message.Typing == nil {
		e := NewError("missing message.conversationKey and/or message.typing", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	convKey :=getConversationKey(*message.Message.ConversationKey)

	err := setTypingStatus(user.username, convKey, *message.Message.Typing)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	rsp.Status = NotificationTyping
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.username
	rsp.Message.Typing = message.Message.Typing

	// notify members of conversation
	members, err := getConversationMembers(convKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	for _, member := range *members {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s %s typing\n", user.username, func() string {
		if *message.Message.Typing {
			return "started"
		} else {
			return "stopped"
		}
	}())

	return nil
}

func handleSendMessage(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot send message:"

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if (message.Message.To == nil && message.Message.ConversationKey == nil) || message.Message.Text == nil || message.Message.ClientTime == nil {
		e := NewError("missing message.to, message.conversationKey, message.text and/or message.clientTime", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if ((len(*message.Message.To) == 0 || len((*message.Message.To)[0]) == 0) && len(*message.Message.ConversationKey) == 0) || len(*message.Message.Text) == 0 || len(*message.Message.ClientTime) == 0 {
		e := NewError("empty message.to, message.conversationKey, message.text and/or message.clientTime", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	isNewConversation := message.Message.ConversationKey == nil

	message.Message.From = &user.username
	m, err := sendMessage(*message.Message)
	if err != nil {
		if err == ErrInvalidConversationKey {
			log.Println(errStr, err)
			rsp.setError(ErrInvalidConversationKey)
			return sendServerMessage(conn, rsp)
		}
	}

	// notify members that a message was received
	rsp.Status = NotificationMessageReceived
	msg := new(Message)
	msg.MessageKey = &m.key.Name
	msg.ConversationKey = &m.key.Parent.Name
	msg.ServerTime = &m.Time
	msg.From = &m.From.Name
	msg.Text = &m.Text

	memberStatuses, err := getConversationMemberStatuses(m.key.Parent)
	if err != nil {
		log.Println(ErrorTag, errStr, "could not get member statuses:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	if isNewConversation {
		// move message to Conversation.Messages[0]
		conv := new(Conversation)
		conv.ConversationKey = *msg.ConversationKey
		conv.MemberStatus = memberStatuses
		conv.Messages[0] = *msg
		rsp.Conversations = new([]Conversation)
		(*rsp.Conversations)[0] = *conv
	} else {
		rsp.Message = msg
	}

	// send message to all users in conversation, excluding sender
	for member := range memberStatuses {
		if member != user.username {
			sendServerMessageToUser(member, rsp)
		}
	}

	// send message to all sender's connections, excluding the originating connection
	for t := range conns[user.username].connections {
		if t != user.connection.time {
			conn := conns[user.username].connections[t]
			err = sendServerMessage(conn.conn, rsp)
			if err != nil {
				log.Printf("socket closed for '%s'\n", user.username)
			}
		}
	}

	// set ClientTime to the time sent by the client (for identifying message internally)
	rsp.Message.ClientTime = message.Message.ClientTime
	log.Println(user.username, "sent message to:", *message.Message.To)

	return sendServerMessage(conn, rsp)
}

func handleUpdateMessage(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot update message:"

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.ConversationKey == nil || message.Message.MessageKey == nil || message.Message.Text == nil {
		e := NewError("missing message.conversationKey, message.messageKey and/or message.text", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 || len(*message.Message.MessageKey) == 0 || len(*message.Message.Text) == 0 {
		e := NewError("empty message.conversationKey, message.messageKey and/or message.text", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	convKey := getConversationKey(*message.Message.ConversationKey)

	// get Message (MessageKey) from datastore
	dsMsgKey := getMessageKey(*message.Message.MessageKey, convKey)
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
		return sendServerMessage(conn, rsp)
	}

	// ensure that the user who wrote the message is the same one trying to update it
	if dsMsg.From.Name != user.username {
		e := NewError(fmt.Sprintf("user can only update messages %s sent", func() string {
			switch user.Profile.Gender {
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
		return sendServerMessage(conn, rsp)
	}

	// update message
	dsMsg.Text = *message.Message.Text

	// put Message (MessageKey) into datastore
	_, err = client.Put(c, dsMsgKey, dsMsg)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update Message in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
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
		return sendServerMessage(conn, rsp)
	}

	// notify all users in Conversation (NotificationMessageUpdated), including the sender
	rsp.Status = NotificationMessageUpdated
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.MessageKey = message.Message.MessageKey
	rsp.Message.Text = message.Message.Text

	memberStatuses, err := getConversationMemberStatuses(convKey)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get member statuses:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	for member := range memberStatuses {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s updated a message\n", user.username)

	return nil
}

func handleAddUserToConversation(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot add user to conversation:"

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if message.Username == nil || message.Message.ConversationKey == nil {
		e := NewError("missing username and/or message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Username) == 0 || len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty username and/or message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	errStr = fmt.Sprintf("%s cannot add user %s to conversation:", user.username, *message.Username)

	// get Conversation from datastore (ConversationKey)
	err := addUserToConversation(user.username, *message.Username, *message.Message.ConversationKey)

	convKey := getConversationKey(*message.Message.ConversationKey)
	conv := new(DSConversation)
	err = client.Get(c, convKey, conv)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// notify all users in Conversation
	rsp.Status = NotificationUserAddedToConversation
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	memberStatuses, err := getConversationMemberStatuses(convKey)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get member statuses:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	for member := range memberStatuses {
		sendServerMessageToUser(member, rsp)
	}

	// send message to new member (message.Username) with all conversation data
	rsp.Conversations = new([]Conversation)
	conversation := Conversation{MemberStatus:memberStatuses, LastMessage:conv.LastMessage, ConversationKey:convKey.Name}
	(*rsp.Conversations)[0] = conversation

	messages, err := getMessages(convKey)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get conversation messages:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	(*rsp.Conversations)[0].Messages = *messages

	sendServerMessageToUser(*message.Username, rsp)
	log.Printf("%s added %s to conversation with: %s\n", user.username, *message.Username, func() []string {
		members, err := getConversationMembers(convKey)
		if err != nil {
			log.Println(ErrorTag, errStr, err)
		}
		return *members
	}())

	return nil
}

func handleRemoveUserFromConversation(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot remove user from conversation:"

	if message.Message == nil || message.Username == nil {
		e := NewError("missing message and/or username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.ConversationKey == nil {
		e := NewError("missing message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Username) == 0 || len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty username and/or message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	errStr = fmt.Sprintf("%s cannot remove %s from conversation:", user.username, *message.Username)

	convKey := getConversationKey(*message.Message.ConversationKey)
	err := removeUserFromConversation(user.username, *message.Username, convKey)
	if err != nil {
		if err == ErrInvalidUsername {
			// intentionally not sending a response
			return err
		}
		log.Println(ErrorTag, errStr, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// notify all users in Conversation (NotificationUserRemovedFromConversation)
	rsp.Status = NotificationUserRemovedFromConversation
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	members, err := getConversationMembers(convKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	for _, member := range *members {
		sendServerMessageToUser(member, rsp)
	}

	// only send notification to user if he/she didn't remove himself/herself
	if user.username == *message.Username {
		log.Printf("'%s' removed %s from conversation with: %s\n", user.username, func() string {
			switch user.Profile.Gender {
			case GenderFemale: return "herself"
			case GenderMale: return "himself"
			default: return "themself"
			}
		}(), members)
	} else {
		sendServerMessageToUser(*message.Username, rsp)
		log.Printf("'%s' was removed from conversation with: %s\n", *message.Username, members)
	}

	return nil
}

func handleReadMessage(user *DSUser, conn net.Conn, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := user.username + " cannot read message:"

	if message.Message == nil {
		e := NewError("missing message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if message.Message.ConversationKey == nil {
		e := NewError("missing message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(conn, rsp)
	}

	convKey := getConversationKey(*message.Message.ConversationKey)

	err := readConversation(user.username, convKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	rsp.Status = NotificationMessageRead
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.username

	memberStatuses, err := getConversationMemberStatuses(convKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(conn, rsp)
	}

	// notify members, including the sender
	for member := range memberStatuses {
		sendServerMessageToUser(member, rsp)
	}

	log.Println(user.username, "read a message")

	return nil
}