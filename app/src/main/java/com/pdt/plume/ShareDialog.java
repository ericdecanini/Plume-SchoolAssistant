package com.pdt.plume;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class ShareDialog extends DialogFragment {

    public static ShareDialog newInstance() {
        ShareDialog fragment = new ShareDialog();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_share, container);

        Button button = (Button) rootView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performShareIntent();
            }
        });

        rootView.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return rootView;
    }

    private void performShareIntent() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            String sAux = "\n" + getString(R.string.recommendation) + "\n\n";
            sAux = sAux + getString(R.string.app_link) + "\n\n";
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, getString(R.string.select_application)));
        } catch(Exception e) {
            //e.toString();
        }
        dismiss();
    }

}
