package me.mvega.foodapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import me.mvega.foodapp.model.Notification;
import me.mvega.foodapp.model.Recipe;

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

        ParseObject.registerSubclass(Recipe.class);
        ParseObject.registerSubclass(Notification.class);
    }
}
