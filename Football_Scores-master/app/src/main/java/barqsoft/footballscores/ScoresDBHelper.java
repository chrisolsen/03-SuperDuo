package barqsoft.footballscores;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import barqsoft.footballscores.DatabaseContract.ScoresTable;

public class ScoresDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Scores.db";
    private static final int DATABASE_VERSION = 2;

    public ScoresDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // scores
        final String createScoresTable = "CREATE TABLE " + DatabaseContract.SCORES_TABLE + " ("
                + DatabaseContract.ScoresTable._ID + " INTEGER PRIMARY KEY,"
                + ScoresTable.DATE_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresTable.TIME_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresTable.HOME_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresTable.AWAY_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresTable.LEAGUE_COL + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresTable.HOME_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresTable.AWAY_GOALS_COL + " TEXT NOT NULL,"
                + DatabaseContract.ScoresTable.MATCH_ID + " INTEGER NOT NULL,"
                + DatabaseContract.ScoresTable.MATCH_DAY + " INTEGER NOT NULL,"
                + " UNIQUE (" + DatabaseContract.ScoresTable.MATCH_ID + ") ON CONFLICT REPLACE"
                + " );";

        // teams
        final String createTeamsTable = "CREATE TABLE " + DatabaseContract.TEAMS_TABLE + " ("
                + DatabaseContract.TeamsTable._ID               + " integer primary key,"
                + DatabaseContract.TeamsTable.TEAM_ID           + " integer not null,"
                + DatabaseContract.TeamsTable.TEAM_NAME         + " text not null,"
                + DatabaseContract.TeamsTable.TEAM_SHORT_NAME   + " text not null,"
                + DatabaseContract.TeamsTable.TEAM_CREST_URL    + " text not null,"
                + " unique (" + DatabaseContract.TeamsTable.TEAM_ID + ") on conflict replace"
                + ");";

        db.execSQL(createScoresTable);
        db.execSQL(createTeamsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Remove old values when upgrading.
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.SCORES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TEAMS_TABLE);
    }
}
