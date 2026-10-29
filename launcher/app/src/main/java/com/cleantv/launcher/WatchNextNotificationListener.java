package com.cleantv.launcher;

import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class WatchNextNotificationListener extends NotificationListenerService {
    private static final String TAG = "WatchNextNotif";
    private static final String PREFS_NAME = "CleanTvWatchNextPrefs";
    private static final String KEY_NOTIF_ITEMS = "notif_recommendations_json";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn == null) return;
        try {
            String pkg = sbn.getPackageName();
            if (pkg == null) return;
            String pkgLower = pkg.toLowerCase();

            // Strict User Requirement: NEVER show YouTube
            if (pkgLower.contains("youtube") || pkgLower.contains("yt_")) return;

            // Only capture streaming and media apps
            boolean isStreamingApp = pkgLower.contains("hbo") || pkgLower.contains("wbd")
                    || pkgLower.contains("disney") || pkgLower.contains("apple")
                    || pkgLower.contains("peacock") || pkgLower.contains("hulu")
                    || pkgLower.contains("netflix") || pkgLower.contains("amazonvideo")
                    || pkgLower.contains("prime") || pkgLower.contains("paramount")
                    || pkgLower.contains("cbs") || pkgLower.contains("starz");

            Notification n = sbn.getNotification();
            if (n == null) return;

            boolean isRecommendation = Notification.CATEGORY_RECOMMENDATION.equals(n.category);
            Bundle extras = n.extras;
            if (extras == null) return;

            CharSequence titleSeq = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence textSeq = extras.getCharSequence(Notification.EXTRA_TEXT);
            if (titleSeq == null || titleSeq.toString().trim().isEmpty()) return;

            String title = titleSeq.toString().trim();
            // Ignore system alert titles
            if (title.equalsIgnoreCase("Updating") || title.equalsIgnoreCase("Downloading")
                    || title.toLowerCase().contains("running in background")) {
                return;
            }

            if (!isRecommendation && !extras.containsKey(Notification.EXTRA_PICTURE) && !isStreamingApp) {
                return;
            }

            String ep = textSeq != null ? textSeq.toString().trim() : "Resume";

            String imageFile = null;
            if (extras.containsKey(Notification.EXTRA_PICTURE)) {
                Object picObj = extras.get(Notification.EXTRA_PICTURE);
                if (picObj instanceof Bitmap) {
                    imageFile = saveThumbnailLocally(this, "notif_" + sbn.getId(), (Bitmap) picObj);
                }
            }

            saveRecommendationItem(this, sbn.getKey(), pkg, title, ep, imageFile);
        } catch (Throwable t) {
            Log.e(TAG, "Error handling notification", t);
        }
    }

    private static String saveThumbnailLocally(Context context, String prefix, Bitmap bmp) {
        try {
            File dir = new File(context.getCacheDir(), "watch_thumbs");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, prefix + "_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (Throwable t) {
            return null;
        }
    }

    private static void saveRecommendationItem(Context context, String key, String pkg, String title, String ep, String imageFile) {
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String raw = sp.getString(KEY_NOTIF_ITEMS, "[]");
            JSONArray arr = new JSONArray(raw);

            // Check if already present, update or prepend
            JSONArray newArr = new JSONArray();
            JSONObject itemObj = new JSONObject();
            itemObj.put("key", key);
            itemObj.put("packageName", pkg);
            itemObj.put("title", title);
            itemObj.put("episodeInfo", ep);
            if (imageFile != null) itemObj.put("imageFile", imageFile);
            itemObj.put("timestamp", System.currentTimeMillis());

            newArr.put(itemObj);

            for (int i = 0; i < arr.length() && newArr.length() < 10; i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!title.equalsIgnoreCase(obj.optString("title"))) {
                    newArr.put(obj);
                }
            }

            sp.edit().putString(KEY_NOTIF_ITEMS, newArr.toString()).apply();
        } catch (Throwable t) {
            Log.e(TAG, "Error saving recommendation item", t);
        }
    }

    public static List<WatchNextItem> loadNotificationRecommendations(Context context) {
        List<WatchNextItem> list = new ArrayList<>();
        if (context == null) return list;
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String raw = sp.getString(KEY_NOTIF_ITEMS, "[]");
            JSONArray arr = new JSONArray(raw);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String pkg = obj.optString("packageName");
                if (pkg == null || pkg.toLowerCase().contains("youtube")) continue;

                String title = obj.optString("title");
                if (title == null || title.isEmpty() || title.toLowerCase().contains("youtube")) continue;

                String ep = obj.optString("episodeInfo", "Resume");
                String imgPath = obj.optString("imageFile", null);

                Bitmap bmp = null;
                if (imgPath != null) {
                    File f = new File(imgPath);
                    if (f.exists()) {
                        bmp = android.graphics.BitmapFactory.decodeFile(f.getAbsolutePath());
                    }
                }

                String badge = WatchNextManager.resolveBadgeFromPackage(pkg);
                int color = WatchNextManager.resolveBrandColor(pkg);

                list.add(new WatchNextItem(
                        "notif_" + i,
                        pkg,
                        badge,
                        title,
                        ep,
                        false,
                        50,
                        color,
                        null,
                        null,
                        0,
                        bmp
                ));
            }
        } catch (Throwable t) {
            Log.e(TAG, "loadNotificationRecommendations error", t);
        }
        return list;
    }
}
