package barqsoft.footballscores.widget;

import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class WidgetService extends RemoteViewsService {

    private final String TAG = this.getClass().getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d(TAG, "onGetViewFactory ");
        return new WidgetViewsFactory(this.getApplicationContext(), intent);
    }
}
