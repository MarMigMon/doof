package me.mvega.foodapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import me.mvega.foodapp.model.Recipe;

public class RecipeFragment extends Fragment {
    Recipe recipe;
    TextView tvName;
    RatingBar ratingBar;
    TextView tvType;
    TextView tvDescription;
    TextView tvPrepTime;
    TextView tvYield;
    TextView tvIngredients;
    TextView tvInstructions;

    // The onCreateView method is called when Fragment should create its View object hierarchy either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Defines the xml file for the fragment
        View view = inflater.inflate(R.layout.fragment_recipe, parent, false);
        return view;
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        tvName = view.findViewById(R.id.tvName);
        ratingBar = view.findViewById(R.id.ratingBar);
        tvType = view.findViewById(R.id.tvType);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvPrepTime = view.findViewById(R.id.tvPrepTime);
        tvYield = view.findViewById(R.id.tvYield);
        tvIngredients = view.findViewById(R.id.tvIngredients);
        tvInstructions = view.findViewById(R.id.tvInstructions);

        tvName.setText(recipe.getName());
        tvType.setText(recipe.getType());
        tvDescription.setText(recipe.getDescription());
        tvPrepTime.setText(recipe.getPrepTime());
        tvYield.setText(recipe.getYield());
        tvIngredients.setText(recipe.getIngredients());
        tvInstructions.setText(recipe.getInstructions());
    }
}
