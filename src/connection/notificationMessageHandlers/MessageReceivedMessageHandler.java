package connection.notificationMessageHandlers;

import application.ViewController;
import connection.ErrorInformation;
import connection.serverMessages.ServerMessage;
import connection.serverMessages.notificationMessages.NotificationMessageReceivedMessage;
import model.Conversation;
import model.Message;
import model.UserUpdater;

import java.util.Iterator;
import java.util.Map;

public class MessageReceivedMessageHandler extends ModelUpdateMessageHandler implements MessageHandler {
    private NotificationMessageReceivedMessage serverMessage;

    public MessageReceivedMessageHandler(ServerMessage messageFromServer, UserUpdater userUpdater) {
        super(userUpdater);
        this.serverMessage = (NotificationMessageReceivedMessage) messageFromServer;
    }

    @Override
    public void handle(ViewController delegate) {
        ErrorInformation errorInformation = new ErrorInformation();
        if (serverMessage.error()) {
            errorInformation.setErrorInformation(serverMessage);
        } else {
            updateUser(serverMessage);
        }

        if (serverMessage.getConversation() == null) {
            Message m = serverMessage.getMessage();
            delegate.messageReceivedNotification(errorInformation, m.getConversationKey(), m.getMessageKey(),
                    m.getClientTime(), m.getFrom(), m.getText());
        } else {
            Map<String, Conversation>  conversationMap = serverMessage.getConversation();
            Conversation conversation = new Conversation();
            Message m = new Message();
            Iterator it1 = conversationMap.entrySet().iterator();
            while (it1.hasNext()) {
                Map.Entry pair = (Map.Entry)it1.next();
                conversation = (Conversation) pair.getValue();
            }
            Iterator it2 = conversation.getMessages().entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry pair = (Map.Entry)it2.next();
                m = (Message) pair.getValue();
            }

            delegate.messageReceivedNotification(errorInformation, m.getConversationKey(), m.getMessageKey(),
                    m.getServerTime(), m.getFrom(), m.getText());
        }
    }
}
