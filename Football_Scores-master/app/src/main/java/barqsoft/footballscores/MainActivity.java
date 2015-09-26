package barqsoft.footballscores;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends ActionBarActivity {
    private static final String STATE_PAGER_CURRENT = "Pager_Current";
    private static final String STATE_FRAG = "myMain";
    public static int currentFragment = 2;
    public static String LOG_TAG = "MainActivity";
    private final String saveTag = "Save Test";
    private PagerFragment myMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            myMain = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, myMain)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent start_about = new Intent(this, AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(saveTag, "will save");
        Log.v(saveTag, "fragment: " + String.valueOf(myMain.mPagerHandler.getCurrentItem()));
        outState.putInt(STATE_PAGER_CURRENT, myMain.mPagerHandler.getCurrentItem());
        getSupportFragmentManager().putFragment(outState, STATE_FRAG, myMain);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(saveTag, "will retrieve");
        Log.v(saveTag, "fragment: " + String.valueOf(savedInstanceState.getInt("Pager_Current")));
        currentFragment = savedInstanceState.getInt(STATE_PAGER_CURRENT);
        myMain = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, STATE_FRAG);
        super.onRestoreInstanceState(savedInstanceState);
    }
}
