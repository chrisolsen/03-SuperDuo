package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

public class FetchService extends IntentService {
    public static final String LOG_TAG = "FetchService";

    public FetchService() {
        super("FetchService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getData("n2");
        getData("p2");
    }

    private void getData(String timeFrame) {
        try {
            String body = FootballApi.get(this, timeFrame);
            JSONArray matches = new JSONObject(body).getJSONArray("fixtures");

            if (matches.length() == 0) {
                //if there is no data, call the function on dummy data
                //this is expected behavior during the off season.
                processJSONdata(this.getResources().getString(R.string.dummy_data), false);
            } else {
                processJSONdata(body, true);
            }
        } catch (IOException | JSONException e) {
            Log.d(LOG_TAG, "Could not connect to server.");
        }
    }

    private void processJSONdata(String JSONdata, boolean isReal) {
        //JSON data
        // This set of league codes is for the 2015/2016 season. In fall of 2016, they will need to
        // be updated. Feel free to use the codes
        final String BUNDESLIGA1 = "394";
        final String BUNDESLIGA2 = "395";
        final String LIGUE1 = "396";
        final String LIGUE2 = "397";
        final String PREMIER_LEAGUE = "398";
        final String PRIMERA_DIVISION = "399";
        final String SEGUNDA_DIVISION = "400";
        final String SERIE_A = "401";
        final String PRIMERA_LIGA = "402";
        final String Bundesliga3 = "403";
        final String EREDIVISIE = "404";


        final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
        final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
        final String FIXTURES = "fixtures";
        final String LINKS = "_links";
        final String SOCCER_SEASON = "soccerseason";
        final String SELF = "self";
        final String MATCH_DATE = "date";
        final String HOME_TEAM = "homeTeamName";
        final String AWAY_TEAM = "awayTeamName";
        final String RESULT = "result";
        final String HOME_GOALS = "goalsHomeTeam";
        final String AWAY_GOALS = "goalsAwayTeam";
        final String MATCH_DAY = "matchday";

        //Match data
        String league;
        String date;
        String time;
        String home;
        String away;
        String homeGoals;
        String awayGoals;
        String matchId;
        String matchDay;


        try {
            JSONArray matches = new JSONObject(JSONdata).getJSONArray(FIXTURES);

            //ContentValues to be inserted
            Vector<ContentValues> values = new Vector<ContentValues>(matches.length());
            for (int i = 0; i < matches.length(); i++) {

                JSONObject match_data = matches.getJSONObject(i);
                league = match_data.getJSONObject(LINKS).getJSONObject(SOCCER_SEASON).
                        getString("href");
                league = league.replace(SEASON_LINK, "");
                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (league.equals(PREMIER_LEAGUE) ||
                        league.equals(SERIE_A) ||
                        league.equals(BUNDESLIGA1) ||
                        league.equals(BUNDESLIGA2) ||
                        league.equals(PRIMERA_DIVISION)) {
                    matchId = match_data.getJSONObject(LINKS).getJSONObject(SELF).
                            getString("href");
                    matchId = matchId.replace(MATCH_LINK, "");
                    if (!isReal) {
                        //This if statement changes the match ID of the dummy data so that it all goes into the database
                        matchId = matchId + Integer.toString(i);
                    }

                    date = match_data.getString(MATCH_DATE);
                    time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0, date.indexOf("T"));
                    SimpleDateFormat match_date = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    match_date.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parsedDate = match_date.parse(date + time);
                        SimpleDateFormat new_date = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        new_date.setTimeZone(TimeZone.getDefault());
                        date = new_date.format(parsedDate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0, date.indexOf(":"));

                        if (!isReal) {
                            //This if statement changes the dummy data's date to match our current date range.
                            Date fragmentDate = new Date(System.currentTimeMillis() + ((i - 2) * 86400000));
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            date = format.format(fragmentDate);
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    home = match_data.getString(HOME_TEAM);
                    away = match_data.getString(AWAY_TEAM);
                    homeGoals = match_data.getJSONObject(RESULT).getString(HOME_GOALS);
                    awayGoals = match_data.getJSONObject(RESULT).getString(AWAY_GOALS);
                    matchDay = match_data.getString(MATCH_DAY);
                    ContentValues match_values = new ContentValues();

                    match_values.put(DatabaseContract.scores_table.MATCH_ID, matchId);
                    match_values.put(DatabaseContract.scores_table.DATE_COL, date);
                    match_values.put(DatabaseContract.scores_table.TIME_COL, time);
                    match_values.put(DatabaseContract.scores_table.HOME_COL, home);
                    match_values.put(DatabaseContract.scores_table.AWAY_COL, away);
                    match_values.put(DatabaseContract.scores_table.HOME_GOALS_COL, homeGoals);
                    match_values.put(DatabaseContract.scores_table.AWAY_GOALS_COL, awayGoals);
                    match_values.put(DatabaseContract.scores_table.LEAGUE_COL, league);
                    match_values.put(DatabaseContract.scores_table.MATCH_DAY, matchDay);

                    values.add(match_values);
                }
            }

            ContentValues[] insert_data = new ContentValues[values.size()];
            values.toArray(insert_data);
            this.getContentResolver().bulkInsert(
                    DatabaseContract.BASE_CONTENT_URI, insert_data);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

    }
}

