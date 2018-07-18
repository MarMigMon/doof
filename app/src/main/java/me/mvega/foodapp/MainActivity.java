package me.mvega.foodapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import me.mvega.foodapp.model.Recipe;

public class MainActivity extends AppCompatActivity {

    RecipeAdapter recipeAdapter;
    ArrayList<Recipe> recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Starts activity with feed fragment displayed
        showFeed();

        // Find the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.search_bar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);

        final BottomNavigationView bottomNavigationView = findViewById(R.id.navigation_bar);
        // handle navigation selection
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tab_feed:
                                showFeed();
                                return true;
                            case R.id.tab_add:
                                showAddRecipe();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    public void onSpeechAction(MenuItem mi) {
        startActivity(new Intent(MainActivity.this, SpeechActivity.class));
    }

    public void showFeed() {
        replaceFragment(FeedFragment.newInstance());
    }

    public void showAddRecipe() {
        replaceFragment(new AddRecipeFragment());
    }

    public void replaceFragment(Fragment f) {
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment and complete the changes added above
        fragmentTransaction.replace(R.id.frameLayout, f).commit();
    }
}
