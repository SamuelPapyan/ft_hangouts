package com.example.ft_hangouts.adapter;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.ft_hangouts.R;
import com.example.ft_hangouts.database.ContactsContract;
import com.example.ft_hangouts.util.Util;

public class ContactsCursorRecyclerAdapter extends CursorRecyclerAdapter {

    private Context mContext;

    public ContactsCursorRecyclerAdapter(Context context, Cursor cursor) {
        super(cursor);
        mContext = context;
    }

    @Override
    public void onBindViewHolderCursor(RecyclerView.ViewHolder holder, Cursor cursor) {
        ContactViewHolder vh = (ContactViewHolder) holder;

        @SuppressLint("Range") final String avatarUri = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.COLUMN_NAME_AVATAR));
        Util.loadAvatar(Uri.parse(avatarUri), vh.ivAvatar, R.drawable.ic_person_black_24dp, 200, mContext);

        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.COLUMN_NAME_NAME));

        @SuppressLint("Range") String lastName = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.COLUMN_NAME_LAST_NAME
        ));
        int nameLen = name.length();
        int lastNameLen = name.length();
        if (nameLen == 0 && lastNameLen == 0) {
            vh.tvName.setText(R.string.txt_no_name);
        } else if (nameLen > 0 && lastNameLen == 0) {
            vh.tvName.setText(name);
        } else if (nameLen == 0 && lastNameLen > 0) {
            vh.tvName.setText(lastName);
        } else {
            String displayName = name + " " + lastName;
            vh.tvName.setText(displayName);
        }

        @SuppressLint("Range") final String phone = cursor.getString(cursor.getColumnIndex(
                ContactsContract.Contacts.COLUMN_NAME_PHONE
        ));
        if (phone.length() == 0) {
            vh.tvPhone.setText(R.string.txt_no_phone);
        } else {
            vh.tvPhone.setText(phone);
        }

        @SuppressLint("Range") final long id = cursor.getLong(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        vh.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);

                mContext.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(v);
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout root;
        ImageView ivAvatar;
        TextView tvName;
        TextView tvPhone;

        public ContactViewHolder(View v) {
            super(v);
            root = (RelativeLayout) v.findViewById(R.id.root);
            ivAvatar = (ImageView) v.findViewById(R.id.iv_avatar);
            tvName = (TextView) v.findViewById(R.id.tv_name);
            tvPhone = (TextView) v.findViewById(R.id.tv_phone);
        }
    }
}
