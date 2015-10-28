package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import it.jaschke.alexandria.MainActivity;
import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";
    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                fetchBook(ean);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if(ean!=null) {
            try {
                getContentResolver().delete(AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
            } catch (NumberFormatException ex) {
                Toast.makeText(this, R.string.invalid_ean, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private void fetchBook(String ean) {

        if (ean.length() != 13) {
            return;
        }

        Cursor bookEntry = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        assert bookEntry != null;
        if (bookEntry.getCount() > 0) {
            bookEntry.close();
            return;
        }

        bookEntry.close();

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            final String QUERY_PARAM = "q";
            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

            URL url = new URL(builtUri.toString());
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();

            Response response = client.newCall(request).execute();
            String json = response.body().string();

            Gson gson = new Gson();
            BookResponse bookResponse = gson.fromJson(json, BookResponse.class);

            if (bookResponse.items == null || bookResponse.items.size() == 0) {
                Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
                messageIntent.putExtra(MainActivity.MESSAGE_KEY, getResources().getString(R.string.not_found));
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                return;
            }

            BookVolumeInfo book = bookResponse.items.get(0).volumeInfo;
            saveBook(ean, book);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveBook(String ean, BookVolumeInfo book) {
        ContentValues values = new ContentValues();

        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, book.title);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, book.subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, book.description);
        if (book.imageLinks != null) {
            values.put(AlexandriaContract.BookEntry.IMAGE_URL, book.imageLinks.thumbnail);
        }

        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI, values);

        if (book.authors != null) saveAuthors(ean, book.authors);
        if (book.categories != null) saveCategories(ean, book.categories);

        Intent messageIntent = new Intent(MainActivity.MESSAGE_EVENT);
        messageIntent.putExtra(MainActivity.MESSAGE_KEY, getResources().getString(R.string.book_added));
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }

    private void saveAuthors(String ean, String[] authors) {
        ContentValues values;
        for (String author : authors) {
            values = new ContentValues();

            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, author);

            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
        }
    }

    private void saveCategories(String ean, String[] categories) {
        ContentValues values;
        for (String category : categories) {
            values = new ContentValues();

            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, category);

            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
        }
    }

    private class BookResponse {
        public List<BookItem> items;
    }

    private class BookItem {
        public String kind;
        public String id;
        public BookVolumeInfo volumeInfo;
    }

    private class BookVolumeInfo {
        public String title;
        public String subtitle;
        public String[] authors;
        public String[] categories;
        public String description;
        public BookImageUrls imageLinks;
    }

    private class BookImageUrls {
        public String thumbnail;
    }
 }