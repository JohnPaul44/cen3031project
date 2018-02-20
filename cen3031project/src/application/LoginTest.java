package application;

import java.io.IOException;

import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import org.testfx.matcher.control.TextMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Labeled;


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
		//Button loginB = NodeMatchers.find("#loginButton");
		//System.out.println(loginB.getText());
		
		FxAssert.verifyThat("#loginButton", NodeMatchers.hasText("Login"));
		FxAssert.verifyThat("#createAnAccount", NodeMatchers.hasText("Create an Account"));
		FxAssert.verifyThat("#forgotPassword", NodeMatchers.hasText("Forgot Password?"));
		FxAssert.verifyThat("#status", NodeMatchers.hasText(""));
	}


	//makes sure that if there is no input entered into the text fields, it does not login
	@Test
	public void testLoginWithNoInputEnterKey() {
		//checks that all of the fields are untouched
		//FxAssert.verifyThat("#username", NodeMatchers.hasText(""));
		//FxAssert.verifyThat("#password", NodeMatchers.hasText("password"));
		FxAssert.verifyThat("#status", LabeledMatchers.hasText(""));
		
		//when the enter key is pressed
		push(KeyCode.ENTER);
		FxAssert.verifyThat("#status", LabeledMatchers.hasText("Incorrect Username or Password"));
	}
	
	@Test
	public void testLoginWithNoInputButton() {
		
	}
	
	//tests with login information not in the database
	@Test
	public void testLoginWithIncorrectInputEnterKey() {
		
	}
	
	@Test
	public void testLoginWithIncorrectInputButton() {
		
	}
	
	//test with login correct information
	@Test
	public void testLoginWithCorrectInputEnterKey() {
		
	}
	
	@Test
	public void testLoginWithCorrectInputButton() {
		
	}

}
