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
import javafx.stage.Stage;

public class ChangePassword_controller {
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
		Scene scene = new Scene(root,700,400);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		registerStage.setScene(scene);
		registerStage.show();
		
		//closes the login screen when the home screen pops up
		((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
	
	@FXML
	public boolean confPassword() {
		if(!newPass.getText().equals(confPass.getText())) {
			status.setText("Passwords do not match");
			return false;
		}
		return true;
	}
}
