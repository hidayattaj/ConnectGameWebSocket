package com.example.connectgamewebsocket;

import okhttp3.OkHttpClient;

public class OkHttpClientProvider {

    private static OkHttpClient client;

    public static OkHttpClient getClient() {
        if (client == null) { client = new OkHttpClient(); }
        return client;
    }

}
