package model;

public class Contact {
    /*Username string `json:"username"`
    Online   bool   `json:"online"`*/

    private String username;
    private boolean online;

    // Test Constructor
    public Contact(String username, boolean online) {
        this.username = username;
        this.online = online;
    }

    public String getUsername() {
        return username;
    }
    public boolean getOnline() {
        return online;
    }

    public void setOnline(boolean online) { this.online = online; }
}
