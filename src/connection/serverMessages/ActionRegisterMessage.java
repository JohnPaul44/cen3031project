package connection.serverMessages;

import model.Profile;

public class ActionRegisterMessage extends ActionServerMessage{
    private String username;
    private String password;
    private Profile profile;

    public ActionRegisterMessage(String username, String password, String firstName, String lastName, String email,
                                 String phone, Profile.Gender gender, String DOB, String securityQuestion, String securityAnswer) {
        this.status = Status.ACTIONREGISTER.ordinal();
        this.username = username;
        this.password = password;
        profile = new Profile(firstName, lastName, email, phone, securityQuestion, securityAnswer, gender, DOB);
    }
}
