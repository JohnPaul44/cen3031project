package model;

public class ContactGame {
    private int wins;
    private int losses;
    private int ties;

    public ContactGame() {
        this.wins = 0;
        this.losses = 0;
        this.ties = 0;
    }

    public int getWins() {return wins;}
    public int getLosses() {return losses;}
    public int getTies() {return ties;}

}

