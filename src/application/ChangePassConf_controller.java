package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ChangePassConf_controller {
	@FXML
	private Label username;
	@FXML
	private Button returnButton;
	
	public void setUsername(String user) {
		username.setText(user);
	}
	
	@FXML
	public void returnToLoginEventKey(KeyEvent keyEvent) throws Exception{
		if(keyEvent.getCode() == KeyCode.ENTER) {
		//calls the same action that occurs when the button is pressed
		ActionEvent aevent = new ActionEvent(keyEvent.getSource(), returnButton);
		//pass the keyEvent into the button action event
		returnToLogin(aevent);
		}
	}
	
	@FXML
	public void returnToLogin(ActionEvent event) throws Exception{
		//closes the confirmation screen
		((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
		
		//call the back button from the change password controller
		
	}
}
