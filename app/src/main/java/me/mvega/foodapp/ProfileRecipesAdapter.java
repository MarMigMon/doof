package me.mvega.foodapp;

import android.content.Context;
import android.support.annotation.NonNull;
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
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import me.mvega.foodapp.model.Recipe;

public class ProfileRecipesAdapter extends RecyclerView.Adapter<ProfileRecipesAdapter.ViewHolder> {

    ParseUser user;
    private ProfileAdapterCommunication pCommunication;
    private final List<Recipe> recipes;
    private Context context;

    // pass in the Recipes array in the constructor
    public ProfileRecipesAdapter(List<Recipe> recipes) {
        this.recipes = recipes;
    }

    // communicates information from adapter to fragment
    public interface ProfileAdapterCommunication {
        void respond(Recipe recipe);

        void showDeleteDialog(Recipe recipe);
    }

    public void setProfileListener(ProfileAdapterCommunication profileListener) {
        this.pCommunication = profileListener;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View recipeView = inflater.inflate(R.layout.item_recipe_square, parent, false);
        return new ViewHolder(recipeView);
    }

    // bind the values on the position of the element
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get the data according to position
        Recipe recipe = recipes.get(position);

        // populate the view according to this data
        holder.tvName.setText(recipe.getName()); // TODO get recipe name
        holder.tvPrepTime.setText(recipe.getPrepTimeString()); // TODO get recipe prep time
        holder.tvViewCount.setText(recipe.getViews().toString());

        ParseFile picture = recipe.getImage(); // TODO get recipe image
        if (picture != null) {
            String imageUrl = picture.getUrl();
            Glide.with(context).load(imageUrl).into(holder.ivRecipe);
        } else holder.ivRecipe.setImageResource(R.drawable.image_placeholder);

        float rating = recipe.getRating().floatValue(); // TODO get recipe rating
        holder.ratingBar.setRating(rating);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final ImageView ivRecipe;
        final TextView tvName;
        final TextView tvPrepTime;
        final RatingBar ratingBar;
        final TextView tvViewCount;

        ViewHolder(View itemView) {
            super(itemView);

            // perform findViewById lookups
            ivRecipe = itemView.findViewById(R.id.ivRecipe);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrepTime = itemView.findViewById(R.id.tvPrepTime);
            ratingBar = itemView.findViewById(R.id.recipeRatingBar);
            tvViewCount = itemView.findViewById(R.id.tvViewCount);

            // add this as the itemView's OnClickListener
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        // when the user clicks on a grid, show details for the selected recipe
        @Override
        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the recipe at the position, this won't work if the class is static
                Recipe recipe = recipes.get(position);
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
                pCommunication.respond(recipe);
            }
        }

        // when the user clicks on a grid, show a dialog box for the selected recipe
        @Override
        public boolean onLongClick(View view) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the recipe at the position, this won't work if the class is static
                final Recipe recipe = recipes.get(position);
                pCommunication.showDeleteDialog(recipe);
                return true;
            }
            return false;
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
