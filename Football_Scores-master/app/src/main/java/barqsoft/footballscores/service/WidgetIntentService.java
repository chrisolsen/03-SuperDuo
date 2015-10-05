package barqsoft.footballscores.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

public class WidgetIntentService extends IntentService {
    public static final String ACTION_FOO = "barqsoft.footballscores.service.action.FOO";

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionFoo(Context context) {
        Intent intent = new Intent(context, WidgetIntentService.class);
        intent.setAction(ACTION_FOO);
        context.startService(intent);
    }

    public WidgetIntentService() {
        super("WidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                handleActionFoo();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo() {
        Intent intent = new Intent(ACTION_FOO);
        sendBroadcast(intent);
    }
}
