package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationSecurityQuestion;

public class SecurityQuestionMessageHandler implements MessageHandler {
    private NotificationSecurityQuestion serverMessage;

    public SecurityQuestionMessageHandler(ServerMessage messageFromServer) {
        this.serverMessage = (NotificationSecurityQuestion) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        }
        delegate.securityQuestionNotification(errorInformation, serverMessage.getSecurityQuestion());
    }
}
