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
    @BindView(R.id.tvInstructions) TextView tvInstructions;

    public static SpeechCardFragmentPlain newInstance(String step, int stepCount) {
        Bundle args = new Bundle();
        args.putString(KEY_STEP, step);
        args.putInt(KEY_STEP_COUNT, stepCount);
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

        String currStep = getArguments().getString(KEY_STEP, "");
        tvInstructions.setText(currStep);
    }
}
