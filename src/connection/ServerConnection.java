package connection;

import java.io.*;
import java.net.*;

import connection.notificationMessageHandlers.*;
import connection.serverMessages.ActionLogInMessage;
import connection.serverMessages.ActionRegisterMessage;
import connection.serverMessages.ServerMessage;
import model.CurrentUser;
import model.Profile;
import model.UserUpdater;

public class ServerConnection implements IServerConnection{

    private PrintWriter out;
    private BufferedReader in;
    private CurrentUser currentUser;
    private String hostname = "35.231.80.25";
    private int portNumber = 8675;

    public ServerConnection() {
        this.currentUser = new CurrentUser();
        try {
            Socket socket = new Socket(hostname, portNumber);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("GET /connect HTTP/1.0\r\n\r\n");
            listenToServer();
        } catch(IOException e) {
            System.out.println("Error connecting to server: " + e);
        }
    }

    public ServerConnection(String hostname, int portNumber) {
        this.currentUser = new CurrentUser();
        try {
            Socket socket = new Socket(hostname, portNumber);
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("GET /connect HTTP/1.0\r\n\r\n");
            listenToServer();
        } catch(IOException e) {
            System.out.println("Error connecting to server: " + e);
        }
    }

    public PrintWriter getOut() { return out; }
    public CurrentUser getCurrentUser() { return currentUser; }

    @Override
    public void sendMessageToServer(ServerMessage serverMessage) {
        out.println(serverMessage.toJsonString());
    }

    @Override
    public void listenToServer() {
        Thread thread = new Thread(() -> {
            String messageFromServer;
            UserUpdater userUpdater = new UserUpdater(currentUser);
            HandlerFactory handlerFactory = new HandlerFactory();
            try {
                while ((messageFromServer = in.readLine()) != null) {
                    MessageHandler handler = handlerFactory.produce(messageFromServer, userUpdater);
                    handler.handle();
                }
            } catch (IOException e) {
                System.out.println("Error while receiving a message from server: " + e);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Done listening");
        });

        thread.start();
    }

    public void login(String username, String password) {
        ServerMessage message = new ActionLogInMessage(username, password);
        sendMessageToServer(message);
    }

    public void registerNewUser(String username, String password, String firstName, String lastName, String email,
                                String phone, Profile.Gender gender, String DOB, String securityQuestion, String securityAnswer) {
        ActionRegisterMessage message = new ActionRegisterMessage(username, password, firstName, lastName, email, phone,
                gender.toString().toLowerCase(), DOB, securityQuestion, securityAnswer);
        sendMessageToServer(message);
    }
}