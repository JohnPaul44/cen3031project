package sample;

import connection.ServerConnection;
import connection.serverMessaging.ActionLogInMessage;
import com.google.gson.Gson;
import connection.serverMessaging.ActionUpdateProfileMessage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Contact;
import model.Conversation;
import model.LoggedInUser;
import model.Profile;

import java.util.ArrayList;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        LoggedInUser loggedInUser = new LoggedInUser();


        //launch(args);
    }
}
