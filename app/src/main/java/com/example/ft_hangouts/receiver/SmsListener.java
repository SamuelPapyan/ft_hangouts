package com.example.ft_hangouts.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.ft_hangouts.R;
import com.example.ft_hangouts.database.ContactsContract;
import com.example.ft_hangouts.util.NotifyingAsyncQueryHandler;

public class SmsListener extends BroadcastReceiver {

    private static final String TAG = "SmsListener";

    private static final String[] PROJECTION =
            new String[]{
                    ContactsContract.Contacts.COLUMN_NAME_PHONE
            };

    private static final int QUERY_TOKEN = 1;
    private static final int INSERT_TOKEN = 2;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                final String phone = smsMessage.getOriginatingAddress();
                String body = smsMessage.getMessageBody();
                Log.d(TAG, "onReceive: " + String.format("phone=[%s], body=[%s]", phone, body));
                NotifyingAsyncQueryHandler queryHandler = new NotifyingAsyncQueryHandler(context,
                        new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                            @Override
                            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                                if (cursor != null) {
                                    Log.d(TAG, "onQueryComplete: cursor size=" + cursor.getCount());
                                    if (cursor.getCount() == 0) {
                                        NotifyingAsyncQueryHandler insertHandler =
                                                new NotifyingAsyncQueryHandler((Context) cookie,
                                                        new NotifyingAsyncQueryHandler.AsyncQueryListener() {
                                                            @Override
                                                            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                                                                // nothing
                                                            }

                                                            @Override
                                                            public void onInsertComplete(int token, Object cookie, Uri uri) {
                                                                Toast.makeText((Context) cookie,
                                                                        R.string.txt_contact_created,
                                                                        Toast.LENGTH_SHORT).show();
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
                                        ContentValues values = new ContentValues();
                                        values.put(ContactsContract.Contacts.COLUMN_NAME_PHONE, phone);
                                        insertHandler.startInsert(INSERT_TOKEN, cookie,
                                                ContactsContract.Contacts.CONTENT_URI, values);
                                    }
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
                String selection = ContactsContract.Contacts.COLUMN_NAME_PHONE + "=?";
                String selectionArgs[] = {phone};
                queryHandler.startQuery(QUERY_TOKEN, context, ContactsContract.Contacts.CONTENT_URI,
                        PROJECTION, selection, selectionArgs, null);
            }
        }
    }
}
