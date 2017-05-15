package com.pdt.plume;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdt.plume.data.DbContract;
import com.pdt.plume.data.DbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;
import static android.os.Build.ID;
import static com.pdt.plume.R.string.task;
import static com.pdt.plume.StaticRequestCodes.REQUEST_GOOGLE_SIGNIN;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_ALARM;
import static com.pdt.plume.StaticRequestCodes.REQUEST_NOTIFICATION_INTENT;

public class LoginActivity extends AppCompatActivity {

    String LOG_TAG = LoginActivity.class.getSimpleName();

    // UI Elements
    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected Button logInButton;
    protected TextView signUpTextView;
    private ImageView visibleIcon;
    ProgressBar spinner;

    // Firebase Variables
    private DatabaseReference mDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    // Facebook Varaibles
    private CallbackManager mCallbackManager;

    // Google Variables
    GoogleApiClient mGoogleApiClient;

    // Theme Variables
    int mSecondaryColor;

    boolean passwordsAreVisible = false;
    boolean isTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.pdt.plume",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        isTablet = getResources().getBoolean(R.bool.isTablet);

        // Initialise the back button
        if (!isTablet)
            findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        signUpTextView = (TextView) findViewById(R.id.signUpText);
        emailEditText = (EditText) findViewById(R.id.emailField);
        passwordEditText = (EditText) findViewById(R.id.passwordField);
        logInButton = (Button) findViewById(R.id.loginButton);
        visibleIcon = (ImageView) findViewById(R.id.visible);
        spinner = (ProgressBar) findViewById(R.id.progressBar);

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
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
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
                    // Hide the buttons, disable the text fields
                    // and show the progress bar
                    logInButton.setVisibility(View.GONE);
                    signUpTextView.setVisibility(View.GONE);
                    emailEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);
                    visibleIcon.setEnabled(false);
                    spinner.setVisibility(View.VISIBLE);

                    mFirebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Cancel all SQLite Fired Notifications
                                        new Utility().cancelOfflineClassNotifications(LoginActivity.this);
                                        DbHelper dbHelper = new DbHelper(LoginActivity.this);
                                        Cursor tasksCursor = dbHelper.getTaskData();
                                        tasksCursor.moveToFirst();
                                        for (int i = 0; i < tasksCursor.getCount(); i++) {
                                            // Get the data
                                            tasksCursor.moveToPosition(i);
                                            String title = tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_TITLE));
                                            String icon = tasksCursor.getString(tasksCursor.getColumnIndex(DbContract.TasksEntry.COLUMN_ICON));

                                            // Rebuild the notification
                                            final android.support.v4.app.NotificationCompat.Builder builder
                                                    = new NotificationCompat.Builder(LoginActivity.this);
                                            Bitmap largeIcon = null;
                                            try {
                                                largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender
                                                    = new NotificationCompat.WearableExtender().setBackground(largeIcon);

                                            Intent contentIntent = new Intent(LoginActivity.this, TasksDetailActivity.class);
                                            contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(LoginActivity.this);
                                            stackBuilder.addParentStack(TasksDetailActivity.class);
                                            stackBuilder.addNextIntent(contentIntent);
                                            final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);
                                            builder.setContentIntent(contentPendingIntent)
                                                    .setSmallIcon(R.drawable.ic_assignment)
                                                    .setColor(getResources().getColor(R.color.colorPrimary))
                                                    .setContentTitle(getString(R.string.notification_message_reminder))
                                                    .setContentText(title)
                                                    .setAutoCancel(true)
                                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                    .extend(wearableExtender)
                                                    .setDefaults(Notification.DEFAULT_ALL);

                                            Notification notification = builder.build();

                                            Intent notificationIntent = new Intent(LoginActivity.this, TaskNotificationPublisher.class);
                                            notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                                            notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                                            final PendingIntent pendingIntent = PendingIntent.getBroadcast
                                                    (LoginActivity.this, REQUEST_NOTIFICATION_ALARM,
                                                            notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                            alarmManager.cancel(pendingIntent);
                                        }
                                        tasksCursor.close();

                                        // Reschedule all Account based notifications
                                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                        DatabaseReference tasksRef = FirebaseDatabase.getInstance().getReference()
                                                .child("users").child(firebaseUser.getUid()).child("tasks");
                                        tasksRef.addChildEventListener(new ChildEventListener() {
                                            @Override
                                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                // Get the data
                                                String title = dataSnapshot.child("title").getValue(String.class);
                                                String icon = dataSnapshot.child("icon").getValue(String.class);
                                                ArrayList reminderDateMillis = dataSnapshot.child("reminderdate").getValue(ArrayList.class);
                                                ArrayList reminderTimeSeconds = dataSnapshot.child("remindertime").getValue(ArrayList.class);
                                                Calendar c = Calendar.getInstance();
                                                if (reminderDateMillis != null)
                                                    for (int i = 0; i < reminderDateMillis.size(); i++) {
                                                        // Rebuild the notification
                                                        c.setTimeInMillis(((long) reminderDateMillis.get(i)));
                                                        int hour = (int) ((long) reminderTimeSeconds.get(i)) / 3600;
                                                        int minute = (int) (((long) reminderTimeSeconds.get(i)) - hour * 3600) / 60;
                                                        c.set(Calendar.HOUR_OF_DAY, hour);
                                                        c.set(Calendar.MINUTE, minute);
                                                        long notificationMillis = (c.getTimeInMillis());

                                                        // Rebuild the notification
                                                        final android.support.v4.app.NotificationCompat.Builder builder
                                                                = new NotificationCompat.Builder(LoginActivity.this);
                                                        Bitmap largeIcon = null;
                                                        try {
                                                            largeIcon = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(icon));
                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                        final android.support.v4.app.NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                                                                .setBackground(largeIcon);

                                                        Intent contentIntent = new Intent(LoginActivity.this, TasksDetailActivity.class);
                                                        contentIntent.putExtra(getString(R.string.INTENT_EXTRA_ID), ID);
                                                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(LoginActivity.this);
                                                        stackBuilder.addParentStack(TasksDetailActivity.class);
                                                        stackBuilder.addNextIntent(contentIntent);
                                                        final PendingIntent contentPendingIntent = stackBuilder.getPendingIntent(REQUEST_NOTIFICATION_INTENT, 0);
                                                        builder.setContentIntent(contentPendingIntent)
                                                                .setSmallIcon(R.drawable.ic_assignment)
                                                                .setColor(getResources().getColor(R.color.colorPrimary))
                                                                .setContentTitle(getString(R.string.notification_message_reminder))
                                                                .setContentText(title)
                                                                .setAutoCancel(true)
                                                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                                                .extend(wearableExtender)
                                                                .setDefaults(Notification.DEFAULT_ALL);

                                                        Notification notification = builder.build();

                                                        Intent notificationIntent = new Intent(LoginActivity.this, TaskNotificationPublisher.class);
                                                        notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION_ID, 1);
                                                        notificationIntent.putExtra(TaskNotificationPublisher.NOTIFICATION, notification);
                                                        final PendingIntent pendingIntent = PendingIntent.getBroadcast
                                                                (LoginActivity.this, REQUEST_NOTIFICATION_ALARM,
                                                                        notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                                        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                                                        if (((long) reminderDateMillis.get(i)) > 0)
                                                            alarmManager.set(AlarmManager.RTC, new Date(notificationMillis).getTime(), pendingIntent);
                                                    }

                                            }

                                            @Override
                                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                            }

                                            @Override
                                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                                            }

                                            @Override
                                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });

                                        // Start MainActivity.class
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                                        startActivity(intent);
                                    } else {
                                        // Unhide/Undisable the buttons and fields
                                        logInButton.setVisibility(View.VISIBLE);
                                        signUpTextView.setVisibility(View.VISIBLE);
                                        emailEditText.setEnabled(true);
                                        passwordEditText.setEnabled(true);
                                        visibleIcon.setEnabled(true);
                                        spinner.setVisibility(View.GONE);

                                        // Show the error message
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

        // Init Facebook Login
        LoginButton facebookButton = (LoginButton) findViewById(R.id.facebookLoginButton);
        facebookButton.setReadPermissions("email", "public_profile");
        mCallbackManager = CallbackManager.Factory.create();
        facebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
            }
        });

        // Init Google
        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // TODO: Handle unsuccessful sign in
                    }
                }).addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        SignInButton googleLoginButton = (SignInButton) findViewById(R.id.googleLoginButton);
        googleLoginButton.setSize(SignInButton.SIZE_STANDARD);
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(intent, REQUEST_GOOGLE_SIGNIN);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GOOGLE_SIGNIN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Signed out, show unauthenticated UI.
                Log.v(LOG_TAG, "Result message: " + result.getStatus().getStatusMessage());
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.login_error_title))
                        .setMessage(result.getStatus().getStatusMessage())
                        .setPositiveButton(getString(R.string.ok), null)
                        .show();
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } else {
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle(getString(R.string.login_error_title))
                                    .setMessage(task.getException().getMessage())
                                    .setPositiveButton(getString(R.string.ok), null)
                                    .show();
                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        if (task.isSuccessful()) {
                            // Initialise data
                            GraphRequest request = GraphRequest.newMeRequest(
                                    token, new GraphRequest.GraphJSONObjectCallback() {
                                        @Override
                                        public void onCompleted(JSONObject object, GraphResponse response) {
                                            // Application code
                                            try {
                                                final String name = object.getString("name");
                                                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                                                String uid = firebaseUser.getUid();

                                                final DatabaseReference nameRef = FirebaseDatabase.getInstance().getReference()
                                                        .child("users").child(uid).child("nickname");
                                                nameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.getValue() == null)
                                                            nameRef.setValue(name);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name");
                            request.setParameters(parameters);
                            request.executeAsync();

                            // Navigate back to the MainActivity
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(LOG_TAG, "signInWithCredential", task.getException());
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setMessage(getString(R.string.fb_already_authenticated))
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            LoginManager.getInstance().logOut();
                                        }
                                    })
                                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                        @Override
                                        public void onDismiss(DialogInterface dialogInterface) {
                                            LoginManager.getInstance().logOut();
                                        }
                                    })
                                    .show();
                        }
                    }
                });
    }


}
