package me.mvega.foodapp;

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
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

public class FilterPopup {

    CheckBox[] types;
    CheckBox[] ratings;
    PopupWindow popup;
    List<ParseQuery <Recipe>> finalQueries;
    int lowestRating = 0;

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

        types = new CheckBox[] {cbSnack, cbEntree, cbAppetizer, cbDessert};
        ratings = new CheckBox[] {cb5Stars, cb4Stars, cb3Stars, cb2Stars};
        finalQueries = new ArrayList<>();

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

    private void clearFilters() {
        popup.dismiss();

        for (CheckBox type : types) {
            toggleChecked(type);
        }

        for (CheckBox rating : ratings) {
            toggleChecked(rating);
        }

        etMaxPrepTime.setText("");
        FeedFragment.loadTopRecipes();
    }

    private void toggleChecked(CheckBox checkbox) {
        if (checkbox.isChecked()) {
            checkbox.setSelected(false);
        }
    }

    private void filterRecipes() {
        popup.dismiss();
        Recipe.Query filter = new Recipe.Query();

        // Process checkboxes
        filter.findLowestRating(ratings);
        ArrayList<ParseQuery<Recipe>> ratingQueries = filter.addTypeQueries(types);

        // Process max prep time entered
        String maxPrepTimeEntered = etMaxPrepTime.getText().toString().trim();

        // Not implemented
        if (!maxPrepTimeEntered.equals("")) {
            filter = filter.containsQuery(Recipe.KEY_PREP_TIME, maxPrepTimeEntered);
        }

        if (!ratingQueries.isEmpty()) {
            filter.or(ratingQueries).findInBackground(new FindCallback<Recipe>() {
                @Override
                public void done(List<Recipe> newRecipes, ParseException e) {
                    Recipe.lowestRating = 0;
                    FeedFragment.resetAdapter(newRecipes, e);
                }
            });
        }
    }




}
