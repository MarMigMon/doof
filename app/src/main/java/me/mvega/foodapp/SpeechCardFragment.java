package me.mvega.foodapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpeechCardFragment extends Fragment {

    private static final String KEY_STEP = "step";
    @BindView(R.id.tvInstructions) TextView tvInstructions;
    @BindView(R.id.btStart) Button btStart;

    private SpeechFragmentCommunication listenerFragment;

    public interface SpeechFragmentCommunication {
        void startRecipe();
        void finishRecipe();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SpeechFragmentCommunication) {
            listenerFragment = (SpeechFragmentCommunication) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement SpeechCardFragment.FragmentCommunication");
        }
    }

    public static SpeechCardFragment newInstance(String step) {
        Bundle args = new Bundle();
        args.putString(KEY_STEP, step);
        SpeechCardFragment fragment = new SpeechCardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_speech_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        String currStep = getArguments().getString(KEY_STEP, "");
        tvInstructions.setText(currStep);


        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listenerFragment.startRecipe();
            }
        });

    }
}
