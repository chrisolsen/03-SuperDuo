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

import barqsoft.footballscores.service.FetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MainScreenFragment.class.getSimpleName();
    private static final int SCORES_LOADER = 0;
    private ScoresAdapter mAdapter;
    private String mFragmentDate;

    public MainScreenFragment() {
    }

    // FIXME: having this method in the fragment means it is being called for
    // each section on the main page, which is NOT required; it only needs to
    // be called once.
    private void updateScores() {
        Intent service_start = new Intent(getActivity(), FetchService.class);
        getActivity().startService(service_start);
    }

    public void setFragmentDate(String date) {
        mFragmentDate = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView scoreList = (ListView) rootView.findViewById(R.id.scores_list);

        mAdapter = new ScoresAdapter(getActivity(), null, 0);

        updateScores();
        scoreList.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader URI " + DatabaseContract.ScoresTable.buildScoreWithDate().toString());
        return new CursorLoader(getActivity(), DatabaseContract.ScoresTable.buildScoreWithDate(),
                null, null, new String[]{mFragmentDate}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
