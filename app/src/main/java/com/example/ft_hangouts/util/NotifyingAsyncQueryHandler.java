package com.example.ft_hangouts.util;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.lang.ref.WeakReference;

public class NotifyingAsyncQueryHandler extends AsyncQueryHandler {
    private AsyncQueryListener mListener;

    public interface AsyncQueryListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);

        void onInsertComplete(int token, Object cookie, Uri uri);

        void onUpdateComplete(int token, Object cookie, int result);

        void onDeleteComplete(int token, Object cookie, int result);
    }

    public NotifyingAsyncQueryHandler(Context context, AsyncQueryListener listener) {
        super(context.getContentResolver());
        setQueryListener(listener);
    }

    public void setQueryListener(AsyncQueryListener listener) {
        mListener = listener;
    }

    @Override
    protected  void onQueryComplete(int token, Object cookie, Cursor cursor) {
        Log.d("NaqHandler", "onQueryComplete");
        final AsyncQueryListener listener = mListener;
        Log.d("NaqHandler", "our listener is " + listener);
        if (listener != null) {
            listener.onQueryComplete(token, cookie, cursor);
        } else if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
        final AsyncQueryListener listener = mListener;
        if (listener != null) {
            listener.onInsertComplete(token, cookie, uri);
        }
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
        final AsyncQueryListener listener = mListener;
        if (listener != null) {
            listener.onUpdateComplete(token, cookie, result);
        }
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        final AsyncQueryListener listener = mListener;
        if (listener != null) {
            listener.onDeleteComplete(token, cookie, result);
        }
    }
}
