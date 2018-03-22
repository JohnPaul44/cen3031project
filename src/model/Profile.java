package model;

public class Profile {
    /*Name  string  `json:"name"`
    Email string  `json:"email"`
    Phone *string `json:"phone,omitempty"`*/

    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    public enum Gender { FEMALE, MALE, OTHER, NA }
    private String gender; // 0 = female, 1 = male, 2 = other, 3 = NA
    private String birthday;
    private String color;

    // Test Constructor
    public Profile(String firstName, String lastName, String email, String phone, Gender gender, String birthday,
                   String color) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.gender = gender.toString().toLowerCase();
        if (!birthday.equals("")) {
            this.birthday = birthday;
        }
        this.color = color;
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
    public String getGender() {
        return gender;
    }
    public String getBirthday() {
        return birthday;
    }
    public String getColor() {return color;}
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
    public void setColor(String color){ this.color = color;}

}
