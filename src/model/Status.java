package model;

public class Status {
    /*Read   bool `json:"read"`
    Typing bool `json:"typing"`*/

    private boolean read;
    private boolean typing;

    // Test Constructor
    public Status(boolean read, boolean typing) {
        this.read = read;
        this.typing = typing;
    }

    public boolean getRead() {
        return read;
    }
    public boolean getTyping() {
        return typing;
    }
}
