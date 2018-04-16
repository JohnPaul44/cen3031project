package application;

import connection.ServerConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.Profile;
import sun.plugin.javascript.navig.Anchor;
import sun.plugin.javascript.navig.Array;

import java.util.ArrayList;
import java.util.HashMap;

public class Explore_View_Controller extends ViewController{
    ServerConnection connection;
    public void passConnection(ServerConnection con){
        connection = con;
        setUp();
    }

    @FXML
    private VBox vbox;
    @FXML
    private Label item;
    @FXML
    private AnchorPane anchor;
    private AnchorPane noRes;

    ArrayList<String> interestsList;
    int interestsCount;
    int interestsSize;
    ArrayList<String> hobbiesList;
    int hobbiesCount;

    public void setUp(){
        interestsList = connection.getCurrentUser().getProfile().getInterests();
        interestsCount = 0;
        interestsSize = interestsList.size();
        hobbiesList = connection.getCurrentUser().getProfile().getHobbies();
        hobbiesCount = 0;

        for(String item : interestsList){
            connection.queryUsers(item);
            try {
                Thread.sleep(1000);
            } catch(Exception e){
                e.printStackTrace();
            }
        }

        for(String item : hobbiesList){
            connection.queryUsers(item);
            try {
                Thread.sleep(1000);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public void setSearchResults(HashMap<String, Profile> userResults) {
        //call the set search results function from the search view controller
        Search_View_Controller search = new Search_View_Controller();
        search.passConnection(connection);
        GridPane grid = search.setSearchResultsHelper(userResults, true, anchor);


        if(grid == null){
            if(interestsCount < interestsSize){
                interestsCount++;
            }
            else{
                hobbiesCount++;
            }
            return;
        }

        noRes.setVisible(false);
        setInterestLabel();
        vbox.getChildren().add(grid);
    }

    public void setNoResults(){
        noRes = new AnchorPane();
        noRes.setId("noRes");
        noRes.setStyle("-fx-background-color: #0A122A");

        Label interestLabel = new Label("There are no users with similar interests or hobbies");

        interestLabel.setTextFill(Color.WHITE);
        interestLabel.setLayoutX(5);

        noRes.getChildren().add(interestLabel);

        vbox.getChildren().add(noRes);
    }

    public void setInterestLabel(){
        AnchorPane interest = new AnchorPane();
        interest.setPrefHeight(20);
        interest.setMinHeight(20);
        interest.setStyle("-fx-background-color: #0A122A");

        Label interestLabel = new Label();
        if(interestsCount < interestsSize){
            interestLabel. setText("Users also Interested in " + interestsList.get(interestsCount++) + ": ");
        }
        else{
            interestLabel. setText("Users also Interested in " + hobbiesList.get(hobbiesCount++) + ": ");
        }

        interestLabel.setTextFill(Color.WHITE);
        interestLabel.setFont(Font.font(16));
        interestLabel.setLayoutX(5);

        interest.getChildren().add(interestLabel);

        vbox.getChildren().add(interest);
    }


}
