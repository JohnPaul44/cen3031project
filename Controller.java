package registerUser;

import javafx.fxml.FXML;

import javax.xml.soap.Text;
import java.awt.*;
import java.awt.event.ActionEvent;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
     private TextField usernameField;
    @FXML
    private TextField passwordField;

    public void registerButtonClicked() {
        System.out.println(usernameField.getText());
        System.out.println(passwordField.getText());
    }
}
