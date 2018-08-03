package me.mvega.foodapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import me.mvega.foodapp.model.Recipe;
import nl.dionsegijn.konfetti.KonfettiView;
import nl.dionsegijn.konfetti.models.Shape;
import nl.dionsegijn.konfetti.models.Size;

import static me.mvega.foodapp.MainActivity.currentUser;

public class SpeechActivity extends AppCompatActivity implements
        RecognitionListener, SpeechCardFragment.SpeechFragmentCommunication, SpeechCardFragmentPlain.SpeechFragmentCommunication {

    private static final String TTS_SEARCH = "Text to speech";
    private static final String PLAYER_SEARCH = "Player";
    private static final String KEY_STEP_COUNT = "Step count";
    private static final String KEY_RESUMED_RECIPE = "Resumed";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private Recipe recipe;

    // Instructions
    private int stepCount = 0;
    private int totalSteps;
    private ArrayList<String> instructions;

    // Ingredients
    private String ingredients;

    // Recognizer and text to speech
    private TextToSpeech tts;
    private SpeechRecognizer recognizer;
    private Boolean initializedTts;

    // Handler
    HandlerThread speakThread;
    Handler speechHandler;

    // Player
    private ParseFile audioFile;
    private MediaPlayer player;
    private Boolean isPaused = false;

    private static Boolean startedRecipe = false;

    private ParseUser user;

    // Buttons
    @BindView(R.id.ivStop) ImageView ivStop;
    @BindView(R.id.ivPause) ImageView ivPause;
    @BindView(R.id.ivResume) ImageView ivResume;
    @BindView(R.id.ivIngredients) ImageView ivIngredients;

    // Text views
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.tvStepCount) TextView tvStepCount;

    // Progress bars
    @BindView(R.id.dbProgress) ProgressBar dbProgress;
    @BindView(R.id.pbLoading) ProgressBar pbLoading;

    // View Pager
    @BindView(R.id.vpSteps) ViewPager vpSteps;
    private SpeechCardAdapter speechCardAdapter;

    // Confetti
    @BindView(R.id.viewKonfetti) KonfettiView viewKonfetti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_speech);

        ButterKnife.bind(this);

        // Check if user has given permission to record audio
        checkPermissions();

        new SetupTask(this).execute();

        // Set views
        recipe = getIntent().getParcelableExtra("recipe");
        audioFile = recipe.getMedia();
        tvName.setText(recipe.getName());
        ingredients = TextUtils.join("\n", recipe.getIngredients());

        ArrayList<String> components = new ArrayList<>();
        components.addAll(recipe.getComponents());

        instructions = new ArrayList<>();
        instructions.add(getResources().getString(R.string.before_start_recipe_caption));
        instructions.addAll(recipe.getSteps());

        totalSteps = instructions.size() - 1;
        tvStepCount.setText("Step " + stepCount + "/" + totalSteps);

        setSpeechCardAdapter(instructions, components);

        ivStop.setVisibility(View.INVISIBLE);
        ivStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishRecipe();
            }
        });

        ivPause.setVisibility(View.INVISIBLE);
        ivPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pauseTts();
            }
        });

        ivResume.setVisibility(View.INVISIBLE);
        ivResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resumeTts();
            }
        });

        ivIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showIngredients(ingredients);
            }
        });

        setTextToSpeech();

        if (savedInstanceState != null) {
            stepCount = savedInstanceState.getInt(KEY_STEP_COUNT);
            startedRecipe = savedInstanceState.getBoolean(KEY_RESUMED_RECIPE);
            vpSteps.setCurrentItem(stepCount);
        }
    }

    private void showIngredients(String ingredients) {
        FragmentManager fm = getSupportFragmentManager();
        IngredientsDialogFragment ingredientsDialog = IngredientsDialogFragment.newInstance(ingredients);
        ingredientsDialog.show(fm, "fragment_ingredients");
    }

    private void setSpeechCardAdapter(ArrayList<String> instructions, ArrayList<String> components) {
        speechCardAdapter = new SpeechCardAdapter(getSupportFragmentManager(), instructions, components);
        vpSteps.setAdapter(speechCardAdapter);
        vpSteps.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            // This method will be invoked when a new page becomes selected.
            @Override
            public void onPageSelected(int step) {
                stepCount = step;
                updateProgressBar(step);
                if (step > 0) {
                    speakStep(step);
                } else if (step == 0) {
                    if (startedRecipe) {
                        finishRecipe();
                    }
                }
            }

            // This method will be invoked when the current page is scrolled
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Code goes here
            }

            // Called when the scroll state changes:
            // SCROLL_STATE_IDLE, SCROLL_STATE_DRAGGING, SCROLL_STATE_SETTLING
            @Override
            public void onPageScrollStateChanged(int state) {
                // Code goes here
            }
        });

    }

    private void finishRecipe() {
        shutdown();
        resetSpeechActivity();
    }

    private void showConfetti() {
        Log.d("Confetti", "Showing confetti");
        viewKonfetti.build()
                .addColors(getResources().getColor(R.color.colorPrimary), getResources().getColor(R.color.colorSecondary))
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(800L)
                .addShapes(Shape.RECT, Shape.CIRCLE)
                .addSizes(new Size(12, 5f))
                .setPosition(-50f, viewKonfetti.getWidth() + 50f, -50f, -50f)
                .stream(80, 2000L);
    }

    private void addCompletedRecipe() {
        user = currentUser;
        String recipeId = recipe.getObjectId();
        ArrayList<String> recipesCompleted = new ArrayList<>();

        if (user.get("recipesCompleted") != null) {
            recipesCompleted.addAll((ArrayList<String>) user.get("recipesCompleted"));
        }

        if (!recipesCompleted.contains(recipeId)) {
            recipesCompleted.add(recipeId);
        }

        user.put("recipesCompleted", recipesCompleted);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Toast.makeText(SpeechActivity.this, "Completed", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void startRecipe() {
        stepCount = 1;
        vpSteps.setCurrentItem(1);
        beginRecipe();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            speakStep(1);
        } else {
            Toast.makeText(SpeechActivity.this, "Failed to play audio. Minimum API requirements not met", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save custom values into the bundle
        if (stepCount > 0) {
            savedInstanceState.putInt(KEY_STEP_COUNT, stepCount - 1);
            savedInstanceState.putBoolean(KEY_RESUMED_RECIPE, true);
        } else {
            savedInstanceState.putInt(KEY_STEP_COUNT, 0);
            savedInstanceState.putBoolean(KEY_RESUMED_RECIPE, false);
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void setTextToSpeech() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    tts.setSpeechRate(0.9f);
                    if (startedRecipe) {
                        beginRecipe();
                        speakStep(stepCount);
                    }
                }
            }
        });
    }

    private void beginRecipe() {
        startedRecipe = true;

        // Toggle views
        ivStop.setVisibility(View.VISIBLE);
        ivPause.setVisibility(View.VISIBLE);

        // If audio file exists, start player
        if (audioFile != null) {
            startRecognition(PLAYER_SEARCH);
            player = new MediaPlayer();
            pbLoading.setVisibility(ProgressBar.VISIBLE);
            startPlayer();
            Toast.makeText(SpeechActivity.this, "Listening for start or stop", Toast.LENGTH_SHORT).show();
        } else {
            initializedTts = true;
            startRecognition(TTS_SEARCH);
        }
    }

    private void speakStep(final int step) {
        speakThread = new HandlerThread("SpeakStep");
        speakThread.start();
        speechHandler = new Handler(speakThread.getLooper());
        speechHandler.post(new Runnable() {
            @Override
            public void run() {
                checkIfCompleted(step);
                if (step < speechCardAdapter.getCount() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String currStep = instructions.get(step);
                    tts.speak(currStep, TextToSpeech.QUEUE_FLUSH, null, "Instructions");
                }
            }
        });
        speakThread.quitSafely();
    }

    private void checkIfCompleted(int step) {
        Log.d("Confetti", "Step is " + step + "and totalSteps is " + totalSteps + "and startedRecipe is " + startedRecipe);
        if (step == totalSteps && startedRecipe) {
            showConfetti();
            addCompletedRecipe();
        }
    }

    @Override
    public void replayStep() {
        repeatTts();
    }

    private void updateProgressBar(int step) {
        tvStepCount.setText("Step " + step + "/" + totalSteps);
        int progress = (int) (step * 1.0 / totalSteps * 100);
        dbProgress.setProgress(progress);
    }

    private void shutdown() {
        HandlerThread handlerThread = new HandlerThread("FinishRecipe");
        handlerThread.start();
        // Create a handler attached to the HandlerThread's Looper
        Handler mHandler = new Handler(handlerThread.getLooper());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // Stop speech recognition and player or text to speech
                recognizer.stop();
                if (player != null) {
                    stopPlayer();
                }
                if (initializedTts) {
                    tts.stop();
                }
            }
        });
        handlerThread.quitSafely();
    }

    public void resetSpeechActivity() {
        vpSteps.setCurrentItem(0);
        dbProgress.setProgress(0);
        stepCount = 0;
        tvStepCount.setText("Step " + stepCount + "/" + totalSteps);

        // Toggle views
        ivStop.setVisibility(View.INVISIBLE);
        ivPause.setVisibility(View.INVISIBLE);
        ivResume.setVisibility(View.INVISIBLE);
    }

    private void stopPlayer() {
        if (player.isPlaying()) {
            try {
                player.reset();
                player.prepare();
                player.stop();
                player.release();
                player = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startPlayer() {
        try {
            String audioFileURL = audioFile.getUrl();
            player.setDataSource(audioFileURL);
            player.prepare();
            player.start();
            pbLoading.setVisibility(ProgressBar.INVISIBLE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        final WeakReference<SpeechActivity> activityReference;

        SetupTask(SpeechActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                Toast.makeText(activityReference.get(), "Failed to init recognizer " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new SetupTask(this).execute();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.stop();
            recognizer.shutdown();
        }

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        if (player != null) {
            stopPlayer();
        }

        stepCount = 0;
        speakThread.quitSafely();
    }

    /**
     * After end of speech, stop recognizer and get a final result
     */
    @Override
    public void onEndOfSpeech() {
        Log.d("Speech recognition", "Calling end of speech");
        recognizer.stop();
    }

    private void startRecognition(String searchName) {
        recognizer.startListening(searchName);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();

        recognizer.addListener(this);

        // Create grammar-based search for selection between demos
        File playerKeywords = new File(assetsDir, "player.gram");
        File ttsKeywords = new File(assetsDir, "keywords.gram");

        recognizer.addKeywordSearch(PLAYER_SEARCH, playerKeywords);
        recognizer.addKeywordSearch(TTS_SEARCH, ttsKeywords);
    }

    @Override
    public void onError(Exception error) {
        Log.d("Speech recognition", error.toString());
    }

    // Called in onResult if using media player
    private void processPlayerResult(String text) {
        int length = player.getCurrentPosition();
        isPaused = !player.isPlaying() && length > 1;

        if (text.equals("start recipe") && isPaused) {
            player.seekTo(length);
            player.start();
        } else if (text.equals("stop recipe") && !isPaused) {
            player.pause();
        }
    }

    // Called in onResult if using text to speech
    private void processTtsResult(String text) {
        switch (text) {
            case "next step":
                if (stepCount + 1 < speechCardAdapter.getCount()) {
                    vpSteps.setCurrentItem(stepCount + 1);
                }
                break;
            case "finish recipe":
                finishRecipe();
                break;
            case "repeat step":
                repeatTts();
                break;
            case "previous step":
                previousTts();
                break;
        }
    }

    private void pauseTts() {
        Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
        ivPause.setVisibility(View.INVISIBLE);
        ivResume.setVisibility(View.VISIBLE);
        isPaused = true;
        shutdown();
    }

    private void repeatTts() {
        speakStep(stepCount);
    }

    private void previousTts() {
        if (stepCount > 1) {
            stepCount -= 1;
            vpSteps.setCurrentItem(stepCount);
            speakStep(stepCount);
        }
    }

    private void resumeTts() {
        Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show();
        ivResume.setVisibility(View.INVISIBLE);
        ivPause.setVisibility(View.VISIBLE);
        isPaused = false;
        startRecognition(TTS_SEARCH);
        repeatTts();
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    // Called after recognizer is stopped
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();

            if (audioFile != null) {
                processPlayerResult(text);
            } else {
                processTtsResult(text);
            }
        }

        if (!isPaused) {
            if (initializedTts) {
                startRecognition(TTS_SEARCH);
            } else {
                startRecognition(PLAYER_SEARCH);
            }
        }
    }

    /**
     * Unused methods
     */
    // Called when speech begins
    @Override
    public void onBeginningOfSpeech() {
    }

    // Called if we set a time out on recognizer's start listening method
    @Override
    public void onTimeout() {
    }
}
