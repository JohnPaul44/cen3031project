package connection.serverMessages.actionMessages;

public class ActionDeleteAccount extends ActionServerMessage {
    public ActionDeleteAccount() {
        this.status = Status.ACTIONDELETEACCOUNT.ordinal();
    }
}
