package me.mvega.foodapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

public class MainActivity extends AppCompatActivity implements FeedFragment.FragmentCommunication, ProfileFragment.ProfileFragmentCommunication {

    @BindView(R.id.navigation_bar) BottomNavigationView bottomNavigationView;

    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Starts activity with feed fragment displayed
        showFeed();

        ButterKnife.bind(this);

//        // Sets the Toolbar to act as the ActionBar for this Activity window.
//        // Make sure the toolbar exists in the activity and is not null
//        setSupportActionBar(toolbar);

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

                            case R.id.tab_profile:
                                showProfile();
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

    public void onLogoutAction(MenuItem mi) {
        ParseUser.logOut();
        currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    public void showFeed() {
        replaceFragment(FeedFragment.newInstance());
    }

    public void showAddRecipe() {
        replaceFragment(new AddRecipeFragment());
    }

    public void showProfile() {
        replaceFragment(ProfileFragment.newInstance());
    }

    public void replaceFragment(Fragment f) {
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment and complete the changes added above
        fragmentTransaction.addToBackStack("main");
        fragmentTransaction.replace(R.id.frameLayout, f).commit();
    }

    @Override
    public void respond(Recipe recipe, ImageView image) {
        RecipeFragment recipeFragment = new RecipeFragment();

        // Set transitions if minimum api requirements are met
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recipeFragment.setSharedElementEnterTransition(new Fade());
            recipeFragment.setEnterTransition(new Fade());
            recipeFragment.setExitTransition(new Fade());
            recipeFragment.setSharedElementReturnTransition(new Explode());
        }

        recipeFragment.recipe = recipe;
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment and complete the changes added above
        fragmentTransaction.addToBackStack("main");
        fragmentTransaction.addSharedElement(image, "image");
        fragmentTransaction.replace(R.id.frameLayout, recipeFragment).commit();

    }

    @Override
    public void respond(Recipe recipe) {
        RecipeFragment recipeFragment = new RecipeFragment();
        recipeFragment.recipe = recipe;
        replaceFragment(recipeFragment);
    }
}
