package me.mvega.foodapp;

import android.app.Application;

import com.parse.Parse;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final Parse.Configuration configuration = new Parse.Configuration.Builder(this)
                .applicationId("fbufood")
                .clientKey("marmigmon")
                .server("http://fbufood.herokuapp.com/parse")
                .build();

        Parse.initialize(configuration);
    }
}
