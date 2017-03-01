package com.ottamotta.pavement.tools;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonProvider {

    private static final Gson gson = new GsonBuilder().enableComplexMapKeySerialization()
            .create();

    public static Gson provideGson() {
        return gson;
    }


}
