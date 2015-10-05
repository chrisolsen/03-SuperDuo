package barqsoft.footballscores.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

public class WidgetViewsFactory implements RemoteViewsFactory {

    private final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private Cursor mCursor;
    private Date today, tomorrow, dayAfterTomorrow;
    private Map<String, String> mTeams = new HashMap<>();
    private SimpleDateFormat mParseDate = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
    private SimpleDateFormat mToTime = new SimpleDateFormat("h:mm a");
    private SimpleDateFormat mToDateTime = new SimpleDateFormat("MMMM d");

    public WidgetViewsFactory(Context context, Intent intent) {
        mContext = context;
    }

    private boolean isToday(Date d) {
        return d.after(today) && d.before(tomorrow);
    }

    private boolean isTomorrow(Date d) {
        return d.after(tomorrow) && d.before(dayAfterTomorrow);
    }

    @Override
    public void onCreate() {
        Uri uri = DatabaseContract.ScoresTable.buildScoreWithDate();

        // reset times to 0:00
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        // get day markers for time comparisons
        today = cal.getTime();    cal.add(Calendar.DAY_OF_MONTH, 1);
        tomorrow = cal.getTime(); cal.add(Calendar.DAY_OF_MONTH, 1);
        dayAfterTomorrow = cal.getTime();

        // obtain today's games
        String dateFilter = new SimpleDateFormat("yyyy-MM-dd").format(today);
        mCursor = mContext.getContentResolver().query(uri, null, null, new String[]{dateFilter}, null);

        // extract team names and emblems
        Cursor c = mContext.getContentResolver().query(
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

                if (filename.endsWith(".svg")) {
                    filename = filename.replace(".svg", ".png");
                }

                if (filename.endsWith(".gif")) {
                    filename = filename.replace(".gif", ".png");
                }

                mTeams.put(name, filename);

            } while (c.moveToNext());

            c.close();
        }
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(mContext.getPackageName(), R.layout.widget_score_list_item);

        String rawDate, homeName, awayName, score;
        int homeScore, awayScore;
        Date gameDate;

        if (mCursor.moveToPosition(position)) {
            homeName = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.ScoresTable.HOME_COL));
            awayName = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.ScoresTable.AWAY_COL));
            homeScore = mCursor.getInt(mCursor.getColumnIndex(DatabaseContract.ScoresTable.HOME_GOALS_COL));
            awayScore = mCursor.getInt(mCursor.getColumnIndex(DatabaseContract.ScoresTable.AWAY_GOALS_COL));

            bindEmblem(view, homeName, R.id.home_crest);
            bindEmblem(view, awayName, R.id.away_crest);

            // score
            if (homeScore >= 0 && awayScore >= 0) {
                score = Integer.toString(homeScore) + " - " + Integer.toString(awayScore);
            } else {
                score = "@";
            }
            view.setTextViewText(R.id.score_textview, score);

            // game time
            rawDate = mCursor.getString(mCursor.getColumnIndex(DatabaseContract.ScoresTable.DATE_COL))
                    + "T"
                    + mCursor.getString(2)
                    + ":00Z";
            try {
                gameDate = mParseDate.parse(rawDate);
                if (isToday(gameDate)) {
                    view.setTextViewText(R.id.gametime_textview, "Today @ " + mToTime.format(gameDate));
                } else if (isTomorrow(gameDate)){
                    view.setTextViewText(R.id.gametime_textview, "Tomorrow @ " + mToTime.format(gameDate));
                } else {
                    view.setTextViewText(R.id.gametime_textview, mToDateTime.format(gameDate));
                }
            } catch (ParseException e) {
                Log.d(TAG, e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void bindEmblem(RemoteViews view, String teamName, int viewId) {
        InputStream inputStream;
        String url = "";
        try {
            url = mTeams.get(teamName);
            if (url == null) {
                url = teamName + ".png";  // These emblems were manually downloaded and saved. Ugly hack :(
            }
            inputStream = mContext.getAssets().open(url);
            view.setImageViewBitmap(viewId, BitmapFactory.decodeStream(inputStream));
            inputStream.close();
        } catch (IOException|NullPointerException|AndroidRuntimeException e) {
            Log.d(TAG, "Missing log for " + teamName + " => " + url);
            view.setImageViewResource(viewId, R.mipmap.no_emblem);
        }
    }
}
