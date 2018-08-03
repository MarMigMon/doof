package me.mvega.foodapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

public class FeedFragment extends Fragment {

    private RecipeAdapter recipeAdapter;
    private SwipeRefreshLayout swipeContainer;
    private static EndlessRecyclerViewScrollListener scrollListener;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<String> recipeNames;
    private Context context;

    @BindView(R.id.rvRecipes)
    RecyclerView rvRecipes;
    @BindView(R.id.search_bar)
    Toolbar toolbar;
    @BindView(R.id.search)
    AutoCompleteTextView search;
    @BindView(R.id.search_btn)
    Button btSearch;
    @BindView(R.id.filter_btn)
    Button btFilter;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;

    TextView tvViewCount;

    private FragmentCommunication listenerFragment;

    // implement interface
    public interface FragmentCommunication {
        void respond(Recipe recipe, ImageView image);
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_feed, parent, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentCommunication) {
            listenerFragment = (FragmentCommunication) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement FeedFragment.FragmentCommunication");
        }
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        swipeContainer = view.findViewById(R.id.swipeContainer);
        context = view.getContext();

        pbLoading.setVisibility(ProgressBar.VISIBLE);

        initializeAdapter();
        initializeEndlessScrolling();
        setSwipeContainer();
        loadTopRecipes();

        btFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFilterPopup(view);
            }
        });
    }

    private void initializeEndlessScrolling() {
        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view, String query) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page, query);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvRecipes.addOnScrollListener(scrollListener);
    }

    private void loadNextDataFromApi(int page, String query) {
        Recipe.Query recipeQuery = new Recipe.Query();
        if (query.equals("")) {
            recipeQuery.newestFirst().getTop().withUser().skipToPage(page);
        } else {
            recipeQuery.newestFirst().getTop().withUser().containsQuery(Recipe.KEY_NAME, query).skipToPage(page);
        }

        recipeQuery.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> newRecipes, ParseException e) {
                Log.i("Infinite scrolling", "Loaded " + newRecipes.size());
                recipeAdapter.addAll(newRecipes);
            }
        });
    }

    private void initializeAdapter() {
        // initialize the ArrayList (data source)
        ArrayList<Recipe> recipes = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(recipes);

        // Layout Manager
        linearLayoutManager = new LinearLayoutManager(getContext());
        rvRecipes.setLayoutManager(linearLayoutManager);

        // Set adapter
        rvRecipes.setAdapter(recipeAdapter);

        recipeAdapter.setListener(new RecipeAdapter.AdapterCommunication() {
            @Override
            public void respond(Recipe recipe, ImageView image) {
                listenerFragment.respond(recipe, image);
            }
        });
    }

    private void initializeSearch() {
        ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_dropdown, recipeNames);

        // Will start suggesting searches after one character is typed
        search.setThreshold(1);
        search.setAdapter(searchAdapter);

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String query = search.getText().toString();
                searchRecipes(query);
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String query = search.getText().toString().trim();
                if (!query.equals("")) {
                    searchRecipes(query);
                } else {
                    loadTopRecipes();
                }

            }
        });
    }

    // Display anchored popup menu based on view selected
    private void showFilterPopup(View v) {
        PopupWindow popup = new PopupWindow(getContext());
        View layout = getLayoutInflater().inflate(R.layout.popup_filter, null);

        new FilterPopup(layout, popup, v, this);
    }

    private void searchRecipes(String query) {
        Recipe.Query recipeQuery = new Recipe.Query();
        recipeQuery.getTop().withUser().newestFirst().containsQuery(Recipe.KEY_NAME, query);
        EndlessRecyclerViewScrollListener.query = query;
        recipeQuery.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> newRecipes, ParseException e) {
                if (newRecipes != null) {
                    resetAdapter(newRecipes, e);
                }
            }
        });
    }

    private void setSwipeContainer() {
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                loadTopRecipes();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public static FeedFragment newInstance() {
        FeedFragment fragmentFeed = new FeedFragment();
        fragmentFeed.setArguments(new Bundle());
        return fragmentFeed;
    }

    public void resetAdapter(List<Recipe> newRecipes, ParseException e) {
        if (e == null) {
            recipeAdapter.clear();
            // ...the data has come back, add new items to your adapter...
            recipeAdapter.addAll(newRecipes);
            scrollListener.resetState();
            swipeContainer.setRefreshing(false);
        } else {
            e.printStackTrace();
        }
    }

    public void loadTopRecipes() {
        clearPreferences();
        EndlessRecyclerViewScrollListener.query = "";

        Recipe.Query recipeQuery = new Recipe.Query();
        recipeQuery.getTop().withUser().newestFirst();
        recipeQuery.findInBackground(new FindCallback<Recipe>() {
            @Override
            public void done(List<Recipe> newRecipes, ParseException e) {
                recipeNames = new ArrayList<>();
                if (newRecipes != null) {
                    for (Recipe recipe : newRecipes) {
                        recipeNames.add(recipe.getName());
                    }
                    resetAdapter(newRecipes, e);
                    initializeSearch();
                    pbLoading.setVisibility(ProgressBar.INVISIBLE);
                }
            }
        });
    }

    private void clearPreferences() {
        SharedPreferences preferences = context.getSharedPreferences(FilterPopup.KEY_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }


}
