package barqsoft.footballscores.service;

import android.content.Context;
import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import barqsoft.footballscores.R;

public class FootballApi {
    private static final String BASE_URL = "http://api.football-data.org/alpha/fixtures";
    private static final String QUERY_TIME_FRAME = "timeFrame"; //Time Frame parameter to determine days

    private static Uri getUri(String param) {
        return Uri.parse(BASE_URL)
                .buildUpon()
                .appendQueryParameter(QUERY_TIME_FRAME, param)
                .build();
    }

    public static String get(Context c, String timeFrame) throws IOException {
        String apiKey = c.getResources().getString(R.string.api_key);
        String url = getUri(timeFrame).toString();

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Auth-Token", apiKey)
                .build();

        Response response = client.newCall(request).execute();

        return response.body().string();
    }
}
