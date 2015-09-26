package barqsoft.footballscores;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
                String pngFile = parts[parts.length - 1].replace(".svg", ".png");
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

        mHolder.homeName.setText(homeTeamName);
        mHolder.awayName.setText(awayTeamName);
        mHolder.date.setText(cursor.getString(COL_MATCH_TIME));
        mHolder.score.setText(score);
        mHolder.matchId = cursor.getDouble(COL_ID);

        try {
            String homeUrl = mTeams.get(homeTeamName);
            InputStream homeS = context.getResources().getAssets().open(homeUrl);
            Drawable homeD = Drawable.createFromStream(homeS, null);
            mHolder.homeCrest.setImageDrawable(homeD);

            String awayUrl = mTeams.get(awayTeamName);
            InputStream awayS = context.getResources().getAssets().open(awayUrl);
            Drawable awayD = Drawable.createFromStream(awayS, null);
            mHolder.awayCrest.setImageDrawable(awayD);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if (mHolder.matchId == detailMatchId) {

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
//            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
//            match_day.setText(Utilities.getMatchDay(context, cursor.getInt(COL_MATCH_DAY),
//                    cursor.getInt(COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);

            // FIXME: if we want to show leagues, additional queries must be added to the content provider
            // to prevent all the hardcoded values, for now comment it out.
            //league.setText(Utilities.getLeague(context, cursor.getInt(COL_LEAGUE)));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.homeName.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.awayName.getText() + " "));
                }
            });
        } else {
            container.removeAllViews();
        }

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
