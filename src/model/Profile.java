package model;

public class Profile {
    /*Name  string  `json:"name"`
    Email string  `json:"email"`
    Phone *string `json:"phone,omitempty"`*/

    private String name;
    private String email;
    private String phone;

    public Profile(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }}
