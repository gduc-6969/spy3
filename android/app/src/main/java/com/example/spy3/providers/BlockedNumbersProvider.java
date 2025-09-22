package com.example.spy3.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BlockedNumbersProvider extends ContentProvider {
    private static final String TAG = "BlockedNumbersProvider";
    
    // Database info
    private static final String DATABASE_NAME = "blocked_numbers.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_BLOCKED_NUMBERS = "blocked_numbers";
    
    // Table columns
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DATE_ADDED = "date_added";
    public static final String COLUMN_BLOCKED_CALLS = "blocked_calls";
    public static final String COLUMN_BLOCKED_SMS = "blocked_sms";
    
    // Content provider authority
    public static final String AUTHORITY = "com.example.spy3.blockednumbers";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_BLOCKED_NUMBERS);
    
    // URI matcher codes
    private static final int BLOCKED_NUMBERS = 1;
    private static final int BLOCKED_NUMBER_ID = 2;
    
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
    static {
        sUriMatcher.addURI(AUTHORITY, TABLE_BLOCKED_NUMBERS, BLOCKED_NUMBERS);
        sUriMatcher.addURI(AUTHORITY, TABLE_BLOCKED_NUMBERS + "/#", BLOCKED_NUMBER_ID);
    }
    
    private DatabaseHelper mDatabaseHelper;
    
    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }
    
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                       @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_BLOCKED_NUMBERS);
        
        switch (sUriMatcher.match(uri)) {
            case BLOCKED_NUMBERS:
                break;
            case BLOCKED_NUMBER_ID:
                queryBuilder.appendWhere(COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        SQLiteDatabase database = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = queryBuilder.query(database, projection, selection, selectionArgs,
                null, null, sortOrder);
        
        if (cursor != null && getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        
        return cursor;
    }
    
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case BLOCKED_NUMBERS:
                return "vnd.android.cursor.dir/vnd.spy3.blocked_number";
            case BLOCKED_NUMBER_ID:
                return "vnd.android.cursor.item/vnd.spy3.blocked_number";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }
    
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (sUriMatcher.match(uri) != BLOCKED_NUMBERS) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        long id = database.insert(TABLE_BLOCKED_NUMBERS, null, values);
        
        if (id > 0) {
            Uri newUri = ContentUris.withAppendedId(CONTENT_URI, id);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(newUri, null);
            }
            return newUri;
        }
        
        return null;
    }
    
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        int count;
        
        switch (sUriMatcher.match(uri)) {
            case BLOCKED_NUMBERS:
                count = database.delete(TABLE_BLOCKED_NUMBERS, selection, selectionArgs);
                break;
            case BLOCKED_NUMBER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    count = database.delete(TABLE_BLOCKED_NUMBERS, COLUMN_ID + "=" + id, null);
                } else {
                    count = database.delete(TABLE_BLOCKED_NUMBERS, 
                            COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return count;
    }
    
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                     @Nullable String[] selectionArgs) {
        SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
        int count;
        
        switch (sUriMatcher.match(uri)) {
            case BLOCKED_NUMBERS:
                count = database.update(TABLE_BLOCKED_NUMBERS, values, selection, selectionArgs);
                break;
            case BLOCKED_NUMBER_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    count = database.update(TABLE_BLOCKED_NUMBERS, values, COLUMN_ID + "=" + id, null);
                } else {
                    count = database.update(TABLE_BLOCKED_NUMBERS, values,
                            COLUMN_ID + "=" + id + " AND " + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        
        return count;
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_BLOCKED_NUMBERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NUMBER + " TEXT NOT NULL UNIQUE, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_DATE_ADDED + " INTEGER, "
                + COLUMN_BLOCKED_CALLS + " INTEGER DEFAULT 0, "
                + COLUMN_BLOCKED_SMS + " INTEGER DEFAULT 0"
                + ");";
        
        public DatabaseHelper(@Nullable Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        
        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.d(TAG, "Creating database table");
            db.execSQL(CREATE_TABLE);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCKED_NUMBERS);
            onCreate(db);
        }
    }
}