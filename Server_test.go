package main

import (
	"testing"
	"log"
	ds "./Datastore"
	msg "./ServerMessage"
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
)

func TestMain(m *testing.M) {
	err := ds.Connect()
	if err != nil {
		log.Fatal(err)
	}

	m.Run()
}

/* Datastore Tests */
func TestUserExists(t *testing.T) {
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

	_, err = ds.CreateUserAccount(TestUser, TestPass, msg.Profile{FirstName: TestFirstName, LastName: TestLastName, Email: TestEmail,
		Phone: TestPhone, Gender: msg.GenderMale, Birthday: TestBirthday}, TestSecurityQuestion, TestSecurityAnswer)
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

}

func TestRemoveContact(t *testing.T) {

}

func TestGetContacts(t *testing.T) {

}

func TestCreateConversation(t *testing.T) {

}

func TestDeleteConversation(t *testing.T) {

}

func TestGetConversations(t *testing.T) {

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

/* Server Function Tests */
func TestRegister(t *testing.T) {

}

func TestLogIn(t *testing.T) {

}

func TestUpdateOnlineStatus(t *testing.T) {

}

/* Handler Tests */
func TestHandleLogOut(t *testing.T) {

}

func TestHandleAddContact(t *testing.T) {

}

func TestHandleRemoveContact(t *testing.T) {

}

func TestHandleUpdateProfile(t *testing.T) {

}

func TestHandleSendMessage(t *testing.T) {

}

func TestHandleReadMessage(t *testing.T) {

}

func TestHandleSetTyping(t *testing.T) {

}

func TestHandleUpdateMessage(t *testing.T) {

}

func TestHandleAddUserToConversation(t *testing.T) {

}

func TestHandleRemoveUserFromConversation(t *testing.T) {

}
