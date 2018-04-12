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
import javafx.scene.control.*;
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
    Home_View_controller home;

    public void setHome(Home_View_controller h){
        home = h;
    }

    public void passConnection(ServerConnection con) {
        connection = con;
    }

    public void setValuesProfile(Profile prof){
        setName(prof.getFirstName(), prof.getLastName());
        setEmail(prof.getEmail());
        setBirthday(prof.getBirthday());
        setGender(prof.getGender());
        setMind(prof.getStatus());
        setBio(prof.getBio());
        setIcon(prof.getColor());
        setHobbies(prof.getHobbies());
        setInterests(prof.getInterests());
        if(connection.getCurrentUser().getContactList().containsKey(thisUser)) {
            setValuesFriendshipStats(connection.getCurrentUser().getContactList().get(thisUser).getFriendshipStats());
        }
    }

    public void setValuesContact(){
        setValuesFriendshipStats(connection.getCurrentUser().getContactList().get(thisUser).getFriendshipStats());
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
        setSent(stats.getSentMessages());
        setReceived(stats.getReceivedMessages());
    }

    private String thisUser;
    @FXML
    private Button backButton;
    @FXML
    private TextField name;
    @FXML
    private TextField email;
    @FXML
    private TextField gender;
    @FXML
    private TextField dob;
    @FXML
    private TextField level;
    @FXML
    private TextField sent;
    @FXML
    private TextField received;
    @FXML
    private ProgressBar levelProgress;
    @FXML
    private Label usern;
    @FXML
    private TextField mind;
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
    private Button add;
    @FXML
    private Label sentLabel;
    @FXML
    private Label receivedLabel;

    @FXML
    public void setUsername(String user, boolean home){
        usern.setText(user);
        thisUser = user;
        if(connection.getCurrentUser().getContactList().containsKey(user)){
            backButton.setVisible(true);
            add.setVisible(false);
            remove.setVisible(true);
            level.setVisible(true);
            levelProgress.setVisible(true);
            sent.setVisible(true);
            received.setVisible(true);
            sentLabel.setVisible(true);
            receivedLabel.setVisible(true);
        }
        else {
            backButton.setVisible(true);
            add.setVisible(true);
            remove.setVisible(false);
            level.setVisible(false);
            levelProgress.setVisible(false);
            sent.setVisible(false);
            received.setVisible(false);
            sentLabel.setVisible(false);
            receivedLabel.setVisible(false);
        }
        if(home){
            backButton.setVisible(false);
        }
    }

    public String getThisUser() {return thisUser;}

    private void setLevel(int lvl){
        level.setText(Integer.toString(lvl/100));
        levelProgress.setProgress((double)(lvl%100)/100);
    }

    private void setSent(int sentMessages){
        sent.setText(Integer.toString(sentMessages));
    }

    private void setReceived(int receivedMessages){
        received.setText(Integer.toString(receivedMessages));
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
            home.SearchHelper(usern.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void removeContact(){
        connection.removeContact(usern.getText());
    }

    @FXML
    public void addContact() {
        connection.addContact(usern.getText());
    }
}
