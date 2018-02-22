package model;

public class Reactions {
    /*Reactions []int  `json:"type"`
    User      string `json:"user"`*/

    private int[] type;
    private String user;

    // Test Constructor
    public Reactions(int[] type, String user) {
        this.type = type;
        this.user = user;
    }
}
