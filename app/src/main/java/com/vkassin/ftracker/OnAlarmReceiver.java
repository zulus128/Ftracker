package com.vkassin.ftracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import java.util.Calendar;

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
        Log.i("position", "--- " + intent.getAction());

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Calendar cal = Calendar.getInstance();
            AlarmManager am = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            Intent notifyintent = new Intent(context, OnAlarmReceiver.class);
            notifyintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            notifyintent.setAction("android.intent.action.NOTIFY");
            PendingIntent notifysender = PendingIntent.getBroadcast(context, 0, notifyintent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 10 * 60 * 1000,
                    notifysender);
        }

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
