package application;

import connection.ErrorInformation;
import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
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

    @FXML
    private TextField searchField;
    @FXML
    private GridPane searchResults;
    @FXML
    private Label status;
    @FXML
    private Button add;
    @FXML
    private Button view;
    @FXML
    private AnchorPane anchor;
    HashMap<String, Profile> userResults;
    boolean error;

    public void setSearchField(String s){
        searchField.setText(s);
        setSearchResults();
    }

    public void setSearchResults() {
        error = false;
        connection.queryUsers(searchField.getText());

        if(error){
            return;
        }

        if(userResults == null){
            status.setText("No Results");
            return;
        }
        if(userResults.isEmpty()){
            status.setText("No Results");
            return;
        }

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

            searchResults.addRow(0, new Label(username), new Label(prof.getFirstName() + prof.getLastName()), new Label(prof.getEmail()), new Label(""+ calcAge(prof.getBirthday())), add, view);

            add.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addContact(username);
                }
            });
            view.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    viewProfile(username);
                }
            });
        }
    }

    public void addContact(String username){
        //TODO:add function

    }

    public void viewProfile(String username){
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
        view.setUsername(username);
    }

    private int calcAge(String DOB) {
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

    //TODO: function that searches and returns the information from users

    @Override
    public void queryResultsNotification(ErrorInformation errorInformation, HashMap<String, Profile> results) {
        if(errorInformation.getErrorNumber() == 0){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    userResults = results;
                }
            });
        }
        else{
            System.out.println(errorInformation.getErrorString());
            error = true;
        }
    }
}
