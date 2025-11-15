package com.example.connectgamewebsocket;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectionManager {

    public static String currentUser;
    public static String sessionID;


    // android localhost ip = 10.0.2.2
    static String registrationURL = "http://10.0.2.2:5000/register/";
    static String loginURL = "http://10.0.2.2:5000/login/";
    static String logoutURL = "http://10.0.2.2:5000/logout/";


    static String webSocketServerURL = "ws://10.0.2.2:8080/?token=";


    public ConnectionManager() { }

    public static void setCurrentUser(String user) {
        currentUser = user;
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    //=====================================================================================
    public interface RegisterListener {
        void onSuccess();
        void onFailure();
    }
    public static void register(@NonNull OkHttpClient client, String username, String password, RegisterListener registerListener) {

        //=======================================================
        MediaType JSON = MediaType.get("application/json");

        // Build JSON dynamically
        JsonObject json = new JsonObject();
        json.addProperty("Email", username);
        json.addProperty("Password", password);

        String jsonString = new Gson().toJson(json);                 // Convert to string
        RequestBody body = RequestBody.create(jsonString, JSON);     // Create request body
        //=======================================================


        Request.Builder builder = new Request.Builder();
        builder.url(registrationURL);
        builder.post(body);

        Request request = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Network Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    registerListener.onSuccess();
                } else {
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    registerListener.onFailure();
                }
                response.close();
            }
        });
    }



    //=====================================================================================
    public interface LoginListener {
        void onSuccess();
        void onFailure();
    }
    public static void login(@NonNull OkHttpClient client, String username, String password, LoginListener loginListener) {

        //=======================================================
        MediaType JSON = MediaType.get("application/json");

        // Build JSON dynamically
        JsonObject json = new JsonObject();
        json.addProperty("Email", username);
        json.addProperty("Password", password);

        String jsonString = new Gson().toJson(json);                    // Convert to string
        RequestBody body = RequestBody.create(jsonString, JSON);        // Create request body
        //=======================================================

        Request.Builder builder = new Request.Builder();
        builder.url(loginURL);
        builder.post(body);

        Request request = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Network Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonObject js = JsonParser.parseString(responseBody).getAsJsonObject();
                    currentUser = username;
                    sessionID = js.get("sessionId").getAsString();
                    loginListener.onSuccess();
                } else {
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    loginListener.onFailure();
                }
                response.close();
            }
        });
    }



    //=====================================================================================
    public interface LogoutListener {
        void onSuccess();
        void onFailure();
    }
    public static void logout(@NonNull OkHttpClient client, LogoutListener logoutListener) {

        String token = "Bearer " + sessionID;

        RequestBody body = RequestBody.create("", null);

        Request.Builder builder = new Request.Builder();
        builder.url(logoutURL);
        builder.post(body);
        builder.addHeader("Authorization", token);

        Request request = builder.build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Network Failure");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    logoutListener.onSuccess();
                } else {
                    String responseBody = response.body().string();
                    System.out.println(responseBody);
                    logoutListener.onFailure();
                }
                response.close();
            }
        });
    }

}
