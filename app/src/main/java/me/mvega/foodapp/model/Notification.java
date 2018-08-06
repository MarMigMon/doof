package me.mvega.foodapp.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Notification")
public class Notification extends ParseObject {
    private static final String KEY_ACTIVE_USER = "activeUser";
    private static final String KEY_RECIPE = "recipe";
    private static final String KEY_RECIPE_USER = "recipeUser";
    private static final String KEY_FAVORITE = "favorite";
    private static final String KEY_RATE = "rate";

    public ParseUser getActiveUser() {
        return getParseUser(KEY_ACTIVE_USER);
    }

    public void setActiveUser(ParseUser activeUser) {
        put(KEY_ACTIVE_USER, activeUser);
    }

    public ParseObject getRecipe() {
        return getParseObject(KEY_RECIPE);
    }

    public void setRecipe(ParseObject recipe) {
        put(KEY_RECIPE, recipe);
    }

    public ParseUser getRecipeUser() {
        return getParseUser(KEY_RECIPE_USER);
    }

    public void setRecipeUser(ParseUser recipeUser) {
        put(KEY_RECIPE_USER, recipeUser);
    }

    public Boolean getFavorite() {
        return getBoolean(KEY_FAVORITE);
    }

    public void setFavorite(Boolean favorite) {
        put(KEY_FAVORITE, favorite);
    }

    public Boolean getRate() {
        return getBoolean(KEY_RATE);
    }

    public void setRate(Boolean rate) {
        put(KEY_RATE, rate);
    }

    public static class Query extends ParseQuery<Notification> {
        public Query() {
            super(Notification.class);
        }

        public Query getTop() {
            orderByDescending("createdAt");
            return this;
        }

        public Query newestFirst() {
            orderByDescending("createdAt");
            return this;
        }

        public Query withUser() {
            include("user");
            return this;
        }

        public Query recipeUser(ParseUser user) {
            whereEqualTo(KEY_RECIPE_USER, user);
            return this;
        }

        public Query activeUser(ParseUser user) {
            whereEqualTo(KEY_ACTIVE_USER, user);
            return this;
        }
    }
}
