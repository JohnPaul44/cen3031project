package application;

import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.application.Platform;
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

public class Login_View_controller extends ViewController{
	ServerConnection connection;

	public void passConnection(ServerConnection con){
		connection = con;
	}

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
	public void LoginEventButton(ActionEvent event){
		connection.login(username.getText(), password.getText());
	}
	
	@FXML
	public void RegisterEvent(ActionEvent event) throws Exception{
		//opens a new window where a user can register their account

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("/application/registerUser.fxml"));
		loader.load();

		//creates instance of the change password controller
		//passes the username to the change password screen
		Register_View_controller reg = loader.getController();
		reg.passConnection(connection);
		connection.setDelegate(reg);

		Parent root = loader.getRoot();
		Stage changeStage = new Stage();
		Scene scene = new Scene(root,700,500);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		changeStage.setScene(scene);
		changeStage.show();

		//closes the login screen when the change password screen pops up
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
		ChangePassword_View_controller pass = loader.getController();
		pass.setUsername(username.getText());
		pass.passConnection(connection);
		connection.setDelegate(pass);

		
		Parent root = loader.getRoot();
		Stage changeStage = new Stage();
		Scene scene = new Scene(root,700,500);
		scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		changeStage.setScene(scene);
		changeStage.show();
		
		//closes the login screen when the change password screen pops up
		((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
	}

	@FXML
	public void openHome() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/application/home.fxml"));
			loader.load();

			Home_View_controller home = loader.getController();
			home.passConnection(connection);
			connection.setDelegate(home);
			//home.initialize();

			Parent root = loader.getRoot();
			Stage registerStage = new Stage();
			Scene scene = new Scene(root, 700, 500);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			registerStage.setScene(scene);
			registerStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}

		//TODO: close login screen after opening the home screen, without having an action event
	}

	@Override
	public void notification(ServerMessage message) {
		switch (message.getStatus()) {
			case NOTIFICATIONLOGGEDIN:
				//opens the next screen
				System.out.println("login success");
				//status.setText("Login Successful");
                Platform.runLater(new Runnable(){
                    @Override
                    public void run(){
                        openHome();
                    }
                });
				break;
			case NOTIFICATIONERROR:
				System.out.println("login failed");
				//prints to the ui that the login failed
				//status.setText("Incorrect Username or Password");
				break;
			default:
				break;
		}
	}
}