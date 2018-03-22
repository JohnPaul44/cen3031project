package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
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

import java.time.Duration;

public class ChangePassword_View_controller extends ViewController {
	ServerConnection connection;

	public void passConnection(ServerConnection con){
		connection = con;
	}

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
	private Button changeButton;
	@FXML
	private Label status;
	@FXML
	private ChoiceBox securityQuestion;

	public Button getChangeButton(){
		return changeButton;
	}
	
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
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/login.fxml"));
            loader.load();

            Login_View_controller login = loader.getController();
            login.passConnection(connection);
            connection.setDelegate(login);

            Parent root = loader.getRoot();
            Stage registerStage = (Stage) changeButton.getScene().getWindow();
            Scene scene = new Scene(root, 700, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
		connection.changePassword(username.getText(), answer.getText(), phone.getText(), newPass.getText());
	}

	@FXML
	public void confPassChanged(){
    	try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/application/login.fxml"));
			loader.load();

			Login_View_controller login = loader.getController();
			login.passConnection(connection);
			connection.setDelegate(login);

			Parent root = loader.getRoot();
			Stage registerStage = (Stage) changeButton.getScene().getWindow();
			Scene scene = new Scene(root, 700, 500);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			registerStage.setScene(scene);
			registerStage.show();

		} catch(Exception e){
    		e.printStackTrace();
		}

	}

	@FXML
	public boolean confPassword() {
		if(!newPass.getText().equals(confPass.getText())) {
			status.setText("Passwords do not match");
			return false;
		}
		return true;
	}

	@Override
	public void notification(ServerMessage message) {
		switch (message.getStatus()) {
			case NOTIFICATIONERROR:
				//prints to the ui that the login failed
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						status.setText("Incorrect Credentials");
					}
				});
				break;
			case NOTIFICATIONPASSWORDCHANGED:
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						status.setText("Password Changed");
						PauseTransition pause = new PauseTransition(
								javafx.util.Duration.seconds(3)
						);
						pause.setOnFinished(event -> {
							confPassChanged();
						});
						pause.play();
					}
				});
				break;
			default:
				break;
		}
	}
}
