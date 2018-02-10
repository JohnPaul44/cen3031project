package connection;

import java.io.*;
import java.net.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import connection.serverMessaging.*;

public class ServerConnection {

    private PrintWriter out;
    private BufferedReader in;

    public ServerConnection() {
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
                            break;
                        case 1: // Error Message
                            NotificationErrorMessage errorMessage = gson.fromJson(messageFromServer, NotificationErrorMessage.class);
                            break;
                        case 2: // Logged In Message
                            NotificationLoggedInMessage loggedInMessage = gson.fromJson(messageFromServer, NotificationLoggedInMessage.class);
                            break;
                        case 3: // User Online Status
                            NotificationUserOnlineStatusMessage userOnlineStatusMessage = gson.fromJson(messageFromServer, NotificationUserOnlineStatusMessage.class);
                            break;
                        case 4: // Logged Out Message
                            NotificationLoggedOutMessage loggedOutMessage = gson.fromJson(messageFromServer, NotificationLoggedOutMessage.class);
                            break;
                        case 5: // Contact Added
                            NotificationContactAddedMessage contactAddedMessage = gson.fromJson(messageFromServer, NotificationContactAddedMessage.class);
                            break;
                        case 6: // Contact Removed
                            NotificationContactRemovedMessage contactRemovedMessage = gson.fromJson(messageFromServer, NotificationContactRemovedMessage.class);
                            break;
                        case 7: // Profile Updated
                            NotificationProfileUpdatedMessage profileUpdatedMessage = gson.fromJson(messageFromServer, NotificationProfileUpdatedMessage.class);
                            break;
                        case 8: // Message Received
                            NotificationMessageReceivedMessage messageReceivedMessage = gson.fromJson(messageFromServer, NotificationMessageReceivedMessage.class);
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