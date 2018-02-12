package connection;

import java.io.*;
import java.net.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import connection.messageHandlers.*;
import connection.serverMessaging.*;
import model.CurrentUser;

public class ServerConnection {

    private PrintWriter out;
    private BufferedReader in;
    private CurrentUser currentUser;

    public ServerConnection() {
        this.currentUser = new CurrentUser();
        try {
            Socket socket = new Socket(Server.hostname, Server.portNumber);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch(IOException e) {
            System.out.println("Error while creating connection to server");
        }
    }

    public PrintWriter getOut() {
        return out;
    }

    public CurrentUser getCurrentUser() { return currentUser; }

    public void startListeningToServer() {
        Thread thread = new Thread(() -> {
            String userInput;
            try {
                while ((userInput = in.readLine()) != null) {
                    Gson gson = new Gson();
                    JsonParser parser = new JsonParser();
                    JsonObject messageFromServer = parser.parse(userInput).getAsJsonObject();
                    int status = messageFromServer.get("status").getAsInt();

                    switch (status) {
                        case 0: // Uninitialized
                            UninitializedMessageHandler uninitializedHandler = new UninitializedMessageHandler();
                            uninitializedHandler.handle();
                            break;
                        case 1: // Error Message
                            ErrorMessageHandler errorHandler = new ErrorMessageHandler(messageFromServer);
                            errorHandler.handle();
                            break;
                        case 2: // Logged In Message
                            LoggedInMessageHandler loggedInMessageHandler = new LoggedInMessageHandler(messageFromServer, currentUser);
                            loggedInMessageHandler.handle();
                            break;
                        case 3: // User Online Status
                            UserOnlineStatusMessageHandler userOnlineStatusMessageHandler = new UserOnlineStatusMessageHandler(messageFromServer, currentUser);
                            userOnlineStatusMessageHandler.handle();
                            break;
                        case 4: // Logged Out Message
                            LoggedOutMessageHandler loggedOutMessageHandler = new LoggedOutMessageHandler(messageFromServer, currentUser);
                            loggedOutMessageHandler.handle();
                            break;
                        case 5: // Contact Added
                            ContactAddedMessageHandler contactAddedMessageHandler = new ContactAddedMessageHandler(messageFromServer, currentUser);
                            contactAddedMessageHandler.handle();
                            break;
                        case 6: // Contact Removed
                            ContactRemovedMessageHandler contactRemovedMessageHandler = new ContactRemovedMessageHandler(messageFromServer, currentUser);
                            contactRemovedMessageHandler.handle();
                            break;
                        case 7: // Profile Updated
                            ProfileUpdatedMessageHandler profileUpdatedMessageHandler = new ProfileUpdatedMessageHandler(messageFromServer, currentUser);
                            profileUpdatedMessageHandler.handle();
                            break;
                        case 8: // Message Received
                            MessageReceivedMessageHandler messageReceivedMessageHandler = new MessageReceivedMessageHandler(messageFromServer, currentUser);
                            messageReceivedMessageHandler.handle();
                            break;
                        case 9: // Message Updated
                            NotificationMessageUpdatedMessage messageUpdatedMessage = gson.fromJson(messageFromServer, NotificationMessageUpdatedMessage.class);
                            break;
                        case 10: // User Added to Conversation
                            NotificationUserAddedToConversationMessage userAddedToConversationMessage = gson.fromJson(messageFromServer, NotificationUserAddedToConversationMessage.class);
                            break;
                        case 11: // User Removed from Conversation
                            NotificationUserRemovedFromConversationMessage userRemovedFromConversationMessage = gson.fromJson(messageFromServer, NotificationUserRemovedFromConversationMessage.class);
                            break;
                        case 12: // Message Read
                            NotificationMessageReadMessage messageReadMessage = gson.fromJson(messageFromServer, NotificationMessageReadMessage.class);
                            break;
                        case 13: // Typing
                            NotificationTypingMessage typingMessage = gson.fromJson(messageFromServer, NotificationTypingMessage.class);
                            break;
                        default:
                            System.out.println("ERROR invalid message received. Status of message received: " + status);
                    }

                    System.out.println("\nClient Received: " + userInput);
                }
            } catch (IOException e) {
                System.out.println("Error while receiving a message from server: " + e);
            }
        });

        thread.start();
    }
}