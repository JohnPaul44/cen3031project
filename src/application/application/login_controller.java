package application;

import connection.ServerConnection;
import connection.serverMessages.ActionLogInMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import model.CurrentUser;

public class login_controller {
	//ServerConnection connection = new ServerConnection();

	@FXML
	private Label status;
	
	@FXML
	private TextField username;
	
	@FXML
	private TextField password;
	
	@FXML
	private Button loginButton;
	
	
	//event handlers for both when the login button is pressed or when the enter key is used
	@FXML
	public void LoginEventKey(KeyEvent keyEvent) throws Exception{
		if(keyEvent.getCode() == KeyCode.ENTER) {
			//calls the same action that occurs when the button is pressed
			ActionEvent aevent = new ActionEvent(keyEvent.getSource(), loginButton);
			//pass the keyEvent into the button action event
			LoginEventButton(aevent);
		}
	}
	
	@FXML
	public void LoginEventButton(ActionEvent event) throws Exception{
		//checks whether the username and password match
		//need to add in a check to the database to check username and password
		//case sensitive
		if(username.getText().equals("user") && password.getText().equals("password")) {
			status.setText("Login Successful");
			
			//creates the new window for the home screen
			//must make a stage, then loads the fxml document for the scene
			Stage primaryStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("/application/home.fxml"));
			Scene scene = new Scene(root,700,400); //sets the size of the window
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			primaryStage.setTitle("Welcome " + username.getText());
			
			//closes the login screen when the home screen pops up
			((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
		}
		else {
			//alerts the user to incorrect credentials
			status.setText("Incorrect Username or Password");
		}
		//connection.sendMessageToServer(new ActionLogInMessage(username.getText(), password.getText()));
	}
	
	@FXML
	public void RegisterEvent(ActionEvent event) throws Exception{
		//opens a new window where a user can register their account
		Stage registerStage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/application/registerUser.fxml"));
		Scene scene = new Scene(root,700,400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		registerStage.setScene(scene);
		registerStage.show();
		
		//closes the login screen when the home screen pops up
		((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
	}
	
	@FXML
	public void ChangePassEvent(ActionEvent event) throws Exception{
		
		if(username.getText().equals("")) {
			status.setText("Please enter your username");
			return;
		}
		
		//check that username is in the database
		//if not, then return
	
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/application/changePass.fxml"));
		loader.load();
		
		//creates instance of the change password controller
		//passes the username to the change password screen
		ChangePassword_controller pass = loader.getController();
		pass.setUsername(username.getText());
		
		Parent root = loader.getRoot();
		Stage changeStage = new Stage();
		Scene scene = new Scene(root,700,400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		changeStage.setScene(scene);
		changeStage.show();
		
		//closes the login screen when the change password screen pops up
		((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
	}
}