package application;

import connection.ErrorInformation;
import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Contact;
import model.Profile;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Search_View_Controller extends ViewController{

    ServerConnection connection;
    public void passConnection(ServerConnection con){
        connection = con;
    }

    Home_View_controller home;
    public void setHome(Home_View_controller h){
        home = h;
    }

    @FXML
    private TextField searchField;
    @FXML
    private GridPane grid;
    @FXML
    private Label status;
    @FXML
    private Button add;
    @FXML
    private Button view;
    @FXML
    private AnchorPane anchor;

    public void setSearchField(String s){
        searchField.setText(s);
        search();
    }

    public void search(){
        if(searchField.getText().equals(connection.getCurrentUser().getUserName())){
            status.setText("No Results");
            return;
        }
        connection.queryUsers(searchField.getText());
    }

    public void setSearchResults(HashMap<String, Profile> userResults) {
        HashMap<String, Contact> contacts = connection.getCurrentUser().getContactList();

        if(userResults == null){
            status.setText("No Results");
            return;
        }
        if(userResults.isEmpty()){
            status.setText("No Results");
            return;
        }

        int count = 1;
        for(Map.Entry<String, Profile> entry : userResults.entrySet()){
            //TODO: make it add additional rows
            String username = entry.getKey();
            Profile prof = entry.getValue();

            Button add = new Button();
            add.setText("Add Friend");
            add.setId("add");
            add.setCursor(Cursor.HAND);
            add.setStyle("-fx-background-color: #698F3F");
            add.setTextFill(Color.WHITE);

            Button view = new Button();
            view.setText("View Profile");
            view.setId("view");
            view.setCursor(Cursor.HAND);
            view.setStyle("-fx-background-color: #698F3F");
            view.setTextFill(Color.WHITE);

            Label age = new Label();
            if(calcAge(prof.getBirthday()) == -1){
                age.setText("N/A");
            }
            else{
                age.setText("" + calcAge(prof.getBirthday()));
            }

            grid.addRow(count++, new Label(username), new Label(prof.getFirstName() + " " + prof.getLastName()), new Label(prof.getEmail()), age, add, view);

            add.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addContact(username);
                }
            });
            view.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    viewProfile(username, prof);
                }
            });

            //check if the user is already in their contacts
            Contact user = contacts.get(username);
            //if the user is already in the contacts
            if(user != null){
                add.setVisible(false);
            }
        }
    }

    public void addContact(String username){
        //TODO:add function
        connection.addContact(username);
    }

    public void viewProfile(String username, Profile prof){
        FXMLLoader loadEdit = new FXMLLoader();
        loadEdit.setLocation(getClass().getResource("/application/viewProfile.fxml"));
        AnchorPane temp = new AnchorPane();
        try {
            temp = loadEdit.load();
        } catch(Exception e){
            e.printStackTrace();
        }
        anchor.getChildren().add(temp);

        ViewProfile_View_Controller view = loadEdit.getController();
        view.passConnection(connection);
        view.setUsername(username, false);
        view.setValuesProfile(prof);
        view.setHome(home);
    }

    private int calcAge(String DOB) {
        if(DOB == null || DOB.equals("") || DOB.equals("null")){
            return -1;
        }
        LocalDate birthdate = LocalDate.parse(DOB);
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        if((DOB == null)){
            int age = 0;
            return age;
        }
        int birthYear = (birthdate.getYear());
        int birthMonth = (birthdate.getMonthValue());
        int birthDay = (birthdate.getDayOfMonth());
        int age = year - birthYear;
        if (birthMonth > month){
            age--;
        }
        else if (month == birthMonth){
            if(birthDay > day){
                age--;
            }
        }
        return age;
    }
}
