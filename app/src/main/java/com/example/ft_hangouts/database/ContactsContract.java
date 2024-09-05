package com.example.ft_hangouts.database;

import android.net.Uri;
import android.provider.BaseColumns;

public final class ContactsContract {
    public static final String AUTHORITY = "com.example.provider.ft_hangouts";

    private ContactsContract(){
    }

    public static final class Contacts implements BaseColumns {
        private Contacts() {
        }

        public static final String TABLE_NAME = "contacts";

        private static final String SCHEME = "content://";

        private static final String PATH_ENTRIES = "/contacts";

        private static final String PATH_ENTRY_ID = "/contacts/";

        public static final int ENTRY_ID_PATH_POSITION = 1;

        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ENTRIES);

        public static final Uri CONTENT_ID_URI_BASE
                = Uri.parse(SCHEME + AUTHORITY + PATH_ENTRY_ID);

        public static final Uri CONTENT_ID_URI_PATTERN
                = Uri.parse(SCHEME + AUTHORITY + PATH_ENTRY_ID + "/#");


        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.florin.contacts";

        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.florin.contacts";

        public static final String DEFAULT_SORT_ORDER = "name ASC";


        public static final String COLUMN_NAME_AVATAR = "avatar";

        public static final String COLUMN_NAME_NAME = "name";

        public static final String COLUMN_NAME_LAST_NAME = "last_name";

        public static final String COLUMN_NAME_PHONE = "phone";

        public static final String COLUMN_NAME_EMAIL = "email";

        public static final String COLUMN_NAME_ADDRESS = "address";

        public static final String COLUMN_NAME_CREATE_DATE = "created";

        public static final String COLUMN_NAME_MODIFICATION_DATE = "modified";
    }
}
