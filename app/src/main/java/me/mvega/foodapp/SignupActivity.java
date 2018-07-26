package me.mvega.foodapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

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
                ParseUser user = new ParseUser();
                user.setUsername(etNewUser.getText().toString());
                user.setPassword(etNewPass.getText().toString());
                user.put("Name", etFullName.getText().toString());

                user.signUpInBackground(new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(SignupActivity.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // Sign up didn't succeed
                            Toast.makeText(SignupActivity.this, "Sign-up failed: try a different username?", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}
