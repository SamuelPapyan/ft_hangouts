package com.example.ft_hangouts.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.ft_hangouts.R;
import com.example.ft_hangouts.database.ContactsContract;
import com.example.ft_hangouts.util.NotifyingAsyncQueryHandler;
import com.example.ft_hangouts.util.Util;

public class ContactViewActivity extends BaseActivity{

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

    private static final int QUERY_TOKEN = 1;
    private static final int DELETE_TOKEN = 2;

    private static final String STATE_URI = "uri";

    private Uri mUri;
    private Cursor mCursor;

    private ImageView mAvatarIv;
    private TextView mNameTv;
    private TextView mLastNameTv;
    private RelativeLayout mPhoneHolderRl;
    private TextView mPhoneTv;
    private ImageView mMessageTv;
    private LinearLayout mEmailHolderLl;
    private TextView mEmailTv;
    private TextView mAddressTv;

    private String mName;
    private String mLastName;
    private String mPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            Log.d(TAG, "onCreate: restore instance state");
            String uri = savedInstanceState.getString(STATE_URI);
            if (uri != null) {
                mUri = Uri.parse(uri);
            }
        }

        mAvatarIv = (ImageView) findViewById(R.id.iv_avatar);
        mNameTv = (TextView) findViewById(R.id.tv_name);
        mLastNameTv = (TextView) findViewById(R.id.tv_last_name);
        mPhoneHolderRl = (RelativeLayout) findViewById(R.id.rl_phone_holder);
        mPhoneTv = (TextView) findViewById(R.id.tv_phone);
        mMessageTv = (ImageView) findViewById(R.id.iv_message);
        mEmailHolderLl = (LinearLayout) findViewById(R.id.ll_email_holder);
        mEmailTv = (TextView) findViewById(R.id.tv_email);
        mAddressTv = (TextView) findViewById(R.id.tv_address);

        final Intent intent = getIntent();

        final String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            mUri = intent.getData();
        } else {
            Log.e(TAG, "Unknown action, exiting");
            finish();
        }

        mPhoneHolderRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL);
                i.setData(Uri.parse("tel:" + mPhoneTv.getText()));
                startActivity(i);
            }
        });

        mMessageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Util.ACTION_VIEW_SMS);
                i.setData(mUri);

                i.putExtra(Util.EXTRA_PHONE, mPhone);

                int nameLen = mName.length();
                int lastNameLen = mLastName.length();
                String name;
                if (nameLen == 0 && lastNameLen == 0) {
                    name = mPhone;
                } else if (nameLen > 0 && lastNameLen == 0) {
                    name = mName;
                } else if (nameLen == 0 && lastNameLen > 0) {
                    name = mLastName;
                } else {
                    name = mName + " " + mLastName;
                }
                i.putExtra(Util.EXTRA_NAME, name);
                startActivity(i);
            }
        });

        mEmailHolderLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setData(Uri.parse("mailto:" + mEmailTv.getText()));
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.txt_storage_permission_refused, Toast.LENGTH_LONG).show();
            finish();
        }

        NotifyingAsyncQueryHandler queryHandler = new NotifyingAsyncQueryHandler(this,
                new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                    @Override
                    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                        mCursor = cursor;
                        if (mCursor != null) {
                            mCursor.moveToFirst();
                            setTitle(getText(R.string.title_view));

                            int avatarIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_AVATAR);
                            Util.loadAvatar(Uri.parse(mCursor.getString(avatarIndex)),
                                    mAvatarIv, R.drawable.ic_person_white_24dp,
                                    500, ContactViewActivity.this);

                            int nameIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_NAME);
                            String name = mCursor.getString(nameIndex);
                            mName = name;
                            if (name.length() == 0) {
                                name = getString(R.string.txt_no_name);
                            }
                            mNameTv.setTextKeepState(name);

                            int lastNameIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_LAST_NAME);
                            String lastName = mCursor.getString(lastNameIndex);
                            mLastName = lastName;
                            if (lastName.length() == 0) {
                                lastName = getString(R.string.txt_no_lastname);
                            }
                            mLastNameTv.setTextKeepState(lastName);

                            int phoneIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_PHONE);
                            String phone = mCursor.getString(phoneIndex);
                            mPhone = phone;
                            if (phone.length() == 0) {
                                phone = getString(R.string.txt_no_phone);
                            }
                            mPhoneTv.setTextKeepState(phone);

                            int emailIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_EMAIL);
                            String email = mCursor.getString(emailIndex);
                            if (email.length() == 0) {
                                email = getString(R.string.txt_no_email);
                            }
                            mEmailTv.setTextKeepState(email);

                            int addressIndex = mCursor.getColumnIndex(
                                    ContactsContract.Contacts.COLUMN_NAME_ADDRESS);
                            String address = mCursor.getString(addressIndex);
                            if (address.length() == 0) {
                                address = getString(R.string.txt_no_address);
                            }
                            mAddressTv.setTextKeepState(address);
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
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState: called");
        if (mUri != null) {
            savedInstanceState.putString(STATE_URI, mUri.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            startActivity(new Intent(Intent.ACTION_EDIT, mUri));
            finish();
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
        startActivity(new Intent(this, ContactsListActivity.class));
        finish();
        super.onBackPressed();
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
                            Toast.makeText(ContactViewActivity.this, R.string.txt_contact_deleted,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            deleteHandler.startDelete(DELETE_TOKEN, null, mUri, null, null);
        } else {
            Log.d(TAG, "deleteEntry: cursor is null");
        }
    }
}
