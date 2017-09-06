package com.pdt.plume;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static android.R.attr.textColor;


public class MatchingClassAdapter extends ArrayAdapter {

    String LOG_TAG = MatchingClassAdapter.class.getSimpleName();

    Context context;
    int layoutResourceId;
    ArrayList<MatchingClass> data = null;

    public MatchingClassAdapter(Context context, int layoutResourceId, ArrayList<MatchingClass> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.icon = (ImageView) row.findViewById(R.id.icon);
            holder.title = (TextView) row.findViewById(R.id.title);

            holder.title2 = (TextView) row.findViewById(R.id.title2);
            holder.delete = (ImageView) row.findViewById(R.id.delete);

            row.setTag(holder);
        }

        else {
            holder = (ViewHolder) row.getTag();
        }

        int textColor = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.KEY_THEME_TITLE_COLOUR), context.getResources().getColor(R.color.gray_900));
        int mPrimaryColor = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.KEY_THEME_PRIMARY_COLOR), context.getResources().getColor(R.color.colorPrimary));

        final MatchingClass classItem = data.get(position);

        if (classItem.activated == 1) {
            holder.title.setTextColor(mPrimaryColor);
            holder.title.setTypeface(null, Typeface.BOLD);
        } else {
            holder.title.setTextColor(textColor);
            holder.title.setTypeface(null, Typeface.NORMAL);
        }


        if (holder.delete != null) holder.delete.setColorFilter(textColor);
        if (holder.title2 != null) {
            holder.title2.setTextColor(textColor);
            holder.title2.setText(classItem.title);
        }

        if (classItem.originalTitle.equals("") && classItem.originalIcon.equals("")) {
            holder.title.setText(classItem.title);
            holder.icon.setImageURI(Uri.parse(classItem.icon));
        } else {
            holder.title.setText(classItem.originalTitle);
            holder.icon.setImageURI(Uri.parse(classItem.originalIcon));

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle(context.getString(R.string.confirm_delete_shared_title))
                            .setMessage(context.getString(R.string.irreversible))
                            .setNegativeButton(context.getString(R.string.cancel), null)
                            .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FirebaseUser mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    String mUserId = mFirebaseUser.getUid();
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("users").child(mUserId).child("requests")
                                            .child(((Activity) context).getIntent().getStringExtra("id"))
                                            .child("classes").child(classItem.originalTitle)
                                            .removeValue();
                                    remove(classItem);
                                    notifyDataSetChanged();

                                    // No items left, navigate back to AcceptPeerActivity
                                    if (getCount() == 0) {
                                        ((Activity) context).finish();
                                    }

                                }
                            }).show();
                }
            });
        }

        return row;
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView title2;
        ImageView delete;
    }

}
