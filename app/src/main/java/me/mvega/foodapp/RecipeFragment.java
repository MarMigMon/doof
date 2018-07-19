package me.mvega.foodapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

public class RecipeFragment extends Fragment {

    Recipe recipe;
    ImageView image;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.ratingBar) RatingBar ratingBar;
    @BindView(R.id.tvType) TextView tvType;
    @BindView(R.id.tvDescription) TextView tvDescription;
    @BindView(R.id.tvPrepTime) TextView tvPrepTime;
    @BindView(R.id.tvYield) TextView tvYield;
    @BindView(R.id.tvIngredients) TextView tvIngredients;
    @BindView(R.id.tvInstructions) TextView tvInstructions;
    @BindView(R.id.ivImage) ImageView ivImage;
    @BindView(R.id.btPlay) ImageButton btPlay;

    // The onCreateView method is called when Fragment should create its View object hierarchy either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_recipe, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        ButterKnife.bind(this, view);

        tvName.setText(recipe.getName());
        tvType.setText(recipe.getType());
        tvDescription.setText(recipe.getDescription());
        tvPrepTime.setText(recipe.getPrepTime());
        tvYield.setText(recipe.getYield());
        tvIngredients.setText(recipe.getIngredients());
        tvInstructions.setText(recipe.getInstructions());

        btPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginRecipe();
            }
        });

        ParseFile image = recipe.getImage();
        if (image != null) {
            String imageUrl = image.getUrl();
            Glide.with(getContext()).load(imageUrl).into(ivImage);
        } else {
            Glide.with(getContext()).load(R.drawable.image_placeholder).into(ivImage);
        }

        float rating = (float) (double) recipe.getRating();
        ratingBar.setRating(rating);
    }

    public void beginRecipe() {
        Intent i = new Intent(getContext(), SpeechActivity.class);
        i.putExtra("recipe", recipe);
        startActivity(i);
    }
}
