package com.example.connectgamewebsocket;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class LobbyActivity extends AppCompatActivity {

    OkHttpClient client = OkHttpClientProvider.getClient();
    WebSocket webSocket;

    TextView wait_textView, searchingPlayer_textView;
    Button proceedToGameplayButton;


    String currentUser;
    String bearerToken;
    String gameID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        initializeViews();

        configureWebSocketListener();
    }


    private void initializeViews() {
        searchingPlayer_textView = findViewById(R.id.searchingPlayer_textView);
        wait_textView = findViewById(R.id.wait_textView);

        proceedToGameplayButton = findViewById(R.id.proceedToGameplay_button);
        proceedToGameplayButton.setEnabled(false);

        proceedToGameplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToGameplay();
            }
        });
    }


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
                searchGame();
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                System.out.println("Received: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String message = json.getString("message");
                    String gameId = json.optString("gameId");
                    if ("proceed-to-game".equals(message)) {
                        if (webSocket != null) { webSocket.close(1000, "Lobby destroyed"); }
                        runOnUiThread(() -> {
                            gameID = gameId;
                            searchingPlayer_textView.setText("Player found");
                            wait_textView.setText("Match ready. Click Start");
                            proceedToGameplayButton.setEnabled(true);
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


    private void searchGame() {
        JSONObject json = new JSONObject();
        try {
            json.put("message", "search-game");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Send JSON string over WebSocket
        webSocket.send(json.toString());
    }


    private void proceedToGameplay() {
        Intent intent = new Intent(getApplicationContext(), OnlineGameplayActivity.class);
        intent.putExtra("GAME_ID", gameID);
        startActivity(intent);
        finish();
    }

}