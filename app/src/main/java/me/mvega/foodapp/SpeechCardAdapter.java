package me.mvega.foodapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class SpeechCardAdapter extends FragmentPagerAdapter {
    private final ArrayList<String> steps;
    private final ArrayList<String> components;

    public SpeechCardAdapter(FragmentManager fragmentManager, ArrayList<String> steps, ArrayList<String> components) {
        super(fragmentManager);
        this.steps = steps;
        this.components = components;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return steps.size();
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int step) {
        String currStep = steps.get(step);
        if (step > 0) {
            String ingredients = matchesIngredient(currStep);
            return SpeechCardFragmentPlain.newInstance(currStep, step, ingredients);
        } else {
            return SpeechCardFragment.newInstance(currStep);
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }

    private String matchesIngredient(String currStep) {
        String[] stepSplit = currStep.toLowerCase().replaceAll("[,./]", "").split(" ");
        StringBuilder matchingIngredients = new StringBuilder();
        String[] filter = {"and", "or", "from", "tbsp", "TB", "cup", "into", "a", "to", "c", "the", "tsp", "teaspoon", "tablespoon", "of"};
        List<String> filterList = Arrays.asList(filter);

        for (String component : components) {
            String[] componentSplit = component.toLowerCase().replaceAll("[/,.0-9]", "").split(" ");
            for (String word : stepSplit) {
                List<String> componentList = Arrays.asList(componentSplit);
                if ((componentList.contains(word) || componentList.contains(word + "s")) && !matchingIngredients.toString().contains(component) && !filterList.contains(word)) {
                    matchingIngredients.append(component).append("\n");
                }
            }
        }
        return matchingIngredients.toString();
    }

}
