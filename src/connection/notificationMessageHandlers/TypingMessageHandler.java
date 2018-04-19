package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationTypingMessage;
import model.UserUpdater;

public class TypingMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationTypingMessage serverMessage;

    public TypingMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationTypingMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        } else {
            updateUser(serverMessage);
        }
        delegate.typingNotification(errorInformation, serverMessage.getMessage().getConversationKey(),
                serverMessage.getMessage().getFrom(), serverMessage.getMessage().getTyping());
    }
}
