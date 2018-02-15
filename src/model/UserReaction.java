package model;

public class UserReaction {
    /*Reactions []int  `json:"type"`
    User      string `json:"user"`*/

    private int[] type;
    private String user;

    // Test Constructor
    public UserReaction(int[] type, String user) {
        this.type = type;
        this.user = user;
    }
}
