package me.mvega.foodapp.model;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

@ParseClassName("Recipe")
public class Recipe extends ParseObject {
    private static final String KEY_NAME = "recipeName";
    private static final String KEY_TYPE = "type";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_INGREDIENTS = "ingredients";
    private static final String KEY_INSTRUCTIONS = "instructions";
    private static final String KEY_YIELD = "yield";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_USER = "user";
    private static final String KEY_RATING = "rating";
    private static final String KEY_PREP_TIME = "prepTime";
    private static final String KEY_MEDIA = "media";

    public ParseFile getMedia() {
        return getParseFile(KEY_MEDIA);
    }
    public void setMedia(ParseFile media) {
        put(KEY_MEDIA, media);
    }

    public String getName() {
        return getString(KEY_NAME);
    }
    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getType() {
        return getString(KEY_TYPE);
    }
    public void setType(String type) {
        put(KEY_TYPE, type);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }
    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public String getIngredients() {
        return getString(KEY_INGREDIENTS);
    }
    public void setIngredients(String ingredients) {
        put(KEY_INGREDIENTS, ingredients);
    }

    public String getInstructions() {
        return getString(KEY_INSTRUCTIONS);
    }
    public void setInstructions(String instructions) {
        put(KEY_INSTRUCTIONS, instructions);
    }

    public String getYield() {
        return getString(KEY_YIELD);
    }
    public void setYield(String yield) {
        put(KEY_YIELD, yield);
    }

    public String getPrepTime() {
        return getString(KEY_PREP_TIME);
    }
    public void setPrepTime(String prepTime) {
        put(KEY_PREP_TIME, prepTime);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }
    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public Double getRating() {
        return getDouble(KEY_RATING);
    }
    public void setRating(Double rating) {
        put(KEY_RATING, rating);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }
    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public Date getTimestamp() {
        return getCreatedAt();
    }

    public static class Query extends ParseQuery<Recipe> {
        public Query() {
            super(Recipe.class);
        }

        public Query newestFirst() {
            orderByDescending("createdAt");
            return this;
        }

        public Query getTop() {
            setLimit(20);
            return this;
        }

        public Query withUser() {
            include("user");
            return this;
        }

        public Query containsQuery(String query) {
            whereFullText("recipeName", query);
            return this;
        }

        public Query fromUser(ParseUser user) {
            whereEqualTo(KEY_USER, user);
            return this;
        }
    }
}
