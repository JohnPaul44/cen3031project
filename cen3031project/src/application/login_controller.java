package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class login_controller {
	@FXML
	private Label status;
	
	@FXML
	private TextField username;
	
	@FXML
	private TextField password;
	
	@FXML
	public void LoginEvent(ActionEvent event) throws Exception{
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
			
			//closes the login screen when the home screen pops up
			((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
		}
		else {
			//alerts the user to incorrect credentials
			status.setText("Incorrect Username or Password");
		}
	}
	
	@FXML
	public void RegisterEvent(ActionEvent event) throws Exception{
		//opens a new window where a user can register their account
		Stage registerStage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/application/register.fxml"));
		Scene scene = new Scene(root,700,400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		registerStage.setScene(scene);
		registerStage.show();
	}
	
	@FXML
	public void ChangePassEvent(ActionEvent event) throws Exception{
		//opens a new window where a user can change their password
		Stage changeStage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/application/changePass.fxml")); //loads the window
		Scene scene = new Scene(root,700,400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		changeStage.setScene(scene);
		changeStage.show();
	}
}