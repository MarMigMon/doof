package me.mvega.foodapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseFile;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import me.mvega.foodapp.model.Recipe;

public class SpeechActivity extends AppCompatActivity implements
        RecognitionListener {

    private static final String MENU_SEARCH = "say start or stop";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private TextToSpeech tts;
    private Recipe recipe;
    private String instructions;
    private ParseFile audioFile;
    private SpeechRecognizer recognizer;
    private MediaPlayer player;
    private Boolean isPaused;
    private Boolean isSpeaking;

    @BindView(R.id.btStart) Button btStart;
    @BindView(R.id.btStop) Button btStop;
    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.tvInstructions) TextView tvInstructions;
    @BindView(R.id.tvIngredients) TextView tvIngredients;
    @BindView(R.id.pbLoading) ProgressBar pbLoading;

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
        tvIngredients.setText(recipe.getIngredients());

        instructions = recipe.getInstructions();
        tvInstructions.setText(instructions);

        btStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginRecipe();
            }
        });

        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishRecipe();
            }
        });

        setTextToSpeech();
    }

    private void setTextToSpeech() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.US);
                    tts.setSpeechRate(0.9f);
                }
            }
        });
    }

    private void beginRecipe() {
        toggleVisibility(btStop);
        toggleVisibility(btStart);

        // If audio file exists, start player
        if (audioFile != null) {
            player = new MediaPlayer();
            pbLoading.setVisibility(ProgressBar.VISIBLE);
            startPlayer();
            Toast.makeText(SpeechActivity.this, "Listening for start or stop", Toast.LENGTH_SHORT).show();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Toast.makeText(SpeechActivity.this, "Custom audio file not found, playing instructions", Toast.LENGTH_LONG).show();
                tts.speak(instructions, TextToSpeech.QUEUE_FLUSH, null, "Instructions");
            } else {
                Toast.makeText(SpeechActivity.this, "Custom audio file not found", Toast.LENGTH_LONG).show();
            }
        }

        startRecognition(MENU_SEARCH);
    }

    private void finishRecipe() {
        // Stop speech recognition and player or text to speech and reset to start button
        recognizer.stop();
        stopPlayer();
        if (tts.isSpeaking()) {
            tts.stop();
        }
        toggleVisibility(btStop);
        toggleVisibility(btStart);
    }

    public void toggleVisibility(View view) {
        view.setVisibility((view.getVisibility() == View.VISIBLE)
                ? View.INVISIBLE
                : View.VISIBLE);
    }

    private void stopPlayer() {
        if (player.isPlaying()) {
            try {
                player.reset();
                player.prepare();
                player.stop();
                player.release();
                player=null;
            }
            catch (Exception e)
            {
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
            return;
        }
    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<SpeechActivity> activityReference;
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
                Toast.makeText(activityReference.get(),"Failed to init recognizer " + result, Toast.LENGTH_LONG);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
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
            recognizer.shutdown();
        }

        if (tts != null) {
            tts.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
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
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
    }

    @Override
    public void onError(Exception error) {
        Log.d("Speech recognition", error.toString());
    }

    // Called in onResult if using media player
    private void processPlayerResult(String text) {
        int length = player.getCurrentPosition();
        isPaused = !player.isPlaying() && length > 1;

        if (text.equals("start") && isPaused) {
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
            player.seekTo(length);
            player.start();
        } else if (text.equals("stop") && !isPaused) {
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            player.pause();
        }
    }

    // Called in onResult if using text to speech
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void processTtsResult(String text) {
        isSpeaking = tts.isSpeaking();

        if (text.equals("start") && !isSpeaking) {
            Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show();
            tts.speak(instructions, TextToSpeech.QUEUE_FLUSH, null, "Instructions");

        } else if (text.equals("stop") && isSpeaking) {
            Toast.makeText(this, "Stop", Toast.LENGTH_SHORT).show();
            tts.stop();
        }
    }

    // Called after recognizer is stopped
    @Override
    public void onResult(Hypothesis hypothesis) {

        if (hypothesis != null) {
            String text = hypothesis.getHypstr();

            if (audioFile != null) {
                processPlayerResult(text);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    processTtsResult(text);
                }
            }
        }

        startRecognition(MENU_SEARCH);
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
