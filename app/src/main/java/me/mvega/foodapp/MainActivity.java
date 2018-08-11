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
import android.transition.Fade;
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

public class MainActivity extends AppCompatActivity implements FeedFragment.FragmentCommunication, ProfileFragment.ProfileFragmentCommunication, NotificationFragment.NotificationRecipeFragmentCommunication, NotificationFragment.NotificationUserFragmentCommunication, RecipeFragment.RecipeUserCommunication {

    private static final String KEY_FRAGMENT = "main";
    private static final String KEY_ADD_RECIPE = "addRecipe";
    @BindView(R.id.navigation_bar) BottomNavigationView bottomNavigationView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.mainFrame) FrameLayout mainFrame;

    public static ParseUser currentUser;

    private FragmentManager fragmentManager;
    public AddRecipeFragment addRecipeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        currentUser = getIntent().getParcelableExtra("user");

        if (savedInstanceState == null) {
            showFeed();
        } else {
            int topOfBackStack = fragmentManager.getBackStackEntryCount() - 1;
            Fragment f = fragmentManager.findFragmentByTag(fragmentManager.getBackStackEntryAt(topOfBackStack).getName());
            refreshFragment(f);
        }

        currentUser = ParseUser.getCurrentUser();

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
                                showProfile();
                                return true;

                            case R.id.tab_notification:
                                showNotification();
                                return true;

                            default:
                                return false;
                        }
                    }
                });

        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tab_feed:
                        refreshFragment(FeedFragment.newInstance());
                        break;

                    case R.id.tab_profile:
                        refreshFragment(ProfileFragment.newInstance(currentUser));
                        break;

                    case R.id.tab_notification:
                        refreshFragment(NotificationFragment.newInstance());
                        break;

                    default:
                        break;
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

    private void showFeed() {
        replaceFragment(FeedFragment.newInstance());
    }

    public void showAddRecipe() {
        addRecipeFragment = (AddRecipeFragment) fragmentManager.findFragmentByTag(KEY_ADD_RECIPE);
        if (addRecipeFragment == null) {
            addRecipeFragment = new AddRecipeFragment();
        }
        setFadeTransition(addRecipeFragment);
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the contents of the container with the new fragment and complete the changes added above
        fragmentTransaction.addToBackStack(KEY_ADD_RECIPE);
        fragmentTransaction.replace(R.id.frameLayout, addRecipeFragment, KEY_ADD_RECIPE).commit();
    }

    private void showNotification() {
        replaceFragment(NotificationFragment.newInstance());
    }

    private void showProfile() {
        replaceFragment(ProfileFragment.newInstance(currentUser));
    }

    public void replaceFragmentWithTransition(Fragment f) {
        setFadeTransition(f);
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the contents of the container with the new fragment and complete the changes added above
        fragmentTransaction.addToBackStack(KEY_FRAGMENT);
        fragmentTransaction.replace(R.id.frameLayout, f, KEY_FRAGMENT).commit();
    }
    private void refreshFragment(Fragment f) {
        setFadeTransition(f);
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace the contents of the container with the new fragment and complete the changes added above
        fragmentTransaction.replace(R.id.frameLayout, f).commit();
    }

    public void replaceFragment(Fragment f) {
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
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
            recipeFragment.setSharedElementReturnTransition(new Fade());
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
        replaceFragmentWithTransition(recipeFragment);
    }

    @Override
    public void respond (ParseObject notificationRecipe) {
        RecipeFragment recipeFragment = new RecipeFragment();
        recipeFragment.recipe = (Recipe) notificationRecipe;
        replaceFragmentWithTransition(recipeFragment);
    }

    @Override
    public void respond(ParseUser notificationUser) {
        ProfileFragment profileFragment = ProfileFragment.newInstance(notificationUser);
        replaceFragmentWithTransition(profileFragment);
    }


    private void setFadeTransition(Fragment f) {
        // Set transitions if minimum api requirements are met
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            f.setEnterTransition(new Fade());
            f.setExitTransition(new Fade());
        }
    }

    @Override
    public void startEdit(Recipe recipe) {
        replaceFragmentWithTransition(AddRecipeFragment.newInstance(recipe, true));
    }

    @Override
    public void editProfile() {
        replaceFragmentWithTransition(new EditProfileFragment());
    }
}
