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
import javafx.geometry.Pos;
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
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    public void setSearchResults(HashMap<String, Profile> userResults, boolean explore){
        grid = setSearchResultsHelper(userResults, explore, anchor);
    }

    public GridPane setSearchResultsHelper(HashMap<String, Profile> userResults, boolean explore, AnchorPane anchorp) {
        HashMap<String, Contact> contacts = connection.getCurrentUser().getContactList();

        if(explore){
            GridPane gridp = new GridPane();
            //set the sizes of each column in the gridpane
            ColumnConstraints col0 = new ColumnConstraints(137);
            ColumnConstraints col1 = new ColumnConstraints(116);
            ColumnConstraints col2 = new ColumnConstraints(126);
            ColumnConstraints col3 = new ColumnConstraints(49);
            ColumnConstraints col4 = new ColumnConstraints(100);
            ColumnConstraints col5 = new ColumnConstraints(100);
            gridp.getColumnConstraints().addAll(col0, col1, col2, col3, col4, col5);

            Label u = new Label("Username");
            u.setFont(Font.font("System", FontWeight.BOLD,14));
            u.setAlignment(Pos.CENTER);
            Label n = new Label("Name");
            n.setFont(Font.font("System", FontWeight.BOLD,14));
            n.setAlignment(Pos.CENTER);
            Label e = new Label("Email");
            e.setFont(Font.font("System", FontWeight.BOLD, 14));
            e.setAlignment(Pos.CENTER);
            Label a = new Label("Age");
            a.setFont(Font.font("System", FontWeight.BOLD,14));
            a.setAlignment(Pos.CENTER);

            gridp.addRow(0, u, n, e, a, new Label(""), new Label(""));
            grid = gridp;
        }

        if(userResults == null){
            status.setText("No Results");
            return null;
        }
        if(userResults.isEmpty()){
            status.setText("No Results");
            return null;
        }
        if(userResults.size() == 1 && userResults.containsKey(connection.getCurrentUser().getUserName())){
            if(!explore) {
                status.setText("No Results");
            }
            return null;
        }

        int count = 1;
        for(Map.Entry<String, Profile> entry : userResults.entrySet()){
            //TODO: make it add additional rows
            String username = entry.getKey();
            Profile prof = entry.getValue();

            if(username.equals(connection.getCurrentUser().getUserName())){
                continue;
            }

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
            age.setAlignment(Pos.CENTER);
            if(calcAge(prof.getBirthday()) == -1){
                age.setText("N/A");
            }
            else{
                age.setText("" + calcAge(prof.getBirthday()));
            }

            Label us = new Label(username);
            us.setAlignment(Pos.CENTER);
            Label na = new Label(prof.getFirstName() + " " + prof.getLastName());
            na.setAlignment(Pos.CENTER);
            Label em = new Label(prof.getEmail());
            grid.addRow(count++, us, na, em, age, add, view);

            add.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    addContact(username);
                }
            });
            view.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    viewProfile(username, prof, anchorp, explore);
                }
            });

            //check if the user is already in their contacts
            Contact user = contacts.get(username);
            //if the user is already in the contacts
            if(user != null){
                add.setVisible(false);
            }
        }
        return grid;
    }

    public void addContact(String username){
        connection.addContact(username);
    }

    public void viewProfile(String username, Profile prof, AnchorPane anchorp, boolean exp){
        FXMLLoader loadEdit = new FXMLLoader();
        loadEdit.setLocation(getClass().getResource("/application/viewProfile.fxml"));
        AnchorPane temp = new AnchorPane();
        try {
            temp = loadEdit.load();
        } catch(Exception e){
            e.printStackTrace();
        }
        anchorp.getChildren().add(temp);

        ViewProfile_View_Controller view = loadEdit.getController();
        view.passConnection(connection);
        view.setUsername(username, exp);
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
