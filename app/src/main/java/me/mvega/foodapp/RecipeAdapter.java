package me.mvega.foodapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private AdapterCommunication mCommunication;
    private final List<Recipe> recipes;
    private Context context;

    // communicates information from adapter to fragment
    public interface AdapterCommunication {
        void respond(Recipe recipe);

        void respond(Recipe recipe, ImageView image);
    }

    public void setListener(AdapterCommunication listener) {
        this.mCommunication = listener;
    }

    // pass in the Recipes array in the constructor
    public RecipeAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View recipeView = inflater.inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(recipeView);
    }

    // bind the values on the position of the element
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get the data according to position
        Recipe recipe = recipes.get(position);
//        ParseUser user = recipe.getUser();

        // populate the view according to this data
        holder.tvName.setText(recipe.getName()); // TODO get recipe name
        holder.tvType.setText(recipe.getType()); // TODO get recipe type
        holder.tvDescription.setText(recipe.getDescription()); // TODO get recipe description
        holder.tvPrepTime.setText(recipe.getPrepTimeString()); // TODO get recipe prep time
        holder.tvViewCount.setText(recipe.getViews().toString());

        ParseFile picture = recipe.getImage(); // TODO get recipe image
        if (picture != null) {
            String imageUrl = picture.getUrl();
            Glide.with(context).load(imageUrl).into(holder.ivRecipe);
        } else holder.ivRecipe.setImageResource(R.drawable.image_placeholder);

        if (recipe.getRating() != null) {
            holder.ratingBar.setRating(recipe.getRating().floatValue());
        } else {
            holder.ratingBar.setRating(0);
        }
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.cvRecipeCard) CardView cvRecipeCard;
        @BindView(R.id.ivRecipe) ImageView ivRecipe;
        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.tvType) TextView tvType;
        @BindView(R.id.tvDescription) TextView tvDescription;
        @BindView(R.id.tvPrepTime) TextView tvPrepTime;
        @BindView(R.id.recipeRatingBar) RatingBar ratingBar;
        @BindView(R.id.tvViewCount) TextView tvViewCount;

        ViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            // add this as the itemView's OnClickListener
            itemView.setOnClickListener(this);

        }

        // when the user clicks on a row, show details for the selected recipe
        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the recipe at the position, this won't work if the class is static
                final Recipe recipe = recipes.get(position);
                // update view count when recipe is clicked
                recipe.put("views", recipe.getViews() + 1);
                recipe.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("Recipe", "Saved");
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
                mCommunication.respond(recipe, ivRecipe);
            }
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        recipes.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Recipe> list) {
        recipes.addAll(list);
        notifyDataSetChanged();
    }
}
