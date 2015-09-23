package barqsoft.footballscores;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class TeamsProvider extends ContentProvider {

    // path ids
    static final int MATCH_TEAM = 100;
    static final int MATCH_TEAMS = 101;

    static UriMatcher mUriMatcher = buildUriMatcher();

    private ScoresDBHelper mDbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DatabaseContract.TeamsTable.CONTENT_AUTHORITY;

        matcher.addURI(authority, DatabaseContract.TeamsTable.PATH + "/#", MATCH_TEAM);
        matcher.addURI(authority, DatabaseContract.TeamsTable.PATH, MATCH_TEAMS);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new ScoresDBHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        int match = mUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor c;

        switch (match) {
            case MATCH_TEAMS:
                c = db.query(DatabaseContract.TEAMS_TABLE,
                        projection, null, null, null, null, sortOrder);
                break;
            case MATCH_TEAM:
                c = db.query(DatabaseContract.TEAMS_TABLE,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Invalid Uri: " + uri);
        }

        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        int match = mUriMatcher.match(uri);

        switch (match) {
            case MATCH_TEAM:
                return DatabaseContract.TeamsTable.CONTENT_ITEM_TYPE;
            case MATCH_TEAMS:
                return DatabaseContract.TeamsTable.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Invalue uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = mDbHelper.getWritableDatabase().insertWithOnConflict(
                DatabaseContract.TEAMS_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return ContentUris.withAppendedId(DatabaseContract.TeamsTable.BASE_CONTENT_URI, id);
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        int match = mUriMatcher.match(uri);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        switch (match) {
            case MATCH_TEAMS:
                int count = 0;
                try {
                    db.beginTransaction();
                    for (ContentValues v : values) {
                        long id = db.insertWithOnConflict(DatabaseContract.TEAMS_TABLE,
                                null, v, SQLiteDatabase.CONFLICT_REPLACE);
                        if (id != -1) {
                            count++;
                        }
                    }
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);

                return count;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
