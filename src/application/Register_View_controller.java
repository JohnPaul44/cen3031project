package application;

import connection.ErrorInformation;
import connection.ServerConnection;
import connection.serverMessages.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import model.Profile;

public class Register_View_controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
    }

    ObservableList<Profile.Gender> genderFieldList = FXCollections.observableArrayList(Profile.Gender.values());
    ObservableList<String> securityQuestionList = FXCollections.observableArrayList("<Security Questions>", "What is your mother's maiden name?", "What was the name of your first pet?", "What was your high school mascot?");


    //Fields on the Register Screen
    @FXML
     private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confirmPasswordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneNumberField;
    @FXML
    private ChoiceBox genderField;
    @FXML
    private DatePicker DOBField;
    @FXML
    private Button registerButton;
    @FXML
    private Button backButton;
    @FXML
    private Label status;
    @FXML
    private ChoiceBox securityQuestion;
    @FXML
    private TextField securityQuestionAnswer;
    @FXML
    private BorderPane border;

    //overrides so the enter key allows the user to register
    public void registerEnterKey(KeyEvent keyEvent) throws Exception{
    		if(keyEvent.getCode() == KeyCode.ENTER) {
    			//calls the same action that occurs when the enter button is pressed
    			ActionEvent aevent = new ActionEvent(keyEvent.getSource(), registerButton);
    			registerButtonClicked(aevent);
    		}
    }
    
    	@FXML
    public void registerButtonClicked(ActionEvent event) throws Exception {
        //error checking for empty fields
        if(username().equals("") || (passwordField.getText()).equals("") ||firstName().equals("") || lastName().equals("") || email().equals("") || phoneNumberField.getText().equals("") || securityAnswer().equals("") || securityQuestion().equals("<Security Questions>")) {
        		status.setText("Please enter: ");
        		if(username().equals("")) {
        			status.setText(status.getText() + "|username|  ");
        		}
        		if(passwordField.getText().equals("")) {
        			status.setText(status.getText() + "|password|  ");
        		}
        		if(firstName().equals("")) {
        			status.setText(status.getText() + "|first name|  ");
        		}
        		if(lastName().equals("")) {
        			status.setText(status.getText() + "|last name|  ");
        		}
        		if(email().equals("")) {
        			status.setText(status.getText() + "|email|  ");
        		}
        		if(phoneNumberField.getText().equals("")){
        		    status.setText(status.getText() + "|phone number|  ");
                }
                if(securityAnswer().equals("") || securityQuestion().equals("<Security Questions>")){
        		    status.setText(status.getText() + "|security question|  ");
                }
        		return;
        }
        //error checking for mismatched passwords
        else if(!confPassword()) {
        		//sets incorrect status in the message
        		return;
        }
        else if(phoneNumber() == null){
            status.setText("Invalid Phone Number");
            return;
        }
        else {
            registerButton.getScene().setCursor(Cursor.WAIT);

            String default_color = String.valueOf(Color.GREEN);
            connection.registerNewUser(username(), checkedPassword(), firstName(), lastName(), email(), phoneNumber(), gender(), birthDay(), securityQuestion(), securityAnswer(), default_color);
            status.setText("Register Successful");
        }
    }

    public void loggedIn(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/home.fxml"));
            loader.load();

            Home_View_controller home = loader.getController();
            home.passConnection(connection);
            connection.setDelegate(home);
           //home.initialize();

            Parent root = loader.getRoot();
            Stage registerStage = new Stage();
            Scene scene = new Scene(root, 880, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

            registerButton.getScene().getWindow().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String username () {
        String username = usernameField.getText();
        return username;
    }

    private boolean confPassword (){
        String password = passwordField.getText();
        String confPassword = confirmPasswordField.getText();

        if (!password.equals(confPassword)){
            status.setText("Error: passwords do not match");
            return false;
        }
        else if (password.equals(confPassword)) {
            String passwordMathces = confPassword;
            return true;
        }
        return true;
    }

    private String checkedPassword(){
        if (confPassword() == true){
            return passwordField.getText();
        }
        else{
            return null;
        }
    }

    private String firstName () {
        String firstName = firstNameField.getText();
        return firstName;
    }

    private String lastName () {
        String lastName = lastNameField.getText();
        return lastName;
    }

    private String email () {
        String email = emailField.getText();
        return email;
    }


    private String phoneNumber() {
        String phoneNum = phoneNumberField.getText();
        System.out.println(phoneNum);
        if (phoneNum.matches("[0-9]*") && !phoneNum.isEmpty() && phoneNum.length() == 10) {
            System.out.println("Phone # accepted!");
            return phoneNum;
        }
        else if (phoneNum.isEmpty()){
            phoneNum = "N/A";
            return phoneNum;
        }
        else {
            System.out.println("Numbers only! Please re-enter a valid phone number!");
        }
        return null;
    }

    private Profile.Gender gender (){
        return (Profile.Gender) genderField.getValue();
    }

    private String securityQuestion() {
        String securityQues = (String) securityQuestion.getValue();
        return securityQues;
    }

    private String securityAnswer(){
        String securityAns = securityQuestionAnswer.getText();
        return securityAns;
    }

    @FXML
    private void initialize(){
        //Sets initial value in the drop down
        genderField.setValue(Profile.Gender.NA);
        genderField.setItems(genderFieldList);
        securityQuestion.setValue("<Security Questions>");
        securityQuestion.setItems(securityQuestionList);
    }

    private String birthDay () {
        String bDay = String.valueOf(DOBField.getValue());
        return bDay;
    }

    @FXML
    public void BackButton(ActionEvent event) throws Exception{
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/login.fxml"));
            loader.load();

            Login_View_controller login = loader.getController();
            login.passConnection(connection);
            connection.setDelegate(login);

            Parent root = loader.getRoot();
            Stage registerStage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root, 880, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loggedInNotification(ErrorInformation errorInformation) {
            if (errorInformation.getErrorNumber() == 0) {
                status.setText("Register Successful");
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        loggedIn();
                    }
                });
            }
            else {
                System.out.println(errorInformation.getErrorString());
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            status.setText("Username Unavailable");
                        }
                    });
            }
        }
    }
    	
    /*
    @FXML
    private int calcAge() {
            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH) + 1;
            int day = now.get(Calendar.DAY_OF_MONTH);
            if((DOBField.getValue() == null)){
                int age = 0;
                return age;
            }
            int birthYear = (DOBField.getValue().getYear());
            int birthMonth = (DOBField.getValue().getMonthValue());
            int birthDay = (DOBField.getValue().getDayOfMonth());
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
    */

