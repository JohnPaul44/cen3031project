package application;

import connection.ServerConnection;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import model.Contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CreateGroupMessage_View_Controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
        setContactList();
    }

    @FXML
    GridPane grid;
    @FXML
    TextField addList;
    @FXML
    Button create;

    ArrayList<String> groupMembers = new ArrayList<>();

    public void setContactList(){
        HashMap<String, Contact> contacts = connection.getCurrentUser().getContactList();

        int count = 1;
        for(Map.Entry<String, Contact> entry : contacts.entrySet()){
            String username = entry.getKey();
            Contact user = entry.getValue();

            Button add = new Button();
            add.setText("+");
            add.setCursor(Cursor.HAND);
            add.setStyle("-fx-background-color: #698F3F");
            add.setTextFill(Color.WHITE);

            add.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addContact(username, add);
                }
            });

            grid.setAlignment(Pos.CENTER);
            grid.setVgap(10);
            grid.addRow(count++, new Label(username), new Label(user.getProfile().getFirstName() + " " + user.getProfile().getLastName()), add);
            grid.setPadding(new Insets(0,7,0,7));
        }
    }

    public void addContact(String user, Button add){
        String current = addList.getText();
        System.out.println("saved in field " + current);
        if(current.equals("")){
            addList.setText(user);
            groupMembers.add(user);
            add.setVisible(false);
            return;
        }
        addList.setText(current + ", " + user);
        groupMembers.add(user);
        add.setVisible(false);
    }

    public void createConversation(){
        //TODO: pass the list of users over to create a group message
        create.getScene().getWindow().hide();
    }
}
