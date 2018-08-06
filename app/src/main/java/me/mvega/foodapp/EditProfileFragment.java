package me.mvega.foodapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;
import static me.mvega.foodapp.MainActivity.currentUser;


public class EditProfileFragment extends Fragment {

    private final static int PICK_PHOTO_CODE = 1046;

    @BindView(R.id.tvName)
    TextView tvName;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.ivProfile)
    ImageView ivProfile;
    @BindView(R.id.btChangeProfilePic)
    Button btChangeProfilePic;
    @BindView(R.id.btSave)
    Button btSave;
    @BindView(R.id.tvDescription)
    TextView tvDescription;
    @BindView(R.id.etDescription)
    EditText etDescription;
    @BindView(R.id.btSaveDescription)
    Button btSaveDescription;
    @BindView(R.id.btAddDescription)
    Button btAddDescription;

    private final ParseUser user = currentUser;
    private final String name = (String) user.get("Name");
    private String description = (String) user.get("description");

    private final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1000;
    private File photoFile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        tvName.setText(name);

        if (description != null) {
            tvDescription.setText(description);
        } else {
            tvDescription.setText("Hello there!");
        }


        ParseFile profileImage = user.getParseFile("image");
        if (profileImage != null) {
            String imageUrl = profileImage.getUrl();
            Glide.with(getContext()).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(ivProfile);
        } else
            Glide.with(getContext()).load(R.drawable.image_placeholder).apply(RequestOptions.circleCropTransform()).into(ivProfile);

        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etName.setVisibility(View.VISIBLE);
                tvName.setVisibility(View.INVISIBLE);
                btSave.setVisibility(View.VISIBLE);
                etName.setText(name);
            }
        });

        if (description == null) {
            description = "";
        }
        if (description.equals("")) {
            btAddDescription.setVisibility(View.VISIBLE);
            btAddDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btAddDescription.setVisibility(View.INVISIBLE);
                    etDescription.setText(description);
                    etDescription.setVisibility(View.VISIBLE);
                    tvDescription.setVisibility(View.INVISIBLE);
                    btSaveDescription.setVisibility(View.VISIBLE);
                }
            });
        } else {
            tvDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etDescription.setText(description);
                    etDescription.setVisibility(View.VISIBLE);
                    tvDescription.setVisibility(View.INVISIBLE);
                    btSaveDescription.setVisibility(View.VISIBLE);
                }
            });
        }


        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeName();
            }
        });

        btChangeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog editProfilePicDialog = new AlertDialog.Builder(getActivity()).create();
                editProfilePicDialog.setCancelable(true);
                editProfilePicDialog.setCanceledOnTouchOutside(true);

                editProfilePicDialog.setButton(DialogInterface.BUTTON_POSITIVE, "TAKE PHOTO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                onLaunchCamera();
                            }
                        });
                editProfilePicDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "CHOOSE PHOTO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                changeProfilePic();
                            }
                        });
                editProfilePicDialog.show();
            }
        });

        btSaveDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDescription();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(EditProfileFragment.this).attach(EditProfileFragment.this).commit();
            }
        });

    }

    private void changeDescription() {
        final String newDescription = etDescription.getText().toString().trim();
        user.put("description", newDescription);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                    tvDescription.setText(newDescription);
                    etDescription.setText(newDescription);
                    tvDescription.setVisibility(View.VISIBLE);
                    etDescription.setVisibility(View.INVISIBLE);
                    btSaveDescription.setVisibility(View.INVISIBLE);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changeProfilePic() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    private ParseFile prepareImage(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] bitmapBytes = stream.toByteArray();
            return new ParseFile("ProfilePicture", bitmapBytes);
        } else {
            return null;
        }
    }

    // Returns the File for a photo stored on disk given the fileName
    private void onLaunchCamera() {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference to access to future access
        String photoFileName = "photo.jpg";
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

    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        String APP_TAG = "FoodApp";
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(APP_TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        return new File(mediaStorageDir.getPath() + File.separator + fileName);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                String imagePath = photoFile.getAbsolutePath();
                Bitmap rawTakenImage = BitmapFactory.decodeFile(new File(imagePath).getAbsolutePath());
                try {
                    ExifInterface ei = new ExifInterface(new File(imagePath).getAbsolutePath());
                    int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_UNDEFINED);

                    Bitmap photo;
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            photo = rotateImage(rawTakenImage, 90);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_180:
                            photo = rotateImage(rawTakenImage, 180);
                            break;

                        case ExifInterface.ORIENTATION_ROTATE_270:
                            photo = rotateImage(rawTakenImage, 270);
                            break;

                        case ExifInterface.ORIENTATION_NORMAL:
                        default:
                            photo = rawTakenImage;
                    }
                    ivProfile.setImageBitmap(photo);
                    Glide.with(getContext()).load(photo).apply(RequestOptions.circleCropTransform()).into(ivProfile);
                    user.put("image", prepareImage(photo));
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                    ivProfile.setImageBitmap(rawTakenImage);
                    Glide.with(getContext()).load(rawTakenImage).apply(RequestOptions.circleCropTransform()).into(ivProfile);
                    user.put("image", prepareImage(rawTakenImage));
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            } else { // Result was a failure
                Toast.makeText(getActivity(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == PICK_PHOTO_CODE) {
            if (data != null && resultCode == RESULT_OK) {
                Log.d("Update", "gallery photo updated");
                Uri photoUri = data.getData();
                // Do something with the photo based on Uri
                Bitmap selectedImage = null;
                try {
                    selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ivProfile.setImageBitmap(selectedImage);
                Glide.with(getContext()).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(ivProfile);
                user.put("image", prepareImage(selectedImage));
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                        } else {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Toast.makeText(getActivity(), "Picture wasn't uploaded", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("Update", "failure");
        }
    }

    private void changeName() {
        final String newName = etName.getText().toString();
        user.put("Name", newName);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    btSave.setVisibility(View.INVISIBLE);
                    etName.setVisibility(View.INVISIBLE);
                    tvName.setVisibility(View.VISIBLE);
                    tvName.setText(newName);
                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
}
