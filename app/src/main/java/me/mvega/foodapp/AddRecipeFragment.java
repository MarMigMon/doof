package me.mvega.foodapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

import static android.app.Activity.RESULT_OK;

public class AddRecipeFragment extends Fragment {
    /* Used to handle permission request */
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 399;

    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.etRecipeName)
    EditText etRecipeName;
    @BindView(R.id.etDescription)
    EditText etDescription;
    @BindView(R.id.etIngredients)
    EditText etIngredients;
    @BindView(R.id.etYield)
    EditText etYield;
    @BindView(R.id.etPrepTime)
    EditText etPrepTime;
    @BindView(R.id.spType)
    Spinner spType;
    @BindView(R.id.spPrepTime)
    Spinner spPrepTime;
    @BindView(R.id.btAdd)
    Button btAdd;
    @BindView(R.id.btImage)
    Button btImage;
    @BindView(R.id.btAudio)
    Button btAudio;
    @BindView(R.id.buttonLayout)
    LinearLayout buttonLayout;
    @BindView(R.id.btAddStep)
    Button btAddStep;
    @BindView(R.id.btRemoveStep)
    Button btRemoveStep;
    @BindView(R.id.ivPreview)
    ImageView ivPreview;
    @BindView(R.id.instructionsLayout)
    RelativeLayout instructionsLayout;
    @BindView(R.id.tvInstructions)
    TextView tvInstructions;
    @BindView(R.id.step1)
    EditText step1;
    @BindView(R.id.tvRecipeTitle)
    TextView title;

    private Bitmap recipeImage;
    private Uri audioUri;
    private String audioName;
    private final static int PICK_PHOTO_CODE = 1046;
    private final static int PICK_AUDIO_CODE = 1;
    private int stepCount = 1;
    private ArrayList<EditText> steps;

    private String typeText = "";
    private String prepTimeText = "minutes"; // Automatically recognizes the prep-time time period as minutes

    public final String APP_TAG = "FoodApp";
    public final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    public String photoFileName = "photo.jpg";
    File photoFile;
    private File selectedPhotoFile;


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
                try {
                    addRecipe(null);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        btImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog addPhotoDialog = new AlertDialog.Builder(getActivity()).create();
                addPhotoDialog.setCancelable(true);
//                addPhotoDialog.setMessage(Html.fromHtml("Choose ));

                addPhotoDialog.setButton(DialogInterface.BUTTON_POSITIVE, "TAKE PHOTO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onLaunchCamera();
                            }
                        });
                addPhotoDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "CHOOSE EXISTING PHOTO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onPickPhoto();
                            }
                        });
                addPhotoDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                addPhotoDialog.show();
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
                // Adds the "remove step" button so the user can remove the last added step
                btRemoveStep.setVisibility(View.VISIBLE);
                scrollToButtonLayout(false);
            }
        });

        btRemoveStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveStep();
                // Removes "remove step" button if there is only one step left
                if (steps.size() == 1) {
                    btRemoveStep.setVisibility(View.GONE);
                }
                scrollToButtonLayout(true);
            }
        });

        /*--------------*\
        |    Spinners    |
        \*--------------*/

        //////////////////
        // Type Spinner //
        //////////////////

        // Create an ArrayAdapter using the string array and a default spinner layout
        String[] typeArray = getResources().getStringArray(R.array.type_array);
        final ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_spinner, typeArray) {
            @Override
            public boolean isEnabled(int position) { // First item will be used as a hint
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    tv.setTextColor(Color.GRAY); // Sets the hint's text color to gray
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setPadding(0, 0, 0, 0);
                return view;
            }
        };
        // Specify the layout to use when the list of choices appears
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spType.setAdapter(typeAdapter);
        // Listens for when the user makes a selection
        spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String item = (String) adapterView.getItemAtPosition(position);
                if (position > 0) {
                    typeText = item;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ///////////////////////
        // Prep Time Spinner //
        ///////////////////////

        // Create an ArrayAdapter using the string array and a default spinner layout
        String[] prepTimeArray = getResources().getStringArray(R.array.prep_time_array);
        final ArrayAdapter<String> prepTimeAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_spinner, prepTimeArray) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setPadding(0, 0, 0, 0);
                return view;
            }
        };
        // Specify the layout to use when the list of choices appears
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spPrepTime.setAdapter(prepTimeAdapter);
        // Listens for when the user makes a selection
        spPrepTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                prepTimeText = (String) adapterView.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
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

    /**
     * Pre-fills the fragment's instructions with the given list
     *
     * @param instructions steps for recipe
     */
    private void addSteps(List<String> instructions) {
        step1.setText(instructions.get(0));
        for (String instruction : instructions.subList(1, instructions.size())) {
            onAddStep();
            steps.get(stepCount - 1).setText(instruction);
        }
    }

    /**
     * Adds a new step (EditText) to the layout for the user to input text
     */
    private void onAddStep() {
        EditText step = new EditText(getContext());

        // Set layout params
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
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
        instructionsLayout.addView(step);
    }

    /**
     * Removes the last added step (EditText) from the layout
     */
    private void onRemoveStep() {
        EditText lastStep = steps.get(steps.size() - 1);
        steps.remove(lastStep);
        stepCount -= 1;
        instructionsLayout.removeView(lastStep);
    }

    /**
     * Scrolls scrollview to the top of the LinearLayout containing the Add Step and Remove Step buttons
     */
    private void scrollToButtonLayout(boolean reverse) {
        if (reverse) {
            scrollView.scrollBy(0, -step1.getHeight());
        } else {
            scrollView.scrollBy(0, step1.getHeight());
        }
    }

    private void onPickAudio() {
        Intent intent_upload = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
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

    // Returns the File for a photo stored on disk given the fileName
    public void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        photoFile = getPhotoFileUri(photoFileName);

        // wrap File object into a content provider
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "me.mvega.fileprovider", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }
    public File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
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
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                String imagePath = photoFile.getAbsolutePath();
                setSelectedPhoto(new File(imagePath));
            } else { // Result was a failure
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
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
            if (data != null && resultCode == RESULT_OK) {
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

    public void setSelectedPhoto(File file) {
        selectedPhotoFile = file;

        Bitmap rawTakenImage = BitmapFactory.decodeFile(file.getAbsolutePath());
        ivPreview.setImageBitmap(rawTakenImage);
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        byte[] audioBytes = out.toByteArray();
        return audioBytes;
    }

    private ArrayList<String> parseInstructions() {
        ArrayList<String> stepStrings = new ArrayList<>();
        String stepText;

        for (int i = 0; i < steps.size(); i++) {
            stepText = steps.get(i).getText().toString().trim();
            if (!stepText.equals("")) {
                stepStrings.add(stepText);
            }
        }

        return stepStrings;
    }

    private void addRecipe(Recipe oldRecipe) throws IllegalArgumentException {
        final Recipe recipe;
        boolean newRecipe = false;

        // checks if this is submission is an edit or a new recipe
        if (oldRecipe == null) {
            recipe = new Recipe();
            newRecipe = true;
        } else {
            recipe = oldRecipe;
        }

        ArrayList<String> steps = parseInstructions();
        String name = etRecipeName.getText().toString();
        String description = etDescription.getText().toString();
        String ingredients = etIngredients.getText().toString();


        // Checks to ensure every required field is filled out
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Please enter a name for your recipe.");
        } else {
            recipe.setName(name);
        }
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Please enter a description for your recipe.");
        } else {
            recipe.setDescription(description);
        }
        try {
            Number yieldNumber = Integer.valueOf(etYield.getText().toString());
            if (yieldNumber.doubleValue() == 1) {
                String yield = String.valueOf(yieldNumber) + " serving";
                recipe.setYield(yield);
            } else {
                String yield = String.valueOf(yieldNumber) + " servings";
                recipe.setYield(yield);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a number of servings for your recipe.");
        }
        try {
            Number prepTime = Integer.valueOf(etPrepTime.getText().toString());
            recipe.setPrepTime(prepTime);
            if (prepTime.doubleValue() == 1) {
                recipe.setPrepTimePeriod(prepTimeText.substring(0, prepTimeText.length() - 1));
            } else {
                recipe.setPrepTimePeriod(prepTimeText);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter an amount of time for your recipe.");
        }

        if (typeText.isEmpty()) {
            throw new IllegalArgumentException("Please select a type from the type drop-down.");
        } else {
            recipe.setType(typeText);
        }
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Please enter some ingredients for your recipe.");
        } else {
            recipe.setIngredients(ingredients);
        }
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("Please enter some instructions for your recipe.");
        } else {
            recipe.setSteps(steps);
        }

        // Empty images and audio are permissible
        if (recipeImage != null) {
            recipe.setImage(prepareImage(recipeImage));
        }
        if (audioUri != null) {
            recipe.setMedia(prepareAudio(audioUri));
        }

        pbLoading.setVisibility(ProgressBar.VISIBLE);

        if (newRecipe) {
            // Recipe user and rating are automatically filled in Parse
            recipe.setUser(ParseUser.getCurrentUser());
            recipe.setRating(0);
            recipe.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(getContext(), "Recipe successfully created!", Toast.LENGTH_LONG).show();
                        pbLoading.setVisibility(ProgressBar.INVISIBLE);
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.frameLayout, new FeedFragment());
                        ft.commit();
                    } else {
                        Toast.makeText(getContext(), "Recipe creation failed!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        } else {
            recipe.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        Toast.makeText(getContext(), "Recipe successfully edited!", Toast.LENGTH_LONG).show();
                        pbLoading.setVisibility(ProgressBar.INVISIBLE);
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        RecipeFragment recipeFragment = new RecipeFragment();
                        recipeFragment.recipe = recipe;
                        ft.replace(R.id.frameLayout, recipeFragment);
                        ft.commit();
                    } else {
                        Toast.makeText(getContext(), "Recipe edit failed!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Utilizes the Add Recipe Fragment to edit recipes
     *
     * @param recipe the recipe the user wants to edit
     */
    public void setupEdit(final Recipe recipe) {

        title.setText("Edit Recipe"); // This special fragment says "Edit Recipe" rather than New Recipe

        // Ensures that when a recipe is submitted
        btAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    addRecipe(recipe);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        String name = recipe.getName();
        String description = recipe.getDescription();
        String yieldText = recipe.getYield();
        Number yield = Integer.parseInt(yieldText.substring(0, yieldText.indexOf(' ')));
        Number prepTime = recipe.getPrepTime();
        String prepTimeString = recipe.getPrepTimeString();
        prepTimeText = prepTimeString.substring(prepTimeString.indexOf(' ') + 1);
        Log.d("AddRecipeFragment", prepTimeText);
        typeText = recipe.getType();
        String ingredients = recipe.getIngredients();

        etRecipeName.setText(name);
        etDescription.setText(description);
        etYield.setText(yield.toString());
        etPrepTime.setText(prepTime.toString());
        spPrepTime.setSelection(((ArrayAdapter<String>) spPrepTime.getAdapter()).getPosition(prepTimeText));
        spType.setSelection(((ArrayAdapter<String>) spType.getAdapter()).getPosition(typeText));
        etIngredients.setText(ingredients);

        addSteps(recipe.getSteps());

        recipe.getImage().getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] data, ParseException e) {
                if (data != null) {
                    ivPreview.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
                }
            }
        });
    }

}