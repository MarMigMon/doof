package me.mvega.foodapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.CountCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

import static me.mvega.foodapp.MainActivity.currentUser;

public class ProfileFragment extends Fragment implements YourRecipesFragment.YourRecipesFragmentCommunication {

    private ProfileFragmentCommunication yourRecipesListenerFragment;
    private Context context;
    ParseUser user;
    @BindView(R.id.ivProfile)
    ImageView ivProfile;
    @BindView(R.id.tvUsername)
    TextView tvUsername;
    @BindView(R.id.tvContributed)
    TextView tvContributed;
    @BindView(R.id.tvCompleted)
    TextView tvCompleted;
    @BindView(R.id.tvReviewed)
    TextView tvReviewed;
    @BindView(R.id.profileTabs)
    TabLayout tabLayout;
    @BindView(R.id.tvDescription)
    TextView tvDescription;
    @BindView(R.id.btEditProfile) TextView btEditProfile;

    private static int page;

    public interface ProfileFragmentCommunication {
        void respond(Recipe recipe);
        void editProfile();
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
        return inflater.inflate(R.layout.fragment_profile, parent, false);
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof ProfileFragmentCommunication) {
            yourRecipesListenerFragment = (ProfileFragmentCommunication) context;
        }
    }

    public static ProfileFragment newInstance(ParseUser user) {
        Bundle args = new Bundle();
        args.putParcelable("user", user);
        args.putInt("page", page);
        ProfileFragment fragment = new ProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        user = getArguments().getParcelable("user");
        // Prevents app crashing when switching orientations
        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable("user");
            page = savedInstanceState.getInt("page");
        } else {
            user = getArguments().getParcelable("user");
            page = 0;
        }

        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    // gets user's name
                    String userName = user.get("Name").toString();
                    setUserName(userName);
                    setUserDescription();
                    setUserContributions();
                    setUserCompleted();
                    setUserReviewed();
                    setProfileImage();
                    setTabs(userName);
                } else {
                    e.printStackTrace();
                }
            }
        });

        btEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yourRecipesListenerFragment.editProfile();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("user", user);
        outState.putInt("page", page);
    }

    private void setUserContributions() {
        final Recipe.Query recipeQuery = new Recipe.Query();
        recipeQuery.fromUser(user).withUser().include("recipesRated");
        recipeQuery.countInBackground(new CountCallback() {
            @Override
            public void done(int count, ParseException e) {
                if (e == null) {
                    tvContributed.setText(Integer.toString(count));
                } else {
                    e.printStackTrace();
                    tvContributed.setText("0");
                }
            }
        });
    }

    private void setUserReviewed() {
        HashMap<String, Number> recipesRated = (HashMap<String, Number>) user.get("recipesRated");
        if (recipesRated == null) {
            tvReviewed.setText("0");
        } else {
            tvReviewed.setText(Integer.toString(recipesRated.size()));
        }
    }

    private void setUserCompleted() {
        ArrayList<Recipe> recipesCompleted = (ArrayList<Recipe>) user.get("recipesCompleted");
        if (recipesCompleted != null) {
            tvCompleted.setText(((Integer) recipesCompleted.size()).toString());
        } else {
            tvCompleted.setText("0");
        }

    }

    private void setUserName(String userName) {
        tvUsername.setText(userName);
    }

    private void setUserDescription() {
        String userDescription = (String) user.get("description");
        if (userDescription != null) {
            tvDescription.setText(userDescription);
        } else {
            tvDescription.setText("Hello there!");
        }
    }

    private void setProfileImage() {
        ParseFile profileImage = user.getParseFile("image");
        if (profileImage != null) {
            String imageUrl = profileImage.getUrl();
            Glide.with(context).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(ivProfile);
        } else
            Glide.with(context).load(R.drawable.image_placeholder).apply(RequestOptions.circleCropTransform()).into(ivProfile);
    }

    private void setTabs(String userName) {
        showYourRecipes(); // Automatically selects Your Recipes tab to start profile screen

        // get first part of user's name
        String[] nameSplit = userName.split(" ");

        final TabLayout.Tab yourRecipes = tabLayout.newTab();
        if (user.getObjectId().equals(currentUser.getObjectId())) {
            yourRecipes.setText("Your Recipes");
        } else {
            yourRecipes.setText(nameSplit[0] + "'s recipes");
        }
        final TabLayout.Tab favorites = tabLayout.newTab().setText("Favorites");
        final TabLayout.Tab completed = tabLayout.newTab().setText("Completed");
        tabLayout.addTab(yourRecipes, 0, true);
        tabLayout.addTab(favorites, 1, false);
        tabLayout.addTab(completed, 2, false);
        // select tab
        tabLayout.getTabAt(page).select();

        // handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.equals(yourRecipes)) {
                    showYourRecipes();
                    page = 0;
                } else if (tab.equals(favorites)) {
                    showFavorites();
                    page = 1;
                } else if (tab.equals(completed)) {
                    showCompleted();
                    page = 2;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }
        });
    }

    private void showYourRecipes() {
        ParseUser thisUser = user;
        YourRecipesFragment thisUserRecipes = new YourRecipesFragment();
        thisUserRecipes.user = thisUser;
        replaceFragment(thisUserRecipes);
    }

    public void respond(Recipe recipe) {
        yourRecipesListenerFragment.respond(recipe);
    }

    private void showFavorites() {
        ParseUser thisUser = user;
        FavoritesFragment thisUserFavorites = new FavoritesFragment();
        thisUserFavorites.user = thisUser;
        replaceFragment(thisUserFavorites);
    }

    private void showCompleted() {
        ParseUser thisUser = user;
        RecipesCompletedFragment thisUserCompleted = new RecipesCompletedFragment();
        thisUserCompleted.user = thisUser;
        replaceFragment(thisUserCompleted);
    }

    private void replaceFragment(Fragment f) {
        final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.userRecipes, f).commit();

    }
}
