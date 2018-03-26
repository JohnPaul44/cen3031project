package application;

import connection.ErrorInformation;
import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import model.Profile;

import java.awt.*;
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
    HashMap<String, Profile> userResults;

    public void setSearchField(String s){
        searchField.setText(s);
        setSearchResults();
    }

    public void setSearchResults() {
        connection.queryUsers(searchField.getText());
        if(userResults.isEmpty()){
            status.setText("No Results");
        }
        else{
            for(Map.Entry<String, Profile> entry : userResults.entrySet()){
                //TODO: make it add additional rows
                String username = entry.getKey();
                Profile prof = entry.getValue();
                Button add = new Button();
                add.setText("Add Friend");
                Button view = new Button();
                view.setText("View Profile");
                searchResults.addRow(0, new Label(username), new Label(prof.getFirstName() + prof.getLastName()), new Label(prof.getEmail()), new Label(""+ calcAge(prof.getBirthday())), add, view);
            }
        }
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
        }
    }
}
