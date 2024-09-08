package com.example.ft_hangouts.activity;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ft_hangouts.R;
import com.example.ft_hangouts.adapter.SmsCursorRecyclerAdapter;
import com.example.ft_hangouts.util.Util;
import com.google.android.material.snackbar.Snackbar;

public class SmsActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "SmsActivity";

    private static final String[] SMS_PROJECTION = new String[]{
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.TYPE,
            Telephony.Sms.DATE
    };
    private static final String SMS_SORT_ORDER = Telephony.Sms.DATE + " ASC";
    private static final int REQUEST_READ_SMS = 1;
    private static final int SMS_LOADER_ID = 2;

    private Uri mUri;
    private String mPhone;
    private String mName;
    private SmsCursorRecyclerAdapter mAdapter;

    private CoordinatorLayout mCoordinatorLayout;
    private TextView mNoMessagesTv;
    private RecyclerView mRecyclerView;
    private EditText mSendMessageEt;
    private ImageButton mSendMessageIb;

    private static final String BUFFER_MESSAGE = "buffer_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Intent intent = getIntent();

        final String action = intent.getAction();

        if (Util.ACTION_VIEW_SMS.equals(action)) {
            mUri = intent.getData();
            Bundle extras = intent.getExtras();
            if (extras != null) {
                mName = extras.getString(Util.EXTRA_NAME);
                mPhone = extras.getString(Util.EXTRA_PHONE);
            }
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
        }

        setTitle(mName);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mNoMessagesTv = (TextView) findViewById(R.id.tv_no_messages);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSendMessageEt = (EditText) findViewById(R.id.et_send_message);
        mSendMessageIb = (ImageButton) findViewById(R.id.ib_send_message);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new SmsCursorRecyclerAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(SMS_LOADER_ID, null, this);
        } else {
            mNoMessagesTv.setText(R.string.txt_no_sms_permission);
            requestReadSmsPermission();
        }

        mSendMessageIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(SmsActivity.this,
                        Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {
                    String message = mSendMessageEt.getText().toString();
                    if (message.length() > 0) {
                        SmsManager.getDefault().sendTextMessage(mPhone, null, message, null, null);
                        mSendMessageEt.setText(null);
                    } else {
                        Toast.makeText(SmsActivity.this, R.string.txt_empty_message,
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    requestReadSmsPermission();
                }
            }
        });

        if (savedInstanceState != null) {
            mSendMessageEt.setTextKeepState(savedInstanceState.getString(BUFFER_MESSAGE));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState: called");
        savedInstanceState.putString(BUFFER_MESSAGE, mSendMessageEt.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: called");
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: called");
        super.onBackPressed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: called");
        String selection = Telephony.Sms.ADDRESS + "=?";
        String[] selectionArgs = {mPhone};
        return new CursorLoader(
                this,
                Telephony.Sms.CONTENT_URI,
                SMS_PROJECTION,
                selection,
                selectionArgs,
                SMS_SORT_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: cursor size=" + data.getCount());
        if (data.getCount() > 0) {
            mNoMessagesTv.setVisibility(View.GONE);
            mAdapter.swapCursor(data);
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: called");
        mAdapter.swapCursor(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        if (requestCode == REQUEST_READ_SMS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            } else {
                Toast.makeText(this, R.string.txt_sms_permission_refused, Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    private void requestReadSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_SMS)) {
            Snackbar.make(mCoordinatorLayout, R.string.txt_read_sms_rationale,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.txt_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(SmsActivity.this,
                                    new String[]{Manifest.permission.READ_SMS},
                                    REQUEST_READ_SMS);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS},
                    REQUEST_READ_SMS);
        }
    }
}