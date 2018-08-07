package me.mvega.foodapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    @BindView(R.id.ivReplay)
    ImageButton ibReplay;

    SpeechCardFragmentPlain.SpeechFragmentCommunication listenerFragment;

    public interface SpeechFragmentCommunication {
        void replayStep();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SpeechCardFragmentPlain.SpeechFragmentCommunication) {
            listenerFragment = (SpeechCardFragmentPlain.SpeechFragmentCommunication) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement SpeechCardFragmentPlain.FragmentCommunication");
        }
    }

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
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

        ibReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listenerFragment.replayStep();
            }
        });
    }
}
