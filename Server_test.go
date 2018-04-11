package main

import (
	"testing"
	"log"
	ds "./Datastore"
	msg "./ServerMessage"
	"fmt"
	"cloud.google.com/go/datastore"
	"time"
)

const (
	TestUser             = "test_user"
	TestPass             = "test_password"
	TestFirstName        = "Test"
	TestLastName         = "User"
	TestEmail            = "test@email.com"
	TestPhone            = "1234567890"
	TestSecurityQuestion = "security question"
	TestSecurityAnswer   = "security answer"
	TestGender           = msg.GenderMale
	TestBirthday         = "2018-02-21"

	TestContact          = "test_contact"
	TestContactPass      = "test_contact_pass"
	TestContactFirstName = "Test"
	TestContactLastName  = "Contact"
	TestContactEmail     = "test_contact@email.com"
	TestContactPhone     = "5558675309"
	TestContactGender    = msg.GenderFemale
	TestContactBirthday  = "2018-03-21"
)

func TestMain(m *testing.M) {
	err := ds.Connect()
	if err != nil {
		log.Fatal(err)
	}

	m.Run()
}

/* Datastore Tests */
func TestUsers(t *testing.T) {
	// create test user
	exists, err := ds.UserExists(TestUser)
	if exists {
		err = ds.DeleteUserAccount(TestUser)
		if err != nil {
			t.Fatal(err)
		}
	}

	_, err = ds.CreateUserAccount(TestUser, TestPass, msg.Profile{}, TestSecurityQuestion, TestSecurityAnswer)
	if err != nil {
		t.Fatal(err)
	}

	// check if user exists
	exists, err = ds.UserExists(TestUser)
	if err != nil {
		t.Fatal(err)
	}

	if !exists {
		t.Fatal("new user account does not exist")
	}

	// get user account
	_, err = ds.GetUserAccountAuthenticated(TestUser, TestPass)
	if err != nil {
		t.Fatal(err)
	}

	// delete user
	err = ds.DeleteUserAccount(TestUser)
	if err != nil {
		t.Fatal(err)
	}

	// check if user exists
	exists, err = ds.UserExists(TestUser)
	if err != nil {
		t.Fatal(err)
	}

	if exists {
		t.Fatal("user account still exists")
	}
}

func TestCreateUserAccount(t *testing.T) {
	exists, err := ds.UserExists(TestUser)
	if err != nil {
		t.Fatal(err)
	}

	if exists {
		err = ds.DeleteUserAccount(TestUser)
		if err != nil {
			t.Fatal(err)
		}
	}

	exists, err = ds.UserExists(TestContact)
	if err != nil {
		t.Fatal(err)
	}

	if exists {
		err = ds.DeleteUserAccount(TestContact)
		if err != nil {
			t.Fatal(err)
		}
	}

	_, err = ds.CreateUserAccount(TestUser, TestPass, msg.Profile{FirstName: TestFirstName, LastName: TestLastName, Email: TestEmail,
		Phone: TestPhone, Gender: msg.GenderMale, Birthday: TestBirthday}, TestSecurityQuestion, TestSecurityAnswer)
	if err != nil {
		t.Fatal(err)
	}

	_, err = ds.CreateUserAccount(TestContact, TestContactPass, msg.Profile{FirstName: TestContactFirstName, LastName: TestContactLastName,
		Email: TestContactEmail, Phone: TestContactPhone, Gender: TestContactGender, Birthday: TestContactBirthday}, TestSecurityQuestion, TestSecurityAnswer)
	if err != nil {
		t.Fatal(err)
	}

	usr, err := ds.GetUserAccountAuthenticated(TestUser, TestPass)
	if err != nil {
		t.Fatal(err)
	}

	if usr.Username != TestUser {
		t.Error("usernames do not match")
	}

	if usr.Profile.FirstName != TestFirstName {
		t.Error("first names do not match")
	}

	if usr.Profile.LastName != TestLastName {
		t.Error("last names do not match")
	}

	if usr.Profile.Email != TestEmail {
		t.Error("emails do not match")
	}

	if usr.Profile.Gender != TestGender {
		t.Error("genders do not match")
	}

	if usr.Profile.Phone != TestPhone {
		t.Error("phone numbers do not match")
	}

	if usr.Profile.Birthday != TestBirthday {
		t.Error("birthdays do not match")
	}
}

func TestAddContact(t *testing.T) {
	userContact, err := ds.AddContact(TestUser, TestContact)
	if err != nil {
		t.Fatal(err)
	}

	testContact, err := ds.AddContact(TestContact, TestUser)
	if err != nil {
		t.Fatal(err)
	}

	if userContact.Owner != TestUser || userContact.Contact != TestContact {
		t.Fatal(fmt.Sprintf("userContact.Owner != TestUser || userContact.Contact != TestContact: %+v", userContact))
	}

	if testContact.Owner != TestContact || testContact.Contact != TestUser {
		t.Fatal(fmt.Sprintf("testContact.Owner != TestContact || testContact.Contact != TestUser: %+v", testContact))
	}
}

func TestGetContacts(t *testing.T) {
	testUser, err := ds.GetUserAccount(TestUser)
	if err != nil {
		t.Fatal(err)
	}

	testContact, err := ds.GetUserAccount(TestContact)
	if err != nil {
		t.Fatal(err)
	}

	userContacts, err := ds.GetContacts(testUser)
	if err != nil {
		t.Fatal(err)
	}

	contactContacts, err := ds.GetContacts(testContact)
	if err != nil {
		t.Fatal(err)
	}

	if len(userContacts) != 1 || userContacts[0].Owner != TestUser || userContacts[0].Contact != TestContact {
		t.Fatal(fmt.Sprintf("%s's contacts do not match: %s", TestUser, userContacts))
	}

	if len(contactContacts) != 1 || contactContacts[0].Owner != TestContact || contactContacts[0].Contact != TestUser {
		t.Fatal(fmt.Sprintf("%s's contacts do not match: %s", TestContact, userContacts))
	}
}

func TestConversations(t *testing.T) {
	newConversation, err := ds.CreateConversation(TestUser, []string{TestContact})
	if err != nil {
		t.Fatal(err)
	}

	defer ds.DeleteConversation(newConversation.Key)

	testUser, err := ds.GetUserAccount(TestUser)
	if err != nil {
		t.Fatal(err)
	}

	contactUser, err := ds.GetUserAccount(TestContact)
	if err != nil {
		t.Fatal(err)
	}

	testUserConversations, err := ds.GetConversations(testUser)
	if err != nil {
		t.Fatal(err)
	}

	testContactUserConversations, err := ds.GetConversations(contactUser)
	if err != nil {
		t.Fatal(err)
	}

	conversationKeyString := fmt.Sprintf("%d", newConversation.Key.ID)

	if len(*testUserConversations) == 0 || (*testUserConversations)[conversationKeyString].ConversationKey != fmt.Sprintf("%d", newConversation.Key.ID) {
		t.Fatal("testUserConversations does not contain conversation:", *testUserConversations)
	}

	if len((*testUserConversations)[fmt.Sprintf("%d", newConversation.Key.ID)].MemberStatus) != 2 {
		t.Fatal("testUserConversations.MemberStatus has incorrect length")
	}

	testUserMember, contains := (*testUserConversations)[conversationKeyString].MemberStatus[TestContact]
	if !contains {
		t.Fatal("testUserConversations.MemberStatus does not contain", TestContact)
	}

	if testUserMember.Read || testUserMember.Typing {
		t.Fatal("testUserMember.[Read | Typing] is set to true (should be false)")
	}

	if len(*testContactUserConversations) == 0 || (*testContactUserConversations)[conversationKeyString].ConversationKey != fmt.Sprintf("%d", newConversation.Key.ID) {
		t.Fatal("testContactUserConversations does not contain conversation")
	}

	if len((*testContactUserConversations)[conversationKeyString].MemberStatus) != 2 {
		t.Fatal("testContactUserConversations.MemberStatus has incorrect length")
	}

	testContactMember, contains := (*testContactUserConversations)[conversationKeyString].MemberStatus[TestUser]
	if !contains {
		t.Fatal("testUserConversations.MemberStatus does not contain", TestUser)
	}

	if !testContactMember.Read || testContactMember.Typing {
		t.Fatal("testContactMember.Read is set to false (should be true) or testUserMember.Typing is set to true (should be false)")
	}

	conversationMembers, err := ds.GetConversationMembers(newConversation.Key)
	if err != nil {
		t.Fatal(err)
	}

	if len(*conversationMembers) != 2 {
		t.Fatal("conversation does not contain proper number of members")
	}

	containsUser, containsContactUser := false, false

	for _, m := range *conversationMembers {
		if m == TestUser {
			containsUser = true
		} else if m == TestContact {
			containsContactUser = true
		}
	}

	if !containsUser || !containsContactUser {
		t.Fatal("conversation does not contain proper members:", conversationMembers)
	}

	testUserStatuses, err := ds.GetConversationMemberStatuses(newConversation.Key)
	if err != nil {
		t.Fatal(err)
	}

	if len(testUserStatuses) != 2 {
		t.Fatal("testUserStatuses has incorrect length")
	}

	testUserStatus, contains := testUserStatuses[TestUser]
	if !contains {
		t.Fatal("testUserStatus does not contain", TestUser)
	}

	testContactStatus, contains := testUserStatuses[TestContact]
	if !contains {
		t.Fatal("testUserStatus does not contain", TestUser)
	}

	if !testUserStatus.Read || testUserStatus.Typing {
		t.Fatal("testUserStatus.Read is set to false (should be true) or testUserMember.Typing is set to true (should be false)")
	}

	if testContactStatus.Read || testContactStatus.Typing {
		t.Fatal("testContactStatus.[Read | Typing] is set to true (should be false)")
	}

	clientTime := new(string)
	*clientTime = time.Now().String()
	text := new(string)
	*text = "test message "
	from := new(string)
	*from = TestUser


	message := msg.Message{
		ClientTime: clientTime,
		ConversationKey: &conversationKeyString,
		Text: text,
		From: from,
	}

	newMessage, err := ds.AddMessage(message)
	if err != nil {
		t.Fatal("could not add message to conversation:", err)
	}

	if newMessage.Key == nil {
		t.Fatal("message key is nil")
	}

	err = ds.DeleteConversation(newConversation.Key)
	if err != nil {
		t.Fatal(err)
	}

	_, err = ds.GetConversation(newConversation.Key)
	if err != datastore.ErrNoSuchEntity {
		if err == nil {
			t.Fatal("conversation was not properly deleted")
		}
		t.Fatal(err)
	}
}

func TestAddUserToConversation(t *testing.T) {

}

func TestRemoveUserFromConversation(t *testing.T) {

}

func TestSetTypingStatus(t *testing.T) {

}

func TestReadConversation(t *testing.T) {

}

func TestSetOnlineStatus(t *testing.T) {

}

func TestRemoveContact(t *testing.T) {
	err := ds.RemoveContact(TestUser, TestContact)
	if err != nil {
		t.Fatal(err)
	}

	err = ds.RemoveContact(TestContact, TestUser)
	if err != nil {
		t.Fatal(err)
	}
}
