package me.mvega.foodapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseFile;
import com.parse.ParseUser;

public class ProfileFragment extends Fragment {
    ParseUser user = ParseUser.getCurrentUser();

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_profile, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);

        ImageButton ivProfile = view.findViewById(R.id.ivProfile);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvContributed = view.findViewById(R.id.tvContributed);
        TextView tvCompleted = view.findViewById(R.id.tvCompleted);
        TextView tvReviewed = view.findViewById(R.id.tvReviewed);

        tvUsername.setText(user.getUsername());
        tvContributed.setText("0"); // TODO get user's # of contributed recipes
        tvCompleted.setText("0"); // TODO get user's # of completed recipes
        tvReviewed.setText("0"); // TODO get user's # of reviewed recipes

        ParseFile profileImage = user.getParseFile("image");
        if (profileImage != null) {
            String imageUrl = profileImage.getUrl();
            Glide.with(getContext()).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(ivProfile);
        } else Glide.with(getContext()).load(R.drawable.image_placeholder).apply(RequestOptions.circleCropTransform()).into(ivProfile);


        final TabLayout tabLayout = view.findViewById(R.id.profileTabs);
        final TabLayout.Tab yourRecipes = new TabLayout.Tab();
        final TabLayout.Tab favorites = new TabLayout.Tab();
        tabLayout.addTab(yourRecipes, 0, true);
        tabLayout.addTab(favorites, 1, false);

        // handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.equals(yourRecipes)) {
                    showYourRecipes();
                } else if (tab.equals(favorites)){
                    showFavorites();
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

    public void showYourRecipes() {
        replaceFragment(YourRecipesFragment.newInstance());
    }

    public void showFavorites() {
        replaceFragment(FavoritesFragment.newInstance());
    }

    public static ProfileFragment newInstance() {
        ProfileFragment fragmentProfile = new ProfileFragment();
        fragmentProfile.setArguments(new Bundle());
        return fragmentProfile;
    }

    public void replaceFragment(Fragment f) {
        // Begin the transaction
        final FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        // Replace the contents of the container with the new fragment and complete the changes added above
        fragmentTransaction.replace(R.id.userRecipes, f).commit();
    }
}
