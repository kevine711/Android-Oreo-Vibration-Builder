package com.kevinersoy.androidoreovibrationbuilder;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.kevinersoy.androidoreovibrationbuilder.VibrationBuilderProviderContract.Profiles;
import com.kevinersoy.androidoreovibrationbuilder.VibrationProfileBuilderDatabaseContract.ProfileInfoEntry;

public class VibrationBuilderProvider extends ContentProvider {

    private VibrationProfileBuilderOpenHelper mDbOpenHelper;
    private static final String MIME_VENDOR_TYPE = "vnd." + VibrationBuilderProviderContract.AUTHORITY + ".";

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int PROFILES = 0;

    private static final int PROFILE_ROW = 1;

    static{
        //use static initializer to make sure our matcher is set up before any requests come in
        sUriMatcher.addURI(VibrationBuilderProviderContract.AUTHORITY, Profiles.PATH, PROFILES);
        //append path # as a wildcard, supports any rowId
        sUriMatcher.addURI(VibrationBuilderProviderContract.AUTHORITY, Profiles.PATH + "/#",
                PROFILE_ROW);
        //add other URI's we want to look for in query
    }

    public VibrationBuilderProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        int uriMatch = sUriMatcher.match(uri);

        switch(uriMatch){
            case PROFILES:
                nRows = db.delete(ProfileInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PROFILE_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = ProfileInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(ProfileInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
        }
        return nRows;
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case PROFILES:
                // this type returns multiple rows from our table
                // vnd.android.cursor.dir/vnd.com.kevinersoy.androidoreovibrationbuilder.provider.profiles
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Profiles.PATH;
                break;
            case PROFILE_ROW:
                // this type returns a signle row from our table
                // vnd.android.cursor.item/vnd.com.kevinersoy.androidoreovibrationbuilder.provider.profiles
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Profiles.PATH;
                break;
        }
        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case PROFILES:
                rowId = db.insert(ProfileInfoEntry.TABLE_NAME, null, values);
                // content://com.kevinersoy.androidoreovibrationbuilder.provider/profiles/1
                rowUri = ContentUris.withAppendedId(Profiles.CONTENT_URI, rowId);
                break;
        }

        //return the Uri of the newly inserted row
        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new VibrationProfileBuilderOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case PROFILES:
                cursor = db.query(ProfileInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PROFILE_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = ProfileInfoEntry._ID + " = ?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};
                cursor = db.query(ProfileInfoEntry.TABLE_NAME, null, rowSelection,
                        rowSelectionArgs, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Uri not supported");
        }

        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        long rowId = -1;
        int nRows = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case PROFILES:
                nRows = db.update(ProfileInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case PROFILE_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = ProfileInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[] {Long.toString(rowId)};
                nRows = db.update(ProfileInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
        }
        return nRows;
    }
}
