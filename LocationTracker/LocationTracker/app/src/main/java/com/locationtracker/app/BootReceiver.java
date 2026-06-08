package com.locationtracker.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Restarts location tracking after device reboot if it was active.
 * Note: User must have granted all permissions and enabled "Start on Boot" in settings.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || "android.intent.action.QUICKBOOT_POWERON".equals(action)) {

            AppPreferences prefs = new AppPreferences(context);
            if (prefs.getUpdateIntervalMs() > 0) {
                // Only auto-start if the preference suggests it was active
                Intent serviceIntent = new Intent(context, LocationTrackingService.class);
                serviceIntent.setAction(LocationTrackingService.ACTION_START);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}
