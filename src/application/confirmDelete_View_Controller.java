package application;

import connection.ServerConnection;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

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
