package com.pdt.plume;


import android.content.ContentResolver;
import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ProviderContract {

    // helper constants for use with the UriMatcher
    private static final int ITEM_LIST = 1;
    private static final int ITEM_ID = 2;
    private static final int PHOTO_LIST = 5;
    private static final int PHOTO_ID = 6;
    private static final int ENTITY_LIST = 10;
    private static final int ENTITY_ID = 11;
    private static final UriMatcher URI_MATCHER;

    // prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(ProviderContract.AUTHORITY,
                "items",
                ITEM_LIST);
        URI_MATCHER.addURI(ProviderContract.AUTHORITY,
                "items/#",
                ITEM_ID);
        URI_MATCHER.addURI(ProviderContract.AUTHORITY,
                "photos",
                PHOTO_LIST);
        URI_MATCHER.addURI(ProviderContract.AUTHORITY,
                "photos/#",
                PHOTO_ID);
        URI_MATCHER.addURI(ProviderContract.AUTHORITY,
                "entities",
                ENTITY_LIST);
        URI_MATCHER.addURI(ProviderContract.AUTHORITY,
                "entities/#",
                ENTITY_ID);
    }

    public static final String AUTHORITY =
            "de.openminds.samples.cpsample.lentitems";

    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);

    /**
     * Constants for the Items table
     * of the lentitems provider.
     */
    public static final class Items
            implements CommonColumns {

        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(
                        ProviderContract.CONTENT_URI,
                        "items");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                        "/vnd.de.pdt.plume_items";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                        "/vnd.de.pdt.plume_items";

    }

    /**
     * Constants for the Photos table of the
     * lentitems provider. For each item there
     * is exactly one photo. You can
     * safely call insert with the an already
     * existing ITEMS_ID. You won’t get constraint
     * violations. The content provider takes care
     * of this.<br>
     * Note: The _ID of the new record in this case
     * differs from the _ID of the old record.
     */
    public static final class Photos
            implements BaseColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(
                        ProviderContract.CONTENT_URI,
                        "items");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                        "/vnd.de.pdt.plume_items";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_PHOTO_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                        "/vnd.de.pdt.plume_items";
    }

    /**
     * Constants for a joined view of Items and
     * Photos. The _id of this joined view is
     * the _id of the Items table.
     */
    public static final class ItemEntities
            implements CommonColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(
                        ProviderContract.CONTENT_URI,
                        "items");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                        "/vnd.de.pdt.plume_items";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ENTITY_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                        "/vnd.de.pdt.plume_items";
    }

    /**
     * This interface defines common columns
     * found in multiple tables.
     */
    public static interface CommonColumns
            extends BaseColumns {
        /**
         * The content URI for this table.
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(
                        ProviderContract.CONTENT_URI,
                        "items");
        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE +
                        "/vnd.de.pdt.plume_items";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +
                        "/vnd.de.pdt.plume_items";
    }

}
