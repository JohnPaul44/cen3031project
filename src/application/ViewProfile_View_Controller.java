package application;

import connection.ErrorInformation;
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
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import model.Contact;
import model.FriendshipStats;
import model.Profile;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewProfile_View_Controller extends ViewController {
    ServerConnection connection;

    public void passConnection(ServerConnection con) {
        connection = con;
    }

    public void setValuesProfile(Profile prof){
       // setLevel(connection.getCurrentUser().getContactList().get(thisUser).getFriendshipLevel());
        setName(prof.getFirstName(), prof.getLastName());
        setEmail(prof.getEmail());
        setBirthday(prof.getBirthday());
        setGender(prof.getGender());
        setMind(prof.getStatus());
        setBio(prof.getBio());
        setIcon(prof.getColor());
        setHobbies(prof.getHobbies());
        setInterests(prof.getInterests());
    }

    public void setValuesContact(){
        setLevel(connection.getCurrentUser().getContactList().get(thisUser).getFriendshipStats().getFriendshipLevel());

        setName(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getFirstName(), connection.getCurrentUser().getContactList().get(thisUser).getProfile().getLastName());
        setEmail(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getEmail());
        setBirthday(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getBirthday());
        setGender(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getGender());
        setMind(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getStatus());
        setBio(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getBio());
        setIcon(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getColor());
        setHobbies(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getHobbies());
        setInterests(connection.getCurrentUser().getContactList().get(thisUser).getProfile().getInterests());
    }

    public void setValuesFriendshipStats(FriendshipStats stats){
        setLevel(stats.getFriendshipLevel());
    }

    private String thisUser;
    @FXML
    private Button backButton;
    @FXML
    private Label name;
    @FXML
    private Label email;
    @FXML
    private Label gender;
    @FXML
    private Label dob;
    @FXML
    private Label level;
    @FXML
    private ProgressBar levelProgress;
    @FXML
    private Label usern;
    @FXML
    private Label mind;
    @FXML
    private TextArea bio;
    @FXML
    private TextArea hobbies;
    @FXML
    private TextArea interests;
    @FXML
    private Circle icon;
    @FXML
    private  Circle icon_design;
    @FXML
    private Label icon_letter;
    @FXML
    private AnchorPane anchor;
    @FXML
    private Button remove;

    @FXML
    public void setUsername(String user){
        usern.setText(user);
        thisUser = user;
    }

    public String getThisUser() {return thisUser;}

    private void setLevel(int lvl){
        level.setText(Integer.toString(lvl/100));
        levelProgress.setProgress((double)(lvl%100)/100);
    }

    private void setName(String first, String last){
        name.setText(first + ' ' + last);
    }

    private void setEmail(String e){
        email.setText(e);
    }

    private void setBirthday(String birthday){ dob.setText(birthday); }

    private void setGender(String sex){ gender.setText(sex); }

    private void setMind(String statusMind){
        mind.setText(statusMind);
    }

    private void setBio(String biography){
        bio.setText(biography);
    }

    public void setIcon(String col){
        String first_letter = "" + usern.getText().charAt(0);
        icon_letter.setText(first_letter);

        Paint icon_color = Paint.valueOf(col);
        icon.setFill(icon_color);
        icon_design.setFill(icon_color);
        icon_design.setOpacity(0.4);
    }

    private void setInterests(ArrayList<String> interestList) {
        interests.setText(ArrayListToString(interestList));
    }

    private void setHobbies(ArrayList<String> hob) {
        hobbies.setText(ArrayListToString(hob));
    }


    @FXML
    public void BackButton(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/search.fxml"));
            AnchorPane temp = new AnchorPane();
            try{
                temp = loader.load();
            }catch(Exception e){
                e.printStackTrace();
            }
            anchor.getChildren().add(temp);

            Search_View_Controller search = loader.getController();
            search.passConnection(connection);
            search.setSearchField(usern.getText());
            connection.setDelegate(search);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void removeContact(){
        connection.removeContact(usern.getText());
    }
}
