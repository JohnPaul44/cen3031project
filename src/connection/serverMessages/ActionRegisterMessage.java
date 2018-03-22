package connection.serverMessages;

import model.Profile;

public class ActionRegisterMessage extends ActionServerMessage{
    private String username;
    private String password;
    private Profile profile;
    private String securityQuestion;
    private String securityAnswer;

    public ActionRegisterMessage(String username, String password, String firstName, String lastName, String email,
                                 String phone, Profile.Gender gender, String DOB, String securityQuestion, String securityAnswer, String color) {
        this.status = Status.ACTIONREGISTER.ordinal();
        this.username = username;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        profile = new Profile(firstName, lastName, email, phone, gender, DOB, color);
    }
}
