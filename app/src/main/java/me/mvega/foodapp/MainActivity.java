package me.mvega.foodapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.parse.ParseObject;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

public class MainActivity extends AppCompatActivity implements FeedFragment.FragmentCommunication, ProfileFragment.ProfileFragmentCommunication, NotificationFragment.NotificationRecipeFragmentCommunication, NotificationFragment.NotificationUserFragmentCommunication, RecipeFragment.RecipeUserCommunication, AddRecipeFragment.NewRecipeCommunication {

    private static final String KEY_FRAGMENT = "main";
    @BindView(R.id.navigation_bar) BottomNavigationView bottomNavigationView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.mainFrame) FrameLayout mainFrame;


    public static ParseUser currentUser = ParseUser.getCurrentUser();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Starts activity with feed fragment displayed
        if (savedInstanceState == null) {
            showFeed();
        } else {
            Fragment f = getSupportFragmentManager().findFragmentByTag(KEY_FRAGMENT);
             replaceFragment(f);
        }

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
                                ProfileFragment profileFragment = new ProfileFragment();
                                profileFragment.user = currentUser;
                                replaceFragment(profileFragment);
                                return true;

                            case R.id.tab_notification:
                                showNotification();
                                return true;

                            default:
                                return false;
                        }
                    }
                });
        View shadow = new View(this);
        shadow.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                15));
        shadow.setBackground(getResources().getDrawable(R.drawable.dropshadow));
        mainFrame.addView(shadow);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return true;
    }

    public void onLogoutAction(MenuItem mi) {
        ParseUser.logOut();
        currentUser = null;
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    public void onEditProfileAction(MenuItem mi) {
        replaceFragment(new EditProfileFragment());
    }

    private void showFeed() {
        replaceFragment(FeedFragment.newInstance());
    }

    public void showAddRecipe() {
        Log.d("Add Recipe", "Runs on tab click");
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment addRecipeFragment = fragmentManager.findFragmentByTag("newRecipe");
        // if fragment doesn't exist yet, create one
        if (addRecipeFragment == null) {
            fragmentTransaction.addToBackStack("newRecipe");
            fragmentTransaction.add(R.id.frameLayout, new AddRecipeFragment(), "newRecipe").commit();
            Log.d("create fragment", "addRecipe == null");
        } else {
            fragmentTransaction.replace(R.id.frameLayout, addRecipeFragment, "newRecipe").commit();
            Log.d("replace fragment", "replaces fragment");
        }
//        replaceFragment(new AddRecipeFragment());
    }

    private void showNotification() {
        replaceFragment(NotificationFragment.newInstance());
    }

    public void replaceFragment(Fragment f) {
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment and complete the changes added above
        fragmentTransaction.addToBackStack(KEY_FRAGMENT);
        fragmentTransaction.replace(R.id.frameLayout, f, KEY_FRAGMENT).commit();
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
        fragmentTransaction.addToBackStack(KEY_FRAGMENT);
        fragmentTransaction.addSharedElement(image, "image");
        fragmentTransaction.replace(R.id.frameLayout, recipeFragment, KEY_FRAGMENT).commit();
    }

    @Override
    public void respond(Recipe recipe) {
        RecipeFragment recipeFragment = new RecipeFragment();
        recipeFragment.recipe = recipe;
        setFadeTransition(recipeFragment);
        replaceFragment(recipeFragment);
    }

    @Override
    public void respond (ParseObject notificationRecipe) {
        RecipeFragment recipeFragment = new RecipeFragment();
        setFadeTransition(recipeFragment);
        recipeFragment.recipe = (Recipe) notificationRecipe;
        replaceFragment(recipeFragment);
    }

    @Override
    public void respond(ParseUser notificationUser ) {
        ProfileFragment profileFragment = new ProfileFragment();
        setFadeTransition(profileFragment);
        profileFragment.user = notificationUser;
        replaceFragment(profileFragment);
    }

//    @Override
    public void respond(Fragment newRecipeFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment addRecipeFragment = fragmentManager.findFragmentByTag("newRecipe");
        Log.d("new recipe listener", "responsive listener");
        if (addRecipeFragment != null) {
            fragmentTransaction.remove(addRecipeFragment).commit();
            fragmentManager.popBackStack();
            Log.d("Fragment success", "replaced");
        }
        showAddRecipe();
//        Fragment addRecipeFragment = null;
//        frg = getFragmentManager().findFragmentByTag("Your_Fragment_TAG");
//        final FragmentTransaction ft = getFragmentManager().beginTransaction();
//        ft.detach(frg);
//        ft.attach(frg);
//        ft.commit();
//        replaceFragment(addRecipeFragment);
    }

    private void setFadeTransition(Fragment f) {
        // Set transitions if minimum api requirements are met
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            f.setEnterTransition(new Fade());
            f.setExitTransition(new Fade());
        }
    }
}
