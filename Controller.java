package registerUser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.util.Calendar;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class Controller {

    ObservableList<String> genderFieldList = FXCollections.observableArrayList("Male", "Female", "N/A");


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

    public void registerButtonClicked() {
        String username = usernameField.getText();  //TODO: link username(string) to server
        String password = passwordField.getText();
        String confPassword = confirmPasswordField.getText();

        if (!password.equals(confPassword)){
            System.out.println("Your password does not match!");
        }
        else if (password.equals(confPassword)){
            String passwordMathces = confPassword;  //TODO: link password(string) to server
        }

        String firstName = firstNameField.getText(); //TODO: link first name(string) to server

        String lastName = lastNameField.getText(); //TODO: link last name(string) to server

        String email = emailField.getText(); //TODO: link email(string) to server

       // phoneNumber();  //TODO: Limit amount of characters then link phone number to server

        String gender = (String) genderField.getValue();  //TODO: link gender(string) to server
        //System.out.println(gender);


       // calcAge();

        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("Confirm Password: " + confPassword);
        System.out.println("First Name: " + firstName);
        System.out.println("Last Name: " + lastName);
        System.out.println("Email: " + email);
        System.out.println("Phone #: " + phoneNumber());
        System.out.println("Gender: " + gender);


    }

    private String phoneNumber() {  //TODO: Limit amount of numbers to be entered
        String phoneNum = phoneNumberField.getText();
        System.out.println(phoneNum);
        if (phoneNum.matches("[0-9]*") || phoneNum.isEmpty()) {
            System.out.println("Phone # accepted!");
            return phoneNum;
        }
        else {
            System.out.println("Numbers only!");
        }
        return phoneNum;
    }


    @FXML
    private void initialize(){
        //Sets initial value in the drop down
        genderField.setValue("N/A");
        genderField.setItems(genderFieldList);
    }


    @FXML
    private String calcAge() { //TODO: Work in progress only based off year right now, need to get month and day
        if (DOBField == null) {
            String noAge = "N/A";
            return noAge;
        } else {
            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH);
            int day = now.get(Calendar.DAY_OF_MONTH);
            int birthYear = (DOBField.getValue().getYear());
            int age = year - birthYear;
            System.out.println(age);
        }
        return null;
    }
}
