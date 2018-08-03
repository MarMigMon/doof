package me.mvega.foodapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SignupActivity extends AppCompatActivity {

    @BindView(R.id.etNewUser) EditText etNewUser;
    @BindView(R.id.etNewPass) EditText etNewPass;
    @BindView(R.id.btCreateUser) Button btCreateUser;
    @BindView(R.id.etFullName) EditText etFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);

        btCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    checkComplete();
                    checkUsers();
                    ParseUser user = new ParseUser();
                    user.setUsername(etNewUser.getText().toString());
                    user.setPassword(etNewPass.getText().toString());
                    user.put("Name", etFullName.getText().toString());
                    user.put("recipesCompleted", new ArrayList<>());

                    user.signUpInBackground(new SignUpCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(SignupActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // Sign up didn't succeed
                                Toast.makeText(SignupActivity.this, "Sign-up failed: That username already exists, please enter a different username.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } catch (IllegalArgumentException e) {
                    Toast.makeText(SignupActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void checkComplete() throws IllegalArgumentException {
        String fullName = etFullName.getText().toString();
        String username = etNewUser.getText().toString();
        String password = etNewPass.getText().toString();

        if (fullName.isEmpty()) {
            throw new IllegalArgumentException("Sign-up failed: Please enter your name.");
        }
        if (username.isEmpty()) {
            throw new IllegalArgumentException("Sign-up failed: Please enter a username.");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("Sign-up failed: Please enter a password.");
        }
    }

    private void checkUsers() throws IllegalArgumentException{
        final String username = etNewUser.getText().toString();
        ParseUser.getQuery().include("username").findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> existingUsers, ParseException error) {
                for (int i = 0; i < existingUsers.size(); i++) {
                    if (username == existingUsers.get(i).toString()) {
                        throw new IllegalArgumentException();
                    }
                }
            }
        });
    }
}
