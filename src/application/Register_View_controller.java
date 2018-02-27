package application;

//John Paul
import connection.serverMessages.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

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
import javafx.stage.Stage;

public class Register_View_controller extends ViewController {

    ObservableList<Gender> genderFieldList = FXCollections.observableArrayList(Gender.values());
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
        //Manual Testing
        System.out.println("Username: " + username());
       // System.out.println("Password: " + password());
        System.out.println("Confirm Password: " + passwordField.getText());
        System.out.println("First Name: " + firstName());
        System.out.println("Last Name: " + lastName());
        System.out.println("Email: " + email());
        System.out.println("Phone #: " + phoneNumber());
        System.out.println("Gender: " + gender());
        System.out.println("Birthday: " + birthDay());
        System.out.println("Security Question: " + securityQuestion());
        System.out.println("Security Answer: " + securityAnswer());
        
        //error checking for empty fields
        if(username().equals("") || (passwordField.getText()).equals("") ||firstName().equals("") || lastName().equals("") || email().equals("")) { 	
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
        		return;
        }
        
        //TODO: error checking for duplicate username
        
        //error checking for mismatched passwords
        if(!confPassword()) {
        		//sets incorrect status in the message
        		return;
        }
        
        //TODO: error check password strength? maybe not
        
        //TODO: error check email address
              
        
        status.setText("Register Successful");
        loggedIn();
    }

    public void loggedIn() throws Exception{
        try {
            //opens new window for creating a profile
            Stage primaryStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/application/createProfile.fxml"));
            Scene scene = new Scene(root, 700, 500); //sets the size of the window
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();

            //closes the login screen when the home screen pops up
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    private String username () {
        String username = usernameField.getText();  //TODO: link username(string) to server
        return username;
    }

    private boolean confPassword (){
        String password = passwordField.getText();
        String confPassword = confirmPasswordField.getText();

        if (!password.equals(confPassword)){
            System.out.println("Your password does not match! Please try again!");
            status.setText("Error: passwords do not match");
            return false;        //TODO: Make some function to delay registering
        }
        else if (password.equals(confPassword)) {
            String passwordMathces = confPassword;
            return true;     //TODO: link password(string) to server
        }
        return true;
    }

    private String firstName () {
        String firstName = firstNameField.getText(); //TODO: link first name(string) to server
        return firstName;
    }

    private String lastName () {
        String lastName = lastNameField.getText(); //TODO: link last name(string) to server
        return lastName;
    }

    private String email () {
        String email = emailField.getText(); //TODO: link email(string) to server
        return email;
    }


    private String phoneNumber() {  //TODO: Catch for when invalid entry delays register
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

    private String gender (){
        String gender = (String) genderField.getValue();  //TODO: link gender(string) to server
        return gender;
    }

    private String securityQuestion() {
        String securityQues = (String) securityQuestion.getValue();
        return securityQues;
    }

    private String securityAnswer(){
        String securityAns = securityQuestionAnswer.getText();
        return securityAns;
    }

    enum Gender { FEMALE, MALE, OTHER, NA};

    @FXML
    private void initialize(){
        //Sets initial value in the drop down
        genderField.setValue(Gender.NA);
        genderField.setItems(genderFieldList);
        securityQuestion.setValue("<Security Questions>");
        securityQuestion.setItems(securityQuestionList);
    }

    private String birthDay () {        //TODO: link birthDay(string) to server
        String bDay = String.valueOf(DOBField.getValue());
        return bDay;
    }

    @FXML
    public void BackButton(ActionEvent event) throws Exception{
    		//opens the main login screen up again
    		Stage registerStage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/application/login.fxml"));
		Scene scene = new Scene(root,700,500);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		registerStage.setScene(scene);
		registerStage.show();
		
		//closes the login screen when the home screen pops up
		((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @Override
    void notification(ServerMessage message) {
        switch(message.getStatus()){
            case NOTIFICATIONLOGGEDIN:
                loggedIn();
                break;
            case NOTIFICATIONERROR:
                status.setText("Unavailable Username");
                break;
            default:
                break;
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
}
