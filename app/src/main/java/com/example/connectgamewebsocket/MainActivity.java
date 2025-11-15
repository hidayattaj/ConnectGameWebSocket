package com.example.connectgamewebsocket;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;


public class MainActivity extends AppCompatActivity {

    OkHttpClient client = OkHttpClientProvider.getClient();

    Button createAccountActivity;
    Button loginButton;

    EditText email_editText;
    EditText password_editText;

    CheckBox rememberMe;

    TextView error;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createAccountActivity = findViewById(R.id.createAccountActivity_button);

        loginButton = findViewById(R.id.login_button);

        email_editText = findViewById(R.id.email_editText);
        password_editText = findViewById(R.id.password_editText);

        error = findViewById(R.id.error_textView);

        rememberMe = findViewById(R.id.rememberMe_checkBox);

        createAccountActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email = String.valueOf(email_editText.getText());
                String password = String.valueOf(password_editText.getText());

                if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
                    error.setText("Please enter your information.");
                    error.setVisibility(View.VISIBLE);
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    error.setText("Please enter your email.");
                    error.setVisibility(View.VISIBLE);
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    error.setText("Please enter your password.");
                    error.setVisibility(View.VISIBLE);
                    return;
                }

                login(email, password);
            }

        });

    }

    private void login(String email, String password) {
        ConnectionManager.LoginListener loginListener = new ConnectionManager.LoginListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure() {

            }
        };

        ConnectionManager.login(client, email, password, loginListener);
    }

}