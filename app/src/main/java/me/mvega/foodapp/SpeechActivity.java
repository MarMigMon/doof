package me.mvega.foodapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class SpeechActivity extends AppCompatActivity implements
        RecognitionListener {

    /* We only need the keyphrase to start recognition, one menu with list of choices,
       and one word that is required for method switchSearch - it will bring recognizer
       back to listening for the keyphrase*/
    private static final String KEYPHRASE_SEARCH = "say begin";
    private static final String MENU_SEARCH = "say start or stop";
    /* Keyword we are looking for to activate recognition */
    private static final String KEYPHRASE = "begin";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    /* Recognition object */
    private SpeechRecognizer recognizer;
    private TextView caption_text;
    private TextView result_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        // Prepare the data for UI
        caption_text = (TextView) findViewById(R.id.caption_text);
        result_text = (TextView) findViewById(R.id.result_text);


        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }

        new SetupTask(this).execute();

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
                activityReference.get().caption_text.setText("Failed to init recognizer " + result);
            } else {
                activityReference.get().switchSearch(KEYPHRASE_SEARCH);
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
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            result_text.setText("begin");
            switchSearch(MENU_SEARCH);
        } else if (text.equals("start")) {
            result_text.setText("start");
        } else if (text.equals("stop")) {
            result_text.setText("stop");
        }
    }

    /**
     * After end of speech, call switchSearch method to stop recognizer and get a final result
     */
    @Override
    public void onEndOfSpeech() {
        Log.d("Speech recognition", "Calling end of speech");
        switchSearch(MENU_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();
        if (searchName.equals(MENU_SEARCH)) {
            Log.d("Speech recognition", "Listening for start or stop");
            caption_text.setText(MENU_SEARCH);
        } else {
            Log.d("Speech recognition", "Listening for begin");
            caption_text.setText(KEYPHRASE_SEARCH);
        }
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

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KEYPHRASE_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
        File menuGrammar = new File(assetsDir, "menu.gram");
        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);

    }

    @Override
    public void onError(Exception error) {
        Log.d("Speech recognition", error.toString());
    }


    /**
     * Unused methods
     */

    // Called after recognizer is stopped
    @Override
    public void onResult(Hypothesis hypothesis) {
    }

    // Called when speech begins
    @Override
    public void onBeginningOfSpeech() {
    }

    // Called if we set a time out on recognizer's start listening method
    @Override
    public void onTimeout() {
    }
}
