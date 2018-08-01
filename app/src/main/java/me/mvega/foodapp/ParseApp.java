package me.mvega.foodapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import me.mvega.foodapp.model.Notification;
import me.mvega.foodapp.model.Recipe;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class ParseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Use for troubleshooting -- remove this line for production
        Parse.setLogLevel(Parse.LOG_LEVEL_DEBUG);

        // Use for monitoring Parse OkHttp traffic
        // Can be Level.BASIC, Level.HEADERS, or Level.BODY
        // See http://square.github.io/okhttp/3.x/logging-interceptor/ to see the options.
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.networkInterceptors().add(httpLoggingInterceptor);

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
