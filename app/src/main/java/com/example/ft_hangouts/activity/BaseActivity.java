package com.example.ft_hangouts.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.ft_hangouts.App;
import com.example.ft_hangouts.R;
import com.example.ft_hangouts.util.Util;
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String theme = pref.getString(SettingsActivity.PREF_KEY_THEME, "ip");
        Log.d(TAG, "onCreate: theme=" + theme);

        switch (theme) {
            case "ip":
                setTheme(R.style.AppTheme_IndigoPink_NoActionBar);
                break;
            case "tdo":
                setTheme(R.style.AppTheme_TealDeepOrange_NoActionBar);
                break;
            case "bgr":
                setTheme(R.style.AppTheme_BlueGreyRed_NoActionBar);
                break;
            case "dpl":
                setTheme(R.style.AppTheme_DeepPurpleLime_NoActionBar);
                break;
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        App app = (App) this.getApplication();
        if (app.wasInBackground) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            long time = sharedPref.getLong(Util.PREF_LAST_VISIT, System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat(Util.TOAST_DATE_FORMAT, Locale.US);
            Date date = new Date(time);
            String resDate = formatter.format(date);
            String message = getString(R.string.txt_last_visit) + ": " + resDate;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        app.stopActivityTransitionTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        long time = System.currentTimeMillis();
        editor.putLong(Util.PREF_LAST_VISIT, time);
        editor.apply();

        ((App) this.getApplication()).startActivityTransitionTimer();
    }
}
