package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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

    private static final int CHAMPIONS_LEAGUE = 405;

    public static final int COL_HOME = 3;
    public static final int COL_AWAY = 4;
    public static final int COL_HOME_GOALS = 6;
    public static final int COL_AWAY_GOALS = 7;
    public static final int COL_LEAGUE = 5;
    public static final int COL_MATCH_DAY = 9;
    public static final int COL_ID = 8;
    public static final int COL_MATCH_TIME = 2;
    private static final String TAG = ScoresAdapter.class.getSimpleName();
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";

    private Map<String, String> mTeams = new HashMap<>();

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        mContext = context;

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
        int matchDay = cursor.getInt(COL_MATCH_DAY);

        int leagueId = cursor.getInt(COL_LEAGUE);
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

        SimpleDateFormat parseDate = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
        SimpleDateFormat toTime = new SimpleDateFormat("h:mm a");
        SimpleDateFormat toDateTime = new SimpleDateFormat("EEEE, MMMM d 'at' h:mm a");
        String dateStr = "2006-01-02T" + cursor.getString(COL_MATCH_TIME) + ":00Z";

        Resources r = context.getResources();
        Date happensOn;
        String gameTimeStr;
        String gameDateContentDesc;

        try {
            happensOn = parseDate.parse(dateStr);
            gameDateContentDesc = toDateTime.format(happensOn);
            gameTimeStr = toTime.format(happensOn);

            if (homeScore >=0 && awayScore >= 0) {
                String desc = String.format(r.getString(R.string.content_description_game_happened), gameDateContentDesc, homeTeamName, awayTeamName, homeScore, awayScore);
                view.setContentDescription(desc);
            } else {
                String desc = String.format(r.getString(R.string.content_description_game_upcoming), gameDateContentDesc, homeTeamName, awayTeamName);
                view.setContentDescription(desc);
            }
        } catch (ParseException e) {
            gameTimeStr = "";
        }

        mHolder.homeName.setText(homeTeamName);
        mHolder.awayName.setText(awayTeamName);
        mHolder.date.setText(gameTimeStr);
        mHolder.score.setText(score);
        mHolder.matchDay.setText(getMatchDay(matchDay, leagueId));
        mHolder.shareButton.setContentDescription(
                String.format(r.getString(R.string.content_description_share_button), homeTeamName, awayTeamName)
        );

        mHolder.shareButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Share " + FOOTBALL_SCORES_HASHTAG);
                context.startActivity(Intent.createChooser(shareIntent, context.getResources().getText(R.string.send_to)));
            }
        });

        bindEmblem(mTeams.get(homeTeamName), mHolder.homeCrest);
        bindEmblem(mTeams.get(awayTeamName), mHolder.awayCrest);
    }

    private String getMatchDay(int matchDay, int leagueId) {
        if (leagueId == CHAMPIONS_LEAGUE) {
            if (matchDay <= 6) {
                return mContext.getResources().getString(R.string.group_stage_text);
            } else if (matchDay == 7 || matchDay == 8) {
                return mContext.getResources().getString(R.string.first_knockout_round);
            } else if (matchDay == 9 || matchDay == 10) {
                return mContext.getResources().getString(R.string.quarter_final);
            } else if (matchDay == 11 || matchDay == 12) {
                return mContext.getResources().getString(R.string.semi_final);
            } else {
                return mContext.getResources().getString(R.string.final_text);
            }
        }

        return "";
    }

    public static class ViewHolder {
        public TextView homeName;
        public TextView awayName;
        public TextView score;
        public TextView date;
        public ImageView homeCrest;
        public ImageView awayCrest;
        public TextView matchDay;
        public ImageButton shareButton;

        public ViewHolder(View view) {
            homeName = (TextView) view.findViewById(R.id.home_name);
            awayName = (TextView) view.findViewById(R.id.away_name);
            score = (TextView) view.findViewById(R.id.score_textview);
            date = (TextView) view.findViewById(R.id.game_time);
            homeCrest = (ImageView) view.findViewById(R.id.home_crest);
            awayCrest = (ImageView) view.findViewById(R.id.away_crest);
            matchDay = (TextView) view.findViewById(R.id.match_day);
            shareButton = (ImageButton) view.findViewById(R.id.share_game);
        }
    }

}
