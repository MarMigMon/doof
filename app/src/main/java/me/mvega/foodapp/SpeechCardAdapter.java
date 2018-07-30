package me.mvega.foodapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class SpeechCardAdapter extends FragmentPagerAdapter {
    ArrayList<String> steps;

    public SpeechCardAdapter(FragmentManager fragmentManager, ArrayList<String> steps) {
        super(fragmentManager);
        this.steps = steps;
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
            return SpeechCardFragmentPlain.newInstance(currStep, step);
        } else {
            return SpeechCardFragment.newInstance(currStep);
        }
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }

}
