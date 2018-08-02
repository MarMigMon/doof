package me.mvega.foodapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

import static android.support.constraint.Constraints.TAG;
import static me.mvega.foodapp.MainActivity.currentUser;

public class YourRecipesFragment extends Fragment {

    private ProfileRecipesAdapter profileRecipesAdapter;
    private YourRecipesFragmentCommunication profileListenerFragment;

    @BindView(R.id.pbLoading)
    private ProgressBar pbLoading;
    @BindView(R.id.rvRecipes) RecyclerView rvRecipes;
    @BindView(R.id.swipeContainer)
    private SwipeRefreshLayout swipeContainer;
    ParseUser user;

    // implement interface
    public interface YourRecipesFragmentCommunication {
        void respond(Recipe recipe);
    }

//    public void setYourRecipeListener(YourRecipesFragmentCommunication yourRecipesListener) {
//        this.profileListenerFragment = (YourRecipesFragmentCommunication) yourRecipesListener;
//    }


    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            user = savedInstanceState.getParcelable("user");
        }

        onAttachToParentFragment(getParentFragment());
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.tab_profile, parent, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("user", user);
    }

    private void onAttachToParentFragment(Fragment childFragment) {
        try {
            profileListenerFragment = (YourRecipesFragmentCommunication) childFragment;

        } catch (ClassCastException e) {
            throw new ClassCastException(
                    childFragment.toString() + " must implement OnPlayerSelectionSetListener");
        }
    }


    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        pbLoading.setVisibility(ProgressBar.VISIBLE);

        // find the Recycler View
        RecyclerView rvRecipes = view.findViewById(R.id.rvRecipes);
        // initialize the ArrayList (data source)
        ArrayList<Recipe> recipes = new ArrayList<>();

        // construct the adapter from this data source
        profileRecipesAdapter = new ProfileRecipesAdapter(recipes);
        // RecyclerView setup (layout manager, use adapter)
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        rvRecipes.setLayoutManager(layoutManager);
        //set the adapter
        rvRecipes.setAdapter(profileRecipesAdapter);
        rvRecipes.addItemDecoration(new SpacesItemDecoration(32));

        profileRecipesAdapter.setProfileListener(new ProfileRecipesAdapter.ProfileAdapterCommunication() {
            @Override
            public void respond(Recipe recipe) {
                profileListenerFragment.respond(recipe);
            }

            @Override
            public void showDeleteDialog(final Recipe recipe) {
                // Create alert dialog
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                // Show error when userProfile is not the currentUser's profile
                if (user == currentUser) {
                    // Add cancel option and message
                    alertDialog.setCancelable(true);
                    alertDialog.setMessage(Html.fromHtml("Are you sure you want to delete <b>" + recipe.getName() + "</b>?"));

                    // Configure dialog button (OK)
                    alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    // Delete recipe
                                    recipe.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e == null) {
                                                loadYourRecipes();
                                                reduceContributed();
                                                dialog.dismiss();
                                            } else {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("Notification");
                                    query.whereEqualTo("recipe", recipe);
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        @Override
                                        public void done(List<ParseObject> notifications, ParseException e) {
                                            if (e == null) {
                                                for (ParseObject notification : notifications) {
                                                    notification.deleteInBackground();
                                                }
                                            } else {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            });

                    // Configure dialog button (CANCEL)
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    // Display the dialog
                    alertDialog.show();
                } else {
                    alertDialog.setCancelable(true);
                    alertDialog.setMessage(Html.fromHtml("You can't delete <b>" + recipe.getUser().get("Name") + "</b>'s recipe! "));
                    // Configure dialog button (OOPS)
                    alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OOPS!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                    // Display the dialog
                    alertDialog.show();
                }
            }
        });

        loadYourRecipes();

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                loadYourRecipes();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public static YourRecipesFragment newInstance() {
        YourRecipesFragment fragmentYourRecipes = new YourRecipesFragment();
        fragmentYourRecipes.setArguments(new Bundle());
        return fragmentYourRecipes;
    }

    private void loadYourRecipes() {
        final Recipe.Query recipeQuery = new Recipe.Query();
        recipeQuery.fromUser(user).withUser();

        recipeQuery.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> newRecipes, ParseException e) {
                if (e == null) {
                    // Remember to CLEAR OUT old items before appending in the new ones
                    profileRecipesAdapter.clear();
                    // ...the data has come back, add new items to your adapter...
                    profileRecipesAdapter.addAll(newRecipes);
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                    pbLoading.setVisibility(ProgressBar.INVISIBLE);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void reduceContributed() {
        TextView contributed = getParentFragment().getView().findViewById(R.id.tvContributed);
        contributed.setText(Integer.toString(Integer.parseInt(contributed.getText().toString()) - 1));
    }
}

