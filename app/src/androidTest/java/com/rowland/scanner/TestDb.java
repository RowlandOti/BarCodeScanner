package com.rowland.scanner;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.rowland.scanner.provider.AlexandriaContract;
import com.rowland.scanner.provider.DbHelper;

import java.util.Map;
import java.util.Set;

/**
 * Created by saj on 23/12/14.
 */
public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public final static long ean = 9780137903955L;
    public final static String title = "Artificial Intelligence";
    public final static String subtitle = "A Modern Approach";
    public final static String imgUrl = "http://books.google.com/books/content?id=KI2WQgAACAAJ&printsec=frontcover&img=1&zoom=1";
    public final static String desc = "Presents a guide to artificial intelligence, covering such topics as intelligent agents, problem-solving, logical agents, planning, uncertainty, learning, and robotics.";
    public final static String author = "Stuart Jonathan Russell";
    public final static String category = "Computers";

    static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(columnName, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
        valueCursor.close();
    }

    public static ContentValues getBookValues() {

        final ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);

        return values;
    }

    public static ContentValues getAuthorValues() {

        final ContentValues values = new ContentValues();
        values.put(AlexandriaContract.AuthorEntry._ID, ean);
        values.put(AlexandriaContract.AuthorEntry.AUTHOR, author);

        return values;
    }

    public static ContentValues getCategoryValues() {

        final ContentValues values = new ContentValues();
        values.put(AlexandriaContract.CategoryEntry._ID, ean);
        values.put(AlexandriaContract.CategoryEntry.CATEGORY, category);

        return values;
    }

    public static ContentValues getFullDetailValues() {

        final ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        values.put(AlexandriaContract.AuthorEntry.AUTHOR, author);
        values.put(AlexandriaContract.CategoryEntry.CATEGORY, category);
        return values;
    }

    public static ContentValues getFullListValues() {

        final ContentValues values = new ContentValues();
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.AuthorEntry.AUTHOR, author);
        values.put(AlexandriaContract.CategoryEntry.CATEGORY, category);
        return values;
    }

    public void testCreateDb() throws Throwable {
        mContext.deleteDatabase(DbHelper.DATABASE_NAME);
        SQLiteDatabase db = new DbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertReadDb() {

        DbHelper dbHelper = new DbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = getBookValues();

        long retEan = db.insert(AlexandriaContract.BookEntry.TABLE_NAME, null, values);
        assertEquals(ean, retEan);

        String[] columns = {
                AlexandriaContract.BookEntry._ID,
                AlexandriaContract.BookEntry.TITLE,
                AlexandriaContract.BookEntry.IMAGE_URL,
                AlexandriaContract.BookEntry.SUBTITLE,
                AlexandriaContract.BookEntry.DESC
        };

        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                AlexandriaContract.BookEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);

        values = getAuthorValues();


        retEan = db.insert(AlexandriaContract.AuthorEntry.TABLE_NAME, null, values);

        columns = new String[]{
                AlexandriaContract.AuthorEntry._ID,
                AlexandriaContract.AuthorEntry.AUTHOR
        };

        cursor = db.query(
                AlexandriaContract.AuthorEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);
        // test category table

        values = getCategoryValues();
        retEan = db.insert(AlexandriaContract.CategoryEntry.TABLE_NAME, null, values);

        columns = new String[]{
                AlexandriaContract.CategoryEntry._ID,
                AlexandriaContract.CategoryEntry.CATEGORY
        };

        cursor = db.query(
                AlexandriaContract.CategoryEntry.TABLE_NAME,  // Table to Query
                columns,
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        validateCursor(cursor, values);

        dbHelper.close();

    }
}