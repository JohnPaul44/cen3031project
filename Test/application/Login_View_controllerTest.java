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
import org.testfx.matcher.control.LabeledMatchers;

import java.io.IOException;

class Login_View_Controller_Mock extends Login_View_controller{
    DummyUser dummy = new DummyUser();

    //overrides the login event, so it checks with the dummy user rather than the server
    @Override
    public void LoginEventButton(ActionEvent event){
        if(!getUsername().getText().equals(dummy.username1)){
            setStatus("Incorrect Username or Password");
        }
        else if(!getPassword().getText().equals(dummy.password1)){
            setStatus("Incorrect Username or Password");
        }
        else{
            setStatus("Login Successful");
            connection.setCurrentUser(dummy.getCurrentUser());
            openHome();
        }
    }

    //overrides the button to change password, so that it checks with the dummy user rather than the server
    @Override
    public void ChangePassEvent(ActionEvent event) {

        if(getUsername().getText().equals("")) {
            setStatus("Please enter your username");
        }
        else if(!getUsername().getText().equals(dummy.username1)){
            setStatus("Invalid Username");
        }
        else{
            openChangePassword(dummy.securityQuestion1);
        }
    }
}

public class Login_View_controllerTest extends ApplicationTest{
    DummyUser dummy = new DummyUser();
    //overrides the start function to open the selected fxml file for testing
    @Override
    public void start(Stage primaryStage) throws IOException{
        try {
            ServerConnection connection = new ServerConnection();
            //opens the login window on start up
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/login.fxml"));
            loader.setController(new Login_View_Controller_Mock());
            loader.load();

            Login_View_Controller_Mock login = loader.getController();
            login.passConnection(connection);
            connection.setDelegate(login);

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

    //checks that the screen has all the expected buttons and text fields
    @Test
    public void shouldContainAllButtons(){
        clickOn("#username").type(KeyCode.H, KeyCode.E, KeyCode.L, KeyCode.L, KeyCode.O);
        FxAssert.verifyThat("#username", NodeMatchers.hasChild("hello"));

        //checks that all the buttons are there and have the proper text
        FxAssert.verifyThat(lookup("#loginButton").queryButton(), NodeMatchers.hasChild("Login"));
        FxAssert.verifyThat("#createAnAccount", NodeMatchers.hasChild("Create an Account"));
        FxAssert.verifyThat("#forgotPassword", NodeMatchers.hasChild("Forgot Password?"));

        //checks that the status label is there and defaults to no text
        FxAssert.verifyThat("#status", NodeMatchers.hasChild(""));
    }

    //makes sure that if there is no input entered into the text fields, it does not login
	@Test
    public void testLoginWithNoInputEnterKey() {
        //checks the existence of the username and password text fields
        // checks that all of the fields are untouched
        FxAssert.verifyThat("#username", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#password", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#status", LabeledMatchers.hasText(""));

        //have to initalize some action before the enter key works
        clickOn("#username");

        //when the enter key is pressed
        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
    }

    @Test
    public void testLoginWithNoInputButton() {
        //checks the existence of the username and password text fields
        //checks that all of the fields are untouched
        FxAssert.verifyThat("#username", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#password", NodeMatchers.hasChild(""));
        FxAssert.verifyThat("#status", LabeledMatchers.hasText(""));

        //when the login button is pressed
        clickOn("#loginButton");
        FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
    }

    //tests with login information not in the database
	@Test
	public void testLoginWithIncorrectInputEnterKey() {
        //inputs an incorrect username
        //TODO:figure out how to type a string instead of key codes
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R, KeyCode.R );

        push(KeyCode.TAB);
        //inputs an incorrect password
        type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D, KeyCode.D);

        //press enter key
        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Incorrect Username or Password"));
    }

    @Test
	public void testLoginWithIncorrectInputButton() {
        //inputs an incorrect username
        //TODO:figure out how to type a string instead of key codes
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R, KeyCode.R );

        //inputs an incorrect password
        clickOn("#password").type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D, KeyCode.D);

        //press enter key
        clickOn("#loginButton");
        FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
    }

    @Test
	public void testLoginWithIncorrectUsernameEnterKey() {
        //inputs an incorrect username
        //TODO:figure out how to type a string instead of key codes
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R, KeyCode.R );

        push(KeyCode.TAB);
        //inputs a correct password
        type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D);

        //press enter key
        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
    }

    @Test
	public void testLoginWithIncorrectUsernameButton() {
        //inputs an incorrect username
        //TODO:figure out how to type a string instead of key codes
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R, KeyCode.R );

        //inputs a correct password
        clickOn("#password").type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D);

        //press enter key
        clickOn("#loginButton");
        FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
    }

    @Test
    public void testLoginWithIncorrectPasswordEnterKey() {
        //inputs a correct username
        //TODO:figure out how to type a string instead of key codes
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R);

        push(KeyCode.TAB);
        //inputs an incorrect password
        type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D, KeyCode.D);

        //press enter key
        push(KeyCode.ENTER);
        FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
    }

    @Test
    public void testLoginWithIncorrectPasswordButton() {
        //inputs a correct username
        //TODO:figure out how to type a string instead of key codes
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R);

        //inputs an incorrect password
        clickOn("#password").type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D, KeyCode.D);

        //press enter key
        clickOn("#loginButton");
        FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
    }

    //test with login correct information
	@Test
	public void testLoginWithCorrectInputEnterKey() {
        //input a correct username
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R);

        push(KeyCode.TAB);
        //input a correct password
        type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D);

        push(KeyCode.ENTER);

        //check that the opened window has some of the required fields to indicate the proper window was opened
        FxAssert.verifyThat(lookup("#view"), NodeMatchers.hasChild("#anchor"));
        FxAssert.verifyThat(lookup("#split"), NodeMatchers.hasChild("#scrollPane"));
        FxAssert.verifyThat(lookup("#scrollPane"), NodeMatchers.hasChild("#conversations"));
        FxAssert.verifyThat(lookup("#conversations"), NodeMatchers.hasChild("#usernameAcc"));
        FxAssert.verifyThat(lookup("#usernameAcc"), NodeMatchers.hasChild("user"));
    }

    @Test
	public void testLoginWithCorrectInputButton() {
        //input a correct username
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R);

        //input a correct password
        clickOn("#password").type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D);

        clickOn("#loginButton");

        //check that the opened window has some of the required fields to indicate the proper window was opened
        FxAssert.verifyThat(lookup("#view"), NodeMatchers.hasChild("#anchor"));
        FxAssert.verifyThat(lookup("#split"), NodeMatchers.hasChild("#scrollPane"));
        FxAssert.verifyThat(lookup("#scrollPane"), NodeMatchers.hasChild("#conversations"));
        FxAssert.verifyThat(lookup("#conversations"), NodeMatchers.hasChild("#usernameAcc"));
        FxAssert.verifyThat(lookup("#usernameAcc"), NodeMatchers.hasChild("user"));
    }

    @Test
    public void testOpenRegisterUser(){
        //click on the register user button
        clickOn("#createAnAccount");

        //check that the opened window has some of the required fields to indicate the proper window was opened
        FxAssert.verifyThat(lookup("#border"), NodeMatchers.hasChild("#usernameField"));
        FxAssert.verifyThat(lookup("#border"),NodeMatchers.hasChild("#passwordField"));
        FxAssert.verifyThat(lookup("#border"), NodeMatchers.hasChild("#registerButton"));
    }

    @Test
    public void testOpenForgotPasswordWithNoUsername(){
        //click on the forgot password button without entering a username
        clickOn("#forgotPassword");

        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Please enter your username"));
    }

    @Test
    public void testOpenForgotPasswordWithIncorrectUsername(){
        //inputs an incorrect username
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R, KeyCode.R );
        clickOn("#forgotPassword");

        FxAssert.verifyThat("#status", NodeMatchers.hasChild("Invalid Username"));
    }

    @Test
    public void testOpenForgotPasswordWithCorrectUsername(){
        //input a correct username
        clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R);
        clickOn("#forgotPassword");

        //check that the opened window has some of the required fields
        FxAssert.verifyThat("#backButton", NodeMatchers.hasChild("<"));
        FxAssert.verifyThat("#changeButton", NodeMatchers.hasChild("Change Password"));
        FxAssert.verifyThat("#username", NodeMatchers.hasChild("user"));
        FxAssert.verifyThat("#securityQuestion", NodeMatchers.hasChild(dummy.securityQuestion1));
    }

    @Test
    public void testRandomKeys(){
        push(KeyCode.E);
        push(KeyCode.U);
        push(KeyCode.DIGIT3);

        //all of the first assertions should still hold true
        //checks that all the buttons are there and have the proper text
        FxAssert.verifyThat(lookup("#loginButton").queryButton(), NodeMatchers.hasChild("Login"));
        FxAssert.verifyThat("#createAnAccount", NodeMatchers.hasChild("Create an Account"));
        FxAssert.verifyThat("#forgotPassword", NodeMatchers.hasChild("Forgot Password?"));

        //checks that the status label is there and defaults to no text
        FxAssert.verifyThat("#status", NodeMatchers.hasChild(""));
    }

}*/
