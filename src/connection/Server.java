package connection;

import java.net.*;
import java.io.*;

public class Server {

    public static String hostname = "localhost";
    public static int portNumber = 500;
    private PrintWriter out;
    private BufferedReader in;

    public Server() throws IOException{
        ServerSocket serverSocket = new ServerSocket(portNumber);
        Socket clientSocket = serverSocket.accept();
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void startServerEcho() throws IOException {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Server Echo: " + inputLine);
            out.println(inputLine);
        }
    }

}

