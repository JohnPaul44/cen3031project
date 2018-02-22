/*package application;

import java.io.IOException;

import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;


public class LoginTest extends ApplicationTest{
	
	//overrides the start function to open the selected fxml file for testing
	@Override
	public void start(Stage primaryStage) throws IOException {
		//loads the login.fxml file for testing
		Parent root = FXMLLoader.load(getClass().getResource("/application/login.fxml"));
		Scene scene = new Scene(root,700,400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	//checks that the screen has all the expected buttons and text fields
	@Test
	public void shouldContainAllButtons() {
		//clickOn("#username").type(KeyCode.H, KeyCode.E, KeyCode.L, KeyCode.L, KeyCode.O );
		//FxAssert.verifyThat("#username", NodeMatchers.hasText("hello"));

		//checks that all the buttons are there and have the proper text
		FxAssert.verifyThat("#loginButton", NodeMatchers.hasText("Login"));
		FxAssert.verifyThat("#createAnAccount", NodeMatchers.hasText("Create an Account"));
		FxAssert.verifyThat("#forgotPassword", NodeMatchers.hasText("Forgot Password?"));
		//checks that the status label is there and defaults to no text
		FxAssert.verifyThat("#status", NodeMatchers.hasText(""));
	}


	//makes sure that if there is no input entered into the text fields, it does not login
	@Test
	public void testLoginWithNoInputEnterKey() {
		//checks the existence of the username and password text fields
		//checks that all of the fields are untouched
		FxAssert.verifyThat("#username", NodeMatchers.hasText(""));
		FxAssert.verifyThat("#password", NodeMatchers.hasText(""));
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
		FxAssert.verifyThat("#username", NodeMatchers.hasText(""));
		FxAssert.verifyThat("#password", NodeMatchers.hasText(""));
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
		FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
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
		FxAssert.verifyThat("#status", LabeledMatchers.hasText("Login Successful"));
	}
	
	@Test
	public void testLoginWithCorrectInputButton() {
		//input a correct username
		clickOn("#username").type(KeyCode.U, KeyCode.S, KeyCode.E, KeyCode.R);
		
		//input a correct password
		clickOn("#password").type(KeyCode.P, KeyCode.A, KeyCode.S, KeyCode.S, KeyCode.W, KeyCode.O, KeyCode.R, KeyCode.D);
		
		clickOn("#loginButton");
		FxAssert.verifyThat("#status", LabeledMatchers.hasText("Login Successful"));
	}

}
*/