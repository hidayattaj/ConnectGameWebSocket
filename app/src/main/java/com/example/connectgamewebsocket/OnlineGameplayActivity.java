package com.example.connectgamewebsocket;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class OnlineGameplayActivity extends AppCompatActivity {


    OkHttpClient client = OkHttpClientProvider.getClient();
    WebSocket webSocket;
    String currentUser, bearerToken, gameID;


    // If value is 0 then image view is yellow (There is no token yet)
    // If value is 1 then image view is red (Player1's token is there)
    // If value is 2 then image view is blue (Player2's token is there)
    int[][] grid = {
            {1, 1, 1, 1, 1},
            {1, 0, 0, 0, 0},
            {1, 0, 0, 0, 0},
            {1, 0, 0, 0, 0},
            {1, 0, 0, 0, 0}
    };

    TextView currentTurn_textView;



    Button column1_button, column2_button, column3_button, column4_button;
    Button newGame_button, quit_button;



    ImageView row1_col1, row2_col1, row3_col1, row4_col1;
    ImageView row1_col2, row2_col2, row3_col2, row4_col2;
    ImageView row1_col3, row2_col3, row3_col3, row4_col3;
    ImageView row1_col4, row2_col4, row3_col4, row4_col4;



    boolean player1Turn;
    boolean player2Turn;



    boolean player1Won;
    boolean player2Won;



    boolean gameDrawn;
    boolean gameAbandoned;


    String player1ID, player2ID;
    String player1Move, player2Move;
    String thisDevicePlayerID;
    String thisDevicePlayerString;


    int currentRow, currentColumn = -1;

    Game game;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_gameplay);

        //*********************
        initialize_views();
        initialize_values();
        //*********************


        Intent receivedIntent = getIntent();
        gameID = receivedIntent.getStringExtra("GAME_ID");



        newGame_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //newGame();
            }
        });
        quit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { quitGame(); }
        });


        column1_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player1Turn) {
                    insertToken(1);
                    disableButtons();
                    updateValuesAndCheckWinConditions();
                } else {
                    insertToken(1);
                    disableButtons();
                    updateValuesAndCheckWinConditions();
                }
            }
        });
        column2_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player1Turn) {
                    insertToken(2);
                    disableButtons();
                    updateValuesAndCheckWinConditions();
                } else {
                    insertToken(2);
                    disableButtons();
                    updateValuesAndCheckWinConditions();
                }
            }
        });
        column3_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player1Turn) {
                    insertToken(3);
                    disableButtons();
                    updateValuesAndCheckWinConditions();
                } else {
                    insertToken(3);
                    disableButtons();
                    updateValuesAndCheckWinConditions();
                }
            }
        });
        column4_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player1Turn) {
                    insertToken(4);
                    disableButtons();
                    updateValuesAndCheckWinConditions();
                } else {
                    insertToken(4);
                    disableButtons();
                    updateValuesAndCheckWinConditions();
                }
            }
        });

        // create websocket connection and start websocket listener
        configureWebSocketListener();
    }

    private void setCurrentTurn() {
        if (player1Turn) {
            player1Turn = false;
            player2Turn = true;
            currentTurn_textView.setText("Player2 Turn");
        } else {
            player1Turn = true;
            player2Turn = false;
            currentTurn_textView.setText("Player1 Turn");
        }

    }


    private void initialize_views() {
        row1_col1 = findViewById(R.id.row1_col1);
        row2_col1 = findViewById(R.id.row2_col1);
        row3_col1 = findViewById(R.id.row3_col1);
        row4_col1 = findViewById(R.id.row4_col1);

        row1_col2 = findViewById(R.id.row1_col2);
        row2_col2 = findViewById(R.id.row2_col2);
        row3_col2 = findViewById(R.id.row3_col2);
        row4_col2 = findViewById(R.id.row4_col2);

        row1_col3 = findViewById(R.id.row1_col3);
        row2_col3 = findViewById(R.id.row2_col3);
        row3_col3 = findViewById(R.id.row3_col3);
        row4_col3 = findViewById(R.id.row4_col3);

        row1_col4 = findViewById(R.id.row1_col4);
        row2_col4 = findViewById(R.id.row2_col4);
        row3_col4 = findViewById(R.id.row3_col4);
        row4_col4 = findViewById(R.id.row4_col4);

        currentTurn_textView = findViewById(R.id.currentTurn_textView);

        column1_button = findViewById(R.id.column1_button);
        column2_button = findViewById(R.id.column2_button);
        column3_button = findViewById(R.id.column3_button);
        column4_button = findViewById(R.id.column4_button);

        newGame_button = findViewById(R.id.newGame_button);
        quit_button = findViewById(R.id.quit_button);

        disableButtons();
    }

    private void initialize_values() {
        player1Turn = false;
        player2Turn = false;

        player1Won = false;
        player2Won = false;

        gameDrawn = false;
    }

    private void updateValuesAndCheckWinConditions() {
        checkHorizontal();
        checkVertical();
        checkDiagonal();

        if (!checkEmptyCell()) { gameDrawn = true; }

        if (gameDrawn) {
            player1Turn = false;
            player2Turn = false;
            disableButtons();
            JSONObject json = new JSONObject();
            try {
                json.put("message", "game-drawn");
                json.put("gameId", gameID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocket.send(json.toString());
            return;
        }

        if (player1Won) {
            player1Turn = false;
            player2Turn = false;
            disableButtons();
            JSONObject json = new JSONObject();
            try {
                json.put("message", "game-won");
                json.put("gameId", gameID);
                json.put("winnerIs", player1ID);
                json.put("lastMove", player1Move);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocket.send(json.toString());
            return;
        }

        if (player2Won) {
            player1Turn = false;
            player2Turn = false;
            disableButtons();
            JSONObject json = new JSONObject();
            try {
                json.put("message", "game-won");
                json.put("gameId", gameID);
                json.put("winnerIs", player2ID);
                json.put("lastMove", player2Move);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocket.send(json.toString());
            return;
        }

        if (player1Turn) {
            setCurrentTurn();
            JSONObject json = new JSONObject();
            try {
                json.put("message", "update-moveAndTurn");
                json.put("gameId", gameID);
                json.put("setPreviousMove", player1Move);
                json.put("setCurrentTurn", player2ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocket.send(json.toString());
        } else {
            setCurrentTurn();
            JSONObject json = new JSONObject();
            try {
                json.put("message", "update-moveAndTurn");
                json.put("gameId", gameID);
                json.put("setPreviousMove", player2Move);
                json.put("setCurrentTurn", player1ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocket.send(json.toString());
        }
    }

    //============================================================

    private void configureWebSocketListener() {
        currentUser = ConnectionManager.currentUser;
        bearerToken = ConnectionManager.sessionID;

        String serverURL = ConnectionManager.webSocketServerURL + bearerToken;
        Request request = new Request.Builder().url(serverURL).build();

        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                System.out.println("Connected to server");
                webSocket = ws;

                JSONObject json = new JSONObject();
                try {
                    json.put("message", "get-gameObject");
                    json.put("gameId", gameID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                webSocket.send(json.toString());
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                System.out.println("Received: " + text);

                try {
                    JSONObject json = new JSONObject(text);
                    if (json.getString("message").equals("done-gameObject")) {

                        game = new Game(json.getString("player1ID"),
                                        json.getString("player1Move"),
                                        json.getString("player2ID"),
                                        json.getString("player2Move"),
                                        json.getString("status"),
                                        json.getString("turn"),
                                        json.getString("winner"));

                        player1ID = game.player1ID;
                        player2ID = game.player2ID;
                        if (currentUser.equals(player1ID)) {
                            thisDevicePlayerID = player1ID;
                            thisDevicePlayerString = "player1";
                        }
                        if (currentUser.equals(player2ID)) {
                            thisDevicePlayerID = player2ID;
                            thisDevicePlayerString = "player2";
                        }

                        runOnUiThread(() -> {
                            checkPlayerTurn();
                        });
                    }

                    if (json.getString("message").equals("done-moveAndTurn")) {
                        game.player1ID = json.getString("player1ID");
                        game.player1Move = json.getString("player1Move");

                        game.player2ID = json.getString("player2ID");
                        game.player2Move = json.getString("player2Move");

                        game.status = json.getString("status");
                        game.turn = json.getString("turn");
                        game.winner = json.getString("winner");

                        runOnUiThread(() -> {
                            checkPlayerTurn();
                        });
                    }

                    if (json.getString("message").equals("done-game-won")) {
                        game.player1ID = json.getString("player1ID");
                        game.player1Move = json.getString("player1Move");

                        game.player2ID = json.getString("player2ID");
                        game.player2Move = json.getString("player2Move");

                        game.status = json.getString("status");
                        game.turn = json.getString("turn");
                        game.winner = json.getString("winner");

                        runOnUiThread(() -> {
                            checkPlayerTurn();
                        });
                    }

                    if (json.getString("message").equals("done-game-drawn")) {
                        game.player1ID = json.getString("player1ID");
                        game.player1Move = json.getString("player1Move");

                        game.player2ID = json.getString("player2ID");
                        game.player2Move = json.getString("player2Move");

                        game.status = json.getString("status");
                        game.turn = json.getString("turn");
                        game.winner = json.getString("winner");

                        runOnUiThread(() -> {
                            checkPlayerTurn();
                        });
                    }

                    if (json.getString("message").equals("opponent-abandoned-game")) {
                        gameAbandoned = true;

                        game.player1ID = json.getString("player1ID");
                        game.player1Move = json.getString("player1Move");

                        game.player2ID = json.getString("player2ID");
                        game.player2Move = json.getString("player2Move");

                        game.status = json.getString("status");
                        game.turn = json.getString("turn");
                        game.winner = json.getString("winner");

                        runOnUiThread(() -> {
                            checkPlayerTurn();
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onClosing(WebSocket ws, int code, String reason) {
                System.out.println("Closing: " + code + " / " + reason);
                ws.close(code, reason);
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                System.err.println("Error: " + t.getMessage());
            }
        };

        client.newWebSocket(request, listener);
    }

    //============================================================




    private void checkPlayerTurn() {

        if (game.status.equals("abandoned")) {
            gameAbandoned = true;
            player1Turn = false;
            player2Turn = false;
            disableButtons();
            currentTurn_textView.setText("Opponent abandoned the game");
            dialogGameAbandoned();
            return;
        }

        if (game.status.equals("drawn")) {
            gameDrawn = true;
            player1Turn = false;
            player2Turn = false;
            disableButtons();
            currentTurn_textView.setText("Game Drawn");
            dialogGameDrawn();
            return;
        }

        if (game.winner.equals(player1ID)) {
            player1Won = true;
            player1Turn = false;
            player2Turn = false;
            disableButtons();
            helperParseMove("player1", game.player1Move);
            currentTurn_textView.setText(player1ID);
            dialogPlayerWin(player1ID + " won the game");
            return;
        }
        if (game.winner.equals(player2ID)) {
            player2Won = true;
            player1Turn = false;
            player2Turn = false;
            disableButtons();
            helperParseMove("player2", game.player2Move);
            currentTurn_textView.setText(player2ID);
            dialogPlayerWin(player2ID + " won the game");
            return;
        }

        displayTurnTextView();

        if (game.turn.equals(thisDevicePlayerID)) {

            if (thisDevicePlayerString.equals("player1")) {
                if (game.player2Move.equals("none") == false) {
                    player2Move = game.player2Move;
                    String[] str = player2Move.split("_");
                    char rowChar = str[0].charAt(3);
                    char colChar = str[1].charAt(3);
                    int rowInt = Character.getNumericValue(rowChar);
                    int colInt = Character.getNumericValue(colChar);
                    updateGrid("player2", rowInt, colInt);
                }

                player1Turn = true;
                player2Turn = false;
                enableButtons();
            }

            if (thisDevicePlayerString.equals("player2")) {
                player1Move = game.player1Move;
                String[] str = player1Move.split("_");
                char rowChar = str[0].charAt(3);
                char colChar = str[1].charAt(3);
                int rowInt = Character.getNumericValue(rowChar);
                int colInt = Character.getNumericValue(colChar);
                updateGrid("player1", rowInt, colInt);

                player1Turn = false;
                player2Turn = true;
                enableButtons();
            }

        }

    }




    private void displayTurnTextView() {
        if (thisDevicePlayerString.equals("player1")) {
            if (game.turn.equals(thisDevicePlayerID)) {
                currentTurn_textView.setText(player1ID);
            } else {
                currentTurn_textView.setText(player2ID);
            }
        } else {
            if (game.turn.equals(thisDevicePlayerID)) {
                currentTurn_textView.setText(player2ID);
            } else {
                currentTurn_textView.setText(player1ID);
            }
        }
    }


    private void insertToken(int column) {

        if (!checkEmptyCell()) {
            gameDrawn = true;
            return;
        }

        if (player1Won || player2Won) { return; }


        if (column == 1) {
            for (int i = 4; i >= 1; i--) {
                if (player1Turn) {
                    if (grid[i][column] == 0) {
                        currentRow = i;
                        currentColumn = column;
                        grid[i][column] = 1;        // Player1 token
                        player1Move = "row" + currentRow + "_" + "col" + currentColumn;
                        if (i == 1) { row1_col1.setImageResource(R.drawable.redstar); }
                        if (i == 2) { row2_col1.setImageResource(R.drawable.redstar); }
                        if (i == 3) { row3_col1.setImageResource(R.drawable.redstar); }
                        if (i == 4) { row4_col1.setImageResource(R.drawable.redstar); }
                        return;
                    }
                }
                if (player2Turn) {
                    if (grid[i][column] == 0) {
                        currentRow = i;
                        currentColumn = column;
                        grid[i][column] = 2;        // Player2 token
                        player2Move = "row" + currentRow + "_" + "col" + currentColumn;
                        if (i == 1) { row1_col1.setImageResource(R.drawable.bluestar); }
                        if (i == 2) { row2_col1.setImageResource(R.drawable.bluestar); }
                        if (i == 3) { row3_col1.setImageResource(R.drawable.bluestar); }
                        if (i == 4) { row4_col1.setImageResource(R.drawable.bluestar); }
                        return;
                    }
                }
            }
        }

        if (column == 2) {
            for (int i = 4; i >= 1; i--) {
                if (player1Turn) {
                    if (grid[i][column] == 0) {
                        currentRow = i;
                        currentColumn = column;
                        grid[i][column] = 1;
                        player1Move = "row" + currentRow + "_" + "col" + currentColumn;
                        if (i == 1) { row1_col2.setImageResource(R.drawable.redstar); }
                        if (i == 2) { row2_col2.setImageResource(R.drawable.redstar); }
                        if (i == 3) { row3_col2.setImageResource(R.drawable.redstar); }
                        if (i == 4) { row4_col2.setImageResource(R.drawable.redstar); }
                        return;
                    }
                }
                if (player2Turn) {
                    if (grid[i][column] == 0) {
                        currentRow = i;
                        currentColumn = column;
                        grid[i][column] = 2;
                        player2Move = "row" + currentRow + "_" + "col" + currentColumn;
                        if (i == 1) { row1_col2.setImageResource(R.drawable.bluestar); }
                        if (i == 2) { row2_col2.setImageResource(R.drawable.bluestar); }
                        if (i == 3) { row3_col2.setImageResource(R.drawable.bluestar); }
                        if (i == 4) { row4_col2.setImageResource(R.drawable.bluestar); }
                        return;
                    }
                }
            }
        }

        if (column == 3) {
            for (int i = 4; i >= 1; i--) {
                if (player1Turn) {
                    if (grid[i][column] == 0) {
                        currentRow = i;
                        currentColumn = column;
                        grid[i][column] = 1;
                        player1Move = "row" + currentRow + "_" + "col" + currentColumn;
                        if (i == 1) { row1_col3.setImageResource(R.drawable.redstar); }
                        if (i == 2) { row2_col3.setImageResource(R.drawable.redstar); }
                        if (i == 3) { row3_col3.setImageResource(R.drawable.redstar); }
                        if (i == 4) { row4_col3.setImageResource(R.drawable.redstar); }
                        return;
                    }
                }
                if (player2Turn) {
                    if (grid[i][column] == 0) {
                        currentRow = i;
                        currentColumn = column;
                        grid[i][column] = 2;
                        player2Move = "row" + currentRow + "_" + "col" + currentColumn;
                        if (i == 1) { row1_col3.setImageResource(R.drawable.bluestar); }
                        if (i == 2) { row2_col3.setImageResource(R.drawable.bluestar); }
                        if (i == 3) { row3_col3.setImageResource(R.drawable.bluestar); }
                        if (i == 4) { row4_col3.setImageResource(R.drawable.bluestar); }
                        return;
                    }
                }
            }
        }

        if (column == 4) {
            for (int i = 4; i >= 1; i--) {
                if (player1Turn) {
                    if (grid[i][column] == 0) {
                        currentRow = i;
                        currentColumn = column;
                        grid[i][column] = 1;
                        player1Move = "row" + currentRow + "_" + "col" + currentColumn;
                        if (i == 1) { row1_col4.setImageResource(R.drawable.redstar); }
                        if (i == 2) { row2_col4.setImageResource(R.drawable.redstar); }
                        if (i == 3) { row3_col4.setImageResource(R.drawable.redstar); }
                        if (i == 4) { row4_col4.setImageResource(R.drawable.redstar); }
                        return;
                    }
                }
                if (player2Turn) {
                    if (grid[i][column] == 0) {
                        currentRow = i;
                        currentColumn = column;
                        grid[i][column] = 2;
                        player2Move = "row" + currentRow + "_" + "col" + currentColumn;
                        if (i == 1) { row1_col4.setImageResource(R.drawable.bluestar); }
                        if (i == 2) { row2_col4.setImageResource(R.drawable.bluestar); }
                        if (i == 3) { row3_col4.setImageResource(R.drawable.bluestar); }
                        if (i == 4) { row4_col4.setImageResource(R.drawable.bluestar); }
                        return;
                    }
                }
            }
        }

    }

    private void updateGrid(String player, int row, int column) {

        if (player.equals("player1")) {
            if (column == 1) {
                grid[row][column] = 1;
                if (row == 1) { row1_col1.setImageResource(R.drawable.redstar); }
                if (row == 2) { row2_col1.setImageResource(R.drawable.redstar); }
                if (row == 3) { row3_col1.setImageResource(R.drawable.redstar); }
                if (row == 4) { row4_col1.setImageResource(R.drawable.redstar); }
                return;
            }
            if (column == 2) {
                grid[row][column] = 1;
                if (row == 1) { row1_col2.setImageResource(R.drawable.redstar); }
                if (row == 2) { row2_col2.setImageResource(R.drawable.redstar); }
                if (row == 3) { row3_col2.setImageResource(R.drawable.redstar); }
                if (row == 4) { row4_col2.setImageResource(R.drawable.redstar); }
                return;
            }
            if (column == 3) {
                grid[row][column] = 1;
                if (row == 1) { row1_col3.setImageResource(R.drawable.redstar); }
                if (row == 2) { row2_col3.setImageResource(R.drawable.redstar); }
                if (row == 3) { row3_col3.setImageResource(R.drawable.redstar); }
                if (row == 4) { row4_col3.setImageResource(R.drawable.redstar); }
                return;
            }
            if (column == 4) {
                grid[row][column] = 1;
                if (row == 1) { row1_col4.setImageResource(R.drawable.redstar); }
                if (row == 2) { row2_col4.setImageResource(R.drawable.redstar); }
                if (row == 3) { row3_col4.setImageResource(R.drawable.redstar); }
                if (row == 4) { row4_col4.setImageResource(R.drawable.redstar); }
                return;
            }
        }

        if (player.equals("player2")) {
            if (column == 1) {
                grid[row][column] = 2;
                if (row == 1) { row1_col1.setImageResource(R.drawable.bluestar); }
                if (row == 2) { row2_col1.setImageResource(R.drawable.bluestar); }
                if (row == 3) { row3_col1.setImageResource(R.drawable.bluestar); }
                if (row == 4) { row4_col1.setImageResource(R.drawable.bluestar); }
                return;
            }
            if (column == 2) {
                grid[row][column] = 2;
                if (row == 1) { row1_col2.setImageResource(R.drawable.bluestar); }
                if (row == 2) { row2_col2.setImageResource(R.drawable.bluestar); }
                if (row == 3) { row3_col2.setImageResource(R.drawable.bluestar); }
                if (row == 4) { row4_col2.setImageResource(R.drawable.bluestar); }
                return;
            }
            if (column == 3) {
                grid[row][column] = 2;
                if (row == 1) { row1_col3.setImageResource(R.drawable.bluestar); }
                if (row == 2) { row2_col3.setImageResource(R.drawable.bluestar); }
                if (row == 3) { row3_col3.setImageResource(R.drawable.bluestar); }
                if (row == 4) { row4_col3.setImageResource(R.drawable.bluestar); }
                return;
            }
            if (column == 4) {
                grid[row][column] = 2;
                if (row == 1) { row1_col4.setImageResource(R.drawable.bluestar); }
                if (row == 2) { row2_col4.setImageResource(R.drawable.bluestar); }
                if (row == 3) { row3_col4.setImageResource(R.drawable.bluestar); }
                if (row == 4) { row4_col4.setImageResource(R.drawable.bluestar); }
                return;
            }
        }

    }



    private boolean checkEmptyCell() {
        for (int row = 4; row >= 1; row--) {
            for (int column = 4; column >= 1; column--) {
                if (grid[row][column] == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkHorizontal() {
        for (int row = 1; row <= 4; row++) {
            for (int col = 1; col <= 2; col++) {
                int cell = grid[row][col];
                // For player1
                if (cell == 1) {
                    if (cell == grid[row][col + 1] && cell == grid[row][col + 2]) {
                        player1Won = true;
                        return;
                    }
                }
                // For player2
                if (cell == 2) {
                    if (cell == grid[row][col + 1] && cell == grid[row][col + 2]) {
                        player2Won = true;
                        return;
                    }
                }
            }
        }
    }

    private void checkVertical() {
        for (int col = 1; col <= 4; col++) {
            for (int row = 1; row <= 2; row++) {
                int cell = grid[row][col];
                // For player1
                if (cell == 1) {
                    if (cell == grid[row + 1][col] && cell == grid[row + 2][col]) {
                        player1Won = true;
                        return;
                    }
                }
                // For player2
                if (cell == 2) {
                    if (cell == grid[row + 1][col] && cell == grid[row + 2][col]) {
                        player2Won = true;
                        return;
                    }
                }
            }
        }
    }

    private void checkDiagonal() {

        // check left-to-right up-down Diagonal Win
        for (int row = 1; row <= 2; row++) {
            for (int col = 1; col <= 2; col++) {
                int cell = grid[row][col];
                if (cell == 1) {
                    if (cell == grid[row + 1][col + 1] && cell == grid[row + 2][col + 2]) {
                        player1Won = true;
                        return;
                    }
                }
                if (cell == 2) {
                    if (cell == grid[row + 1][col + 1] && cell == grid[row + 2][col + 2]) {
                        player2Won = true;
                        return;
                    }
                }
            }
        }

        // check left-to-right down-up Diagonal Win
        for (int row = 4; row >= 3; row--) {
            for (int col = 1; col <= 2; col++) {
                int cell = grid[row][col];
                if (cell == 1) {
                    if (cell == grid[row - 1][col + 1] && cell == grid[row - 2][col + 2]) {
                        player1Won = true;
                        return;
                    }
                }
                if (cell == 2) {
                    if (cell == grid[row - 1][col + 1] && cell == grid[row - 2][col + 2]) {
                        player2Won = true;
                        return;
                    }
                }
            }
        }

    }



    private void enableButtons() {
        column1_button.setEnabled(true);
        column2_button.setEnabled(true);
        column3_button.setEnabled(true);
        column4_button.setEnabled(true);
    }

    private void disableButtons() {
        column1_button.setEnabled(false);
        column2_button.setEnabled(false);
        column3_button.setEnabled(false);
        column4_button.setEnabled(false);
    }



    private void dialogPlayerWin(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogGameDrawn() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Drawn");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void dialogGameAbandoned() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your opponent left the game.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void quitGame() {
        if (webSocket != null) { webSocket.close(1000, "gameplay activity destroyed and websocket connection closed"); }
        Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
        startActivity(intent);
        finish();
    }


    private void helperParseMove(String currentPlayer, String playerMove) {
        String[] str = playerMove.split("_");
        char rowChar = str[0].charAt(3);
        char colChar = str[1].charAt(3);
        int rowInt = Character.getNumericValue(rowChar);
        int colInt = Character.getNumericValue(colChar);
        updateGrid(currentPlayer, rowInt, colInt);
    }

}