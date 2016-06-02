package com.example.android.news.app.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by lucky_luke on 5/12/2016.
 */
public class NewsProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    public static final UriMatcher sUriMatcher = buildUriMatcher();
    private NewsDbHelper mOpenHelper;

    public static final int ALLNEWS = 100;
    public static final int ALLNEWS_WITH_ID = 101;
    public static final int FOOTBALLNEWS = 200;
    public static final int FOOTBALLNEWS_WITH_ID = 201;
    public static final int SECURITYNEWS = 300;
    public static final int SECURITYNEWS_WITH_ID = 301;
    public static final int DETAIL_CODE = 400;
    public static final int DETAIL_WITH_ITEM = 401;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, NewsContract.PATH_ALLNEWS, ALLNEWS);
        matcher.addURI(authority, NewsContract.PATH_ALLNEWS + "/#", ALLNEWS_WITH_ID);
        matcher.addURI(authority, NewsContract.PATH_FOOTBALLNEWS, FOOTBALLNEWS);
        matcher.addURI(authority, NewsContract.PATH_FOOTBALLNEWS + "/#", FOOTBALLNEWS_WITH_ID);
        matcher.addURI(authority, NewsContract.PATH_SECURITYNEWS, SECURITYNEWS);
        matcher.addURI(authority, NewsContract.PATH_SECURITYNEWS + "/#", SECURITYNEWS_WITH_ID);
        matcher.addURI(authority, NewsContract.PATH_DETAIL, DETAIL_CODE);
        matcher.addURI(authority, NewsContract.PATH_DETAIL + "/*", DETAIL_WITH_ITEM);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new NewsDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case ALLNEWS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.AllNewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case FOOTBALLNEWS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.FootballNewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case SECURITYNEWS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.SecurityNewsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case DETAIL_CODE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.DetailEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        long _id;

        switch (match) {
            case ALLNEWS:
                _id = db.insert(NewsContract.AllNewsEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    returnUri = NewsContract.AllNewsEntry.buildAllNewsUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case FOOTBALLNEWS:
                _id = db.insert(NewsContract.FootballNewsEntry.TABLE_NAME, null, values);
                if ( _id > 0) {
                    returnUri = NewsContract.FootballNewsEntry.buildFootballNewsUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case SECURITYNEWS:
                _id = db.insert(NewsContract.SecurityNewsEntry.TABLE_NAME, null, values);
                if ( _id > 0 ) {
                    returnUri = NewsContract.SecurityNewsEntry.buildSecurityNewsUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case DETAIL_CODE:
                _id = db.insert(NewsContract.DetailEntry.TABLE_NAME, null, values);
                if ( _id > 0) {
                    returnUri = NewsContract.DetailEntry.buildDetailUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        if ( null == selection ) selection = "1";
        switch (match) {
            case ALLNEWS:
                rowsDeleted = db.delete(
                        NewsContract.AllNewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case FOOTBALLNEWS:
                rowsDeleted = db.delete(
                        NewsContract.FootballNewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SECURITYNEWS:
                rowsDeleted = db.delete(
                        NewsContract.SecurityNewsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DETAIL_CODE:
                rowsDeleted = db.delete(
                        NewsContract.DetailEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if ( rowsDeleted != 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ALLNEWS:
                rowsUpdated = db.update(NewsContract.AllNewsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case FOOTBALLNEWS:
                rowsUpdated = db.update(NewsContract.FootballNewsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case SECURITYNEWS:
                rowsUpdated = db.update(NewsContract.SecurityNewsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case DETAIL_CODE:
                rowsUpdated = db.update(NewsContract.DetailEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if ( rowsUpdated != 0 ) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        long _id;
        switch (match) {
            case ALLNEWS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        _id = db.insert(NewsContract.AllNewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case FOOTBALLNEWS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        _id = db.insert(NewsContract.FootballNewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case SECURITYNEWS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        _id = db.insert(NewsContract.SecurityNewsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
