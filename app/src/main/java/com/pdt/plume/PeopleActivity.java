package com.pdt.plume;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.editable;

public class PeopleActivity extends AppCompatActivity
        implements IconDialogFragment.iconDialogListener,
        NameDialogFragment.onNameSelectedListener {

    String LOG_TAG = PeopleActivity.class.getSimpleName();
    String dummyToken = "dV8vdMhYU34:APA91bHPGoRMky6-LWnWaXJvqBK5aHF1js27mS3-MxKyacvoDnzIbo7URusepOWO1KE6oJl3ejCh3tWZ2zAVxv97JMM0XQuY36KG5wePdbNbQ9ZuzIoq91WSeOiQ7xHiOIEJmstKw7NZ";

    // UI Variables
    ImageView selfIconView;
    TextView selfNameView;

    // UI Data
    String selfIconUri, selfName;

    // Theme Variables
    int mPrimaryColor, mDarkColor;

    private static int REQUEST_IMAGE_GET_ICON = 0;
    private Integer[] mThumbIds = {
            R.drawable.art_arts_64dp,
            R.drawable.art_biology_64dp,
            R.drawable.art_business_64dp,
            R.drawable.art_chemistry_64dp,
            R.drawable.art_childdevelopment_64dp,
            R.drawable.art_class_64dp,
            R.drawable.art_computing_64dp,
            R.drawable.art_cooking_64dp,
            R.drawable.art_creativestudies_64dp,
            R.drawable.art_drama_64dp,
            R.drawable.art_engineering_64dp,
            R.drawable.art_english_64dp,
            R.drawable.art_french_64dp,
            R.drawable.art_geography_64dp,
            R.drawable.art_graphics_64dp,
            R.drawable.art_hospitality_64dp,
            R.drawable.art_ict_64dp,
            R.drawable.art_maths_64dp,
            R.drawable.art_media_64dp,
            R.drawable.art_music_64dp,
            R.drawable.art_pe_64dp,
            R.drawable.art_physics_64dp,
            R.drawable.art_psychology_64dp,
            R.drawable.art_re_64dp,
            R.drawable.art_science_64dp,
            R.drawable.art_spanish_64dp,
            R.drawable.art_task_64dp,
            R.drawable.art_woodwork_64dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);

        // Initialise the theme variables
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mPrimaryColor  = preferences.getInt(getString(R.string.KEY_THEME_PRIMARY_COLOR), getResources().getColor(R.color.colorPrimary));
        float[] hsv = new float[3];
        int tempColor = mPrimaryColor;
        Color.colorToHSV(tempColor, hsv);
        hsv[2] *= 0.8f; // value component
        mDarkColor = Color.HSVToColor(hsv);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(mPrimaryColor));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mDarkColor);
        }

        // Initialise the UI
        selfIconView = (ImageView) findViewById(R.id.icon);
        selfNameView = (TextView) findViewById(R.id.name);

        // If there is previously set data in shared preferences, set it accordingly
        String savedName = preferences.getString(getString(R.string.KEY_PREFERENCES_SELF_NAME), getString(R.string.yourNameHere));
        String savedIconUri = preferences.getString(getString(R.string.KEY_PREFERENCES_SELF_ICON), null);
        selfNameView.setText(savedName);
        if (savedIconUri != null)
            try {
                selfIconView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(savedIconUri)));
            } catch (IOException e) {
                e.printStackTrace();
            }

        selfIconView.setOnClickListener(showIconDialog());
        selfNameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NameDialogFragment fragment = NameDialogFragment.newInstance(selfName);
                fragment.show(getSupportFragmentManager(), "dialog");
            }
        });

        LinearLayout addPeersButton = (LinearLayout) findViewById(R.id.addPeersLayout);
        LinearLayout viewPeersButton = (LinearLayout) findViewById(R.id.viewPeersLayout);
        addPeersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PeopleActivity.this, AddPeerActivity.class));
            }
        });
        viewPeersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PeopleActivity.this, PeersActivity.class));
            }
        });

        ImageView QRCodeView = (ImageView) findViewById(R.id.qr);
        Bitmap QRCodeBitmap = generateQRCode(dummyToken);
        QRCodeView.setImageBitmap(QRCodeBitmap);
    }

    private View.OnClickListener showIconDialog() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dialog = new IconDialogFragment();
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        };
    }

    @Override
    public void OnIconListItemSelected(int item) {
        switch (item) {
            case 0:
                showBuiltInIconsDialog();
                break;
            case 1:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                if (intent.resolveActivity(getPackageManager()) != null)
                    startActivityForResult(intent, REQUEST_IMAGE_GET_ICON);
                break;
        }
    }

    private void showBuiltInIconsDialog() {
        // Prepare grid view
        GridView gridView = new GridView(this);
        final AlertDialog dialog;

        int[] builtinIcons = getResources().getIntArray(R.array.builtin_icons);
        List<Integer> mList = new ArrayList<>();
        for (int i = 1; i < builtinIcons.length; i++) {
            mList.add(builtinIcons[i]);
        }

        gridView.setAdapter(new BuiltInIconsAdapter(this));
        gridView.setNumColumns(4);
        gridView.setPadding(0, 16, 0, 16);
        gridView.setGravity(Gravity.CENTER);
        // Set grid view to alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(gridView);
        builder.setTitle(getString(R.string.new_schedule_icon_builtin_title));
        dialog = builder.show();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int resId = mThumbIds[position];
                selfIconView.setImageResource(resId);
                Resources resources = getResources();
                Uri drawableUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + resources.getResourcePackageName(resId)
                        + '/' + resources.getResourceTypeName(resId) + '/' + resources.getResourceEntryName(resId));
                selfIconUri = drawableUri.toString();
                PreferenceManager.getDefaultSharedPreferences(PeopleActivity.this).edit()
                        .putString(getString(R.string.KEY_PREFERENCES_SELF_ICON), selfIconUri)
                        .apply();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Custom Icon Upload
        if (requestCode == REQUEST_IMAGE_GET_ICON && resultCode == RESULT_OK) {
            Uri dataUri = data.getData();
            Bitmap setImageBitmap = null;

            try {
                setImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dataUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            selfIconView.setImageBitmap(setImageBitmap);

            // Save the icon uri
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(getString(R.string.KEY_PREFERENCES_SELF_ICON), dataUri.toString())
                    .apply();

        }
    }

    @Override
    public void onNameSelected(String name) {
        selfName = name;
        selfNameView.setText(name);
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString(getString(R.string.KEY_PREFERENCES_SELF_NAME), name)
                .apply();
    }

    private Bitmap generateQRCode(String token) {
        //Find screen size
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3/4;

        //Encode with a QR Code image
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(token,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            return bitmap;

        } catch (WriterException e) {
            e.printStackTrace();
        }

        return null;
    }
}
