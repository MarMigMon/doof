package me.mvega.foodapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

import static android.app.Activity.RESULT_OK;

public class AddRecipeFragment extends Fragment {
    /* Used to handle permission request */
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 399;

    @BindView(R.id.pbLoading) ProgressBar pbLoading;
    @BindView(R.id.etRecipeName) EditText etRecipeName;
    @BindView(R.id.etDescription) EditText etDescription;
    @BindView(R.id.etIngredients) EditText etIngredients;
    @BindView(R.id.etInstructions) EditText etInstructions;
    @BindView(R.id.etYield) EditText etYield;
    @BindView(R.id.etPrepTime) EditText etPrepTime;
    @BindView(R.id.etType) EditText etType;
    @BindView(R.id.btAdd) Button btAdd;
    @BindView(R.id.btImage) Button btImage;
    @BindView(R.id.btAudio) Button btAudio;
    @BindView(R.id.btAddStep) Button btAddStep;
    @BindView(R.id.ivPreview) ImageView ivPreview;
    @BindView(R.id.ingredientsLayout) RelativeLayout ingredientsLayout;
    @BindView(R.id.step1) EditText step1;

    private Bitmap recipeImage;
    private Uri audioUri;
    private String audioName;
    private final static int PICK_PHOTO_CODE = 1046;
    private final static int PICK_AUDIO_CODE = 1;
    private int stepCount = 1;
    private ArrayList<EditText> steps;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_add_recipe, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        steps = new ArrayList<>();
        step1.setId(stepCount);
        steps.add(step1);

        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRecipe();
            }
        });

        btImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto();
            }
        });

        btAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickAudio();
            }
        });

        btAddStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddStep();
            }
        });

        checkStoragePermissions();
    }

    /**
     * Request read external storage permissions to upload images and audio
      */

    private void checkStoragePermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            Log.d("AddRecipeFragment", "Permission granted");
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                return;
            } else {
                Toast.makeText(getContext(), "Accept permissions to enable adding recipes", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onAddStep() {
        EditText step = new EditText(getContext());

        // Set layout params
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams (
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.BELOW, stepCount);

        // Set up new EditText view
        stepCount += 1;
        step.setId(stepCount);
        step.setHint("Step " + stepCount);
        step.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        step.setLayoutParams(params);

        // Add step
        steps.add(step);
        ingredientsLayout.addView(step);
    }

    private void onPickAudio() {
        Intent intent_upload = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        if (intent_upload.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent_upload, PICK_AUDIO_CODE);
        }
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto() {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PHOTO_CODE) {
            if (data != null && resultCode == RESULT_OK) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage = null;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Load the selected image into a preview
                ivPreview.setImageBitmap(selectedImage);
                recipeImage = selectedImage;
            } else {
                return;
            }
        } else if (requestCode == PICK_AUDIO_CODE) {
            if (data != null && resultCode == RESULT_OK){
                //the selected audio.
                audioUri = data.getData();
                audioName = getFileName(audioUri);
                btAudio.setText(audioName);
                Log.d("AddRecipeFragment", "Picked audio");
            } else {
                return;
            }

        }
    }

    private ParseFile prepareImage(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);

            byte[] bitmapBytes = stream.toByteArray();

            ParseFile image = new ParseFile("RecipeImage", bitmapBytes);
            return image;
        } else {
            return null;
        }
    }

    private ParseFile prepareAudio(Uri audioUri) {
        if (audioUri != null) {
            byte[] audioBytes = audioToByteArray(audioUri);
            // Create the ParseFile
            ParseFile file = new ParseFile("Audio", audioBytes);
            Log.d("AddRecipeFragment", "Successfully returned audio file");
            return file;
        } else {
            return null;
        }
    }

    private byte[] audioToByteArray(Uri audioUri) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BufferedInputStream in = null;
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(audioUri);
            in = new BufferedInputStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int read;
        byte[] buff = new byte[1024];
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) { //in could not be resolved error by compiler
                    in.close();
                }
                if (out != null) { //out could not be resolved...
                    out.close();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        byte[] audioBytes = out.toByteArray();
        return audioBytes;
    }

    private ArrayList<String> parseInstructions() {
        ArrayList<String> stepStrings = new ArrayList<>();

        for (int i = 0; i < steps.size(); i++) {
            stepStrings.add(steps.get(i).getText().toString());
        }

        return stepStrings;
    }

    private void addRecipe() {
        pbLoading.setVisibility(ProgressBar.VISIBLE);
        final Recipe recipe = new Recipe();

        recipe.setSteps(parseInstructions());
        recipe.setName(etRecipeName.getText().toString());
        recipe.setDescription(etDescription.getText().toString());
        recipe.setIngredients(etIngredients.getText().toString());
        recipe.setInstructions(etInstructions.getText().toString());
        recipe.setPrepTime(etPrepTime.getText().toString());
        recipe.setYield(etYield.getText().toString());
        recipe.setType(etType.getText().toString());

        if (recipeImage != null) {
            recipe.setImage(prepareImage(recipeImage));
        }
        if (audioUri != null) {
            recipe.setMedia(prepareAudio(audioUri));
        }

        recipe.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getContext(),"Create recipe success!", Toast.LENGTH_LONG).show();
                    pbLoading.setVisibility(ProgressBar.INVISIBLE);
                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.frameLayout, new FeedFragment());
                    ft.commit();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}