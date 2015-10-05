package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import barqsoft.footballscores.service.FetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MainScreenFragment.class.getSimpleName();
    private static final int SCORES_LOADER = 0;
    private ScoresAdapter mAdapter;
    private String mFragmentDate;

    private TextView mNoResults;

    private void updateScores() {
        Log.d(TAG, "updateScores again");
        Intent service_start = new Intent(getActivity(), FetchService.class);
        getActivity().startService(service_start);
    }

    /**
     * Sets the date value that is used to fetch the data
     *
     * @param date
     */
    public void setFragmentDate(String date) {
        mFragmentDate = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView scoreList = (ListView) rootView.findViewById(android.R.id.list);
        mNoResults = (TextView) rootView.findViewById(android.R.id.empty);

        mAdapter = new ScoresAdapter(getActivity(), null, 0);

        updateScores();
        scoreList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), DatabaseContract.ScoresTable.buildScoreWithDate(),
                null, null, new String[]{mFragmentDate}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        mNoResults.setVisibility(cursor.getCount() > 0 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
