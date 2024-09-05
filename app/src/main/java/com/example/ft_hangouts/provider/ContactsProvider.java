package com.example.ft_hangouts.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;

import com.example.ft_hangouts.database.ContactsContract;

public class ContactsProvider extends ContentProvider{
    private static final String TAG = "ContactsProvider";

    private static final String DATABASE_NAME = "contacts.db";

    private static final int DATABASE_VERSION = 1;

    private static HashMap<String, String> sContactsProjectionMap;


    private static final int ENTRIES = 1;

    private static final int ENTRY_ID = 2;

    private static final UriMatcher sUriMatcher;

    private DatabaseHelper mOpenHelper;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(ContactsContract.AUTHORITY, "contacts", ENTRIES);

        sUriMatcher.addURI(ContactsContract.AUTHORITY, "contacts/#", ENTRY_ID);


        sContactsProjectionMap = new HashMap<>();

        sContactsProjectionMap.put(ContactsContract.Contacts._ID, ContactsContract.Contacts._ID);

        sContactsProjectionMap.put(ContactsContract.Contacts.COLUMN_NAME_AVATAR,
                ContactsContract.Contacts.COLUMN_NAME_AVATAR);

        sContactsProjectionMap.put(ContactsContract.Contacts.COLUMN_NAME_NAME,
                ContactsContract.Contacts.COLUMN_NAME_NAME);

        sContactsProjectionMap.put(ContactsContract.Contacts.COLUMN_NAME_LAST_NAME,
                ContactsContract.Contacts.COLUMN_NAME_LAST_NAME);

        sContactsProjectionMap.put(ContactsContract.Contacts.COLUMN_NAME_PHONE,
                ContactsContract.Contacts.COLUMN_NAME_PHONE);

        sContactsProjectionMap.put(ContactsContract.Contacts.COLUMN_NAME_EMAIL,
                ContactsContract.Contacts.COLUMN_NAME_EMAIL);

        sContactsProjectionMap.put(ContactsContract.Contacts.COLUMN_NAME_ADDRESS,
                ContactsContract.Contacts.COLUMN_NAME_ADDRESS);

        sContactsProjectionMap.put(ContactsContract.Contacts.COLUMN_NAME_CREATE_DATE,
                ContactsContract.Contacts.COLUMN_NAME_CREATE_DATE);

        sContactsProjectionMap.put(ContactsContract.Contacts.COLUMN_NAME_MODIFICATION_DATE,
                ContactsContract.Contacts.COLUMN_NAME_MODIFICATION_DATE);
    }

    static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + ContactsContract.Contacts.TABLE_NAME + " ("
                    + ContactsContract.Contacts._ID + " INTEGER PRIMARY KEY,"
                    + ContactsContract.Contacts.COLUMN_NAME_AVATAR + " TEXT,"
                    + ContactsContract.Contacts.COLUMN_NAME_NAME + " TEXT,"
                    + ContactsContract.Contacts.COLUMN_NAME_LAST_NAME + " TEXT,"
                    + ContactsContract.Contacts.COLUMN_NAME_PHONE + " TEXT,"
                    + ContactsContract.Contacts.COLUMN_NAME_EMAIL + " TEXT,"
                    + ContactsContract.Contacts.COLUMN_NAME_ADDRESS + " TEXT,"
                    + ContactsContract.Contacts.COLUMN_NAME_CREATE_DATE + " INTEGER,"
                    + ContactsContract.Contacts.COLUMN_NAME_MODIFICATION_DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + ContactsContract.Contacts.TABLE_NAME);

            onCreate(db);
        }
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(ContactsContract.Contacts.TABLE_NAME);

        switch (sUriMatcher.match(uri)) {
            case ENTRIES:
                qb.setProjectionMap(sContactsProjectionMap);
                break;

            case ENTRY_ID:
                qb.setProjectionMap(sContactsProjectionMap);
                qb.appendWhere(
                        ContactsContract.Contacts._ID
                        + "="
                        + uri.getPathSegments().get(ContactsContract.Contacts.ENTRY_ID_PATH_POSITION)
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        String orderBy;

        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = ContactsContract.Contacts.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor c = qb.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
        );

        if (getContext() != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case ENTRIES:
                return ContactsContract.Contacts.CONTENT_TYPE;

            case ENTRY_ID:
                return ContactsContract.Contacts.CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        if (sUriMatcher.match(uri) != ENTRIES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = System.currentTimeMillis();

        if (!values.containsKey(ContactsContract.Contacts.COLUMN_NAME_CREATE_DATE)) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_CREATE_DATE, now);
        }

        if (!values.containsKey((ContactsContract.Contacts.COLUMN_NAME_MODIFICATION_DATE))) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_MODIFICATION_DATE, now);
        }

        if (!values.containsKey(ContactsContract.Contacts.COLUMN_NAME_AVATAR)) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_AVATAR, "");
        }

        if (!values.containsKey(ContactsContract.Contacts.COLUMN_NAME_NAME)) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_NAME, "");
        }

        if (!values.containsKey(ContactsContract.Contacts.COLUMN_NAME_LAST_NAME)) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_LAST_NAME, "");
        }

        if (!values.containsKey(ContactsContract.Contacts.COLUMN_NAME_PHONE)) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_PHONE, "");
        }

        if (!values.containsKey(ContactsContract.Contacts.COLUMN_NAME_EMAIL)) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_EMAIL, "");
        }

        if (!values.containsKey(ContactsContract.Contacts.COLUMN_NAME_ADDRESS)) {
            values.put(ContactsContract.Contacts.COLUMN_NAME_ADDRESS, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insert(
                ContactsContract.Contacts.TABLE_NAME,
                ContactsContract.Contacts.COLUMN_NAME_NAME,
                values
        );

        if (rowId > 0) {
            Uri taskUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_ID_URI_BASE, rowId);

            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(taskUri, null);
            }
            return taskUri;
        }
        throw new SQLException("FAiled to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String finalWhere;

        int count;

        switch(sUriMatcher.match(uri)) {
            case ENTRIES:
                count = db.delete(
                        ContactsContract.Contacts.TABLE_NAME,
                        where,
                        whereArgs
                );
                break;

            case ENTRY_ID:
                finalWhere =
                        ContactsContract.Contacts._ID
                        + " = "
                        + uri.getPathSegments()
                                .get(ContactsContract.Contacts.ENTRY_ID_PATH_POSITION);

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                count = db.delete(
                        ContactsContract.Contacts.TABLE_NAME,
                        finalWhere,
                        whereArgs
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        String finalWhere;

        switch (sUriMatcher.match(uri)) {
            case ENTRIES:
                count = db.update(
                        ContactsContract.Contacts.TABLE_NAME,
                        values,
                        where,
                        whereArgs
                );
                break;

            case ENTRY_ID:
                String entryId = uri.getPathSegments().get(ContactsContract.Contacts.ENTRY_ID_PATH_POSITION);

                finalWhere =
                        ContactsContract.Contacts._ID
                        + " = "
                        + uri.getPathSegments()
                                .get(ContactsContract.Contacts.ENTRY_ID_PATH_POSITION);

                if (where != null) {
                    finalWhere = finalWhere + " AND " + where;
                }

                count = db.update(
                        ContactsContract.Contacts.TABLE_NAME,
                        values,
                        finalWhere,
                        whereArgs
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }
}

