package connection;

import java.io.*;
import java.net.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import connection.serverMessaging.NotificationErrorMessage;
import connection.serverMessaging.NotificationLoggedInMessage;
import model.Profile;

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