/*
package application;

import connection.ServerConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import java.io.IOException;

class ChangePassword_View_controller_Mock extends ChangePassword_View_controller{
    DummyUser dummy = new DummyUser();

    //overrides the function that checks the data with the server
    @Override
    public void changePassword(ActionEvent event){
        //check that the two passwords match
        if(!confPassword()) {
            return;
        }

        //check whether the data matches
        if(!getPhone().getText().equals(dummy.phone1)){
            setStatus("Incorrect Credentials");
        }
        else if(!getAnswer().getText().equals(dummy.securityAnswer1)){
            setStatus("Incorrect Credentials");
        }
        else{
            setStatus("Password Changed");
        }
    }
}

public class ChangePassword_View_controllerTest extends ApplicationTest{
    DummyUser dummy = new DummyUser();

    //overrides the start function to open the selected fxml file for testing
    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            ServerConnection connection = new ServerConnection();
            //opens the login window on start up
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/changePass.fxml"));
            loader.setController(new ChangePassword_View_controller_Mock());
            loader.load();

            ChangePassword_View_controller_Mock pass = loader.getController();
            pass.passConnection(connection);
            connection.setDelegate(pass);
            pass.setUsername(dummy.username1);
            pass.setSecurityQuestion(dummy.securityQuestion1);

            Parent root = loader.getRoot();
            Scene scene = new Scene(root,880,500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setResizable(false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //check that all of the proper buttons and textfields are there
    @Test
    public void shouldContainAllFields(){
        //checks that all the buttons are there and have the proper text
        FxAssert.verifyThat("#backButton", NodeMatchers.hasChild("<"));
        FxAssert.verifyThat("#changeButton", NodeMatchers.hasChild("Change Password"));

        //checks that the text fields are there and initialized properly
        FxAssert.verifyThat("#phone", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#securityQuestion", NodeMatchers.hasChild(dummy.securityQuestion1));
        FxAssert.verifyThat("#answer", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#newPass", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#confPass", NodeMatchers.hasChild(""));

        //check that the labels have the proper information
        FxAssert.verifyThat("#username", NodeMatchers.hasChild(dummy.username1));
        FxAssert.verifyThat("#status", NodeMatchers.hasChild(""));
    }

    //test that the back button leads back to the home screen
    @Test
    public void testBackButton(){
        clickOn("#backButton");

        //verify that the new window opened by checking for some of the fields
        FxAssert.verifyThat(lookup("#loginButton").queryButton(), NodeMatchers.hasChild("Login"));
        FxAssert.verifyThat("#createAnAccount", NodeMatchers.hasChild("Create an Account"));
        FxAssert.verifyThat("#forgotPassword", NodeMatchers.hasChild("Forgot Password?"));
        FxAssert.verifyThat("#status", NodeMatchers.hasChild(""));
    }

    //check that when the change password button is clicked with no input, it doesn't change
    @Test
    public void testNoInputButton(){
        //check that there is no input
        FxAssert.verifyThat("#phone", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#securityQuestion", NodeMatchers.hasChild(dummy.securityQuestion1));
        FxAssert.verifyThat("#answer", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#newPass", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#confPass", NodeMatchers.hasChild(""));

        //click on the change password button when there is no input
        clickOn("#changeButton");
        //check that the status alerts the user to incorrect input
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    @Test
    public void testNoInputKey(){
        //check that there is no input
        FxAssert.verifyThat("#phone", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#securityQuestion", NodeMatchers.hasChild(dummy.securityQuestion1));
        FxAssert.verifyThat("#answer", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#newPass", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#confPass", NodeMatchers.hasChild(""));

        //have to initalize some action before the enter key works
        clickOn("#phone");

        //when the enter key is pressed
        push(KeyCode.ENTER);
        //check that the status alerts the user to incorrect input
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    //try to change password with all incorrect information
    @Test
    public void testAllIncorrectButton(){
        //populate the textfields with all incorrect input
        clickOn("#phone").type(KeyCode.DIGIT9, KeyCode.DIGIT8, KeyCode.DIGIT7, KeyCode.DIGIT6, KeyCode.DIGIT5,
                KeyCode.DIGIT4, KeyCode.DIGIT3, KeyCode.DIGIT2, KeyCode.DIGIT1, KeyCode.DIGIT0);
        clickOn("#answer").type(KeyCode.A, KeyCode.B, KeyCode.C);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S);

        clickOn("#changeButton");
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Passwords do not match"));
    }

    @Test
    public void testAllIncorrectVerifyFieldsButton(){
        //populate the textfields with all incorrect input
        //except the passwords match, to check if the criteria is accurate
        clickOn("#phone").type(KeyCode.DIGIT9, KeyCode.DIGIT8, KeyCode.DIGIT7, KeyCode.DIGIT6, KeyCode.DIGIT5,
                KeyCode.DIGIT4, KeyCode.DIGIT3, KeyCode.DIGIT2, KeyCode.DIGIT1, KeyCode.DIGIT0);
        clickOn("#answer").type(KeyCode.A, KeyCode.B, KeyCode.C);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        clickOn("#changeButton");
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    @Test
    public void testAllIncorrectKey(){
        //populate the textfields with all incorrect input
        clickOn("#phone").type(KeyCode.DIGIT9, KeyCode.DIGIT8, KeyCode.DIGIT7, KeyCode.DIGIT6, KeyCode.DIGIT5,
                KeyCode.DIGIT4, KeyCode.DIGIT3, KeyCode.DIGIT2, KeyCode.DIGIT1, KeyCode.DIGIT0);
        clickOn("#answer").type(KeyCode.A, KeyCode.B, KeyCode.C);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S);

        //when the enter key is pressed
        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Passwords do not match"));
    }

    @Test
    public void testAllIncorrectVerifyFieldsKey(){
        //populate the textfields with all incorrect input
        //except the passwords match, to check if the criteria is accurate
        clickOn("#phone").type(KeyCode.DIGIT9, KeyCode.DIGIT8, KeyCode.DIGIT7, KeyCode.DIGIT6, KeyCode.DIGIT5,
                KeyCode.DIGIT4, KeyCode.DIGIT3, KeyCode.DIGIT2, KeyCode.DIGIT1, KeyCode.DIGIT0);
        clickOn("#answer").type(KeyCode.A, KeyCode.B, KeyCode.C);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        //when the enter key is pressed
        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    //try to change password with incorrect phone number
    @Test
    public void testIncorrectPhoneButton(){
        //populate the phone field with incorrect information
        clickOn("#phone").type(KeyCode.DIGIT9, KeyCode.DIGIT8, KeyCode.DIGIT7, KeyCode.DIGIT6, KeyCode.DIGIT5,
                KeyCode.DIGIT4, KeyCode.DIGIT3, KeyCode.DIGIT2, KeyCode.DIGIT1, KeyCode.DIGIT0);

        //populate the rest of the fields with correct information
        clickOn("#answer").type(KeyCode.D, KeyCode.O, KeyCode.G);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        clickOn("#changeButton");
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    @Test
    public void testIncorrectPhoneKey(){
        //populate the phone field with incorrect information
        clickOn("#phone").type(KeyCode.DIGIT9, KeyCode.DIGIT8, KeyCode.DIGIT7, KeyCode.DIGIT6, KeyCode.DIGIT5,
                KeyCode.DIGIT4, KeyCode.DIGIT3, KeyCode.DIGIT2, KeyCode.DIGIT1, KeyCode.DIGIT0);

        //populate the rest of the fields with correct information
        clickOn("#answer").type(KeyCode.D, KeyCode.O, KeyCode.G);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    @Test
    public void testEmptyPhoneButton(){
        //check that the phone field is empty
        FxAssert.verifyThat("#phone", NodeMatchers.hasChild(""));

        //populate the rest of the fields with correct information
        clickOn("#answer").type(KeyCode.D, KeyCode.O, KeyCode.G);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        clickOn("#changeButton");
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    @Test
    public void testEmptyPhoneKey(){
        //check that the phone field is empty
        FxAssert.verifyThat("#phone", NodeMatchers.hasChild(""));

        //populate the rest of the fields with correct information
        clickOn("#answer").type(KeyCode.D, KeyCode.O, KeyCode.G);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    //try to change password with incorrect security answer
    @Test
    public void testIncorrectAnswerButton(){
        //populate the security answer field with incorrect information
        clickOn("#answer").type(KeyCode.N, KeyCode.O, KeyCode.O);

        //populate the rest of the fields with correct information
        clickOn("#phone").type(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
                KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        clickOn("#changeButton");
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    @Test
    public void testIncorrectAnswerKey(){
        //populate the security answer field with incorrect information
        clickOn("#answer").type(KeyCode.N, KeyCode.O, KeyCode.O);

        //populate the rest of the fields with correct information
        clickOn("#phone").type(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
                KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    @Test
    public void testEmptyAnswerButton(){
        //check that the answer field is empty
        FxAssert.verifyThat("#answer", NodeMatchers.hasChild(""));

        //populate the rest of the fields with correct information
        clickOn("#phone").type(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
                KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        clickOn("#changeButton");
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    @Test
    public void testEmptyAnswerKey(){
        //check that the answer field is empty
        FxAssert.verifyThat("#answer", NodeMatchers.hasChild(""));

        //populate the rest of the fields with correct information
        clickOn("#phone").type(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
                KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0);
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Credentials"));
    }

    //try to change password with non-matching passwords
    @Test
    public void testNonMatchingPasswordsButton(){
        //populate the password fields with two different passwords
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.O, KeyCode.W);

        //populate the rest of the fields with correct information
        clickOn("#phone").type(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
                KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0);
        clickOn("#answer").type(KeyCode.D, KeyCode.O, KeyCode.G);

        clickOn("#changeButton");
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Passwords do not match"));
    }

    @Test
    public void testNonMatchingPasswordsKey(){
        //populate the password fields with two different passwords
        //the second password is empty
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);

        //populate the rest of the fields with correct information
        clickOn("#phone").type(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
                KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0);
        clickOn("#answer").type(KeyCode.D, KeyCode.O, KeyCode.G);

        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Passwords do not match"));
    }

    //change password with correct input
    @Test
    public void testCorrectInformationButton(){
        //populate the fields with correction information
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#phone").type(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
                KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0);
        clickOn("#answer").type(KeyCode.D, KeyCode.O, KeyCode.G);

        clickOn("#changeButton");
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Password Changed"));
    }

    @Test
    public void testCorrectInformationKey(){
        //populate the fields with correction information
        clickOn("#newPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#confPass").type(KeyCode.N, KeyCode.E, KeyCode.W);
        clickOn("#phone").type(KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3, KeyCode.DIGIT4, KeyCode.DIGIT5,
                KeyCode.DIGIT6, KeyCode.DIGIT7, KeyCode.DIGIT8, KeyCode.DIGIT9, KeyCode.DIGIT0);
        clickOn("#answer").type(KeyCode.D, KeyCode.O, KeyCode.G);

        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Password Changed"));
    }

    //check that hitting random keys doesn't do anything
    @Test
    public void testRandomKeys(){
        push(KeyCode.E);
        push(KeyCode.U);
        push(KeyCode.DIGIT3);

        //all of the first assertions should still hold true
        //checks that all the buttons are there and have the proper text
        FxAssert.verifyThat("#backButton", NodeMatchers.hasChild("<"));
        FxAssert.verifyThat("#changeButton", NodeMatchers.hasChild("Change Password"));

        //checks that the text fields are there and initialized properly
        FxAssert.verifyThat("#phone", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#securityQuestion", NodeMatchers.hasChild(dummy.securityQuestion1));
        FxAssert.verifyThat("#answer", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#newPass", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#confPass", NodeMatchers.hasChild(""));

        //check that the labels have the proper information
        FxAssert.verifyThat("#username", NodeMatchers.hasChild(dummy.username1));
        FxAssert.verifyThat("#status", NodeMatchers.hasChild(""));
    }
}*/
