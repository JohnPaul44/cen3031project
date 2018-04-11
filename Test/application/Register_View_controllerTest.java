package application;

import connection.ServerConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.api.FxAssert;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;

import java.io.IOException;

import static org.junit.Assert.*;

class Register_View_Controller_Mock extends Register_View_controller{
    @Override
    public void registerButtonClicked(ActionEvent event) throws Exception{
        //error checking for empty fields
        if(getUsernameField().equals("") || (getPasswordField().getText()).equals("") ||getFirstNameField().equals("") || getLastNameField().equals("") || getEmailField().equals("") || getPhoneNumberField().getText().equals("") || getSecurityQuestionAnswer().equals("") || getSecurityQuestion().equals("<Security Questions>")) {
            setStatus("Please enter: ");
            if(getUsernameField().getText().equals("")) {
                setStatus( "|username|  ");
            }
            if(getPasswordField().getText().equals("")) {
                setStatus("|password|  ");
            }
            if(getFirstNameField().getText().equals("")) {
                setStatus("|first name|  ");
            }
            if(getLastNameField().getText().equals("")) {
                setStatus("|last name|  ");
            }
            if(getEmailField().getText().equals("")) {
               setStatus("|email|  ");
            }
            if(getPhoneNumberField().getText().equals("")){
                setStatus("|phone number|  ");
            }
            if(getSecurityQuestionAnswer().getText().equals("") || getSecurityQuestion().equals("<Security Questions>")){
              setStatus("|security question|  ");
            }
            return;
        }
        //error checking for mismatched passwords
        else if(!confPasswordTest()) {
            //sets incorrect status in the message
            return;
        }
        else if(phoneNumberTest() == null){
            setStatus("Invalid Phone Number");
            return;
        }
        else {
            setStatus("Register Successful");
        }
    }

}

public class Register_View_controllerTest extends ApplicationTest {
    DummyUser dummy = new DummyUser();

    @Override
    public void start(Stage primaryStage) throws IOException{
        try {
            ServerConnection connection = new ServerConnection();
            //opens a new window where a user can register their account
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/registerUser.fxml"));
            loader.setController(new Register_View_Controller_Mock());
            loader.load();

            //creates instance of the change password controller
            //passes the username to the change password screen
            Register_View_Controller_Mock reg = loader.getController();
            reg.passConnection(connection);
            connection.setDelegate(reg);
            reg.setUsernameField(dummy.username1);
            reg.setPasswordField(dummy.password1);
            reg.setConfirmPasswordField(dummy.password1);
            reg.setFirstNameField(dummy.firstName1);
            reg.setLastNameField(dummy.lastName1);
            reg.setEmailField(dummy.email1);
            reg.setPhoneNumberField(dummy.phone1);

            Parent root = loader.getRoot();
            Scene scene = new Scene(root,700,500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setResizable(false);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testButtonsExist(){
        FxAssert.verifyThat("#backButton", NodeMatchers.hasChild("<"));
        FxAssert.verifyThat("#registerButton", NodeMatchers.hasChild("Register!"));
    }

    @Test
    public void testBackButton(){
        clickOn("#backButton");
        //verify that the new window opened by checking for some of the fields
        FxAssert.verifyThat("#loginButton", NodeMatchers.hasChild("Login"));
        FxAssert.verifyThat("#createAnAccount", NodeMatchers.hasChild("Create an Account"));
        FxAssert.verifyThat("#forgotPassword", NodeMatchers.hasChild("Forgot Password?"));
    }

    @Test
    public void testRequiredFields(){
    }
}