package connection.serverMessages;

public class ActionRegisterMessage extends ActionServerMessage{
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String gender;
    private String DOB;
    private String securityQuestion;
    private String securityAnswer;

    public ActionRegisterMessage(String username, String password, String firstName, String lastName, String email,
                                 String phone, String gender, String DOB, String securityQuestion, String securityAnswer) {
        this.status = Status.ACTIONREGISTER;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        if (DOB == null) {
            this.DOB = DOB;
        }
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }
}
