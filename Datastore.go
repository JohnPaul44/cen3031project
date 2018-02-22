package main

import (
	"time"
	"cloud.google.com/go/datastore"
	"log"
	"google.golang.org/api/iterator"
	"golang.org/x/crypto/bcrypt"
	"fmt"
)

const (
	KindUser                        = "User"
	KindConversation                = "Conversation"
	KindConversationMessage         = "Message"
	KindUserContact                 = "Contact"
	KindConversationMember          = "ConversationMember"
	KindConversationMessageReaction = "MessageReaction"
)

type DSUserContact struct {
	// KindUserContact, key=KindUser(contact username), parent=KindUser(contact owner)
	Time    time.Time `datastore:"Time"`
	contact *datastore.Key
	owner   *datastore.Key
}

type DSUser struct {
	// key: username, parent: nil
	username   string
	connection *Connection
	contacts   []string
	PassHash   []byte  `json:"passHash",datastore:"PassHash"`
	Profile    Profile `json:"profile",datastore:"Profile"`
}

type DSMessageReaction struct {
	// KindMessageReaction, key=KindUser(username), parent=KindConversationMessage
	Reactions  Reactions `datastore:"Reactions"`
	messageKey *datastore.Key
}

type DSMessage struct {
	// type: KindConversationMessage, key: random, parent: KindConversation
	key  *datastore.Key
	Time time.Time      `json:"time",datastore:"Time"`
	From *datastore.Key `json:"from",datastore:"From"`
	Text string         `json:"text",datastore:"Text"`
}

type DSConversationMember struct {
	// KindConversationMember, key=KindUser(username), parent=KindConversation
	Status
	member          *datastore.Key
	conversationKey *datastore.Key
}

type DSConversation struct {
	// type: KindConversation, key: random, parent: nil
	key         *datastore.Key
	Created     time.Time `json:"created",datastore:"Created"`         // time the conversation was created
	LastMessage time.Time `json:"lastMessage",datastore:"LastMessage"` // time of last message, used for ordering conversations in client
}

func getUserKey(username string) *datastore.Key {
	return datastore.NameKey(KindUser, username, nil)
}

func getConversationKey(conversation string) *datastore.Key {
	return datastore.NameKey(KindConversation, conversation, nil)
}

func getMessageKey(message string, conversationKey *datastore.Key) *datastore.Key {
	return datastore.NameKey(KindConversationMessage, message, conversationKey)
}

func getContactKey(owner string, contact string) *datastore.Key {
	return datastore.NameKey(KindUserContact, contact, getUserKey(owner))
}

func getMemberKey(username string, conversationKey *datastore.Key) *datastore.Key {
	return datastore.NameKey(KindConversationMember, username, conversationKey)
}

func userExists(username string) (bool, error) {
	user := new(DSUser)
	err := client.Get(c, getUserKey(username), user)
	if err == nil {
		return true, nil
	}
	if err == datastore.ErrNoSuchEntity {
		return false, nil
	}
	return false, err
}

func createUserAccount(username string, password string, profile Profile) (*DSUser, error) {
	errStr := "cannot create user account:"

	exists, err := userExists(username)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot determine if user already exists:", err)
		return nil, err
	}
	if exists {
		e := NewError(fmt.Sprintf("username '%s' is already taken", username), ErrorExistingAccount)
		log.Println(errStr, e)
		return nil, ErrExistingAccount
	}

	user := new(DSUser)
	user.username = username
	user.Profile = profile
	user.PassHash, err = bcrypt.GenerateFromPassword([]byte(password), 10)
	if err != nil {
		return nil, err
	}

	_, err = client.Put(c, getUserKey(username), user)
	if err != nil {
		return nil, err
	}

	return user, nil
}

func getUserAccount(username string) (*DSUser, error) {
	errStr := "cannot get user account:"

	userKey := getUserKey(username)
	user := new(DSUser)
	err := client.Get(c, userKey, user)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			// user doesn't exist
			log.Println(errStr, ErrInvalidUsername)
			return nil, ErrInvalidUsername
		}

		log.Println(ErrorTag, errStr, "cannot get user from datastore:", err)
		return nil, ErrInternalServer
	}

	user.username = username

	return user, nil
}

func getUserAccountAuthenticated(username string, password string) (*DSUser, error) {
	errStr := "cannot get user account:"

	user, err := getUserAccount(username)
	if err != nil {
		log.Println(errStr, err)
		return nil, err
	}

	err = bcrypt.CompareHashAndPassword(user.PassHash, []byte(password))
	if err != nil {
		// invalid password
		log.Println(errStr, ErrInvalidPassword)
		return nil, ErrInvalidLogin
	}

	return user, nil
}

func deleteUserAccount(username string) error {
	// TODO: remove all references to user account from database (conversations, contacts, etc.)
	// TODO: close all connections under username



	userKey := getUserKey(username)
	err := client.Delete(c, userKey)
	return err
}

func canModifyContact(username string, contact string) (bool, error) {
	errStr := "cannot determine if contact can be manipulated:"

	uExists, err := userExists(username)
	if err != nil {
		log.Println(errStr, "cannot determine if user exists:", err)
		return false, err
	}

	if !uExists {
		return false, ErrUnauthorized
	}

	cExists, err := userExists(contact)
	if err != nil {
		log.Println(errStr, "cannot determine if contact exists:", err)
		return false, err
	}

	if !cExists {
		return false, ErrInvalidUsername
	}

	return true, nil
}

func contactExists(owner string, contact string) (bool, error) {
	errStr := "cannot determine if '%s' has '%s' in contacts list:"

	var userContact DSUserContact
	err := client.Get(c, getContactKey(owner, contact), userContact)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			return false, nil
		}
		log.Println(ErrorTag, errStr, err)
		return false, err
	}
	return true, nil
}

func addContact(username string, contact string) error {
	errStr := "cannot add contact:"

	authorized, err := canModifyContact(username, contact)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return err
	}

	if !authorized {
		return ErrUnauthorized
	}

	exists, err := contactExists(username, contact)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot determine if contact exists:", err)
		return err
	}

	if exists {
		return nil
	}

	// add contact
	contactKey := getContactKey(username, contact)
	newContact := DSUserContact{Time: time.Now()}
	_, err = client.Put(c, contactKey, newContact)
	return err
}

func removeContact(username string, contact string) error {
	errStr := "cannot remove contact:"

	authorized, err := canModifyContact(username, contact)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return err
	}

	if !authorized {
		return ErrUnauthorized
	}

	exists, err := contactExists(username, contact)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot determine if contact exists:", err)
		return err
	}

	if !exists {
		return nil
	}

	// remove contact
	contactKey := getContactKey(username, contact)
	return client.Delete(c, contactKey)
}

func getContacts(user *DSUser) (*[]string, error) {
	q := datastore.NewQuery(KindUserContact).Ancestor(getUserKey(user.username)).KeysOnly()
	dsUserContacts, err := client.GetAll(c, q, nil)
	if err != nil {
		return nil, err
	}

	contacts := new([]string)
	for _, dsContact := range dsUserContacts {
		*contacts = append(*contacts, dsContact.Name)
	}

	return contacts, nil
}

func updateProfile(username string, profile Profile) error {
	errStr := fmt.Sprintf("cannot update profile for '%s'", username)

	user, err := getUserAccount(username)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get user from datastore:", err)
		return err
	}

	user.Profile = profile

	_, err = client.Put(c, getUserKey(username), user)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update profile in datastore:", err)
		return err
	}

	return nil
}

func createConversation(username string, members []string) (*DSConversation, error) {
	errStr := "cannot create conversation:"

	exists, err := userExists(username)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot determine if user exists:", err)
		return nil, err
	}

	if !exists {
		return nil, ErrUnauthorized
	}

	conversation := new(DSConversation)
	conversation.Created = time.Now()

	conversationKey := datastore.IncompleteKey(KindConversation, nil)
	conversationKey, err = client.Put(c, conversationKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot create conversation in datastore:", err)
		return nil, err
	}

	conversation.key = conversationKey

	// add members to conversation
	for _, member := range members {
		exists, err := userExists(member)
		if err != nil {
			log.Println(ErrorTag, errStr, "cannot determine if user exists:", err)
			return nil, err
		}
		if exists {
			memberKey := getMemberKey(username, conversationKey)
			memberStatus := Status{Read: false, Typing: false}
			_, err = client.Put(c, memberKey, memberStatus)
			if err != nil {
				log.Println(ErrorTag, errStr, "cannot add member to conversation:", err)
				return nil, err
			}
		}
	}

	// add the user who sent the message to conversation
	memberKey := getMemberKey(username, conversationKey)
	memberStatus := Status{Read: true, Typing: false}
	_, err = client.Put(c, memberKey, memberStatus)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot add user to conversation:", err)
		return nil, err
	}

	return conversation, nil
}

func deleteConversation(conversationKey *datastore.Key) error {
	errStr := "cannot delete conversation:"

	// delete members from conversation
	q := datastore.NewQuery(KindConversationMember).Ancestor(conversationKey).KeysOnly()
	memberKeys, err := client.GetAll(c, q, nil)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get member keys from datastore:", err)
		return err
	}

	err = client.DeleteMulti(c, memberKeys)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot delete members from datastore:", err)
		return err
	}

	// delete all messages from conversation
	q = datastore.NewQuery(KindConversationMessage).Ancestor(conversationKey).KeysOnly()
	msgKeys, err := client.GetAll(c, q, nil)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get message keys from datastore:", err)
		return err
	}

	err = client.DeleteMulti(c, msgKeys)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot remove messages from datastore:", err)
		return err
	}

	// delete conversation
	err = client.Delete(c, conversationKey)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot remove conversation from datastore:", err)
		return err
	}

	return nil
}

func isUserInConversation(username string, conversationKey *datastore.Key) (bool, error) {
	errStr := fmt.Sprintf("cannot determine if '%s' is in conversation:", username)

	memberKey := getMemberKey(username, conversationKey)
	var member DSConversationMember
	err := client.Get(c, memberKey, member)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			return false, nil
		}
		log.Println(ErrorTag, errStr, err)
		return false, err
	}
	return true, nil
}

func getConversationMembers(conversationKey *datastore.Key) (*[]string, error) {
	errStr := "cannot get conversation members:"

	q := datastore.NewQuery(KindConversationMember).Ancestor(conversationKey).KeysOnly()
	memberKeys, err := client.GetAll(c, q, nil)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return nil, err
	}

	members := new([]string)

	for _, memberKey := range memberKeys {
		*members = append(*members, memberKey.Name)
	}

	return members, nil
}

func getConversationMemberStatuses(conversationKey *datastore.Key) (map[string]Status, error) {
	errStr := "cannot get conversation members' statuses:"

	q := datastore.NewQuery(KindConversationMember).Ancestor(conversationKey)
	it := client.Run(c, q)

	memberStatuses := make(map[string]Status)
	var err error

	for {
		var member DSConversationMember
		memberKey, err := it.Next(member)
		if err != nil {
			break
		}

		memberStatuses[memberKey.Name] = Status{Read: member.Read, Typing: member.Typing}
	}

	if err != iterator.Done {
		log.Println(ErrorTag, errStr, err)
		return nil, err
	}

	return memberStatuses, nil
}

func getConversations(user *DSUser) (*[]Conversation, error) {
	errStr := "cannot get conversations:"

	conversations := new([]Conversation)

	q := datastore.NewQuery(KindConversationMember).Filter("__key__ =", getUserKey(user.username))
	it := client.Run(c, q)

	var err error

	for {
		var dsConversation DSConversation
		convKey, err := it.Next(dsConversation)
		if err != nil {
			break
		}

		var conversation Conversation
		conversation.ConversationKey = convKey.Name
		conversation.Time = dsConversation.LastMessage
		conversation.MemberStatus = make(map[string]Status)

		// get members of conversation
		q = datastore.NewQuery(KindConversationMember).Ancestor(convKey)
		memberIt := client.Run(c, q)

		for {
			var member DSConversationMember
			memberKey, err := memberIt.Next(member)
			if err != nil {
				break
			}
			conversation.MemberStatus[memberKey.Name] = Status{Read: member.Read, Typing: member.Typing}
		}

		if err != iterator.Done {
			log.Println(ErrorTag, errStr, "cannot get conversation member from datastore:", err)
			return nil, err
		}

		// get messages
		q = datastore.NewQuery(KindConversationMessage)
		messageIt := client.Run(c, q)

		for {
			var dsMessage DSMessage
			msgKey, err := messageIt.Next(dsMessage)
			if err != nil {
				break
			}

			var message Message
			message.MessageKey = &msgKey.Name
			message.ServerTime = &dsMessage.Time
			message.From = &dsMessage.From.Name
			message.Text = &dsMessage.Text
			*message.Reactions = make(map[string]Reactions)

			// get reactions
			q = datastore.NewQuery(KindConversationMessageReaction).Ancestor(msgKey)
			reactionIt := client.Run(c, q)

			for {
				var dsReaction DSMessageReaction
				reactionKey, err := reactionIt.Next(dsReaction)
				if err != nil {
					break
				}

				(*message.Reactions)[reactionKey.Name] = dsReaction.Reactions
			}

			if err != iterator.Done {
				log.Println(ErrorTag, errStr, "cannot get message reactions from datastore:", err)
				return nil, err
			}
		}

		if err != iterator.Done {
			log.Println(ErrorTag, errStr, "cannot get message from datastore:", err)
			return nil, err
		}

		*conversations = append(*conversations, conversation)
	}

	if err != iterator.Done {
		log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
		return nil, err
	}

	return conversations, nil
}

func addUserToConversation(username string, addUsername string, conversationKey string) error {
	errStr := "cannot add user to conversation:"

	conv := new(DSConversation)
	convKey := getConversationKey(conversationKey)
	err := client.Get(c, convKey, conv)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			e := ErrInvalidConversationKey
			log.Println(errStr, e)
			return ErrInvalidConversationKey
		}

		log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
		return err
	}

	// ensure user is in conversation
	authorized, err := isUserInConversation(username, convKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return err
	}

	if !authorized {
		return ErrUnauthorized
	}

	// add user to conversation
	status := Status{false, false}
	newMemberKey := getMemberKey(addUsername, getConversationKey(conversationKey))
	_, err = client.Put(c, newMemberKey, status)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot add new member to datastore:", err)
		return err
	}

	return nil
}

func getNumberOfMembers(conversationKey *datastore.Key) (int, error) {
	errStr := "cannot get number of members in conversation:"

	q := datastore.NewQuery(KindConversationMember).Ancestor(conversationKey).KeysOnly()
	memberKeys, err := client.GetAll(c, q, nil)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return 0, err
	}

	return len(memberKeys), nil
}

func removeUserFromConversation(username string, removeUser string, conversationKey *datastore.Key) error {
	errStr := "cannot remove user from conversation:"

	authorized, err := isUserInConversation(username, conversationKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return err
	}

	if !authorized {
		return ErrUnauthorized
	}

	err = client.Delete(c, getMemberKey(removeUser, conversationKey))
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			return ErrInvalidUsername
		}
		log.Println(ErrorTag, errStr, err)
		return err
	}

	// delete conversation if there aren't any users in it
	members, err := getNumberOfMembers(conversationKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return err
	}

	if members == 0 {
		deleteConversation(conversationKey)
	}

	return nil
}

func setTypingStatus(username string, conversationKey *datastore.Key, typing bool) error {
	errStr := fmt.Sprintf("'%s' cannot set typing status to %s:", username, typing)

	authorized, err := isUserInConversation(username, conversationKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return err
	}

	if !authorized {
		return ErrUnauthorized
	}

	memberKey := getMemberKey(username, conversationKey)
	var member DSConversationMember
	err = client.Get(c, memberKey, member)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get member from datastore:", err)
		return err
	}

	member.Typing = typing
	_, err = client.Put(c, memberKey, member)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update member in datastore:", err)
		return err
	}

	return nil
}

func readConversation(username string, conversationKey *datastore.Key) error {
	errStr := fmt.Sprintf("'%s' cannot read conversation:", username)

	authorized, err := isUserInConversation(username, conversationKey)
	if err != nil {
		log.Println(ErrorTag, errStr, err)
		return err
	}

	if !authorized {
		return ErrUnauthorized
	}

	memberKey := getMemberKey(username, conversationKey)
	var member DSConversationMember
	err = client.Get(c, memberKey, member)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot get member from datastore:", err)
		return err
	}

	member.Read = true
	_, err = client.Put(c, memberKey, member)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update member in datastore:", err)
		return err
	}

	return nil
}

func sendMessage(message Message) (*DSMessage, error) {
	errStr := "cannot send message:"

	conversation := new(DSConversation)
	var convKey *datastore.Key

	if message.ConversationKey == nil {
		// new conversation
		// create conversation entity and get key
		conversation, err := createConversation(*message.From, *message.To)
		if err != nil {
			log.Println(ErrorTag, errStr, "cannot create conversation in datastore:", err)
			return nil, err
		}

		convKey = conversation.key
		members, err := getConversationMembers(convKey)
		if err != nil {
			log.Println(ErrorTag, errStr, "cannot get conversation members:", err)
			return nil, err
		}

		log.Printf("%s created a new conversation with: %s\n", message.From, members)
	} else {
		convKey = getConversationKey(*message.ConversationKey)
		err := client.Get(c, convKey, conversation)
		if err != nil {
			if err == datastore.ErrNoSuchEntity {
				e := ErrInvalidConversationKey
				log.Println(errStr, e)
				return nil, e
			}
			log.Println(ErrorTag, errStr, "cannot get conversation from datastore:", err)
			return nil, err
		}
	}

	// add message to datastore and get key
	msg := new(DSMessage)
	msg.Time = time.Now()
	msg.Text = *message.Text
	msg.From = getUserKey(*message.From)

	// update time in conversation to reflect most recent message
	conversation.LastMessage = msg.Time
	_, err := client.Put(c, convKey, conversation)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot update conversation in datastore:", err)
		return nil, err
	}

	// put new message in datastore
	messageKey := datastore.IncompleteKey(KindConversationMessage, convKey)
	messageKey, err = client.Put(c, messageKey, msg)
	if err != nil {
		log.Println(ErrorTag, errStr, "cannot add message to datastore:", err)
		return nil, err
	}

	msg.key = messageKey

	return msg, nil
}

func getMessages(conversationKey *datastore.Key) (*[]Message, error) {
	errStr := "cannot get messages:"

	q := datastore.NewQuery(KindConversationMessage).Ancestor(conversationKey).Order("ServerTime")
	it := client.Run(c, q)

	var messages []Message
	var err error

	for {
		var message DSMessage
		messageKey, err := it.Next(message)
		if err != nil {
			break
		}

		reactions, err := getMessageReactions(messageKey)
		if err != nil {
			break
		}

		msg := Message{ServerTime: &message.Time, From: &message.From.Name, MessageKey: &messageKey.Name, Text: &message.Text, Reactions: reactions}
		messages = append(messages, msg)
	}

	if err != iterator.Done {
		log.Println(ErrorTag, errStr, err)
		return nil, err
	}

	return &messages, nil
}

func getMessageReactions(messageKey *datastore.Key) (*map[string]Reactions, error) {
	errStr := "cannot get message reactions:"

	q := datastore.NewQuery(KindConversationMessageReaction).Ancestor(messageKey.Parent)
	it := client.Run(c, q)

	reactions := make(map[string]Reactions)
	var err error

	for {
		var reaction Reactions
		reactionKey, err := it.Next(reaction)
		if err != nil {
			break
		}

		reactions[reactionKey.Name] = reaction
	}

	if err != iterator.Done {
		log.Println(ErrorTag, errStr, err)
		return nil, err
	}

	return &reactions, nil
}

// TODO: don't process message reactions for ActionSendMessage (i.e. only allow reactions to be added after it's sent)
// TODO: move all authentication checks to functions in this file (i.e. message handlers should just check that the parameters exist, then call the appropriate function from this file)
// TODO: write functions for client operations so the errors just have to be handled once
// TODO: standardize error logging locations and returned errors (return ServerError instead of generic error for ease of handling in message handlers)
