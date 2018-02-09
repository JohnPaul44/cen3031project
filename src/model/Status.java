package model;

public class Status {
    /*Read   bool `json:"read"`
    Typing bool `json:"typing"`*/

    private boolean read;
    private boolean typing;

    public Status(boolean read, boolean typing) {
        this.read = read;
        this.typing = typing;
    }
}
