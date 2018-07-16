package me.mvega.foodapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import java.util.List;

import me.mvega.foodapp.model.Recipe;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private List<Recipe> recipes;
    Context context;

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
        holder.tvDescription.setText(recipe.getDescription()); // TODO get recipe description

        ParseFile picture = recipe.getImage(); // TODO get recipe image
        if (picture != null) {
            String imageUrl = picture.getUrl();
            Glide.with(context).load(imageUrl).into(holder.ivRecipe);
        } else holder.ivRecipe.setImageResource(R.drawable.image_placeholder);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView ivRecipe;
        public TextView tvName;
        public TextView tvDescription;
        public TextView tvPrepTime;

        public ViewHolder(View itemView) {
            super(itemView);

            // perform findViewById lookups
            ivRecipe = (ImageView) itemView.findViewById(R.id.ivRecipe);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvDescription = (TextView) itemView.findViewById(R.id.tvDescription);
            tvPrepTime = (TextView) itemView.findViewById(R.id.tvPrepTime);

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
                Recipe recipe = recipes.get(position); 
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
