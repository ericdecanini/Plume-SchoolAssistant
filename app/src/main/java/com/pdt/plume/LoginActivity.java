package com.pdt.plume;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.*;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    // UI Elements
    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected Button logInButton;
    protected TextView signUpTextView;
    private ImageView visibleIcon;

    // Firebase Variables
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;

    // Theme Variables
    int mSecondaryColor;

    boolean passwordsAreVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        signUpTextView = (TextView) findViewById(R.id.signUpText);
        emailEditText = (EditText) findViewById(R.id.emailField);
        passwordEditText = (EditText) findViewById(R.id.passwordField);
        logInButton = (Button) findViewById(R.id.loginButton);
        visibleIcon = (ImageView) findViewById(R.id.visible);

        mSecondaryColor = getResources().getColor(R.color.colorAccent);

        visibleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordsAreVisible) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    passwordEditText.setSelection(passwordEditText.getText().length());
                    visibleIcon.setImageTintList(getResources().getColorStateList(R.color.white));
                    passwordsAreVisible = false;
                } else {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordEditText.setSelection(passwordEditText.getText().length());
                    visibleIcon.setImageTintList(getResources().getColorStateList(R.color.colorAccent));
                    passwordsAreVisible = true;
                }
            }
        });


        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                email = email.trim();
                password = password.trim();

                if (email.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    mFirebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                                        startActivity(intent);
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
