package com.example.ft_hangouts.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ft_hangouts.R;
import com.example.ft_hangouts.database.ContactsContract;
import com.example.ft_hangouts.util.NotifyingAsyncQueryHandler;
import com.example.ft_hangouts.util.Util;

public class ContactEditActivity extends BaseActivity{
    private static final String TAG = "ContactViewActivity";

    private static final String[] PROJECTION =
            new String[]{
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.COLUMN_NAME_AVATAR,
                    ContactsContract.Contacts.COLUMN_NAME_NAME,
                    ContactsContract.Contacts.COLUMN_NAME_LAST_NAME,
                    ContactsContract.Contacts.COLUMN_NAME_PHONE,
                    ContactsContract.Contacts.COLUMN_NAME_EMAIL,
                    ContactsContract.Contacts.COLUMN_NAME_ADDRESS
            };

    private static final int ACTIVITY_STATE_EDIT = 0;
    private static final int ACTIVITY_STATE_INSERT = 1;

    private static final int QUERY_TOKEN = 1;
    private static final int INSERT_TOKEN = 2;
    private static final int UPDATE_TOKEN = 3;
    private static final int DELETE_TOKEN = 4;

    private static final int PICK_AVATAR_CODE = 0;

    private static final String STATE_STATE = "state";
    private static final String STATE_URI = "uri";
    private static final String STATE_AVATAR_URI = "avatar_uri";

    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private Uri mAvatarUri;

    private FrameLayout mAvatarHolderFl;
    private ImageView mAvatarIv;
    private EditText mNameEt;
    private EditText mLastNameEt;
    private EditText mPhoneEt;
    private EditText mEmailEt;
    private EditText mAddressEt;

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: restore instance state");
            mState = savedInstanceState.getInt(STATE_STATE);
            String uri = savedInstanceState.getString(STATE_URI);
            if (uri != null) {
                mUri = Uri.parse(uri);
            }
            String avatarUri = savedInstanceState.getString(STATE_AVATAR_URI);
            if (avatarUri != null) {
                mAvatarUri = Uri.parse(avatarUri);
            }
        }

        mAvatarIv = (ImageView) findViewById(R.id.iv_avatar);
        Util.loadAvatar(mAvatarUri, mAvatarIv, R.drawable.ic_person_white_24dp, 500, ContactEditActivity.this);

        final Intent intent = getIntent();

        final String action = intent.getAction();

        if (Intent.ACTION_EDIT.equals(action)) {
            Log.d(TAG, "onCreate: state edit");

            mState = ACTIVITY_STATE_EDIT;
            mUri = intent.getData();
        } else if (Intent.ACTION_INSERT.equals(action)) {
            Log.d(TAG, "onCreate: state insert");

            mState = ACTIVITY_STATE_INSERT;
            setTitle(getText(R.string.title_create));
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        mAvatarHolderFl = (FrameLayout) findViewById(R.id.fl_avatar_holder);
        mNameEt = (EditText) findViewById(R.id.et_name);
        mLastNameEt = (EditText) findViewById(R.id.et_last_name);
        mPhoneEt = (EditText) findViewById(R.id.et_phone);
        mEmailEt = (EditText) findViewById(R.id.et_email);
        mAddressEt = (EditText) findViewById(R.id.et_address);

        mAvatarHolderFl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseAvatarDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: called");
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.txt_storage_permission_refused, Toast.LENGTH_LONG).show();
            finish();
        }

        if (mState == ACTIVITY_STATE_EDIT) {
            Log.d(TAG, "onResume: state edit, query");
            NotifyingAsyncQueryHandler queryHandler = new NotifyingAsyncQueryHandler(this,
                    new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                        @Override
                        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                            Log.d(TAG, "onQueryComplete: called");
                            mCursor = cursor;

                            if (mCursor != null) {
                                mCursor.moveToFirst();

                                setTitle(getText(R.string.title_edit));

                                if (mAvatarUri == null) {
                                    int avatarIndex = mCursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_AVATAR);
                                    mAvatarUri = Uri.parse(mCursor.getString(avatarIndex));
                                    Util.loadAvatar(mAvatarUri, mAvatarIv, R.drawable.ic_person_white_24dp, 500, ContactEditActivity.this);
                                }

                                int nameIndex = mCursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_NAME);
                                String name = mCursor.getString(nameIndex);
                                mNameEt.setTextKeepState(name);

                                int lastNameIndex = mCursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_LAST_NAME);
                                String lastName = mCursor.getString(lastNameIndex);
                                mLastNameEt.setTextKeepState(lastName);

                                int phoneIndex = mCursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_PHONE);
                                String phone = mCursor.getString(phoneIndex);
                                mPhoneEt.setTextKeepState(phone);

                                int emailIndex = mCursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_EMAIL);
                                String email = mCursor.getString(emailIndex);
                                mEmailEt.setTextKeepState(phone);

                                int addressIndex = mCursor.getColumnIndex(ContactsContract.Contacts.COLUMN_NAME_ADDRESS);
                                String address = mCursor.getString(addressIndex);
                                mAddressEt.setTextKeepState(address);
                            } else {
                                setTitle(getText(R.string.title_error));
                            }
                        }

                        @Override
                        public void onInsertComplete(int token, Object cookie, Uri uri) {
                            // nothing
                        }

                        @Override
                        public void onUpdateComplete(int token, Object cookie, int result) {
                            // nothing
                        }

                        @Override
                        public void onDeleteComplete(int token, Object cookie, int result) {
                            // nothing
                        }
                    });
            queryHandler.startQuery(QUERY_TOKEN, null, mUri, PROJECTION, null, null, null);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState: called");
        savedInstanceState.putInt(STATE_STATE, mState);
        if (mAvatarUri != null) {
            savedInstanceState.putString(STATE_AVATAR_URI, mAvatarUri.toString());
        }
        if (mUri != null) {
            savedInstanceState.putString(STATE_URI, mUri.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: called");
        getMenuInflater().inflate(R.menu.menu_contact_edit, menu);
        if (mState == ACTIVITY_STATE_INSERT) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveEntry();
            return true;
        } else if (id == R.id.action_delete) {
            deleteEntry();
            finish();
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mState == ACTIVITY_STATE_INSERT) {
            startActivity(new Intent(this, ContactsListActivity.class));
            finish();
        } else if (mState == ACTIVITY_STATE_EDIT) {
            startActivity(new Intent(Intent.ACTION_VIEW, mUri));
            finish();
        }
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: called");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AVATAR_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Log.d(TAG, "onActivityResult: uri=" + data.getData());
                mAvatarUri = data.getData();
                Util.loadAvatar(mAvatarUri, mAvatarIv, R.drawable.ic_person_white_24dp, 500, ContactEditActivity.this);
            } else {
                Toast.makeText(this, R.string.txt_avatar_load_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveEntry() {
        Log.d(TAG, "saveEntry: called");

        ContentValues values = new ContentValues();
        if (mState == ACTIVITY_STATE_EDIT) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_MODIFICATION_DATE,
                    System.currentTimeMillis());
        }

        String avatarUri = "";
        if (mAvatarUri != null) {
            avatarUri = mAvatarUri.toString();
        }
        String name = mNameEt.getText().toString();
        String lastName = mLastNameEt.getText().toString();
        String phone = mPhoneEt.getText().toString();
        String email = mEmailEt.getText().toString();
        String address = mAddressEt.getText().toString();

        values.put(ContactsContract.Contacts.COLUMN_NAME_AVATAR, avatarUri);
        values.put(ContactsContract.Contacts.COLUMN_NAME_NAME, name);
        values.put(ContactsContract.Contacts.COLUMN_NAME_LAST_NAME, lastName);
        values.put(ContactsContract.Contacts.COLUMN_NAME_PHONE, phone);
        values.put(ContactsContract.Contacts.COLUMN_NAME_EMAIL, email);
        values.put(ContactsContract.Contacts.COLUMN_NAME_ADDRESS, address);

        NotifyingAsyncQueryHandler saveHandler = new NotifyingAsyncQueryHandler(this,
                new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                    @Override
                    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                        // nothing
                    }
                    @Override
                    public void onInsertComplete(int token, Object cookie, Uri uri) {
                        Toast.makeText(ContactEditActivity.this, R.string.txt_contact_created, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        finish();
                    }

                    @Override
                    public void onUpdateComplete(int token, Object cookie, int result) {
                        Toast.makeText(ContactEditActivity.this, R.string.txt_contact_updated, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Intent.ACTION_VIEW, mUri));
                        finish();
                    }

                    @Override
                    public void onDeleteComplete(int token, Object cookie, int result) {
                        // nothing
                    }
                });
        if (mState == ACTIVITY_STATE_INSERT) {
            saveHandler.startInsert(INSERT_TOKEN, null, ContactsContract.Contacts.CONTENT_URI, values);
        } else if (mState == ACTIVITY_STATE_EDIT) {
            saveHandler.startUpdate(UPDATE_TOKEN, null, mUri, values, null, null);
        }
    }

    private void deleteEntry() {
        Log.d(TAG, "deleteEntry: called");
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            NotifyingAsyncQueryHandler deleteHandler = new NotifyingAsyncQueryHandler(this,
                    new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                        @Override
                        public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                            // nothing
                        }

                        @Override
                        public void onInsertComplete(int token, Object cookie, Uri uri) {
                            // nothing
                        }

                        @Override
                        public void onUpdateComplete(int token, Object cookie, int result) {
                            // nothing
                        }

                        @Override
                        public void onDeleteComplete(int token, Object cookie, int result) {
                            Toast.makeText(ContactEditActivity.this, R.string.txt_contact_deleted, Toast.LENGTH_SHORT).show();
                        }
                    });
            deleteHandler.startDelete(DELETE_TOKEN, null, mUri, null, null);
        } else {
            Log.d(TAG, "deleteEntry: cursor is null");
        }
    }

    private void showChooseAvatarDialog() {
        CharSequence[] items = {
                getString(R.string.txt_choose_avatar),
                getString(R.string.txt_remove_avatar)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.txt_change_avatar);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, PICK_AVATAR_CODE);
                        break;
                    case 1:
                        mAvatarUri = Uri.parse("");
                        Util.loadAvatar(null, mAvatarIv, R.drawable.ic_person_white_24dp, 500, ContactEditActivity.this);
                        break;
                }
            }
        });
        builder.setNegativeButton(R.string.txt_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}