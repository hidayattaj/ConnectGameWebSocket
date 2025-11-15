package com.example.connectgamewebsocket;

import androidx.annotation.NonNull;

public class Game {

    public String player1ID;            // UID of player 1
    public String player1Move;

    public String player2ID;            // UID of player 2
    public String player2Move;

    public String status;               // Status of the current game
    public String turn;                 // UID of the player whose turn it is
    public String winner;               // UID of the winner, null if no winner yet


    // Default constructor (required for Firebase)
    public Game() {

    }

    public Game(String player1ID, String player1Move, String player2ID, String player2Move, String status, String turn, String winner) {
        this.player1ID = player1ID;
        this.player1Move = player1Move;

        this.player2ID = player2ID;
        this.player2Move = player2Move;

        this.status = status;
        this.turn = turn;
        this.winner = winner;
    }

    @NonNull
    public String toString() {
        String str =    player1ID + "\n" +
                player1Move + "\n" +
                player2ID + "\n" +
                player2Move + "\n" +
                status + "\n" +
                turn + "\n" +
                winner + "\n";
        return str;
    }

}