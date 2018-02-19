package connection;

import java.io.*;
import java.net.*;

import connection.notificationMessageHandlers.*;
import model.CurrentUser;

public class ServerConnection {

    private PrintWriter out;
    private BufferedReader in;
    private CurrentUser currentUser;
    private MessageSender messageSender;

    public ServerConnection() {
        this.currentUser = new CurrentUser();
        try {
            Socket socket = new Socket(Server.hostname, Server.portNumber);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.messageSender = new MessageSender(out);
        } catch(IOException e) {
            System.out.println("Error while creating connection to server");
        }
    }

    public PrintWriter getOut() {
        return out;
    }
    public CurrentUser getCurrentUser() { return currentUser; }
    public MessageSender getMessageSender() { return messageSender; }

    public void startListeningToServer() {
        Thread thread = new Thread(() -> {
            String userInput;
            HandlerFactory handlerFactory = new HandlerFactory();
            try {
                while ((userInput = in.readLine()) != null) {
                    MessageHandler handler = handlerFactory.produce(userInput, currentUser);
                    handler.handle();
                }
            } catch (IOException e) {
                System.out.println("Error while receiving a message from server: " + e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }
}