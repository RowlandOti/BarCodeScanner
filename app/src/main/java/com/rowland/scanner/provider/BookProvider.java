package com.rowland.scanner.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by saj on 24/12/14.
 */
public class BookProvider extends ContentProvider {

    private static final int BOOK_ID = 100;
    private static final int BOOK = 101;

    private static final int AUTHOR_ID = 200;
    private static final int AUTHOR = 201;

    private static final int CATEGORY_ID = 300;
    private static final int CATEGORY = 301;

    private static final int BOOK_FULL = 500;
    private static final int BOOK_FULLDETAIL = 501;

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder bookFull;

    static {
        bookFull = new SQLiteQueryBuilder();
        bookFull.setTables(
                com.rowland.scanner.provider.AlexandriaContract.BookEntry.TABLE_NAME + " LEFT OUTER JOIN " +
                        AlexandriaContract.AuthorEntry.TABLE_NAME + " USING (" + AlexandriaContract.BookEntry._ID + ")" +
                        " LEFT OUTER JOIN " + AlexandriaContract.CategoryEntry.TABLE_NAME + " USING (" + AlexandriaContract.BookEntry._ID + ")");
    }

    private DbHelper dbHelper;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AlexandriaContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, AlexandriaContract.PATH_BOOKS + "/#", BOOK_ID);
        matcher.addURI(authority, AlexandriaContract.PATH_AUTHORS + "/#", AUTHOR_ID);
        matcher.addURI(authority, AlexandriaContract.PATH_CATEGORIES + "/#", CATEGORY_ID);

        matcher.addURI(authority, AlexandriaContract.PATH_BOOKS, BOOK);
        matcher.addURI(authority, AlexandriaContract.PATH_AUTHORS, AUTHOR);
        matcher.addURI(authority, AlexandriaContract.PATH_CATEGORIES, CATEGORY);

        matcher.addURI(authority, AlexandriaContract.PATH_FULLBOOK + "/#", BOOK_FULLDETAIL);
        matcher.addURI(authority, AlexandriaContract.PATH_FULLBOOK, BOOK_FULL);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new DbHelper(getContext());
        return true;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (uriMatcher.match(uri)) {
            case BOOK:
                retCursor = dbHelper.getReadableDatabase().query(
                        AlexandriaContract.BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selection == null ? null : selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case AUTHOR:
                retCursor = dbHelper.getReadableDatabase().query(
                        AlexandriaContract.AuthorEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY:
                retCursor = dbHelper.getReadableDatabase().query(
                        AlexandriaContract.CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOK_ID:
                retCursor = dbHelper.getReadableDatabase().query(
                        AlexandriaContract.BookEntry.TABLE_NAME,
                        projection,
                        AlexandriaContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case AUTHOR_ID:
                retCursor = dbHelper.getReadableDatabase().query(
                        AlexandriaContract.AuthorEntry.TABLE_NAME,
                        projection,
                        AlexandriaContract.AuthorEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CATEGORY_ID:
                retCursor = dbHelper.getReadableDatabase().query(
                        AlexandriaContract.CategoryEntry.TABLE_NAME,
                        projection,
                        AlexandriaContract.CategoryEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case BOOK_FULLDETAIL:
                String[] bfd_projection = {
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.TITLE,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.SUBTITLE,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.IMAGE_URL,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.DESC,
                        "group_concat(DISTINCT " + AlexandriaContract.AuthorEntry.TABLE_NAME + "." + AlexandriaContract.AuthorEntry.AUTHOR + ") as " + AlexandriaContract.AuthorEntry.AUTHOR,
                        "group_concat(DISTINCT " + AlexandriaContract.CategoryEntry.TABLE_NAME + "." + AlexandriaContract.CategoryEntry.CATEGORY + ") as " + AlexandriaContract.CategoryEntry.CATEGORY
                };
                retCursor = bookFull.query(dbHelper.getReadableDatabase(),
                        bfd_projection,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry._ID,
                        null,
                        sortOrder);
                break;
            case BOOK_FULL:
                String[] bf_projection = {
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.TITLE,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry.IMAGE_URL,
                        "group_concat(DISTINCT " + AlexandriaContract.AuthorEntry.TABLE_NAME + "." + AlexandriaContract.AuthorEntry.AUTHOR + ") as " + AlexandriaContract.AuthorEntry.AUTHOR,
                        "group_concat(DISTINCT " + AlexandriaContract.CategoryEntry.TABLE_NAME + "." + AlexandriaContract.CategoryEntry.CATEGORY + ") as " + AlexandriaContract.CategoryEntry.CATEGORY
                };
                retCursor = bookFull.query(dbHelper.getReadableDatabase(),
                        bf_projection,
                        null,
                        selectionArgs,
                        AlexandriaContract.BookEntry.TABLE_NAME + "." + AlexandriaContract.BookEntry._ID,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }


    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);

        switch (match) {
            case BOOK_FULLDETAIL:
                return AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE;
            case BOOK_ID:
                return AlexandriaContract.BookEntry.CONTENT_ITEM_TYPE;
            case AUTHOR_ID:
                return AlexandriaContract.AuthorEntry.CONTENT_ITEM_TYPE;
            case CATEGORY_ID:
                return AlexandriaContract.CategoryEntry.CONTENT_ITEM_TYPE;
            case BOOK:
                return AlexandriaContract.BookEntry.CONTENT_TYPE;
            case AUTHOR:
                return AlexandriaContract.AuthorEntry.CONTENT_TYPE;
            case CATEGORY:
                return AlexandriaContract.CategoryEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case BOOK: {
                long _id = db.insert(AlexandriaContract.BookEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = AlexandriaContract.BookEntry.buildBookUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                getContext().getContentResolver().notifyChange(AlexandriaContract.BookEntry.buildFullBookUri(_id), null);
                break;
            }
            case AUTHOR: {
                long _id = db.insert(AlexandriaContract.AuthorEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = AlexandriaContract.AuthorEntry.buildAuthorUri(values.getAsLong("_id"));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CATEGORY: {
                long _id = db.insert(AlexandriaContract.CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = AlexandriaContract.CategoryEntry.buildCategoryUri(values.getAsLong("_id"));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case BOOK:
                rowsDeleted = db.delete(
                        AlexandriaContract.BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case AUTHOR:
                rowsDeleted = db.delete(
                        AlexandriaContract.AuthorEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                rowsDeleted = db.delete(
                        AlexandriaContract.CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                rowsDeleted = db.delete(
                        AlexandriaContract.BookEntry.TABLE_NAME,
                        AlexandriaContract.BookEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int rowsUpdated;
        switch (match) {
            case BOOK:
                rowsUpdated = db.update(AlexandriaContract.BookEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case AUTHOR:
                rowsUpdated = db.update(AlexandriaContract.AuthorEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CATEGORY:
                rowsUpdated = db.update(AlexandriaContract.CategoryEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}