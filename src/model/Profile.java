package model;

public class Profile {
    /*Name  string  `json:"name"`
    Email string  `json:"email"`
    Phone *string `json:"phone,omitempty"`*/

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String securityQuestion;
    private String securityAnswer;
    public enum Gender { FEMALE, MALE, OTHER, NA }
    private String gender; // 0 = female, 1 = male, 2 = other, 3 = NA
    private String birthday;

    // Test Constructor
    public Profile(String firstName, String lastName, String email, String phone, String securityQuestion,
                   String securityAnswer, Gender gender, String birthday) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
        this.gender = gender.toString().toLowerCase();
        if (!birthday.equals("")) {
            this.birthday = birthday;
        }
    }

    public String getName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
    public String getSecurityQuestion() {
        return securityQuestion;
    }
    public String getSecurityAnswer() {
        return securityAnswer;
    }
    public String getGender() {
        return gender;
    }
    public String getBirthday() {
        return birthday;
    }
    public void setName(String firstName){
        this.firstName = firstName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    public void setGender(String gender){
        this.gender = gender;
    }
    public void setBirthday(String birthday){
        this.birthday = birthday;
    }

}
