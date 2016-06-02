package com.example.android.news.app.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by lucky_luke on 5/11/2016.
 */
public class NewsContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.news.app";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_ALLNEWS = "allnews";
    public static final String PATH_FOOTBALLNEWS = "footballnews";
    public static final String PATH_SECURITYNEWS = "securitynews";
    public static final String PATH_DETAIL = "detail";

    public static final class AllNewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ALLNEWS).build();

        public static final String TABLE_NAME = "allnews";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_LINK = "link";

        public static Uri buildAllNewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class FootballNewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FOOTBALLNEWS).build();

        public static final String TABLE_NAME = "footballnews";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_LINK = "link";

        public static Uri buildFootballNewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class SecurityNewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SECURITYNEWS).build();

        public static final String TABLE_NAME = "securitynews";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_LINK = "link";

        public static Uri buildSecurityNewsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class DetailEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DETAIL).build();

        public static final String TABLE_NAME = "detail";

        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_CONTENT = "content";

        public static Uri buildDetailUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
