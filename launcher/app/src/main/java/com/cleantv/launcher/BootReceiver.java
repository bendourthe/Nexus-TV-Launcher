package com.cleantv.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "CleanBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        Log.i(TAG, "onReceive: action=" + action);

        // Re-assert accessibility service state if write settings permission is available
        try {
            Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            Settings.Secure.putString(context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    "com.cleantv.launcher/.HomeButtonAccessibilityService");
        } catch (Throwable ignored) {
        }

        // Launch Clean TV Launcher as soon as boot completes
        dispatchCleanLauncher(context);

        // Schedule follow-up assertions at 400ms, 1200ms, and 2500ms in case the system launcher starts late
        final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
        handler.postDelayed(() -> dispatchCleanLauncher(context), 400);
        handler.postDelayed(() -> dispatchCleanLauncher(context), 1200);
        handler.postDelayed(() -> dispatchCleanLauncher(context), 2500);
    }

    private void dispatchCleanLauncher(Context context) {
        try {
            Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.setAction(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_HOME);
            launchIntent.addCategory(Intent.CATEGORY_DEFAULT);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP
                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);

            try {
                android.app.ActivityOptions opts = android.app.ActivityOptions.makeCustomAnimation(context, 0, 0);
                context.startActivity(launchIntent, opts.toBundle());
            } catch (Throwable optEx) {
                context.startActivity(launchIntent);
            }
            Log.i(TAG, "Clean TV Launcher successfully dispatched on boot / wake");
        } catch (Throwable e) {
            Log.e(TAG, "Failed to launch MainActivity on boot", e);
        }
    }
}
