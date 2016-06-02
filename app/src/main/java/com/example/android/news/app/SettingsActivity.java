package com.example.android.news.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.android.news.app.database.NewsContract;

public class SettingsActivity extends AppCompatActivity {
    public static final String NewsPref = "NewsPref";
    public static final String NOTIFICATION = "notification";
    public static final String MODE_NETWORK = "network";

    private TextView mTextViewShowCache;
    private AlertDialog mAlertDialog;
    private Switch mSwitchNotify;
    private Switch mSwitchModeNetwork;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTextViewShowCache = (TextView) findViewById(R.id.textview_show_cache);
        mSwitchNotify = (Switch) findViewById(R.id.switch_notification);
        mSwitchModeNetwork = (Switch) findViewById(R.id.switch_mode_network);

        boolean checked = getSharedPreferences(NewsPref, Context.MODE_PRIVATE).getBoolean(NOTIFICATION, false);
        mSwitchNotify.setChecked(checked);
        checked = getSharedPreferences(NewsPref, Context.MODE_PRIVATE).getBoolean(MODE_NETWORK, true);
        mSwitchModeNetwork.setChecked(checked);

        mSwitchNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveToSharedPreferences(NOTIFICATION, isChecked);
            }
        });

        mSwitchModeNetwork.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveToSharedPreferences(MODE_NETWORK, isChecked);
            }
        });

        setupDialog();
        getCacheSize();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title_dialog);
        builder.setMessage(R.string.message_dialog);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearCache();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        mAlertDialog = builder.create();
    }

    private void getCacheSize() {
        Cursor resultCursor = getContentResolver().query(NewsContract.DetailEntry.CONTENT_URI, null, null, null, null);
        if (resultCursor.getCount() <= 0) {
            mTextViewShowCache.setText("Cached 0 records");
        } else {
            mTextViewShowCache.setText("Cached " + resultCursor.getCount() + " records");
        }
    }

    public void deleteCache(View view) {
        mAlertDialog.show();
    }

    private void clearCache() {
        getContentResolver().delete(NewsContract.DetailEntry.CONTENT_URI, null, null);
        getCacheSize();
    }

    private void saveToSharedPreferences(String category, boolean isChecked) {
        mSharedPreferences = getSharedPreferences(NewsPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        if (category.equals(NOTIFICATION)) {
            editor.putBoolean(NOTIFICATION, isChecked);
        } else if (category.equals(MODE_NETWORK)) {
            editor.putBoolean(MODE_NETWORK, isChecked);
        }
        editor.commit();
    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return super.getSupportParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
