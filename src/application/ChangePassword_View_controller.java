package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ChangePassword_View_controller extends ViewController {
	@FXML
	private Label username;
	@FXML
	private TextField answer;
	@FXML
	private TextField phone;
	@FXML
	private TextField newPass;
	@FXML
	private TextField confPass;
	@FXML
	private Button backButton;
	@FXML
	private Button confirmButton;
	@FXML
	private Button changeButton;
	@FXML
	private Label status;
	@FXML
	private ChoiceBox securityQuestion;
	
	public String getUsername() {
		return username.getText();
	}
	
	public void setUsername(String user) {
		username.setText(user);
	}
	
	ObservableList<String> securityQuestionList = FXCollections.observableArrayList("<Security Questions>", "What is your mother's maiden name?", "What was the name of your first pet?", "What was your high school mascot?");
    @FXML
    private void initialize() {
    		securityQuestion.setValue("<Security Questions>");
    		securityQuestion.setItems(securityQuestionList);
    }
	
	@FXML
    public void BackButton(ActionEvent event) throws Exception{
    		//opens the main login screen up again
    		Stage registerStage = new Stage();
		Parent root = FXMLLoader.load(getClass().getResource("/application/login.fxml"));
		Scene scene = new Scene(root,700,500);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		registerStage.setScene(scene);
		registerStage.show();
		
		//closes the change password screen when the home screen pops up
		((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
	
	//event handlers for both when the login button is pressed or when the enter key is used
	@FXML
	public void confirmIdentityEventKey(KeyEvent keyEvent) throws Exception{
		if(keyEvent.getCode() == KeyCode.ENTER) {
		//calls the same action that occurs when the button is pressed
		ActionEvent aevent = new ActionEvent(keyEvent.getSource(), confirmButton);
		//pass the keyEvent into the button action event
		confirmIdentity(aevent);
		}
	}
	
	@FXML
	public void confirmIdentity(ActionEvent event) throws Exception{
		//check the validity of the phone number
		if(/*checkPhone(phone.getText())*/!phone.getText().equals("1234567890")) {
			status.setText("Incorrect Credentials");
			return;
		}
		//check the security question answer
		if(/*checkSecurity(answer.getText())*/!answer.getText().equals("ans")) {
			status.setText("Incorrect Credentials");
			return;
		}
		
		confirmButton.setVisible(false);
		newPass.setVisible(true);
		confPass.setVisible(true);
		changeButton.setVisible(true);
	}
	
	@FXML
	public void changePasswordEventKey(KeyEvent keyEvent) throws Exception{
		if(keyEvent.getCode() == KeyCode.ENTER) {
		//calls the same action that occurs when the button is pressed
		ActionEvent aevent = new ActionEvent(keyEvent.getSource(), changeButton);
		//pass the keyEvent into the button action event
		changePassword(aevent);
		}
	}
	
	@FXML
	public void changePassword(ActionEvent event) throws Exception{
		//check that the two passwords match
		if(!confPassword()) {
			return;
		}
		//send the information to the database
		
		//opens confirmation screen
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/application/changePassConf.fxml"));
		loader.load();
		
		//creates instance of the change password controller
		//passes the username to the confirmation screen
		ChangePassConf_View_controller conf = loader.getController();
		conf.setUsername(username.getText());
		
		Parent root = loader.getRoot();
		Stage changeStage = new Stage();
		Scene scene = new Scene(root,250,200);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		changeStage.setScene(scene);
		changeStage.show();
	}
	
	@FXML
	public boolean confPassword() {
		if(!newPass.getText().equals(confPass.getText())) {
			status.setText("Passwords do not match");
			return false;
		}
		return true;
	}
	
	@FXML
	public boolean checkPhone(String number) {
		if(/*number is not associated with the username*/true){
			status.setText("Incorrect phone number");
			return false;
		}
		return true;
	}
	
	@FXML
	public boolean checkSecurityQuestion(String ans) {
		if(/*answers do not match*/true) {
			status.setText("Incorrect information");
			return false;
		}
		return true;
	}
	
	public void close(ActionEvent event) {
		
	}
}
