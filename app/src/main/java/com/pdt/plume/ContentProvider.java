package com.pdt.plume;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pdt.plume.ProviderContract;
import com.pdt.plume.ProviderContract.Items;
import com.pdt.plume.ProviderContract.Photos;
import com.pdt.plume.ProviderContract.ItemEntities;
import com.pdt.plume.data.DbHelper;

import static com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID;


public class ContentProvider extends android.content.ContentProvider {

    public static final String AUTHORITY = "de.pdt.plume.ContentProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + "nameoftable");

    private DbHelper mHelper = null;

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

    @Override
    public boolean onCreate() {
        mHelper = new DbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ITEM_LIST:
                return Items.CONTENT_TYPE;
            case ITEM_ID:
                return Items.CONTENT_ITEM_TYPE;
            case PHOTO_ID:
                return Photos.CONTENT_PHOTO_TYPE;
            case PHOTO_LIST:
                return Photos.CONTENT_TYPE;
            case ENTITY_ID:
                return ItemEntities.CONTENT_ENTITY_TYPE;
            case ENTITY_LIST:
                return ItemEntities.CONTENT_TYPE;
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

}
