package application;

import model.Contact;
import model.CurrentUser;
import model.Profile;
import sun.util.resources.cldr.ta.CurrencyNames_ta;

import java.util.HashMap;

public class DummyUser {
    public String username1 = "user";
    public String password1 = "password";
    public String firstName1 = "Test";
    public String lastName1 = "User";
    public String email1 = "testUser@email.com";
    public String phone1 = "1234567890";
    public Profile.Gender gender1 = Profile.Gender.OTHER;
    public String birthday1 = "";
    public String securityQuestion1 = "What was the name of your first pet?";
    public String securityAnswer1 = "dog";
    public String color = "0xb399ffff";

    public Profile profile;
    public CurrentUser currUser = new CurrentUser();
    DummyUser(){
        profile = new Profile(firstName1, lastName1, email1, phone1, gender1, birthday1, color);
        currUser.setProfile(profile);
        currUser.setUserName(username1);
        setUpContacts(currUser);
    }

    public CurrentUser getCurrentUser(){
        return currUser;
    }

    public Contact contact1;
    public String username2 = "User 2";
    public Contact contact2;
    public String username3 = "User 3";
    public Contact contact3;


    private void setUpContacts(CurrentUser currUser) {
        HashMap<String, Contact> contacts = new HashMap<>();
        contact1 = new Contact(username1, true);
        contact2 = new Contact(username2, false);
        contact3 = new Contact(username3, true);
        contacts.put(username1, contact1);
        contacts.put(username2, contact2);
        contacts.put(username3, contact3);
        currUser.setContactList(contacts);
    }
}
