package Connection;

import java.io.*;
import java.net.*;

import com.google.gson.Gson;
import sample.LoginServerMessage;

public class ServerConnection {

    private PrintWriter out;
    private BufferedReader in;

    public ServerConnection(String hostName, int portNumber) {
        try {
            Socket socket = new Socket(hostName, portNumber);
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
                    LoginServerMessage m = gson.fromJson(userInput, LoginServerMessage.class);
                    System.out.println(userInput);
                }
            } catch (IOException e) {
                System.out.println("Error while receiving a message from server: " + e);
            }
        });

        thread.start();
    }
}