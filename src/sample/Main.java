package sample;

import Connection.ServerConnection;
import com.google.gson.Gson;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        ServerConnection conn = new ServerConnection("localhost", 500);
        conn.startListeningToServer();

        LoginServerMessage m = new LoginServerMessage("thead9","bogus");
        Gson gson = new Gson();
        String jsonString = gson.toJson(m);

        conn.getOut().println(jsonString);

        //launch(args);
    }
}
