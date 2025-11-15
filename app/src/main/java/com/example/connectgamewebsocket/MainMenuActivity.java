package com.example.connectgamewebsocket;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;


public class MainMenuActivity extends AppCompatActivity {

    OkHttpClient client = OkHttpClientProvider.getClient();

    TextView playerEmail;

    Button playComputerButton;
    Button playPartnerButton;
    Button playOnlineButton;
    Button logOutButton;


    @Override
    public void onStart() {
        super.onStart();

        String currentUser = ConnectionManager.getCurrentUser();
        playerEmail.setText("Logged in: " + currentUser);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        playComputerButton = findViewById(R.id.playComputer_button);
        playPartnerButton = findViewById(R.id.playPartner_button);
        playOnlineButton = findViewById(R.id.playOnline_button);
        logOutButton = findViewById(R.id.logOut_button);


        playerEmail = findViewById(R.id.playerEmail_textView);


        playComputerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        playPartnerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        playOnlineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectionManager.LogoutListener logoutListener = new ConnectionManager.LogoutListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Logout Successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onFailure() {

                    }
                };
                ConnectionManager.logout(client, logoutListener);
            }
        });


    }

}