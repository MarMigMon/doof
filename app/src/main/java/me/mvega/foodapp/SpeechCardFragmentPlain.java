package me.mvega.foodapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpeechCardFragmentPlain extends Fragment {

    private static final String KEY_STEP = "step";
    private static final String KEY_STEP_COUNT = "step count";
    private static final String KEY_INGREDIENTS = "ingredients";

    @BindView(R.id.tvInstructions) TextView tvInstructions;
    @BindView(R.id.tvStepCount) TextView tvStepCount;
    @BindView(R.id.tvStepLabel) TextView tvStepLabel;
    @BindView(R.id.tvIngredients) TextView tvIngredients;

    public static SpeechCardFragmentPlain newInstance(String step, int stepCount, String ingredients) {
        Bundle args = new Bundle();
        args.putString(KEY_STEP, step);
        args.putInt(KEY_STEP_COUNT, stepCount);
        args.putString(KEY_INGREDIENTS, ingredients);
        SpeechCardFragmentPlain fragment = new SpeechCardFragmentPlain();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_speech_card_plain, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Integer stepCount = getArguments().getInt(KEY_STEP_COUNT);
        String currStep = getArguments().getString(KEY_STEP, "");
        String ingredients = getArguments().getString(KEY_INGREDIENTS, "");

        tvInstructions.setText(currStep);
        tvStepCount.setText(stepCount.toString());
        tvIngredients.setText(ingredients);
    }
}
