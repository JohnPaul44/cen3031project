package application;

import com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader;
import connection.ErrorInformation;
import connection.ServerConnection;
import connection.serverMessages.ServerMessage;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import model.*;
import sun.plugin.javascript.navig.Anchor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Home_View_controller extends ViewController{

    ServerConnection connection;

    public void passConnection(ServerConnection con){
        connection = con;
        setConversationsList();
        setUsername(connection.getCurrentUser().getUserName());
    }

    public void setConversationsList(){
        HashMap<String, Contact> contactList = connection.getCurrentUser().getContactList();

        if(!contactList.isEmpty()){
            for(Map.Entry<String, Contact> entry: contactList.entrySet()){
                String key = entry.getKey();
                Contact value = entry.getValue();
                createNewContact(key, value.getOnline());
            }
        }

        //TODO: import current conversations
//        HashMap<String, Conversation> conversationList = connection.getCurrentUser().getConversationList();
//        if(!conversationList.isEmpty()){
//            for(Map.Entry<String, Conversation> entry : conversationList.entrySet()){
//                String key = entry.getKey();
//                Conversation value = entry.getValue();
//                if(value.getMemberStatus().size() > 2){
//                    createNewConversationCard(value);
//                }
//            }
//        }
        setMessageNotificationStart();
    }

    ArrayList<TitledPane> contacts = new ArrayList<>();

    public void createNewContact(String user, Boolean online){
        TitledPane newContact = new TitledPane();
        newContact.setText(user);
        newContact.setStyle("-fx-background-color: #E7DECD");

        VBox content = new VBox();
        Label dm = new Label("Direct Message");
        dm.setCursor(Cursor.HAND);
        dm.setLabelFor(directMessage);
        dm.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try{
                    OpenDirectMessage(event, newContact);
                } catch(Exception e){}
            }
        });

        Label vp = new Label("View Profile");
        vp.setCursor(Cursor.HAND);
        vp.setLabelFor(viewProfile);
        vp.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try{
                    ViewOtherProfile(event, user);
                } catch(Exception e){}
            }
        });


        content.setStyle("-fx-background-color: #E7DECD");

        content.getChildren().add(dm);
        content.getChildren().add(vp);

        HBox notification = new HBox();
        notification.setAlignment(Pos.CENTER);
        notification.setSpacing(3);

        Circle notificationOnline = new Circle(3);
        notificationOnline.setFill(Color.GREEN);
        if(!online) {
            notificationOnline.setVisible(false);
        }

        Label notificationNew = new Label("!");
        notificationNew.setTextFill(Color.RED);
        notificationNew.setVisible(false);

        notification.getChildren().addAll(notificationOnline, notificationNew);

        newContact.setContent(content);
        newContact.setGraphic(notification);
        contacts.add(newContact);
        conversations.getPanes().add(newContact);
    }

    public void createNewConversationCard(Conversation convo){
        TitledPane newContact = new TitledPane();
        Map<String, Status> mem = convo.getMemberStatus();
        boolean first = true;
        for(String key : mem.keySet()){
            if(first){
                newContact.setText(key);
                first = false;
            }
            else{
                newContact.setText(newContact.getText() + ", " + key);
            }
        }
        newContact.setStyle("-fx-background-color: #E7DECD");


        VBox content = new VBox();
        Label dm = new Label("Group Message");
        dm.setCursor(Cursor.HAND);
        dm.setLabelFor(directMessage);
        dm.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                try{
                    OpenDirectMessage(event, newContact);
                } catch(Exception e){}
            }
        });

        Label notificationNew = new Label("!");
        notificationNew.setTextFill(Color.RED);
        notificationNew.setVisible(false);

        content.setStyle("-fx-background-color: #E7DECD");
        content.getChildren().add(dm);
        newContact.setContent(content);
        newContact.setGraphic(notificationNew);

        contacts.add(newContact);
        conversations.getPanes().add(newContact);

        //TODO: check whether there are unread messages
    }

    public void setMessageNotificationStart(){
        //checks whether the user has any unread conversations

        HashMap<String, Conversation> convoList = connection.getCurrentUser().getConversationList();
        for(String key : convoList.keySet()){
            Conversation convo = connection.getCurrentUser().getConversationList().get(key);
            Map<String, Status> mem = convo.getMemberStatus();
            Status stat = mem.get(connection.getCurrentUser().getUserName());
            System.out.println(stat.getRead() + " " + key);

            if(!stat.getRead()){
                int size = contacts.size();
                for(int i = 0; i < size; i++){
                    for(String keyMem : mem.keySet()){
                        if(keyMem.equals(contacts.get(i).getText())){
                            HBox notif = (HBox) contacts.get(i).getGraphic();
                            notif.getChildren().get(1).setVisible(true);
                        }
                    }
                }
            }
        }
    }

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Accordion conversations;
    @FXML
    private TitledPane usernameAcc;
    @FXML
    private TextField search;
    @FXML
    private Label directMessage;
    @FXML
    private Label viewProfile;
    @FXML
    private SplitPane split;
    @FXML
    private AnchorPane view;

    private Conversation_View_controller currentConvo;
    private Search_View_Controller currentSearch;
    private ViewProfile_View_Controller vpScreen;
    private Home_View_controller home;
    private Explore_View_Controller expl;

    public void setHome(Home_View_controller h){
        home = h;
    }

    private void setUsername(String user){
        usernameAcc.setText(user);
    }

    public void loadCurrentProfile(){
        try {
            AnchorPane temp = new AnchorPane();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/viewCurrentUser.fxml"));
            temp = loader.load();
            view.getChildren().add(temp);

            ViewCurrentUser_View_controller profile = loader.getController();
            profile.passConnection(connection);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void loadEditProfile(){
        try {
            AnchorPane temp = new AnchorPane();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/createProfile.fxml"));
            temp = loader.load();
            setView(temp);

            EditProfile_View_Controller edit = loader.getController();
            edit.passConnection(connection);

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setView(AnchorPane anchor){
        try{
            view.getChildren().add(anchor);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void Logout(){
        connection.logout();

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/login.fxml"));
            loader.load();

            Login_View_controller login = loader.getController();
            login.passConnection(connection);
            connection.setDelegate(login);

            Parent root = loader.getRoot();
            Stage registerStage = (Stage) scrollPane.getScene().getWindow();
            Scene scene = new Scene(root, 880, 500);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            registerStage.setScene(scene);
            registerStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openExplore(){
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/application/explore.fxml"));
        AnchorPane anchor = new AnchorPane();

        try {
            anchor = loader.load();
        } catch(Exception e){
            e.printStackTrace();
        }
        setView(anchor);

        expl = loader.getController();
        expl.passConnection(connection);
    }

    @FXML
    public void Search(javafx.scene.input.KeyEvent keyEvent) throws Exception{
        if(keyEvent.getCode() == KeyCode.ENTER) {
            SearchHelper(search.getText());
        }
    }

    @FXML
    public void SearchHelper(String searchUser){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/search.fxml"));
            AnchorPane anchor = new AnchorPane();
            anchor = loader.load();

            setView(anchor);

            currentSearch = loader.getController();
            currentSearch.passConnection(connection);
            currentSearch.setSearchField(searchUser);
            currentSearch.setHome(home);
            //connection.setDelegate(currentSearch);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void OpenDirectMessage(MouseEvent actionEvent, TitledPane user) throws Exception{
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/directMessage.fxml"));
            AnchorPane anchor = new AnchorPane();
            anchor = loader.load();
            setView(anchor);

            currentConvo = loader.getController();
            currentConvo.passConnection(connection);
            //connection.setDelegate(dmScreen);
            currentConvo.setUsername(user.getText());
            currentConvo.setTopic();

            int contactSize = contacts.size();
            for(int i = 0; i < contactSize; i++){
                if((user.getText()).equals(contacts.get(i).getText())){
                    HBox notif = (HBox) contacts.get(i).getGraphic();
                    notif.getChildren().get(1).setVisible(false);
                }
            }

            HashMap<String, Conversation> convos = connection.getCurrentUser().getConversationList();
            if(convos != null) {

                for (Map.Entry<String, Conversation> entry : convos.entrySet()) {
                    String key = entry.getKey();
                    Conversation value = entry.getValue();
                    //TODO: set so it checks for all the members of the conversation
                    if (value.getMemberStatus().containsKey(user.getText())) {
                        currentConvo.setConversationKey(key);
                        break;
                    }
                }
            }
            currentConvo.setMessages();

        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void ViewOtherProfile(MouseEvent actionEvent, String user) throws Exception{
        ViewOtherProfileHelper(user);
    }

    @FXML
    public void ViewOtherProfileHelper(String user){
        try{
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/application/viewProfile.fxml"));
            AnchorPane anchor = new AnchorPane();
            anchor = loader.load();
            setView(anchor);
            vpScreen = loader.getController();
            vpScreen.passConnection(connection);
            vpScreen.setUsername(user, true);
            vpScreen.setValuesContact();
            connection.getFriendshipStatistics(user);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @FXML
    public void createGroupMessage(){
        //load the view to add contacts
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/application/createGroupMessage.fxml"));
        try {
            loader.load();
        }catch(Exception e){
            e.printStackTrace();
        }

        CreateGroupMessage_View_Controller group = loader.getController();
        group.passConnection(connection);

        Parent root = loader.getRoot();
        Stage registerStage = new Stage();
        Scene scene = new Scene(root, 463, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        registerStage.setScene(scene);
        registerStage.show();
    }

    @FXML
    public void setOnlineStatus(String username, boolean online){
        for(int i = 0; i < contacts.size(); i++){
            if(username.equals(contacts.get(i).getText())){
                HBox notif = (HBox) contacts.get(i).getGraphic();
                if(online) {
                    notif.getChildren().get(0).setVisible(true);
                }
                else{
                    notif.getChildren().get(0).setVisible(false);
                }
            }
        }
    }

    @FXML
    public void setMessageNotification(String username){
        for(int i = 0; i < contacts.size(); i++){
            if(username.equals(contacts.get(i).getText())){
                HBox notif = (HBox) contacts.get(i).getGraphic();
                notif.getChildren().get(1).setVisible(true);
            }
        }
    }

    public void deliverMessage(String conversationKey, String messageKey, String time, String from, String text){
        int children = view.getChildren().size();
        AnchorPane top = (AnchorPane) view.getChildren().get(children - 1);
        Label openedName = (Label) top.getChildren().get(0);

        view.getScene().getWindow().requestFocus();

        Map<String, Status> mem = connection.getCurrentUser().getConversationList().get(conversationKey).getMemberStatus();
        if(from.equals(connection.getCurrentUser().getUserName())){
            currentConvo.setUsername(openedName.getText());
            currentConvo.newMessage(conversationKey, messageKey, time, from, text, mem);
        }
        //if the message is from the user that is currently open
        else if(openedName.getText().equals(from)){
            currentConvo.setUsername(from);
            currentConvo.newMessage(conversationKey, messageKey, time, from, text, mem);
            if (!currentConvo.getConvKey().isEmpty()) connection.readMessage(currentConvo.getConvKey());
        }
        //if the message is from a user when their conversation is not currently open
        else{
            //do nothing, they will automatically load when that conversation is opened
            //place a notification of a new message
            setMessageNotification(from);
        }
    }


    /*Callbacks*/
    @Override
    public void loggedOutNotification(ErrorInformation errorInformation){
        if(errorInformation.getErrorNumber() != 0){
            System.out.println(errorInformation.getErrorString());
        }
    }

    @Override
    public void messageReceivedNotification(ErrorInformation errorInformation, String conversationKey, String messageKey,
                                            String time, String from, String text) {
        if(errorInformation.getErrorNumber() == 0){

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    deliverMessage(conversationKey, messageKey, time, from, text);
                }
            });
        }
        else{
            System.out.println(errorInformation.getErrorString());
        }
    }

    @Override
    public void userOnlineStatusNotification(ErrorInformation errorInformation, String username, boolean online){
        if(errorInformation.getErrorNumber() == 0){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    setOnlineStatus(username, online);
                }
            });
        }
        else{
            System.out.println(errorInformation.getErrorString());
        }
    }

    @Override
    public void messageReadNotification(ErrorInformation errorInformation, String conversationKey, String from) {
        if(errorInformation.getErrorNumber() == 0){

        }
    }

    @Override
    public void profileUpdatedNotification(ErrorInformation errorInformation, Profile profile) {
        if (errorInformation.getErrorNumber() != 0){
            System.out.println(errorInformation.getErrorString());
        }
    }

    @Override
    public void friendshipStatsNotification(ErrorInformation errorInformation, String username, FriendshipStats friendshipStats) {
        if (errorInformation.getErrorNumber() != 0){
            if(vpScreen.getThisUser().equals(username)) {
                vpScreen.setValuesFriendshipStats(friendshipStats);
            }
        }
        else {
            System.out.println(errorInformation.getErrorString());
        }
    }

    @Override
    public void queryResultsNotification(ErrorInformation errorInformation, HashMap<String, Profile> results) {
        if(errorInformation.getErrorNumber() == 0){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    boolean explore = false;

                    //check if the explore screen is open
                    int children = view.getChildren().size();
                    AnchorPane top = (AnchorPane) view.getChildren().get(children - 1);
                    Label openedName = null;
                    try {
                        openedName = (Label) top.getChildren().get(0);
                    } catch(Exception e){
                        explore = false;
                    }

                    if(openedName != null){
                        if(openedName.getText().equals("Explore")){
                            explore = true;
                        }
                    }

                    if(!explore) {
                        currentSearch.setSearchResults(results, explore);
                    }
                    else{
                        expl.setSearchResults(results);
                    }
                }
            });
        }
        else{
            System.out.println(errorInformation.getErrorString());
        }
    }

    @Override
    public void contactAddedNotification(ErrorInformation errorInformation, String username) {
        if(errorInformation.getErrorNumber() == 0){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    createNewContact(username, false);
                }
            });
        }
        else{
            System.out.println(errorInformation.getErrorString());
        }
    }

    @Override
    public void contactRemovedNotification(ErrorInformation errorInformation, String username){
        System.out.println("Contact removed notification");
        if(errorInformation.getErrorNumber() == 0){
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //load the home view and remove the contact from the home screen
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource("/application/home.fxml"));
                    try {
                        loader.load();
                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    Home_View_controller home = loader.getController();
                    home.passConnection(connection);
                    home.ViewOtherProfileHelper(username);
                    connection.setDelegate(home);

                    Parent root = loader.getRoot();
                    Stage registerStage = (Stage) conversations.getScene().getWindow();
                    Scene scene = new Scene(root, 880, 500);
                    scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
                    registerStage.setScene(scene);
                    registerStage.show();
                }
            });
        }
        else{
            System.out.println(errorInformation.getErrorString());
        }
    }
}
