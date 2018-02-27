package application;

import connection.serverMessages.ServerMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ChangePassConf_View_controller extends ViewController {
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

	@Override
	void notification(ServerMessage message) {

	}
}
