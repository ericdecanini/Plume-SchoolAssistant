package com.pdt.plume;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.*;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    String LOG_TAG = SignUpActivity.class.getSimpleName();

    private DatabaseReference mDatabase;
    private String mUserId;
    private static String defaultIconUri = "android.resource://com.pdt.plume/drawable/ic_person_white";

    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected EditText nicknameEditText;
    protected Button signUpButton;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize FirebaseAuth and Database
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        passwordEditText = (EditText)findViewById(R.id.passwordField);
        emailEditText = (EditText)findViewById(R.id.emailField);
        nicknameEditText = (EditText)findViewById(R.id.nicknameField);
        signUpButton = (Button)findViewById(R.id.signupButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEditText.getText().toString();
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
                    // Successful sign up
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
        });
    }

}
