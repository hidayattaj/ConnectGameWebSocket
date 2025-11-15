package com.example.connectgamewebsocket;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;


public class RegisterActivity extends AppCompatActivity {

    OkHttpClient client = OkHttpClientProvider.getClient();

    Button loginActivity;
    Button createAccountButton;

    EditText email_editText;
    EditText password_editText;

    TextView error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginActivity = findViewById(R.id.loginActivity_button);

        createAccountButton = findViewById(R.id.createAccount_button);


        email_editText = findViewById(R.id.email_editText);
        password_editText = findViewById(R.id.password_editText);

        error = findViewById(R.id.error_textView);


        loginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        createAccountButton.setOnClickListener(new View.OnClickListener() {

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

                register(email, password);
            }

        });

    }

    private void register(String email, String password) {
        ConnectionManager.RegisterListener registerListener = new ConnectionManager.RegisterListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onFailure() {

            }
        };

        ConnectionManager.setCurrentUser(email);
        ConnectionManager.register(client, email, password, registerListener);
    }

}