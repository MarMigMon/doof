package me.mvega.foodapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

public class FilterPopup implements FeedFragment.FilterCommunication {

    private final CheckBox[] types;
    private final CheckBox[] ratings;
    private final PopupWindow popup;
    private final SharedPreferences prefs;
    private final FeedFragment feedFragment;
    private static int lowestRating = 0;
    private static int maxPrepTime = Integer.MAX_VALUE;
    public static final String KEY_PREFERENCES = "private";
    private static final String KEY_MAX_PREP_TIME = "prep time";
    private static final String KEY_PREP_TIME_TEXT = "minutes";
    private String prepTimeText;
    private Context context;
    public static ArrayList<ParseQuery<Recipe>> ratingQueries;
    private Boolean typesChecked;

    // Filter Popup
    @BindView(R.id.cbAppetizer) CheckBox cbAppetizer;
    @BindView(R.id.cbSnack) CheckBox cbSnack;
    @BindView(R.id.cbEntree) CheckBox cbEntree;
    @BindView(R.id.cbDessert) CheckBox cbDessert;
    @BindView(R.id.cb5Stars) CheckBox cb5Stars;
    @BindView(R.id.cb4Stars) CheckBox cb4Stars;
    @BindView(R.id.cb3Stars) CheckBox cb3Stars;
    @BindView(R.id.cb2Stars) CheckBox cb2Stars;
    @BindView(R.id.btDone) Button btDone;
    @BindView(R.id.btClear) Button btClear;
    @BindView(R.id.etMaxPrepTime) EditText etMaxPrepTime;
    @BindView(R.id.spPrepTime) AppCompatSpinner spPrepTime;

    FilterPopup(View layout, PopupWindow popup, View button, FeedFragment f) {
        ButterKnife.bind(this, layout);

        this.popup = popup;
        context = button.getContext();
        prefs = context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE);

        types = new CheckBox[]{cbSnack, cbEntree, cbAppetizer, cbDessert};
        ratings = new CheckBox[]{cb5Stars, cb4Stars, cb3Stars, cb2Stars};
        feedFragment = f;

        // Set up popup window
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when loses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        popup.setBackgroundDrawable(null);
        // Show anchored to button
        popup.showAsDropDown(button);

        setCheckboxes(ratings);
        setCheckboxes(types);
        createPrepTimeSpinner();
        setMaxPrepTime();

        btDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterRecipes();
            }
        });

        btClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFilters();
            }
        });


    }
    ///////////////////////
    // Prep Time Spinner //
    ///////////////////////
    private void createPrepTimeSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        String[] prepTimeArray = feedFragment.getResources().getStringArray(R.array.prep_time_array);
        final ArrayAdapter<String> prepTimeAdapter = new ArrayAdapter<String>(context, R.layout.item_spinner_filter, prepTimeArray) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setPadding(0, 0, 0, 0);
                return view;
            }
        };
        // Specify the layout to use when the list of choices appears
        prepTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spPrepTime.setAdapter(prepTimeAdapter);
        // Listens for when the user makes a selection
        spPrepTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                prepTimeText = (String) adapterView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setMaxPrepTime() {
        Integer max = prefs.getInt(KEY_MAX_PREP_TIME, 0);
        spPrepTime.setSelection(prefs.getInt(KEY_PREP_TIME_TEXT, 0));
        if (max > 0) {
            etMaxPrepTime.setText(max.toString());
        }
    }

    private void setCheckboxes(CheckBox[] checkBoxes) {
        for (CheckBox item : checkBoxes) {
            boolean isChecked = prefs.getBoolean(item.getText().toString(), false);
            if (isChecked) {
                item.setChecked(true);
            }
        }
    }

    private void clearFilters() {
        popup.dismiss();

        for (CheckBox type : types) {
            toggleChecked(type);
        }

        for (CheckBox rating : ratings) {
            toggleChecked(rating);
        }

        etMaxPrepTime.setText("");
        feedFragment.loadTopRecipes();
    }

    private void toggleChecked(CheckBox checkbox) {
        if (checkbox.isChecked()) {
            checkbox.setSelected(false);
        }
    }

    private void saveChecked(String key, Boolean checked) {
        prefs.edit()
                .putBoolean(key, checked)
                .apply();
    }

    private void filterRecipes() {
        popup.dismiss();

        // Process max prep time entered
        String maxPrepTimeEntered = etMaxPrepTime.getText().toString().trim();
        Boolean filterTimeEntered = false;

        if (!maxPrepTimeEntered.equals("")) {
            int timeEntered = Integer.valueOf(maxPrepTimeEntered);
            filterTimeEntered = true;
            if (prepTimeText.equals("hours")) {
                maxPrepTime = timeEntered * 60;
            } else {
                maxPrepTime = timeEntered;
            }

            prefs.edit()
                    .putInt(KEY_MAX_PREP_TIME, timeEntered)
                    .putInt(KEY_PREP_TIME_TEXT, spPrepTime.getSelectedItemPosition())
                    .apply();
        } else {
            prefs.edit()
                    .remove(KEY_MAX_PREP_TIME)
                    .remove(KEY_PREP_TIME_TEXT)
                    .apply();
        }

        // Process checkboxes
        Boolean ratingsChecked = findLowestRating(ratings);
        ratingQueries = addTypeQueries(types);

        if (filterTimeEntered || ratingsChecked || typesChecked) {
            Recipe.Query filter = new Recipe.Query();
            filter.getTop().newestFirst().or(ratingQueries).include("user").findInBackground(new FindCallback<Recipe>() {
                @Override
                public void done(List<Recipe> newRecipes, ParseException e) {
                    lowestRating = 0;
                    maxPrepTime = Integer.MAX_VALUE;
                    FeedFragment.filtering = true;
                    feedFragment.resetAdapter(newRecipes);
                }
            });
        } else {
            feedFragment.loadTopRecipes();
        }
    }

    private ParseQuery executeFilterQueries(Recipe.Query query, int page) {
        lowestRating = 0;
        maxPrepTime = Integer.MAX_VALUE;
        return query.getTop().newestFirst().skipToPage(page).or(ratingQueries).include("user");
    }

    @Override
    public ParseQuery loadMoreRecipes(Recipe.Query query, int page) {
        return executeFilterQueries(query, page);
    }

    private Boolean findLowestRating(CheckBox[] checkBoxes) {
        Boolean checked = false;
        ArrayList<Integer> numbers = new ArrayList<>();
        for (CheckBox item : checkBoxes) {
            String name = item.getText().toString();
            if (item.isChecked()) {
                int value = Integer.valueOf(item.getText().toString().substring(0, 1));
                numbers.add(value);
                checked = true;
                saveChecked(name, true);
            } else {
                saveChecked(name, false);
            }
        }
        if (checked) {
            lowestRating = Collections.min(numbers);
        }
        return checked;
    }

    private ArrayList<ParseQuery<Recipe>> addTypeQueries(CheckBox[] checkBoxes) {
        Boolean checked = false;
        ArrayList<ParseQuery<Recipe>> queries = new ArrayList<>();

        for (CheckBox item : checkBoxes) {
            String name = item.getText().toString();
            if (item.isChecked()) {
                ParseQuery query = new ParseQuery("Recipe");
                query.whereGreaterThanOrEqualTo(Recipe.KEY_RATING, lowestRating).whereLessThanOrEqualTo(Recipe.KEY_PREP_TIME_MINUTES, maxPrepTime).whereEqualTo(Recipe.KEY_TYPE, item.getText().toString());
                queries.add(query);
                checked = true;
                saveChecked(name, true);
            } else {
                saveChecked(name, false);
            }
        }

        if (!checked) {
            ParseQuery query = new ParseQuery("Recipe");
            query.whereGreaterThanOrEqualTo(Recipe.KEY_RATING, lowestRating).whereLessThanOrEqualTo(Recipe.KEY_PREP_TIME_MINUTES, maxPrepTime);
            queries.add(query);
        }

        typesChecked = checked;
        return queries;
    }

}
