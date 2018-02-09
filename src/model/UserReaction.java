package model;

public class UserReaction {
    /*Reactions []int  `json:"type"`
    User      string `json:"user"`*/

    private int[] reaction;
    private String user;

    // Test Constructor
    public UserReaction(int[] reaction, String user) {
        this.reaction = reaction;
        this.user = user;
    }
}
