package model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class FriendshipStats {

    private int sentMessages;
    private int receivedMessages;
    //private HashMap<String, ContactGame> games;
    private int friendshipLevel;
    private ArrayList<String> mutualFriends;


    //Test constructor
    public FriendshipStats(int sentMessages, int receivedMessages, int friendshipLevel) {
        this.sentMessages = sentMessages;
        this.receivedMessages = receivedMessages;
        this.friendshipLevel = friendshipLevel;
    }




    public int getSentMessages() {return sentMessages;}

    public int getReceivedMessages() {return receivedMessages;}

    public int getFriendshipLevel() {return friendshipLevel;}

    public ArrayList getMutualFriends() {return mutualFriends;}

    //public HashMap<String, ContactGame> getGames() {return games;}

}
