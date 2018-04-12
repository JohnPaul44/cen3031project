package Datastore

import (
	"time"
	"cloud.google.com/go/datastore"
	"log"
	"google.golang.org/api/iterator"
	"golang.org/x/crypto/bcrypt"
	"fmt"
	"context"
	e "../Errors"
	msg "../ServerMessage"
	"strconv"
	"math"
)

const (
	KindUser = "User"
	//KindUserInterests               = "UserInterests"
	//KindUserHobbies                 = "UserHobbies"
	KindConversation                = "Conversation"
	KindConversationMessage         = "Message"
	KindUserContact                 = "Contact"
	KindConversationMember          = "ConversationMember"
	KindConversationMessageReaction = "MessageReaction"
)

type UserContact struct {
	// KindUserContact, key=KindUser(contact username), parent=KindUser(contact owner)
	Added      time.Time // when the user was added to contacts
	Statistics msg.FriendshipStatistics
	Contact    string `datastore:"-"`
	Owner      string `datastore:"-"`
}

type User struct {
	// key: username, parent: nil
	Username         string        `datastore:"-"`
	Connection       *Connection   `datastore:"-"`
	Contacts         []UserContact `datastore:"-"`
	PassHash         []byte        `json:"passHash"`
	Profile          msg.Profile   `json:"profile"`
	SecurityQuestion string        `json:"securityQuestion"`
	SecurityAnswer   string        `json:"securityAnswer"`
}

/*type UserInterest struct {
	Interest string
}

type UserHobbies struct {
	Hobby string
}*/

type MessageReaction struct {
	// KindMessageReaction, key=KindUser(username), parent=KindConversationMessage
	Reactions  msg.Reactions
	messageKey *datastore.Key
}

type Message struct {
	// type: KindConversationMessage, key: random, parent: KindConversation
	Key  *datastore.Key `datastore:"-"`
	Time time.Time      `json:"time"`
	From *datastore.Key `json:"from"`
	Text string         `json:"text"`
}

type ConversationMember struct {
	// KindConversationMember, key=KindUser(username), parent=KindConversation
	msg.Status
	Member string
}

type Conversation struct {
	// type: KindConversation, key: random, parent: nil
	Key         *datastore.Key `datastore:"-"`
	Created     time.Time      `json:"created"`     // time the conversation was created
	LastMessage time.Time      `json:"lastMessage"` // time of last message, used for ordering conversations in client
}

func GetUserKey(username string) *datastore.Key {
	return datastore.NameKey(KindUser, username, nil)
}

func GetConversationKey(conversation string) *datastore.Key {
	convID, _ := strconv.ParseInt(conversation, 10, 64)
	return datastore.IDKey(KindConversation, convID, nil)
}

func GetMessageKey(message string, conversationKey *datastore.Key) *datastore.Key {
	msgID, _ := strconv.ParseInt(message, 10, 64)
	return datastore.IDKey(KindConversationMessage, msgID, conversationKey)
}

func GetContactKey(owner string, contact string) *datastore.Key {
	return datastore.NameKey(KindUserContact, contact, GetUserKey(owner))
}

func GetMemberKey(username string, conversationKey *datastore.Key) *datastore.Key {
	return datastore.NameKey(KindConversationMember, username, conversationKey)
}

func UserExists(username string) (bool, error) {
	user := new(User)
	err := client.Get(c, GetUserKey(username), user)
	if err == nil {
		return true, nil
	}
	if err == datastore.ErrNoSuchEntity {
		return false, nil
	}
	return false, err
}

func GeneratePasswordHash(password string) ([]byte, error) {
	return bcrypt.GenerateFromPassword([]byte(password), 10)
}

func CreateUserAccount(username string, password string, profile msg.Profile, securityQuestion string, securityAnswer string) (*User, error) {
	errStr := "cannot create user account:"

	exists, err := UserExists(username)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot determine if user already exists:", err)
		return nil, err
	}
	if exists {
		err := e.New(fmt.Sprintf("username '%s' is already taken", username), e.ExistingAccount)
		log.Println(errStr, err)
		return nil, e.ErrExistingAccount
	}

	user := new(User)
	user.Username = username
	user.Profile = profile
	user.PassHash, err = GeneratePasswordHash(password)
	if err != nil {
		return nil, err
	}

	user.SecurityQuestion = securityQuestion
	user.SecurityAnswer = securityAnswer

	_, err = client.Put(c, GetUserKey(username), user)
	if err != nil {
		return nil, err
	}

	return user, nil
}

func GetUserAccount(username string) (*User, error) {
	errStr := "cannot get user account " + username + ":"

	userKey := GetUserKey(username)
	user := new(User)
	err := client.Get(c, userKey, user)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			// user doesn't exist
			log.Println(errStr, e.ErrInvalidUsername)
			return nil, e.ErrInvalidUsername
		}

		log.Println(e.Tag, errStr, "cannot get user from datastore:", err)
		return nil, e.ErrInternalServer
	}

	user.Username = username

	return user, nil
}

func GetUserAccountAuthenticated(username string, password string) (*User, error) {
	errStr := "cannot get user account:"

	user, err := GetUserAccount(username)
	if err != nil {
		log.Println(errStr, err)
		return nil, err
	}

	err = bcrypt.CompareHashAndPassword(user.PassHash, []byte(password))
	if err != nil {
		// invalid password
		log.Println(errStr, e.ErrInvalidPassword)
		return nil, e.ErrInvalidLogin
	}

	return user, nil
}

func UpdateUserAccount(user *User) error {
	errStr := "cannot update user account:"

	_, err := client.Put(c, GetUserKey(user.Username), user)
	if err != nil {
		log.Println(errStr, err)
		return err
	}

	return nil
}

func DeleteUserAccount(username string) error {
	// remove from conversations
	conversationMemberQuery := datastore.NewQuery(KindConversationMember).Filter("Member =", username).KeysOnly()
	conversationMemberKeys, err := client.GetAll(c, conversationMemberQuery, nil)
	if err != nil {
		log.Printf("cannot get conversation member keys for %s: %s\n", username, err)
	} else {
		err := client.DeleteMulti(c, conversationMemberKeys)
		if err != nil {
			log.Printf("cannot remove %s from conversations: %s\n", username, err)
		}
	}

	// delete contacts
	contactQuery := datastore.NewQuery(KindConversationMember).Ancestor(GetUserKey(username)).KeysOnly()
	contactKeys, err := client.GetAll(c, contactQuery, nil)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			return e.ErrInvalidUsername
		}
		log.Printf("cannot get contacts for %s: %s\n", username, err)
	} else {
		for _, key := range contactKeys {
			err = RemoveContact(username, key.Name)
			if err != nil {
				log.Printf("cannot remove %s from %s's contacts: %s\n", key.Name, username, err)
			}
		}
	}

	// delete account
	err = client.Delete(c, GetUserKey(username))
	if err != nil {
		log.Printf("cannot delete %s's user account: %s\n", username, err)
	}

	userKey := GetUserKey(username)
	return client.Delete(c, userKey)
}

/*func clearUserProfileArray(username string, kind string) error {
	q := datastore.NewQuery(kind).Ancestor(GetUserKey(username)).KeysOnly()
	keys, err := client.GetAll(c, q, nil)
	if err != nil {
		return err
	}

	return client.DeleteMulti(c, keys)
}

func updateUserProfileArray(username string, kind string, values []string) error {
	err := clearUserProfileArray(username, kind)
	if err != nil {
		return err
	}

	var keys []*datastore.Key

	for range values {
		keys = append(keys, datastore.IncompleteKey(kind, GetUserKey(username)))
	}

	_, err = client.PutMulti(c, keys, values)
	return err
}

func setUserInterests(username string, interests []string) error {

}

func GetUserInterests(username string) ([]string, error) {var values []string
	q := datastore.NewQuery(KindUserInterests).Ancestor(GetUserKey(username))
	it := client.Run(c, q)

	var err error

	for {
		var interest string
		_, intErr := it.Next(&interest)
		if intErr != nil {
			err = intErr
			break
		}

		values = append(values, interest)
	}

	if err != iterator.Done {
		log.Println(err)
		return values, err
	}

	return values, nil
}

func setUserHobbies(username string, hobbies []string) error {

}

func GetUserHobbies(username string) ([]string, error) {var values []string
	q := datastore.NewQuery(KindUserHobbies).Ancestor(GetUserKey(username))
	it := client.Run(c, q)

	var err error

	for {
		var interest string
		_, intErr := it.Next(&interest)
		if intErr != nil {
			err = intErr
			break
		}

		values = append(values, interest)
	}

	if err != iterator.Done {
		log.Println(err)
		return values, err
	}

	return values, nil
}*/

func GetUserProfile(username string) (msg.Profile, error) {
	var profile msg.Profile

	user, err := GetUserAccount(username)
	if err != nil {
		return profile, err
	}

	profile = user.Profile

	/*interests, err := GetUserInterests(username)
	if err != nil {
		log.Println("cannot get interests for user:", err)
	}
	profile.Interests = interests

	hobbies, err := GetUserHobbies(username)
	if err != nil {
		log.Println("cannot get hobbies for user:", err)
	}
	profile.Hobbies = hobbies*/

	return user.Profile, nil
}

func contains(list []string, item string) bool {
	for _, itm := range list {
		if itm == item {
			return true
		}
	}
	return false
}

func QueryUserAccounts(query string) (map[string]msg.Profile, error) {
	var err error
	var usernames []string

	// query username
	usernames = append(usernames, query)

	// query interests
	interestsQuery := datastore.NewQuery(KindUser).Filter("Profile.Interests =", query).KeysOnly()
	userKeys, err := client.GetAll(c, interestsQuery, nil)
	if err != nil {
		log.Println("cannot query interests", err)
	} else {
		for _, userKey := range userKeys {
			if !contains(usernames, userKey.Name) {
				usernames = append(usernames, userKey.Name)
			}
		}
	}

	// query hobbies
	hobbiesQuery := datastore.NewQuery(KindUser).Filter("Profile.Hobbies =", query).KeysOnly()
	userKeys, err = client.GetAll(c, hobbiesQuery, nil)
	if err != nil {
		log.Println("cannot query hobbies", err)
	} else {
		for _, userKey := range userKeys {
			if !contains(usernames, userKey.Name) {
				usernames = append(usernames, userKey.Name)
			}
		}
	}

	// query first name
	firstNameQuery := datastore.NewQuery(KindUser).Filter("Profile.FirstName =", query).KeysOnly()
	userKeys, err = client.GetAll(c, firstNameQuery, nil)
	if err != nil {
		log.Println("cannot query first name", err)
	} else {
		for _, userKey := range userKeys {
			if !contains(usernames, userKey.Name) {
				usernames = append(usernames, userKey.Name)
			}
		}
	}

	// query last name
	lastNameQuery := datastore.NewQuery(KindUser).Filter("Profile.LastName =", query).KeysOnly()
	userKeys, err = client.GetAll(c, lastNameQuery, nil)
	if err != nil {
		log.Println("cannot query last name", err)
	} else {
		for _, userKey := range userKeys {
			if !contains(usernames, userKey.Name) {
				usernames = append(usernames, userKey.Name)
			}
		}
	}

	// query email
	emailQuery := datastore.NewQuery(KindUser).Filter("Profile.Email =", query).KeysOnly()
	userKeys, err = client.GetAll(c, emailQuery, nil)
	if err != nil {
		log.Println("cannot query email", err)
	} else {
		for _, userKey := range userKeys {
			if !contains(usernames, userKey.Name) {
				usernames = append(usernames, userKey.Name)
			}
		}
	}

	// query phone
	phoneQuery := datastore.NewQuery(KindUser).Filter("Profile.Phone =", query).KeysOnly()
	userKeys, err = client.GetAll(c, phoneQuery, nil)
	if err != nil {
		log.Println("cannot query phone", err)
	} else {
		for _, userKey := range userKeys {
			if !contains(usernames, userKey.Name) {
				usernames = append(usernames, userKey.Name)
			}
		}
	}

	// get user accounts
	results := make(map[string]msg.Profile)

	for _, username := range usernames {
		userProfile, err := GetUserProfile(username)
		if err == nil {
			results[username] = userProfile
		}
	}

	return results, nil
}

func CanModifyContact(username string, contact string) (bool, error) {
	errStr := "cannot determine if contact can be manipulated:"

	uExists, err := UserExists(username)
	if err != nil {
		log.Println(errStr, "cannot determine if user exists:", err)
		return false, err
	}

	if !uExists {
		return false, e.ErrUnauthorized
	}

	cExists, err := UserExists(contact)
	if err != nil {
		log.Println(errStr, "cannot determine if contact exists:", err)
		return false, err
	}

	if !cExists {
		return false, e.ErrInvalidUsername
	}

	return true, nil
}

func ContactExists(owner string, contact string) (bool, error) {
	errStr := "cannot determine if '%s' has '%s' in contacts list:"

	var userContact UserContact
	err := client.Get(c, GetContactKey(owner, contact), &userContact)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			return false, nil
		}
		log.Println(e.Tag, fmt.Sprintf(errStr, owner, contact), err)
		return false, err
	}
	return true, nil
}

func AddContact(username string, contact string) (UserContact, error) {
	errStr := "cannot add contact:"

	var userContact UserContact

	authorized, err := CanModifyContact(username, contact)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return userContact, err
	}

	if !authorized {
		return userContact, e.ErrUnauthorized
	}

	exists, err := ContactExists(username, contact)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot determine if contact exists:", err)
		return userContact, err
	}

	if exists {
		return userContact, nil
	}

	// add contact
	contactKey := GetContactKey(username, contact)
	userContact = UserContact{
		Added: time.Now(),
		Statistics: msg.FriendshipStatistics{
			SentMessages:     0,
			ReceivedMessages: 0,
			//Games: make(map[string]msg.ContactGame),
			FriendshipLevel: 0,
		},
		Contact: contact,
		Owner:   username,
	}
	_, err = client.Put(c, contactKey, &userContact)
	return userContact, err
}

func GetFriendshipStatistics(username1 string, username2 string) (msg.FriendshipStatistics, error) {
	errStr := fmt.Sprintf("cannot get friendship statistics for user pair (%s, %s):", username1, username2)
	var stats msg.FriendshipStatistics

	// verify contact pair (both users must have the other as a contact)
	var contact UserContact
	err := client.Get(c, datastore.NameKey(KindUserContact, username2, GetUserKey(username1)), &contact)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			log.Println(errStr, fmt.Sprintf("%s is not a contact of %s", username2, username1))
			return stats, err
		}
		log.Println(e.Tag, err)
		return stats, err
	}
	err = client.Get(c, datastore.NameKey(KindUserContact, username1, GetUserKey(username2)), &contact)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			log.Println(errStr, fmt.Sprintf("%s is not a contact of %s", username1, username2))
			return stats, err
		}
		log.Println(e.Tag, err)
		return stats, err
	}

	// calculate number of direct messages sent and received
	userConversationQuery := datastore.NewQuery(KindConversationMember).Filter("Member =", username1).KeysOnly()
	conversationMemberKeys, err := client.GetAll(c, userConversationQuery, nil)
	if err != nil {
		log.Println(e.Tag, "cannot get conversation member keys for " + username1 + ":", err)
		return stats, err
	}

	for _, memberKey := range conversationMemberKeys {
		conversationKey := memberKey.Parent

		members, err := GetConversationMembers(conversationKey)
		if err != nil {
			log.Println(e.Tag, err)
			continue
		}

		if !contains(*members, username2) {
			continue
		}

		log.Println("calculating statistics for conversation with ID:", conversationKey)

		// get keys for conversation messages from username1 (sent)
		sentMessagesQuery := datastore.NewQuery(KindConversationMessage).Ancestor(conversationKey).Filter("From =", GetUserKey(username1)).KeysOnly()
		sentKeys, err := client.GetAll(c, sentMessagesQuery, nil)
		if err != nil {
			log.Println(e.Tag, "cannot get sent messages from conversation:", err)
		} else {
			stats.SentMessages += len(sentKeys)
		}

		// get keys for conversation messages from username2 (received)
		receivedMessagesQuery := datastore.NewQuery(KindConversationMessage).Ancestor(conversationKey).Filter("From =", GetUserKey(username2)).KeysOnly()
		receivedKeys, err := client.GetAll(c, receivedMessagesQuery, nil)
		if err != nil {
			log.Println(e.Tag, "cannot get received messages from conversation:", err)
		} else {
			stats.ReceivedMessages += len(receivedKeys)
		}
	}

	// calculate friendship level using formula: ln(sent + received)
	stats.FriendshipLevel = int(math.Max(math.Log(float64(stats.SentMessages) + float64(stats.ReceivedMessages)), 0))

	log.Printf("calculated friendship statistics for contact pair (%s, %s): %+v", username1, username2, stats)

	return stats, nil
}

func GetContact(username string, contactUsername string) (msg.Contact, error) {
	var contact msg.Contact
	errStr := "cannot get contact from datastore:"

	// get contact from datastore
	var userContact UserContact
	err := client.Get(c, GetContactKey(username, contactUsername), &userContact)
	if err != nil {
		if err != datastore.ErrNoSuchEntity {
			log.Println(e.Tag, errStr, err)
		}
		return contact, err
	}

	// get contact profile from datastore
	userProfile, err := GetUserProfile(contactUsername)
	if err != nil {
		return contact, err
	}

	contact = msg.Contact{
		Online:     ConnectionsContains(contactUsername),
		Added:      userContact.Added,
		Profile:    userProfile,
		Statistics: userContact.Statistics,
	}

	return contact, nil
}

func RemoveContact(username string, contact string) error {
	errStr := "cannot remove contact:"

	authorized, err := CanModifyContact(username, contact)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return err
	}

	if !authorized {
		return e.ErrUnauthorized
	}

	exists, err := ContactExists(username, contact)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot determine if contact exists:", err)
		return err
	}

	if !exists {
		return nil
	}

	// remove contact
	contactKey := GetContactKey(username, contact)
	return client.Delete(c, contactKey)
}

func GetContacts(user *User) ([]UserContact, error) {
	var contacts []UserContact

	q := datastore.NewQuery(KindUserContact).Ancestor(GetUserKey(user.Username))
	var dsUserContacts []UserContact
	dsUserContactKeys, err := client.GetAll(c, q, &dsUserContacts)
	if err != nil {
		return contacts, err
	}

	for i, dsContact := range dsUserContacts {
		stats, err := GetFriendshipStatistics(user.Username, dsUserContactKeys[i].Name)
		if err != nil {
			log.Printf("cannot get friendship statistics for %s: %s\n", dsContact.Contact, err)
		}

		contacts = append(contacts, UserContact{
			Added:      dsContact.Added,
			Statistics: stats,
			Contact:    dsUserContactKeys[i].Name,
			Owner:      user.Username,
		})
	}

	return contacts, nil
}

func UpdateProfile(username string, profile msg.Profile) error {
	errStr := fmt.Sprintf("cannot update profile for '%s'", username)

	user, err := GetUserAccount(username)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get user from datastore:", err)
		return err
	}

	user.Profile = profile

	_, err = client.Put(c, GetUserKey(username), user)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot update profile in datastore:", err)
		return err
	}

	return nil
}

func CreateConversation(username string, members []string) (*Conversation, error) {
	errStr := "cannot create conversation:"

	exists, err := UserExists(username)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot determine if user exists:", err)
		return nil, err
	}

	if !exists {
		return nil, e.ErrUnauthorized
	}

	conversation := new(Conversation)
	conversation.Created = time.Now()

	blankConversationKey := datastore.IncompleteKey(KindConversation, nil)
	conversationKey, err := client.Put(c, blankConversationKey, conversation)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot create conversation in datastore:", err)
		return nil, err
	}

	conversation.Key = new(datastore.Key)
	*conversation.Key = *conversationKey

	// add members to conversation
	for _, member := range members {
		exists, err := UserExists(member)
		if err != nil {
			log.Println(e.Tag, errStr, "cannot determine if user exists:", err)
			return nil, err
		}
		if exists {
			memberKey := GetMemberKey(member, conversationKey)
			conversationMember := ConversationMember{
				Status: msg.Status{Read: false, Typing: false},
				Member: member,
			}

			_, err = client.Put(c, memberKey, &conversationMember)
			if err != nil {
				log.Println(e.Tag, errStr, "cannot add member to conversation:", err)
				return nil, err
			}
		}
	}

	// add the user who sent the message to conversation
	memberKey := GetMemberKey(username, conversationKey)
	conversationMember := ConversationMember{
		Status: msg.Status{Read: true, Typing: false},
		Member: username,
	}
	_, err = client.Put(c, memberKey, &conversationMember)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot add user to conversation:", err)
		return nil, err
	}

	return conversation, nil
}

func GetConversation(conversationKey *datastore.Key) (*Conversation, error) {
	conversation := new(Conversation)

	err := client.Get(c, conversationKey, conversation)
	if err != nil {
		return nil, err
	}

	conversation.Key = conversationKey
	return conversation, nil
}

func DeleteConversation(conversationKey *datastore.Key) error {
	errStr := "cannot delete conversation:"

	// delete members from conversation
	q := datastore.NewQuery(KindConversationMember).Ancestor(conversationKey).KeysOnly()
	memberKeys, err := client.GetAll(c, q, nil)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get member keys from datastore:", err)
		return err
	}

	err = client.DeleteMulti(c, memberKeys)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot delete members from datastore:", err)
		return err
	}

	// delete all messages from conversation
	q = datastore.NewQuery(KindConversationMessage).Ancestor(conversationKey).KeysOnly()
	msgKeys, err := client.GetAll(c, q, nil)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get message keys from datastore:", err)
		return err
	}

	err = client.DeleteMulti(c, msgKeys)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot remove messages from datastore:", err)
		return err
	}

	// delete conversation
	err = client.Delete(c, conversationKey)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot remove conversation from datastore:", err)
		return err
	}

	return nil
}

func IsUserInConversation(username string, conversationKey *datastore.Key) (bool, error) {
	errStr := fmt.Sprintf("cannot determine if '%s' is in conversation:", username)

	memberKey := GetMemberKey(username, conversationKey)
	var member ConversationMember
	err := client.Get(c, memberKey, &member)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			return false, nil
		}
		log.Println(e.Tag, errStr, err)
		return false, err
	}
	return true, nil
}

func GetConversationMembers(conversationKey *datastore.Key) (*[]string, error) {
	errStr := "cannot get conversation members:"

	q := datastore.NewQuery(KindConversationMember).Ancestor(conversationKey).KeysOnly()
	memberKeys, err := client.GetAll(c, q, nil)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return nil, err
	}

	members := new([]string)

	for _, memberKey := range memberKeys {
		*members = append(*members, memberKey.Name)
	}

	return members, nil
}

func GetConversationMemberStatuses(conversationKey *datastore.Key) (map[string]msg.Status, error) {
	errStr := "cannot get conversation members' statuses:"

	q := datastore.NewQuery(KindConversationMember).Ancestor(conversationKey)
	it := client.Run(c, q)

	memberStatuses := make(map[string]msg.Status)
	var err error

	for {
		var member ConversationMember
		memberKey, memberErr := it.Next(&member)
		if memberErr != nil {
			err = memberErr
			break
		}

		memberStatuses[memberKey.Name] = msg.Status{Read: member.Read, Typing: member.Typing}
	}

	if err != iterator.Done {
		log.Println(e.Tag, errStr, err)
		return nil, err
	}

	return memberStatuses, nil
}

func GetConversations(user *User) (*map[string]msg.Conversation, error) {
	errStr := "cannot get conversations:"

	conversations := new(map[string]msg.Conversation)
	*conversations = make(map[string]msg.Conversation)

	q := datastore.NewQuery(KindConversationMember).Filter("Member =", user.Username)
	it := client.Run(c, q)

	var err error

	for {
		var conversationMember ConversationMember
		memberKey, convMemberErr := it.Next(&conversationMember)
		if convMemberErr != nil {
			err = convMemberErr
			break
		}

		conv, convErr := GetConversation(memberKey.Parent)
		if convErr != nil {
			err = convErr
			break
		}

		var conversation msg.Conversation
		conversation.ConversationKey = fmt.Sprintf("%d", conv.Key.ID)
		conversation.LastMessage = conv.LastMessage
		conversation.Created = conv.Created
		conversation.MemberStatus = make(map[string]msg.Status)
		conversation.Messages = make(map[string]msg.Message)

		// get members of conversation
		memberQuery := datastore.NewQuery(KindConversationMember).Ancestor(conv.Key)
		memberIt := client.Run(c, memberQuery)

		for {
			var member ConversationMember
			memberKey, memberErr := memberIt.Next(&member)
			if memberErr != nil {
				err = memberErr
				break
			}
			conversation.MemberStatus[memberKey.Name] = msg.Status{Read: member.Read, Typing: member.Typing}
		}

		if err != iterator.Done {
			log.Println(e.Tag, errStr, "cannot get conversation member from datastore:", err)
			return nil, err
		}

		// get messages
		messageQuery := datastore.NewQuery(KindConversationMessage).Ancestor(conv.Key)
		messageIt := client.Run(c, messageQuery)

		for {
			var dsMessage Message
			msgKey, messageErr := messageIt.Next(&dsMessage)
			if messageErr != nil {
				err = messageErr
				break
			}

			var message msg.Message
			msgKeyString := new(string)
			*msgKeyString = fmt.Sprintf("%d", msgKey.ID)

			message.MessageKey = msgKeyString
			message.ServerTime = &dsMessage.Time
			message.From = &dsMessage.From.Name
			message.Text = &dsMessage.Text
			message.Reactions = new(map[string]msg.Reactions)
			*message.Reactions = make(map[string]msg.Reactions)

			// get reactions
			reactionsQuery := datastore.NewQuery(KindConversationMessageReaction).Ancestor(msgKey)
			reactionsIt := client.Run(c, reactionsQuery)

			for {
				var dsReaction MessageReaction
				reactionKey, reactionsErr := reactionsIt.Next(&dsReaction)
				if reactionsErr != nil {
					err = reactionsErr
					break
				}

				(*message.Reactions)[fmt.Sprintf("%d", reactionKey.ID)] = dsReaction.Reactions
			}

			if err != iterator.Done {
				log.Println(e.Tag, errStr, "cannot get message reactions from datastore:", err)
				return nil, err
			}

			conversation.Messages[*msgKeyString] = message
		}

		if err != iterator.Done {
			log.Println(e.Tag, errStr, "cannot get message from datastore:", err)
			return nil, err
		}

		(*conversations)[fmt.Sprintf("%d", conv.Key.ID)] = conversation
	}

	if err != iterator.Done {
		log.Println(e.Tag, errStr, "cannot get conversation from datastore:", err)
		return nil, err
	}

	return conversations, nil
}

func AddUserToConversation(username string, addUsername string, conversationKey string) error {
	errStr := "cannot add user to conversation:"

	conv := new(Conversation)
	convKey := GetConversationKey(conversationKey)
	err := client.Get(c, convKey, conv)
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			err := e.ErrInvalidConversationKey
			log.Println(errStr, err)
			return err
		}

		log.Println(e.Tag, errStr, "cannot get conversation from datastore:", err)
		return err
	}

	// ensure user is in conversation
	authorized, err := IsUserInConversation(username, convKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return err
	}

	if !authorized {
		return e.ErrUnauthorized
	}

	// add user to conversation
	status := msg.Status{Read: false, Typing: false}
	newMemberKey := GetMemberKey(addUsername, GetConversationKey(conversationKey))
	_, err = client.Put(c, newMemberKey, status)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot add new member to datastore:", err)
		return err
	}

	return nil
}

func GetNumberOfMembers(conversationKey *datastore.Key) (int, error) {
	errStr := "cannot get number of members in conversation:"

	q := datastore.NewQuery(KindConversationMember).Ancestor(conversationKey).KeysOnly()
	memberKeys, err := client.GetAll(c, q, nil)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return 0, err
	}

	return len(memberKeys), nil
}

func RemoveUserFromConversation(username string, removeUser string, conversationKey *datastore.Key) error {
	errStr := "cannot remove user from conversation:"

	authorized, err := IsUserInConversation(username, conversationKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return err
	}

	if !authorized {
		return e.ErrUnauthorized
	}

	err = client.Delete(c, GetMemberKey(removeUser, conversationKey))
	if err != nil {
		if err == datastore.ErrNoSuchEntity {
			return e.ErrInvalidUsername
		}
		log.Println(e.Tag, errStr, err)
		return err
	}

	// delete conversation if there aren't any users in it
	members, err := GetNumberOfMembers(conversationKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return err
	}

	if members == 0 {
		DeleteConversation(conversationKey)
	}

	return nil
}

func SetTypingStatus(username string, conversationKey *datastore.Key, typing bool) error {
	errStr := fmt.Sprintf("'%s' cannot set typing status to %v:", username, typing)

	authorized, err := IsUserInConversation(username, conversationKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return err
	}

	if !authorized {
		return e.ErrUnauthorized
	}

	memberKey := GetMemberKey(username, conversationKey)
	var member ConversationMember
	err = client.Get(c, memberKey, &member)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get member from datastore:", err)
		return err
	}

	member.Typing = typing
	_, err = client.Put(c, memberKey, &member)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot update member in datastore:", err)
		return err
	}

	return nil
}

func ReadConversation(username string, conversationKey *datastore.Key) error {
	errStr := fmt.Sprintf("'%s' cannot read conversation:", username)

	authorized, err := IsUserInConversation(username, conversationKey)
	if err != nil {
		log.Println(e.Tag, errStr, err)
		return err
	}

	if !authorized {
		return e.ErrUnauthorized
	}

	memberKey := GetMemberKey(username, conversationKey)
	var member ConversationMember
	err = client.Get(c, memberKey, &member)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot get member from datastore:", err)
		return err
	}

	member.Read = true
	_, err = client.Put(c, memberKey, &member)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot update member in datastore:", err)
		return err
	}

	return nil
}

func AddMessage(message msg.Message) (*Message, error) {
	errStr := "cannot send message:"

	conversation := new(Conversation)

	if message.ConversationKey == nil {
		// new conversation
		// create conversation entity and get key
		conv, err := CreateConversation(*message.From, *message.To)
		if err != nil {
			log.Println(e.Tag, errStr, "cannot create conversation in datastore:", err)
			return nil, err
		}

		log.Println("conv.Key: ", conv.Key.ID)

		members, err := GetConversationMembers(conv.Key)
		if err != nil {
			log.Println(e.Tag, errStr, "cannot get conversation members:", err)
			return nil, err
		}

		log.Println(*message.From, "created a new conversation with:", *members)
		conversation = conv

	} else {
		convKey := GetConversationKey(*message.ConversationKey)
		conv, err := GetConversation(convKey)
		if err != nil {
			if err == datastore.ErrNoSuchEntity {
				err := e.ErrInvalidConversationKey
				log.Println(errStr, err)
				return nil, err
			} else {
				log.Println(e.Tag, errStr, "cannot get conversation from datastore:", err)
				return nil, err
			}
		}
		conversation = conv
	}

	// add message to datastore and get key
	dsMessage := new(Message)
	dsMessage.Time = time.Now()
	dsMessage.Text = *message.Text
	dsMessage.From = GetUserKey(*message.From)

	// update time in conversation to reflect most recent message
	conversation.LastMessage = dsMessage.Time
	_, err := client.Put(c, conversation.Key, conversation)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot update conversation in datastore:", err)
		return nil, err
	}

	// update conversation member statuses
	memberStatuses, err := GetConversationMemberStatuses(conversation.Key)
	for member, status := range memberStatuses {
		if member != *message.From {
			client.Put(c, datastore.NameKey(KindConversationMember, member, conversation.Key), &ConversationMember{Member: member, Status: msg.Status{Read: false, Typing: status.Typing}})
		}
	}
	client.Put(c, datastore.NameKey(KindConversationMember, *message.From, conversation.Key), &ConversationMember{Member: *message.From, Status: msg.Status{Read: true, Typing: false}})

	// put new message in datastore
	messageKey := datastore.IncompleteKey(KindConversationMessage, conversation.Key)
	messageKey, err = client.Put(c, messageKey, dsMessage)
	if err != nil {
		log.Println(e.Tag, errStr, "cannot add message to datastore:", err)
		return nil, err
	}

	dsMessage.Key = messageKey

	return dsMessage, nil
}

func UpdateMessage(message *Message) error {
	_, err := client.Put(c, message.Key, message)
	return err
}

func GetMessage(messageKey *datastore.Key) (*Message, error) {
	message := new(Message)

	err := client.Get(c, messageKey, message)
	if err != nil {
		return nil, err
	}

	message.Key = messageKey

	return message, nil
}

func GetMessages(conversationKey *datastore.Key) (map[string]msg.Message, error) {
	errStr := "cannot get messages:"

	q := datastore.NewQuery(KindConversationMessage).Ancestor(conversationKey).Order("ServerTime")
	it := client.Run(c, q)

	messages := make(map[string]msg.Message)
	var err error

	for {
		var dsMessage Message
		messageKey, err := it.Next(dsMessage)
		if err != nil {
			break
		}

		reactions, err := getMessageReactions(messageKey)
		if err != nil {
			break
		}

		msgKeyString := new(string)
		*msgKeyString = fmt.Sprintf("%s", messageKey.ID)
		message := msg.Message{ServerTime: &dsMessage.Time, From: &dsMessage.From.Name, MessageKey: msgKeyString, Text: &dsMessage.Text, Reactions: reactions}
		messages[messageKey.Name] = message
	}

	if err != iterator.Done {
		log.Println(e.Tag, errStr, err)
		return nil, err
	}

	return messages, nil
}

func getMessageReactions(messageKey *datastore.Key) (*map[string]msg.Reactions, error) {
	errStr := "cannot get message reactions:"

	q := datastore.NewQuery(KindConversationMessageReaction).Ancestor(messageKey.Parent)
	it := client.Run(c, q)

	reactions := make(map[string]msg.Reactions)
	var err error

	for {
		var reaction msg.Reactions
		reactionKey, err := it.Next(reaction)
		if err != nil {
			break
		}

		reactions[reactionKey.Name] = reaction
	}

	if err != iterator.Done {
		log.Println(e.Tag, errStr, err)
		return nil, err
	}

	return &reactions, nil
}

const ProjectID = "cen3031-192414"

var c context.Context
var client *datastore.Client

func init() {
	c = context.Background()
}

func Connect() error {
	cl, err := datastore.NewClient(c, ProjectID)
	client = cl
	return err
}

// TODO: don't process message reactions for ActionSendMessage (i.e. only allow reactions to be added after it's sent)
// TODO: move all authentication checks to functions in this file (i.e. message handlers should just check that the parameters exist, then call the appropriate function from this file)
// TODO: write functions for client operations so the errors just have to be handled once
// TODO: standardize error logging locations and returned errors (return ServerError instead of generic error for ease of handling in message handlers)
