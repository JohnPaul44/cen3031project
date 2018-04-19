package application;

import connection.ServerConnection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class confirmDelete_View_Controller extends ViewController{
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
    }

    private AnchorPane anchorp;

    public void setAnchorPane(AnchorPane anchor){
        anchorp = anchor;
    }

    public void deleteAccount(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/login.fxml"));
            loader.load();

            Login_View_controller login = loader.getController();
            login.passConnection(connection);
            connection.setDelegate(login);

            Parent root = loader.getRoot();
            Stage registerStage = (Stage) anchorp.getScene().getWindow();
            Scene scene = new Scene(root, 880, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void returnToProfile(){
        FXMLLoader loadEdit = new FXMLLoader();
        loadEdit.setLocation(getClass().getResource("/application/viewCurrentUser.fxml"));
        AnchorPane temp = new AnchorPane();
        try {
            temp = loadEdit.load();
        } catch(Exception e){
            e.printStackTrace();
        }
        anchorp.getChildren().add(temp);

        ViewCurrentUser_View_controller view = loadEdit.getController();
        view.passConnection(connection);
    }
}
