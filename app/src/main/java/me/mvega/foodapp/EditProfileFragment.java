package me.mvega.foodapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class EditProfileFragment extends Fragment {

    public final static int PICK_PHOTO_CODE = 1046;

    @BindView(R.id.tvName) TextView tvName;
    @BindView(R.id.etName) EditText etName;
    @BindView(R.id.ivProfile) ImageView ivProfile;
    @BindView(R.id.btChangeProfilePic) Button btChangeProfilePic;
    @BindView(R.id.btSave) Button btSave;

    ParseUser user = ParseUser.getCurrentUser();
    final String name = (String) user.get("Name");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ButterKnife.bind(this, view);

        tvName.setText(name);

        ParseFile profileImage = user.getParseFile("image");
        if (profileImage != null) {
            String imageUrl = profileImage.getUrl();
            Glide.with(getContext()).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(ivProfile);
        } else Glide.with(getContext()).load(R.drawable.image_placeholder).apply(RequestOptions.circleCropTransform()).into(ivProfile);

        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etName.setVisibility(View.VISIBLE);
                tvName.setVisibility(View.INVISIBLE);
                btSave.setVisibility(View.VISIBLE);
                etName.setText(name);
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeName();
            }
        });

        btChangeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeProfilePic();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            // Do something with the photo based on Uri
            Bitmap selectedImage = null;
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), photoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Glide.with(getContext()).load(selectedImage).apply(RequestOptions.circleCropTransform()).into(ivProfile);
//            ivProfile.setImageBitmap(selectedImage);
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
