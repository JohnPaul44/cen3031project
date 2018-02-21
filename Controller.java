package registerUser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import java.util.Calendar;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class Controller {

    ObservableList<String> genderFieldList = FXCollections.observableArrayList("Male", "Female", "N/A (Optional)");


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

        if (calcAge() == 0){  //If it returns 0 then no age was selected
            String age = "N/A";
        }


        //Manual Testing
        System.out.println("Username: " + username());
       // System.out.println("Password: " + password());
        System.out.println("Confirm Password: " + confPassword());
        System.out.println("First Name: " + firstName());
        System.out.println("Last Name: " + lastName());
        System.out.println("Email: " + email());
        System.out.println("Phone #: " + phoneNumber());
        System.out.println("Gender: " + gender());
        System.out.println("Age: " + calcAge());


    }

    private String username () {
        String username = usernameField.getText();  //TODO: link username(string) to server
        return username;
    }

    private String confPassword (){
        String password = passwordField.getText();
        String confPassword = confirmPasswordField.getText();

        if (!password.equals(confPassword)){
            System.out.println("Your password does not match! Please try again!");
            return password;        //TODO: Make some function to delay registering
        }
        else if (password.equals(confPassword)) {
            String passwordMathces = confPassword;
            return passwordMathces;     //TODO: link password(string) to server
        }
        return password;
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


    private String phoneNumber() {  //TODO: Limit amount of numbers to be entered
        String phoneNum = phoneNumberField.getText();
        System.out.println(phoneNum);
        if (phoneNum.matches("[0-9]*") && !phoneNum.isEmpty()) {
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

    @FXML
    private void initialize(){
        //Sets initial value in the drop down
        genderField.setValue("N/A (Optional)");
        genderField.setItems(genderFieldList);
    }


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
}
