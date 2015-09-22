package barqsoft.footballscores.service;

import android.content.Context;
import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import barqsoft.footballscores.R;

public class FootballApi {
    private static final String BASE_URL = "http://api.football-data.org";

    public static String fetchTeams(Context c) throws IOException {
        Uri uri = Uri.parse(BASE_URL)
                .buildUpon()
                .appendPath("teams")
                .build();

        return get(c, uri);
    }

    public static String fetchGames(Context c, String timeFrame) throws IOException {
        Uri uri = Uri.parse(BASE_URL)
                .buildUpon()
                .appendPath("alpha/fixtures")
                .appendQueryParameter(timeFrame, timeFrame)
                .build();

        return get(c, uri);
    }

    private static String get(Context c, Uri uri) throws IOException {
        String apiKey = c.getResources().getString(R.string.api_key);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(uri.toString())
                .addHeader("X-Auth-Token", apiKey)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
