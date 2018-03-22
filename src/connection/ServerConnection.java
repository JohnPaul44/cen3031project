package connection;

import java.io.*;
import java.net.*;
import java.util.ArrayList;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.security.ntlm.Server;
import connection.notificationMessageHandlers.*;
import connection.serverMessages.*;
import model.CurrentUser;
import model.Profile;
import model.Reactions;
import model.UserUpdater;
import  application.ViewController;

public class ServerConnection implements IServerConnection{

    private PrintWriter out;
    private BufferedReader in;
    private CurrentUser currentUser;
    private String hostname = "35.231.80.25";
    private int portNumber = 8675;
    private ViewController delegate;

    public void setDelegate(ViewController delegate) {
        this.delegate = delegate;
    }

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
        System.out.println(serverMessage.toJsonString());
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
                    // TODO pass delegate to handle
                    handler.handle();

                    JsonParser parser = new JsonParser();
                    JsonObject jsonObject = parser.parse(messageFromServer).getAsJsonObject();
                    MessageFactory messageFactory = new MessageFactory();
                    ServerMessage serverMessage = messageFactory.produce(jsonObject);
                    delegate.notification(serverMessage);
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

    public void requestSecurityQuestion(String username) {
        ServerMessage message = new ActionRequestSecurityQuestion(username);
        sendMessageToServer(message);
    }

    public void changePassword(String username, String securityAnswer, String phone, String password) {
        ServerMessage message = new ActionChangePassword(username, securityAnswer, phone, password);
        sendMessageToServer(message);
    }

    public void queryUsers(String queryString) {
    }

    public void addContact(String username) {
    }

    public void removeContact(String username) {

    }

    public void updateProfile(String firstName, String lastName, String email, String phone, String bio, ArrayList<String> hobbies,
                              ArrayList<String> interests, String status, String gender, String birthday) {

    }

    public void sendFirstMessage(ArrayList<String> to, String text) {

    }

    public void sendMessage(String conversationKey, String text) {

    }

    public void updateMessage(String conversationKey, String messageKey, String text) {

    }

    public void reactToMessage(String conversationKey, String messageKey, Reactions reactions) {

    }

    public void addUserToConversation(String username, String conversationKey) {

    }

    public void removeUserToConversation(String username, String conversationKey) {

    }

    public void readMessage(String conversationKey) {

    }

    public void setTyping(String conversationKey, boolean typing) {

    }

    public void registerNewUser(String username, String password, String firstName, String lastName, String email,
                                String phone, Profile.Gender gender, String DOB, String securityQuestion, String securityAnswer, String color) {
        ActionRegisterMessage message = new ActionRegisterMessage(username, password, firstName, lastName, email, phone,
                gender, DOB, securityQuestion, securityAnswer, color);
        sendMessageToServer(message);
    }

    public void updateProfile(){
        ActionUpdateProfileMessage message = new ActionUpdateProfileMessage(this.currentUser.getProfile());
        sendMessageToServer(message);
    }

    public void logout(){
        ActionLogOutMessage message = new ActionLogOutMessage();
        sendMessageToServer(message);
    }
}