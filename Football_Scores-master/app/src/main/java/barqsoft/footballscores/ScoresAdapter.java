package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ScoresAdapter extends CursorAdapter {
    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCH_DAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCH_TIME = 2;
    private static final String TAG = ScoresAdapter.class.getSimpleName();
    public double detailMatchId = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    private Map<String, String> mTeams = new HashMap<>();

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

//        mContext = context;

        Cursor c = context.getContentResolver().query(
                DatabaseContract.TeamsTable.CONTENT_URI,
                new String[]{
                        DatabaseContract.TeamsTable.TEAM_NAME,
                        DatabaseContract.TeamsTable.TEAM_CREST_URL,
                }, null, null, null);

        if (c.moveToFirst()) {
            do {
                String name = c.getString(c.getColumnIndex(DatabaseContract.TeamsTable.TEAM_NAME));
                String url = c.getString(c.getColumnIndex(DatabaseContract.TeamsTable.TEAM_CREST_URL));
                String[] parts = url.split("/");
                String filename = parts[parts.length - 1];
                String pngFile = filename.endsWith(".svg") ? filename.replace(".svg", ".png") : filename;
                mTeams.put(name, pngFile);

            } while (c.moveToNext());
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        return mItem;
    }

    private void bindEmblem(String url, ImageView view) {
        if (url != null) {
            Drawable drawable;
            try {
                InputStream inputStream = mContext.getResources().getAssets().open(url);
                drawable = Drawable.createFromStream(inputStream, null);
                view.setImageDrawable(drawable);
            } catch (IOException e) {
                view.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.no_emblem));
            }
        } else {
            view.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.no_emblem));
        }
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder mHolder = (ViewHolder) view.getTag();

        String homeTeamName = cursor.getString(COL_HOME);
        String awayTeamName = cursor.getString(COL_AWAY);
        int homeScore = cursor.getInt(COL_HOME_GOALS);
        int awayScore = cursor.getInt(COL_AWAY_GOALS);
        String score;

        // FIXME: quick hack for now, for vertical alignment reasons create 2 different view groups
        // for scores and vs.
        if (homeScore >= 0 && awayScore >= 0) {
            score = Integer.toString(homeScore) + " - " + Integer.toString(awayScore);
        } else {
            score = "@";
        }

        SimpleDateFormat toDate = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        SimpleDateFormat toTime = new SimpleDateFormat("h:mm a");
        String dateStr = "2006-01-02T" + cursor.getString(COL_MATCH_TIME) + ":00Z";

        Date gameDate;
        try {
            gameDate = toDate.parse(dateStr);
        } catch (ParseException e) {
            Log.d(TAG, "date parse " + e.toString());
            gameDate = new Date();
        }

        mHolder.homeName.setText(homeTeamName);
        mHolder.awayName.setText(awayTeamName);
        mHolder.date.setText(toTime.format(gameDate));
        mHolder.score.setText(score);

        bindEmblem(mTeams.get(homeTeamName), mHolder.homeCrest);
        bindEmblem(mTeams.get(awayTeamName), mHolder.awayCrest);
    }

    // FIXME: prevent share provider from being able to be saved
    public Intent createShareForecastIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

    public static class ViewHolder {
        public TextView homeName;
        public TextView awayName;
        public TextView score;
        public TextView date;
        public ImageView homeCrest;
        public ImageView awayCrest;
        public double matchId;

        public ViewHolder(View view) {
            homeName = (TextView) view.findViewById(R.id.home_name);
            awayName = (TextView) view.findViewById(R.id.away_name);
            score = (TextView) view.findViewById(R.id.score_textview);
            date = (TextView) view.findViewById(R.id.data_textview);
            homeCrest = (ImageView) view.findViewById(R.id.home_crest);
            awayCrest = (ImageView) view.findViewById(R.id.away_crest);
        }
    }

}
