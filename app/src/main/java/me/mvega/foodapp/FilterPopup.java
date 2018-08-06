package me.mvega.foodapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.WindowManager;
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

public class FilterPopup {

    private final CheckBox[] types;
    private final CheckBox[] ratings;
    private final PopupWindow popup;
    private final SharedPreferences prefs;
    private final FeedFragment feedFragment;
    private static int lowestRating = 0;
    private static int maxPrepTime = Integer.MAX_VALUE;
    public static final String KEY_PREFERENCES = "private";
    private static final String KEY_MAX_PREP_TIME = "prep time";

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

    FilterPopup(View layout, PopupWindow popup, View button) {
        ButterKnife.bind(this, layout);

        this.popup = popup;
        Context context = button.getContext();
        prefs = context.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE);

        types = new CheckBox[]{cbSnack, cbEntree, cbAppetizer, cbDessert};
        ratings = new CheckBox[]{cb5Stars, cb4Stars, cb3Stars, cb2Stars};
        feedFragment = new FeedFragment();

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

    private void setMaxPrepTime() {
        Integer max = prefs.getInt(KEY_MAX_PREP_TIME, 0);
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
        Recipe.Query filter = new Recipe.Query();

        // Process checkboxes
        findLowestRating(ratings);
        ArrayList<ParseQuery<Recipe>> ratingQueries = addTypeQueries(types);

        // Process max prep time entered
        String maxPrepTimeEntered = etMaxPrepTime.getText().toString().trim();

        if (!maxPrepTimeEntered.equals("")) {
            maxPrepTime = Integer.valueOf(maxPrepTimeEntered);
            prefs.edit()
                    .putInt(KEY_MAX_PREP_TIME, maxPrepTime)
                    .apply();
        }

        if (!ratingQueries.isEmpty()) {
            filter.getTop().withUser().newestFirst().or(ratingQueries).findInBackground(new FindCallback<Recipe>() {
                @Override
                public void done(List<Recipe> newRecipes, ParseException e) {
                    lowestRating = 0;
                    maxPrepTime = Integer.MAX_VALUE;
                    feedFragment.resetAdapter(newRecipes);
                }
            });
        }
    }

    private void findLowestRating(CheckBox[] checkBoxes) {
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
    }

    private ArrayList<ParseQuery<Recipe>> addTypeQueries(CheckBox[] checkBoxes) {
        Boolean checked = false;
        ArrayList<ParseQuery<Recipe>> queries = new ArrayList<>();

        for (CheckBox item : checkBoxes) {
            String name = item.getText().toString();
            if (item.isChecked()) {
                ParseQuery query = new ParseQuery("Recipe");
                query.whereGreaterThanOrEqualTo(Recipe.KEY_RATING, lowestRating).whereLessThanOrEqualTo(Recipe.KEY_PREP_TIME, maxPrepTime).whereEqualTo(Recipe.KEY_TYPE, item.getText().toString());
                queries.add(query);
                checked = true;
                saveChecked(name, true);
            } else {
                saveChecked(name, false);
            }
        }

        if (!checked) {
            ParseQuery query = new ParseQuery("Recipe");
            query.whereGreaterThanOrEqualTo(Recipe.KEY_RATING, lowestRating).whereLessThanOrEqualTo(Recipe.KEY_PREP_TIME, maxPrepTime);
            queries.add(query);
        }

        return queries;
    }

}
