package com.vkassin.ftracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Created by vadim on 25.03.15.
 */
public class OnAlarmReceiver extends BroadcastReceiver {

    private static final String NAME = "com.commonsware.cwac.wakeful.WakefulIntentService";
    private static volatile PowerManager.WakeLock lockStatic = null;
    protected static PowerManager.WakeLock lock;

    // Needed since network will to work when device is sleeping.
    synchronized protected static PowerManager.WakeLock getLock(Context context) {
        if (lockStatic == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

            lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, NAME);
            lockStatic.setReferenceCounted(true);
        }

        return (lockStatic);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // PullPendingRequests.acquireStaticLock(context)
        try {
            lock = getLock(context);
            lock.acquire();
            context.startService(new Intent(context, GPSTracker.class));
        } finally {
            if (lock.isHeld()) {
                lock.release();
            }
        }
    }
}
