package Connection;

import com.google.gson.Gson;
import sample.LoginMessage;

import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {
        String hostName = "localhost";
        int portNumber = 500;

        try (
                Socket socket = new Socket(hostName, portNumber);
                PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
        ) {
            LoginMessage m = new LoginMessage("thead9", "bogus");

            Gson gson = new Gson();
            String jsonString = gson.toJson(m);

            out.println(jsonString);

            String userInput;
            while ((userInput = in.readLine()) != null) {
                System.out.println(userInput);
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
            System.exit(1);
        }
    }
}
