package me.mvega.foodapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpeechCardFragment extends Fragment {

    private static final String KEY_STEP = "step";
    @BindView(R.id.tvInstructions) TextView tvInstructions;
    @BindView(R.id.btStart) Button btStart;
    @BindView(R.id.ibReplay) ImageButton btReplay;
    @BindView(R.id.sbSpeed) SeekBar sbSpeed;

    private SpeechFragmentCommunication listenerFragment;

    public interface SpeechFragmentCommunication {
        void startRecipe();
        void adjustSpeed(float speed);
        void repeatStep();
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
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_speech_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        if (getArguments() != null) {
            String currStep = getArguments().getString(KEY_STEP, "");
            tvInstructions.setText(currStep);
        }

        HandlerThread speedThread = new HandlerThread("Speed");
        speedThread.start();
        final Handler speedHandler = new Handler(speedThread.getLooper());

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listenerFragment.startRecipe();
            }
        });
        btReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listenerFragment.repeatStep();
            }
        });

        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int i, boolean b) {
                speedHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        listenerFragment.adjustSpeed((float) (i * 1.0 / 40));
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
