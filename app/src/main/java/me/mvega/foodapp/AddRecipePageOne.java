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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.mvega.foodapp.model.Recipe;

import static android.app.Activity.RESULT_OK;

public class AddRecipePageOne extends Fragment {

    @BindView(R.id.btImage)
    Button btImage;
    @BindView(R.id.ivPreview)
    ImageView ivPreview;
    @BindView(R.id.etRecipeName)
    EditText etRecipeName;
    @BindView(R.id.spType)
    AppCompatSpinner spType;
    @BindView(R.id.etDescription)
    EditText etDescription;
    @BindView(R.id.etYield)
    EditText etYield;
    @BindView(R.id.etPrepTime)
    EditText etPrepTime;
    @BindView(R.id.spPrepTime)
    AppCompatSpinner spPrepTime;
    @BindView(R.id.btNext)
    Button btNext;

    private String typeText = "";
    private String prepTimeText = "minutes"; // Automatically recognizes the prep-time time period as minutes

    /* Used to handle permission request */
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 399;

    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1034;
    private final static int PICK_PHOTO_CODE = 1046;

    private String imagePath;

    private static final String KEY_RECIPE = "recipe";
    private static final String KEY_EDITING = "editing";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_YIELD = "yield";
    private static final String KEY_PREP_TIME = "prepTime";
    private static final String KEY_IMAGE_PATH = "photo";
    private static final String KEY_PREP_TIME_TEXT = "prepTimeText";
    private static final String KEY_TYPE_TEXT = "typeText";

    // True if a recipe is being edited
    private Boolean editing = false;
    private Recipe editedRecipe;

    private PageOneFragmentCommunication addRecipeListenerFragment;

    // implement interface
    public interface PageOneFragmentCommunication {
        void next(Bundle bundle);

        Bitmap getImage(String imagePath);
    }

    // newInstance constructor for creating fragment with arguments
    public static AddRecipePageOne newInstance(Bundle bundle) {
        AddRecipePageOne fragmentFirst = new AddRecipePageOne();
        fragmentFirst.setArguments(bundle);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreateView(inflater, container, savedInstanceState);
        onAttachToParentFragment(getParentFragment());
        return inflater.inflate(R.layout.page_first_add_recipe, container, false);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(KEY_IMAGE_PATH, imagePath);
        outState.putBoolean(KEY_EDITING, editing);
        outState.putParcelable(KEY_RECIPE, editedRecipe);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        final Bitmap image;

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString(KEY_IMAGE_PATH, "");
            if (!imagePath.equals("")) {
                image = addRecipeListenerFragment.getImage(imagePath);
            } else {
                image = null;
            }
            editing = savedInstanceState.getBoolean(KEY_EDITING, false);
            editedRecipe = savedInstanceState.getParcelable(KEY_RECIPE);
        } else {
            if (getArguments() != null) {
                editing = getArguments().getBoolean(KEY_EDITING);
                editedRecipe = getArguments().getParcelable(KEY_RECIPE);
            }
            image = null;
        }

        // Create a new background thread
//        HandlerThread handlerThread = new HandlerThread("Setup");
//        handlerThread.start();
//        Handler mHandler = new Handler(handlerThread.getLooper());
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                setButtons();
//                checkStoragePermissions();
//            }
//        });
//        handlerThread.quitSafely();

        new Thread(new Runnable() {
            @Override
            public void run() {
                checkStoragePermissions();
                setButtons();
            }
        }).start();

        createTypeSpinner();
        createPrepTimeSpinner();
        setImage(image);

        if (editing) {
            setupEdit(editedRecipe);
        }
    }

    private void onAttachToParentFragment(Fragment childFragment) {
        try {
            addRecipeListenerFragment = (PageOneFragmentCommunication) childFragment;

        } catch (ClassCastException e) {
            throw new ClassCastException(
                    childFragment.toString() + " must implement OnPlayerSelectionSetListener");
        }
    }

    private void setButtons() {
        btNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    checkFirstSection();
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
                addPhotoDialog.setCanceledOnTouchOutside(true);

                addPhotoDialog.setButton(DialogInterface.BUTTON_POSITIVE, "TAKE PHOTO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onLaunchCamera();
                            }
                        });
                addPhotoDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "CHOOSE PHOTO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onPickPhoto();
                            }
                        });
                addPhotoDialog.show();
            }
        });
    }

    //////////////////
    // Type Spinner //
    //////////////////
    private void createTypeSpinner() {
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
    }

    ///////////////////////
    // Prep Time Spinner //
    ///////////////////////
    private void createPrepTimeSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        String[] prepTimeArray = getResources().getStringArray(R.array.prep_time_array);
        final ArrayAdapter<String> prepTimeAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner, prepTimeArray);
        // Specify the layout to use when the list of choices appears
        prepTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
    }

    /**
     * Request read external storage permissions to upload images and audio
     */

    private void checkStoragePermissions() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            Log.d("AddRecipeFragment", "Permission granted");
        }
    }


    // Trigger gallery selection for a photo
    private void onPickPhoto() {
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
    private void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        String photoFileName = "photo.jpg";
        File photoFile = getPhotoFileUri(photoFileName);

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

    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        String APP_TAG = "doof";
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        imagePath = mediaStorageDir.getPath() + File.separator + fileName;
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setImage(addRecipeListenerFragment.getImage(imagePath));
            } else { // Result was a failure
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PICK_PHOTO_CODE) {
            if (data != null && resultCode == RESULT_OK) {
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Cursor cursor = null;
                try {
                    String[] proj = {MediaStore.Images.Media.DATA};
                    cursor = getContext().getContentResolver().query(photoUri, proj, null, null, null);
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    imagePath = cursor.getString(columnIndex);
                    setImage(addRecipeListenerFragment.getImage(imagePath));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    public void setImage(Bitmap image) {
        if (image != null) {
            ivPreview.setImageBitmap(image);
        }
    }

    private void checkFirstSection() {
        Bundle bundle = new Bundle();

        String name = etRecipeName.getText().toString();
        String description = etDescription.getText().toString();

        // Checks to ensure every required field is filled out
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Please enter a name for your recipe.");
        } else {
            bundle.putString(KEY_NAME, name);
        }
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Please enter a description for your recipe.");
        } else {
            bundle.putString(KEY_DESCRIPTION, description);
        }
        try {
            int yield = Integer.valueOf(etYield.getText().toString());
            bundle.putInt(KEY_YIELD, yield);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter a number of servings for your recipe.");
        }
        try {
            int prepTime = Integer.valueOf(etPrepTime.getText().toString());
            bundle.putInt(KEY_PREP_TIME, prepTime);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Please enter an amount of time for your recipe.");
        }
        if (typeText.isEmpty()) {
            throw new IllegalArgumentException("Please select a type from the type drop-down.");
        } else {
            bundle.putString(KEY_TYPE_TEXT, typeText);
        }
        bundle.putString(KEY_IMAGE_PATH, imagePath); // Stores the location of the image
        bundle.putString(KEY_PREP_TIME_TEXT, prepTimeText);

        addRecipeListenerFragment.next(bundle);
    }

    /**
     * Utilizes the Add Recipe Fragment to edit recipes
     *
     * @param recipe the recipe the user wants to edit
     */
    private void setupEdit(final Recipe recipe) {
        String name = recipe.getName();
        String description = recipe.getDescription();
        String yieldText = recipe.getYield();
        Number yield = Integer.parseInt(yieldText.substring(0, yieldText.indexOf(' ')));
        Number prepTime = recipe.getPrepTime();
        String prepTimeString = recipe.getPrepTimeString();
        prepTimeText = prepTimeString.substring(prepTimeString.indexOf(' ') + 1);
        typeText = recipe.getType();

        etRecipeName.setText(name);
        etDescription.setText(description);
        etYield.setText(yield.toString());
        etPrepTime.setText(prepTime.toString());
        spPrepTime.setSelection(((ArrayAdapter<String>) spPrepTime.getAdapter()).getPosition(prepTimeText));
        spType.setSelection(((ArrayAdapter<String>) spType.getAdapter()).getPosition(typeText));

        final ParseFile image = recipe.getImage();
        if (image != null && imagePath == null) {
            recipe.getImage().getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    if (data != null) {
                        final Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
                        ivPreview.setImageBitmap(b);
                    }
                }
            });
        }
    }
}
