package application;
	
import connection.ServerConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			ServerConnection connection = new ServerConnection();
			//opens the login window on start up
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("/application/login.fxml"));
			loader.load();

			Login_View_controller login = loader.getController();
			login.passConnection(connection);
			connection.setDelegate(login);

			Parent root = loader.getRoot();
			Scene scene = new Scene(root,700,500);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			primaryStage.setResizable(false);
			login.getUsername().requestFocus();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}