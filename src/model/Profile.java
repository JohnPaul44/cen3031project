package model;

public class Profile {
    /*Name  string  `json:"name"`
    Email string  `json:"email"`
    Phone *string `json:"phone,omitempty"`*/

    private String name;
    private String email;
    private String phone;
    private int gender; // 0 = female, 1 = male, 2 = other
    private int age;

    // Test Constructor
    public Profile(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPhone() {
        return phone;
    }
}