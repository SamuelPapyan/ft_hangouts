package com.example.ft_hangouts.activity;

import android.Manifest;

import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;

import android.content.Intent;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ft_hangouts.R;
import com.example.ft_hangouts.adapter.ContactsCursorRecyclerAdapter;
import com.example.ft_hangouts.database.ContactsContract;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class ContactsListActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "ContactsListActivity";

    private static final String[] PROJECTION = new String[]{
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.COLUMN_NAME_AVATAR,
            ContactsContract.Contacts.COLUMN_NAME_NAME,
            ContactsContract.Contacts.COLUMN_NAME_LAST_NAME,
            ContactsContract.Contacts.COLUMN_NAME_PHONE,
    };
    private static final int REQUEST_READ_MEDIA_IMAGES = 0;

    private static final int REQUEST_READ_EXTERNAL_STORAGE = 2;

    private static final int CONTACTS_LOADER_ID = 1;

    private CoordinatorLayout mCoordinatorLayout;
    private TextView mContactsNotAvailableTv;
    private RecyclerView mRecyclerView;
    private ContactsCursorRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        Intent intent = getIntent();

        if (intent.getData() == null) {
            intent.setData(ContactsContract.Contacts.CONTENT_URI);
        }

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mContactsNotAvailableTv = (TextView) findViewById(R.id.tv_contacts_not_available);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33 && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            initRecyclerView();
            LoaderManager lm = getSupportLoaderManager();
            lm.initLoader(CONTACTS_LOADER_ID, null, this);
        } else {
            mContactsNotAvailableTv.setVisibility(View.VISIBLE);
            requestReadStoragePermission();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called");
        if (requestCode == REQUEST_READ_MEDIA_IMAGES || requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate();
            } else {
                Toast.makeText(this, R.string.txt_storage_permission_refused, Toast.LENGTH_LONG).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void requestReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= 33 && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < 33 && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(mCoordinatorLayout, R.string.txt_read_storage_rationale,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.txt_ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Build.VERSION.SDK_INT >= 33)
                                ActivityCompat.requestPermissions(ContactsListActivity.this,
                                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                                        REQUEST_READ_MEDIA_IMAGES);
                            else
                                ActivityCompat.requestPermissions(ContactsListActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    })
                    .show();
        } else {
            Log.d(TAG, "requesting permissions...");
            if (Build.VERSION.SDK_INT >= 33)
                ActivityCompat.requestPermissions(ContactsListActivity.this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_READ_MEDIA_IMAGES);
            else
                ActivityCompat.requestPermissions(ContactsListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    private void initRecyclerView() {
        mContactsNotAvailableTv.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ContactsCursorRecyclerAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(
                this,
                ContactsContract.Contacts.CONTENT_URI,
                PROJECTION,
                null,
                null,
                ContactsContract.Contacts.DEFAULT_SORT_ORDER
        );
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        mAdapter.swapCursor(null);
    }
}