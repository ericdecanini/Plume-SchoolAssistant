package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.*;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.pdt.plume.R.id.spinner;

public class SignUpActivity extends AppCompatActivity {

    String LOG_TAG = SignUpActivity.class.getSimpleName();

    private DatabaseReference mDatabase;
    private String mUserId;
    private static String defaultIconUri = "android.resource://com.pdt.plume/drawable/art_profile_default";

    protected EditText passwordEditText;
    private EditText confirmPasswordEditText;
    protected EditText emailEditText;
    protected EditText nicknameEditText;
    protected Button signUpButton;
    private ImageView visibleIcon;
    private ProgressBar spinner;
    private FirebaseAuth mFirebaseAuth;

    private boolean passwordsAreVisible = false;

    // Theme Variables
    int mPrimaryColor;
    int mSecondaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialise the back button
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // Initialize FirebaseAuth and Database
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialise the theme
        mSecondaryColor = getResources().getColor(R.color.colorAccent);

        passwordEditText = (EditText) findViewById(R.id.passwordField);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordField);
        emailEditText = (EditText) findViewById(R.id.emailField);
        nicknameEditText = (EditText) findViewById(R.id.nicknameField);
        signUpButton = (Button) findViewById(R.id.signupButton);
        visibleIcon = (ImageView) findViewById(R.id.visible);
        spinner = (ProgressBar) findViewById(R.id.progressBar);



        visibleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordsAreVisible) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordEditText.setSelection(passwordEditText.getText().length());
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
                    visibleIcon.setImageTintList(getResources().getColorStateList(R.color.white));
                    passwordsAreVisible = false;
                } else {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordEditText.setSelection(passwordEditText.getText().length());
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
                    visibleIcon.setImageTintList(getResources().getColorStateList(R.color.colorAccent));
                    passwordsAreVisible = true;
                }
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEditText.getText().toString();
                String passwordConfirmed = confirmPasswordEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String nickname = nicknameEditText.getText().toString();

                password = password.trim();
                email = email.trim();
                nickname = nickname.trim();

                if (password.isEmpty() || email.isEmpty() || nickname.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(R.string.signup_error_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    if (confirmPasswordEditText.length() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                        builder.setMessage(R.string.signup_error_message_no_confirm)
                                .setTitle(R.string.signup_error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return;
                    } else if (!passwordConfirmed.equals(password)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                        builder.setMessage(R.string.signup_error_message_wrong_confirm)
                                .setTitle(R.string.signup_error_title)
                                .setPositiveButton(android.R.string.ok, null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return;
                    } else {
                        // Successful sign up

                        // Hide/Disable the buttons, Show the Spinner
                        nicknameEditText.setEnabled(false);
                        emailEditText.setEnabled(false);
                        passwordEditText.setEnabled(false);
                        confirmPasswordEditText.setEnabled(false);
                        visibleIcon.setEnabled(false);
                        signUpButton.setVisibility(View.GONE);
                        spinner.setVisibility(View.VISIBLE);


                        final String finalNickname = nickname;
                        final String finalEmail = email;
                        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Get a reference to the newly created user
                                            mUserId = task.getResult().getUser().getUid();

                                            // Fill in the other fields of the database here
                                            DatabaseReference userRef = mDatabase.child("users").child(mUserId);
                                            userRef.child("nickname").setValue(finalNickname);
                                            userRef.child("icon").setValue(defaultIconUri);

                                            // Save the data in SharedPreferences for offline access
                                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignUpActivity.this);
                                            preferences.edit()
                                                    .putString(getString(R.string.KEY_PREFERENCES_SELF_NAME), finalNickname)
                                                    .putString(getString(R.string.KEY_PREFERENCES_SELF_ICON), defaultIconUri)
                                                    .apply();

                                            // Navigate back to MainActivity
                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            // Show/Enable the buttons, Show the Spinner
                                            nicknameEditText.setEnabled(true);
                                            emailEditText.setEnabled(true);
                                            passwordEditText.setEnabled(true);
                                            confirmPasswordEditText.setEnabled(true);
                                            visibleIcon.setEnabled(true);
                                            signUpButton.setVisibility(View.VISIBLE);
                                            spinner.setVisibility(View.GONE);

                                            // Show the error message
                                            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                            builder.setMessage(task.getException().getMessage())
                                                    .setTitle(R.string.login_error_title)
                                                    .setPositiveButton(android.R.string.ok, null);
                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

}
