package main

import (
	"log"
	"bufio"
	"fmt"
	"time"
	"cloud.google.com/go/datastore"
)

type ServerMessageHandler func(*DSUser, *bufio.ReadWriter, *ServerMessage) error

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

func handleServerMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	handler, contains := handlerMap[message.Status]
	if !contains {
		rsp := new(ServerMessage)
		e := ErrInvalidStatus
		log.Println(e)
		rsp.setError(e)
		serr := sendServerMessage(bufrw, rsp)
		if serr != nil {
			return serr
		}
		return e
	}
	return handler(user, bufrw, message)
}

func handleLogOut(user *DSUser, bufrw *bufio.ReadWriter, _ *ServerMessage) error {
	rsp := new(ServerMessage)
	// connection removed in main event loop
	rsp.Status = NotificationLoggedOut

	log.Printf("%s (%s) logged out\n", user.Profile.Name, user.username)

	return sendServerMessage(bufrw, rsp)
}

func handleAddContact(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot add contact:", user.Profile.Name, user.username)

	if message.Username == nil {
		e := NewError("missing username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Username) == 0 {
		e := NewError("empty username", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	errStr = fmt.Sprintf("%s (%s) cannot add '%s' as a contact:", user.Profile.Name, user.username, *message.Username)

	exists, err := userExists(*message.Username)
	if err != nil {
		log.Printf("%s %s cannot get user '%s' from datastore: %s\n", ErrorTag, errStr, *message.Username, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	if !exists {
		e := NewError(fmt.Sprintf("user '%s' does not exist", *message.Username), ErrorInvalidUsername)
		log.Println(errStr, e)
		rsp.setError(e)
		rsp.Username = message.Username
		return sendServerMessage(bufrw, rsp)
	}

	err = client.Get(c, usernameStringToKey(user.username), user)
	if err != nil {
		log.Printf("%s %s cannot get user '%s' from datastore: %s\n", ErrorTag, errStr, user.username, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	contactExists := false
	for _, u := range user.Contacts {
		if u == *message.Username {
			contactExists = true
			break
		}
	}
	if contactExists {
		log.Println(errStr, "user is already a contact")
		// intentionally not sending error to client if contact already exists
		return nil
	}

	user.Contacts = append(user.Contacts, *message.Username)
	_, err = client.Put(c, usernameStringToKey(user.username), user)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update contacts in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	rsp.Status = NotificationContactAdded
	rsp.Username = message.Username

	sendServerMessageToUser(user.username, rsp)
	log.Printf("%s (%s) added user '%s' as a contact\n", user.Profile.Name, user.username, *message.Username)

	return nil
}

func handleRemoveContact(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot remove contact:", user.Profile.Name, user.username)

	if message.Username == nil {
		e := NewError("missing username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Username) == 0 {
		e := NewError("empty username", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	errStr = fmt.Sprintf("%s (%s) cannot remove user '%s' from contacts:", user.Profile.Name, user.username, *message.Username)

	err := client.Get(c, usernameStringToKey(user.username), user)
	if err != nil {
		log.Printf("%s %s cannot get user '%s' from datastore: %s\n", ErrorTag, errStr, user.username, err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	contacts, contains := remove(user.Contacts, *message.Username)
	if !contains {
		log.Println(errStr, "user is not a contact")
		// intentionally not sending error to client if user is not a contact
		return nil
	}

	user.Contacts = contacts

	rsp.Status = NotificationContactRemoved
	rsp.Username = message.Username

	sendServerMessageToUser(user.username, rsp)
	log.Printf("%s (%s) removed '%s' from contacts\n", user.Profile.Name, user.username, *message.Username)

	return nil
}

func handleUpdateProfile(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot update profile:", user.Profile.Name, user.username)

	if message.Profile == nil {
		e := NewError("missing profile", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(message.Profile.Name) == 0 || len(message.Profile.Email) == 0 {
		e := NewError("empty profile.name and/or profile.email", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	_, err := client.Put(c, usernameStringToKey(user.username), message.Profile)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update profile in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// update local profile (for logging) for all connections with the same username
	conns.updateProfile(user, *message.Profile)

	rsp.clear()
	rsp.Status = NotificationProfileUpdated
	rsp.Profile = &user.Profile

	sendServerMessageToUser(user.username, rsp)

	log.Printf("%s (%s) updated %s profile\n", user.Profile.Name, func() string {
		if user.Profile.Gender == nil {
			return "his/her"
		}
		switch *user.Profile.Gender {
		case GenderFemale:
			return "her"
		case GenderMale:
			return "his"
		default:
			return "their"
		}
	}(), user.username)

	return nil
}

func handleSetTyping(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot update typing status:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Message.ConversationKey == nil || message.Message.Typing == nil {
		e := NewError("missing message.conversationKey and/or message.typing", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// get conversation from datastore
	convKey := conversationStringToKey(*message.Message.ConversationKey)
	conversation := new(DSConversation)
	err := client.Get(c, convKey, conversation)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	// update conversation status in datastore
	conversation.MemberStatus[user.username] = Status{conversation.MemberStatus[user.username].Read, *message.Message.Typing}
	_, err = client.Put(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	rsp.Status = NotificationTyping
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.username
	rsp.Message.Typing = message.Message.Typing

	// notify members of conversation
	for member := range conversation.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s (%s) %s typing\n", user.Profile.Name, user.username, func() string {
		if *message.Message.Typing {
			return "started"
		} else {
			return "stopped"
		}
	}())

	return nil
}

func handleSendMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot send message:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if (message.Message.To == nil && message.Message.ConversationKey == nil) || message.Message.Text == nil || message.Message.ClientTime == nil {
		e := NewError("missing message.to, message.conversationKey, message.text and/or message.clientTime", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if ((len(*message.Message.To) == 0 || len((*message.Message.To)[0]) == 0) && len(*message.Message.ConversationKey) == 0) || len(*message.Message.Text) == 0 || len(*message.Message.ClientTime) == 0 {
		e := NewError("empty message.to, message.conversationKey, message.text and/or message.clientTime", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
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
		conversation.MemberStatus[user.username] = Status{true, false} // the user who sent the message has already read it and is socketClosed typing

		conversationKey := datastore.IncompleteKey(KindConversation, nil)
		conversationKey, err := client.Put(c, conversationKey, conversation)
		if err != nil {
			log.Println(ErrorTag, errStr, "cannot add conversation to datastore:", err)
			rsp.setError(ErrInternalServer)
			return sendServerMessage(bufrw, rsp)
		}

		message.Message.ConversationKey = &conversationKey.Name

		// add conversation key to every member's conversation list (including this account)
		for member := range conversation.MemberStatus {
			usrKey := usernameStringToKey(member)
			u := new(DSUser)
			err = client.Get(c, usrKey, u)
			if err != nil {
				log.Println(ErrorTag, errStr, "cannot get user from datastore:", err)
				rsp.setError(ErrInternalServer)
				return sendServerMessage(bufrw, rsp)
			}

			u.Conversations = append(u.Conversations, conversationKey.Name)
			_, err = client.Put(c, usrKey, u)
			if err != nil {
				log.Println(ErrorTag, errStr, "cannot update user's conversations in datastore:", err)
				rsp.setError(ErrInternalServer)
				return sendServerMessage(bufrw, rsp)
			}
		}

		log.Printf("%s (%s) created new conversation with: %s\n", user.Profile.Name, user.username, *message.Message.To)
	}

	// add message to datastore and get key
	m := new(DSMessage)
	m.Time = time.Now()
	m.Text = *message.Message.Text
	m.From = user.username
	if message.Message.Reactions != nil {
		m.Reactions = *message.Message.Reactions
	}

	convKey := conversationStringToKey(*message.Message.ConversationKey)

	err := client.Get(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// update time in conversation to reflect most recent message
	conversation.Time = m.Time
	_, err = client.Put(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	mKey := datastore.IncompleteKey(KindMessage, convKey)
	mKey, err = client.Put(c, mKey, m)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot add message to datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// notify members that message was received
	rsp.Status = NotificationMessageReceived
	msg := new(Message)
	msg.MessageKey = &mKey.Name
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
	for t := range conns[user.username].connections {
		if t != user.connection.time {
			conn := conns[user.username].connections[t]
			err = sendServerMessage(conn.bufrw, rsp)
			if err != nil {
				if socketClosed(err) {
					log.Printf("socket closed for '%s'\n", user.username)
				} else {
					log.Println(ErrorTag, "error sending message to client:", err)
				}
			}
		}
	}

	// set ClientTime to the time sent by the client (for identifying message internally)
	rsp.Message.ClientTime = message.Message.ClientTime
	log.Printf("%s (%s) sent message to: %s\n", user.Profile.Name, user.username, *message.Message.To)

	return sendServerMessage(bufrw, rsp)
}

func handleUpdateMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot update message:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Message.ConversationKey == nil || message.Message.MessageKey == nil || message.Message.Text == nil {
		e := NewError("missing message.conversationKey, message.messageKey and/or message.text", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 || len(*message.Message.MessageKey) == 0 || len(*message.Message.Text) == 0 {
		e := NewError("empty message.conversationKey, message.messageKey and/or message.text", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	convKey := conversationStringToKey(*message.Message.ConversationKey)

	// get Message (MessageKey) from datastore
	dsMsgKey := messageStringToKey(*message.Message.MessageKey, convKey)
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
		return sendServerMessage(bufrw, rsp)
	}

	// ensure that the user who wrote the message is the same one trying to update it
	if dsMsg.From != user.username {
		e := NewError(fmt.Sprintf("user can only update messages %s sent", func() string {
			if user.Profile.Gender == nil {
				return "he/she has"
			}
			switch *user.Profile.Gender {
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
		return sendServerMessage(bufrw, rsp)
	}

	// update message
	dsMsg.Text = *message.Message.Text

	// put Message (MessageKey) into datastore
	_, err = client.Put(c, dsMsgKey, dsMsg)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update Message in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
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
		return sendServerMessage(bufrw, rsp)
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

	log.Printf("%s (%s) updated a message\n", user.Profile.Name, user.username)

	return nil
}

func handleAddUserToConversation(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot add user to conversation:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing Message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Username == nil || message.Message.ConversationKey == nil {
		e := NewError("missing username and/or message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Username) == 0 || len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty username and/or message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	errStr = fmt.Sprintf("%s (%s) cannot add user '%s' to conversation:", user.Profile.Name, user.username, *message.Username)

	// get Conversation from datastore (ConversationKey)
	conv := new(DSConversation)
	convKey := conversationStringToKey(*message.Message.ConversationKey)
	err := client.Get(c, convKey, conv)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	var members []string

	usr := false
	newUsr := false
	for member := range conv.MemberStatus {
		members = append(members, member)
		if member == user.username {
			usr = true
		} else if member == *message.Username {
			newUsr = true
		}
	}

	// ensure usr is in conversation
	if !usr {
		e := NewError(fmt.Sprintf("'%s' is not in conversation", user.username), ErrorUnauthorized)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// ensure newUsr is not already in Conversation
	if newUsr {
		// intentionally not sending response to client, since there is nothing to do if the user is already in the conversation
		return nil
	}

	// update Conversation (add message.Username to MemberStatus status={false, false})
	conv.MemberStatus[*message.Username] = Status{false, false}

	// put Conversation into datastore
	_, err = client.Put(c, convKey, conv)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// notify all users in Conversation
	rsp.Status = NotificationUserAddedToConversation
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	for member := range conv.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	// send message to new member (message.Username) with all conversation data
	rsp.Conversations = new([]Conversation)
	(*rsp.Conversations)[0].MemberStatus = conv.MemberStatus
	(*rsp.Conversations)[0].Time = conv.Time
	(*rsp.Conversations)[0].ConversationKey = convKey.Name

	q := datastore.NewQuery(KindMessage).Ancestor(convKey).Order("ServerTime")
	dsMsgs := new([]DSMessage)
	_, err = client.GetAll(c, q, dsMsgs)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get messages from datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
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
	log.Printf("%s (%s) added '%s' to conversation with: %s\n", user.Profile.Name, user.username, *message.Username, members)

	return nil
}

func handleRemoveUserFromConversation(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot remove user from conversation:", user.Profile.Name, user.username)

	if message.Message == nil || message.Username == nil {
		e := NewError("missing message and/or username", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Message.ConversationKey == nil {
		e := NewError("missing message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Username) == 0 || len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty username and/or message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	errStr = fmt.Sprintf("%s (%s) cannot remove '%s' from conversation:", user.Profile.Name, user.username, *message.Username)

	// get Conversation from datastore
	conv := new(DSConversation)
	convKey := conversationStringToKey(*message.Message.ConversationKey)
	err := client.Get(c, convKey, conv)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
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

	// ensure usr is in conversation
	if !usr {
		e := NewError(fmt.Sprintf("'%s' is not in conversation", user.username), ErrorUnauthorized)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// ensure delUsr is already in conversation
	if !delUsr {
		// intentionally not sending a response to client, since there is nothing to do if the user isn't in the conversation
		return nil
	}


	// delete conversation if the last person is removed
	if len(conv.MemberStatus) == 1 {
		err = client.Delete(c, convKey)
		// intentionally not sending a response to client
		return nil
	}

	// put Conversation into datastore
	_, err = client.Put(c, convKey, conv)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	// notify all users in Conversation (NotificationUserRemovedFromConversation)
	rsp.Status = NotificationUserRemovedFromConversation
	rsp.Message = new(Message)
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Username = message.Username

	var members []string

	for member := range conv.MemberStatus {
		members = append(members, member)
		sendServerMessageToUser(member, rsp)
	}

	// only send notification to user if he/she didn't remove himself/herself
	if user.username == *message.Username {
		log.Printf("'%s' removed %s from conversation with: %s\n", user.username, func() string {
			if user.Profile.Gender == nil {
				return "himself/herself"
			}
			switch *user.Profile.Gender {
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

func handleReadMessage(user *DSUser, bufrw *bufio.ReadWriter, message *ServerMessage) error {
	rsp := new(ServerMessage)
	errStr := fmt.Sprintf("%s (%s) cannot read message:", user.Profile.Name, user.username)

	if message.Message == nil {
		e := NewError("missing message", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if message.Message.ConversationKey == nil {
		e := NewError("missing message.conversationKey", ErrorMissingParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	if len(*message.Message.ConversationKey) == 0 {
		e := NewError("empty message.conversationKey", ErrorEmptyParameter)
		log.Println(errStr, e)
		rsp.setError(e)
		return sendServerMessage(bufrw, rsp)
	}

	// update conversation in datastore
	convKey := conversationStringToKey(*message.Message.ConversationKey)
	conversation := new(DSConversation)
	err := client.Get(c, convKey, conversation)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			rsp.setError(e)
		} else {
			log.Println(ErrorTag, errStr, "cannot get user from datastore:", err)
			rsp.setError(ErrInternalServer)
		}
		return sendServerMessage(bufrw, rsp)
	}

	conversation.MemberStatus[user.username] = Status{true, conversation.MemberStatus[user.username].Typing}
	_, err = client.Put(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update user in datastore:", err)
		rsp.setError(ErrInternalServer)
		return sendServerMessage(bufrw, rsp)
	}

	rsp.Status = NotificationMessageRead
	rsp.Message.ConversationKey = message.Message.ConversationKey
	rsp.Message.From = &user.username

	// notify members, including the sender
	for member := range conversation.MemberStatus {
		sendServerMessageToUser(member, rsp)
	}

	log.Printf("%s (%s) read a message\n", user.Profile.Name, user.username)

	return nil
}