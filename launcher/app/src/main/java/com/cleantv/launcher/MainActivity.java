package com.cleantv.launcher;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String TAG = "CleanMainActivity";

    private static class AppTag {
        final AppItem app;
        final int dockIndex; // >= 0 for dock, -1 for grid

        AppTag(AppItem app, int dockIndex) {
            this.app = app;
            this.dockIndex = dockIndex;
        }
    }

    private static final String PREFS_NAME = "CleanTvLauncherPrefs";
    private static final String KEY_DOCK_PACKAGES = "dock_packages";
    private static final String KEY_THEME_INDEX = "theme_index";
    private static final String KEY_THEME_CATEGORY = "theme_category";
    private static final String KEY_ROTATION_INTERVAL = "theme_rotation_interval";
    private static final String KEY_AUTO_ROTATE = "auto_rotate";

    // 6 requested dock apps supporting both Google TV / Android TV and Amazon Fire
    // TV package identifiers
    private static final String[][] DOCK_SLOT_CANDIDATES = new String[][] {
            { "com.google.android.youtube.tv", "com.amazon.firetv.youtube", "com.amazon.firetv.youtube.tv" }, // YouTube
            { "com.disney.disneyplus" }, // Disney+
            { "com.wbd.stream", "com.hbo.hbonow" }, // Max / HBO
            { "com.apple.atve.androidtv.appletv", "com.apple.atve.amazon.appletv" }, // Apple TV
            { "com.amazon.firebat", "com.amazon.amazonvideo.livingroom", "com.amazon.avod" }, // Prime Video
            { "com.peacocktv.peacockandroid", "com.peacock.peacockfiretv", "com.hulu.plus", "com.cbs.ott" } // Peacock /
                                                                                                            // Hulu /
                                                                                                            // Paramount+
    };

    private AnimatedWaveBackgroundView animatedBackground;
    private TvScrollView mainScrollView;
    private TextView tvGreeting;
    private TextView tvClock;
    private TextView tvDate;
    private LinearLayout btnWeather;
    private TextView tvWeatherLocation;
    private TextView tvWeatherTempF;
    private TextView tvWeatherTempC;
    private ImageView ivWeatherIcon;
    private TextView tvWeatherCondition;
    private TextView btnProfile;
    private TextView btnTheme;
    private TextView btnInputs;
    private TextView btnBluetooth;
    private TextView btnSettings;
    private TextView tvFocusedAppName;
    private LinearLayout dockContainer;
    private LinearLayout containerContinueWatching;
    private TextView tvWatchNextHeader;
    private HorizontalScrollView scrollWatchFilters;
    private LinearLayout filterPillContainer;
    private HorizontalScrollView scrollWatchNext;
    private LinearLayout shelfWatchNext;
    private GridLayout appGrid;

    private boolean isDockMovingMode = false;
    private int movingDockIndex = -1;
    private String currentWifiSsid = "Tutu";

    private String activeWatchFilterKey = "ALL";
    private final List<WatchNextItem> watchNextItems = new ArrayList<>();

    private Typeface outfitTypeface;
    private final List<AppItem> allInstalledApps = new ArrayList<>();
    private final Map<String, AppItem> appMap = new HashMap<>();
    private final List<String> dockPackageList = new ArrayList<>();

    private View lastFocusedDockView = null;
    private int lastFocusedDockIndex = 0;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static volatile MainActivity sInstance;
    private static volatile boolean sIsResumed = false;
    private static volatile boolean sHasWindowFocus = false;
    private final List<android.app.Dialog> activeDialogs = new ArrayList<>();
    private final Runnable snapshotCaptureRunnable = this::captureHomeSnapshot;

    public static boolean isMainActivityFocused() {
        MainActivity inst = sInstance;
        return inst != null && (sIsResumed || sHasWindowFocus || inst.hasWindowFocus());
    }

    public static void requestResetToHomeFromService() {
        MainActivity inst = sInstance;
        if (inst != null) {
            inst.mainHandler.post(inst::dismissAllDialogsAndResetToHome);
        }
    }

    public static void requestShowInputsMenuFromService() {
        MainActivity inst = sInstance;
        if (inst != null) {
            inst.mainHandler.post(inst::showInputsDialog);
        }
    }

    public static void requestShowUserMenuFromService() {
        MainActivity inst = sInstance;
        if (inst != null) {
            inst.mainHandler.post(inst::showProfileSelectionDialog);
        }
    }

    public static void onProfileSyncedFromPeer() {
        MainActivity inst = sInstance;
        if (inst != null) {
            inst.mainHandler.post(() -> {
                try {
                    inst.updateProfilePill();
                    inst.populateContinueWatchingShelf();
                    inst.scheduleHomeSnapshotCapture(150);
                } catch (Throwable ignored) {
                }
            });
        }
    }

    private void notifyFrameDrawnWhenReady() {
        try {
            if (getWindow() != null && getWindow().getDecorView() != null) {
                getWindow().getDecorView().postOnAnimation(
                        HomeButtonAccessibilityService::onMainActivityFrameDrawnAndFocused);
                return;
            }
        } catch (Throwable ignored) {
        }
        HomeButtonAccessibilityService.onMainActivityFrameDrawnAndFocused();
    }

    private AlertDialog trackDialog(AlertDialog dialog) {
        if (dialog == null)
            return null;
        activeDialogs.add(dialog);
        dialog.setOnKeyListener((d, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_HOME) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    dismissAllDialogsAndResetToHome();
                }
                return true;
            }
            return false;
        });
        dialog.setOnDismissListener(d -> {
            activeDialogs.remove(dialog);
            if (activeDialogs.isEmpty()) {
                scheduleHomeSnapshotCapture(250);
            }
        });
        return dialog;
    }

    private AlertDialog showTrackedBuilder(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();
        trackDialog(dialog);
        dialog.show();
        return dialog;
    }

    private boolean hasActiveDialogs() {
        for (int i = activeDialogs.size() - 1; i >= 0; i--) {
            android.app.Dialog d = activeDialogs.get(i);
            if (d != null && d.isShowing()) {
                return true;
            } else {
                activeDialogs.remove(i);
            }
        }
        return false;
    }

    public void dismissAllDialogsAndResetToHome() {
        try {
            if (isDockMovingMode) {
                stopDockMoveMode();
            }
            List<android.app.Dialog> copy = new ArrayList<>(activeDialogs);
            activeDialogs.clear();
            for (android.app.Dialog d : copy) {
                try {
                    if (d != null && d.isShowing()) {
                        d.dismiss();
                    }
                } catch (Throwable ignored) {
                }
            }
            if (mainScrollView != null) {
                mainScrollView.scrollTo(0, 0);
                mainScrollView.smoothScrollToPosition(0, 80);
            }
            if (dockContainer != null && dockContainer.getChildCount() > 0) {
                int target = Math.min(lastFocusedDockIndex, Math.max(0, dockContainer.getChildCount() - 1));
                View v = dockContainer.getChildAt(target);
                if (v != null) {
                    v.requestFocus();
                }
            }
            scheduleHomeSnapshotCapture(220);
        } catch (Throwable t) {
            Log.w(TAG, "dismissAllDialogsAndResetToHome error: " + t.getMessage());
        }
    }

    private void scheduleHomeSnapshotCapture(long delayMs) {
        mainHandler.removeCallbacks(snapshotCaptureRunnable);
        mainHandler.postDelayed(snapshotCaptureRunnable, delayMs);
    }

    private void captureHomeSnapshot() {
        try {
            if (!sIsResumed || hasActiveDialogs() || isDockMovingMode) {
                return;
            }
            if (getWindow() == null || getWindow().getDecorView() == null) {
                return;
            }
            View root = getWindow().getDecorView().getRootView();
            if (root == null || root.getWidth() <= 0 || root.getHeight() <= 0) {
                return;
            }
            android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(
                    root.getWidth(), root.getHeight(), android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bmp);
            root.draw(canvas);
            HomeButtonAccessibilityService.updateHomeSnapshot(bmp);
        } catch (Throwable t) {
            Log.w(TAG, "captureHomeSnapshot error: " + t.getMessage());
        }
    }

    private final BroadcastReceiver timeTickReceiver=new BroadcastReceiver(){@Override public void onReceive(Context context,Intent intent){updateTimeAndGreeting();}};

    private final BroadcastReceiver packageReceiver=new BroadcastReceiver(){@Override public void onReceive(Context context,Intent intent){loadInstalledApps();populateDock();populateContinueWatchingShelf();populateAllAppsGrid();}};

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sInstance = this;
        try {
            if (getWindow() != null) {
                getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            }
            if (android.os.Build.VERSION.SDK_INT >= 27) {
                setShowWhenLocked(true);
                setTurnScreenOn(true);
            }
        } catch (Throwable ignored) {
        }
        setContentView(R.layout.activity_main);

        animatedBackground = findViewById(R.id.animatedBackground);
        mainScrollView = findViewById(R.id.mainScrollView);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvClock = findViewById(R.id.tvClock);
        tvDate = findViewById(R.id.tvDate);
        btnWeather = findViewById(R.id.btnWeather);
        tvWeatherLocation = findViewById(R.id.tvWeatherLocation);
        tvWeatherTempF = findViewById(R.id.tvWeatherTempF);
        tvWeatherTempC = findViewById(R.id.tvWeatherTempC);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        tvWeatherCondition = findViewById(R.id.tvWeatherCondition);
        btnProfile = findViewById(R.id.btnProfile);
        btnTheme = findViewById(R.id.btnTheme);
        btnInputs = findViewById(R.id.btnInputs);
        btnBluetooth = findViewById(R.id.btnBluetooth);
        btnSettings = findViewById(R.id.btnSettings);
        tvFocusedAppName = findViewById(R.id.tvFocusedAppName);
        dockContainer = findViewById(R.id.dockContainer);
        containerContinueWatching = findViewById(R.id.containerContinueWatching);
        tvWatchNextHeader = findViewById(R.id.tvWatchNextHeader);
        scrollWatchFilters = findViewById(R.id.scrollWatchFilters);
        filterPillContainer = findViewById(R.id.filterPillContainer);
        scrollWatchNext = findViewById(R.id.scrollWatchNext);
        shelfWatchNext = findViewById(R.id.shelfWatchNext);
        appGrid = findViewById(R.id.appGrid);

        btnWeather.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.04f).scaleY(1.04f).setDuration(180)
                        .setInterpolator(new DecelerateInterpolator(1.6f)).start();
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(160)
                        .setInterpolator(new DecelerateInterpolator(1.6f)).start();
            }
        });

        try {
            outfitTypeface = Typeface.createFromAsset(getAssets(), "fonts/outfit.ttf");
            if (outfitTypeface != null) {
                tvGreeting.setTypeface(outfitTypeface);
                tvClock.setTypeface(outfitTypeface, Typeface.BOLD);
                tvDate.setTypeface(outfitTypeface);
                if (tvWeatherLocation != null)
                    tvWeatherLocation.setTypeface(outfitTypeface);
                if (tvWeatherTempF != null)
                    tvWeatherTempF.setTypeface(outfitTypeface, Typeface.BOLD);
                if (tvWeatherTempC != null)
                    tvWeatherTempC.setTypeface(outfitTypeface);
                if (tvWeatherCondition != null)
                    tvWeatherCondition.setTypeface(outfitTypeface);
                if (btnProfile != null)
                    btnProfile.setTypeface(outfitTypeface);
                if (btnInputs != null)
                    btnInputs.setTypeface(outfitTypeface);
                if (btnBluetooth != null)
                    btnBluetooth.setTypeface(outfitTypeface);
                if (btnSettings != null)
                    btnSettings.setTypeface(outfitTypeface);
                if (tvFocusedAppName != null)
                    tvFocusedAppName.setTypeface(outfitTypeface);
                if (tvWatchNextHeader != null)
                    tvWatchNextHeader.setTypeface(outfitTypeface);
                ((TextView) findViewById(R.id.tvAllAppsHeader)).setTypeface(outfitTypeface);
            }
        } catch (Throwable ignored) {
        }

        setupTopBar();
        setupWeather();
        loadInstalledApps();
        loadPreferences();
        populateDock();
        populateContinueWatchingShelf();
        populateAllAppsGrid();
        updateTimeAndGreeting();
        updateWifiStatus();
        updateBluetoothStatus();

        registerReceiver(timeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));

        IntentFilter pkgFilter = new IntentFilter();
        pkgFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        pkgFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        pkgFilter.addDataScheme("package");
        try {
            registerReceiver(packageReceiver, pkgFilter);
        } catch (Throwable ignored) {
        }

        handleIntent(getIntent());
        ProfileSyncManager.start(this);
        int defaultAvatarPx = (int) (64 * getResources().getDisplayMetrics().density);
        WatchNextManager.prewarmAvatarsAsync(this, defaultAvatarPx);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        if (intent == null || !intent.getBooleanExtra("EXTRA_OPEN_USER_MENU", false)) {
            dismissAllDialogsAndResetToHome();
        }
        notifyFrameDrawnWhenReady();
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("theme_index")) {
            int t = intent.getIntExtra("theme_index", 0);
            if (animatedBackground != null) {
                animatedBackground.setThemeIndex(t);
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit().putInt(KEY_THEME_INDEX, t).apply();
            }
        }
        if (intent != null && intent.getBooleanExtra("EXTRA_OPEN_USER_MENU", false)) {
            getWindow().getDecorView().post(this::showProfileSelectionDialog);
        }
        if (intent != null && intent.getBooleanExtra("EXTRA_OPEN_INPUTS_MENU", false)) {
            getWindow().getDecorView().post(this::showInputsDialog);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sInstance = this;
        sIsResumed = true;
        ProfileSyncManager.start(this);
        ProfileSyncManager.requestAndBroadcastSync(this);
        updateTimeAndGreeting();
        updateProfilePill();

        if (allInstalledApps.isEmpty()) {
            loadInstalledApps();
            loadPreferences();
            populateDock();
            populateContinueWatchingShelf();
            populateAllAppsGrid();
        } else {
            // Instant resumption: UI is cached in memory; only refresh Continue Watching
            populateContinueWatchingShelf();
        }

        if (!hasActiveDialogs()) {
            int target = Math.min(lastFocusedDockIndex, Math.max(0, dockContainer.getChildCount() - 1));
            if (dockContainer.getChildCount() > target) {
                dockContainer.getChildAt(target).requestFocus();
            }
            mainScrollView.smoothScrollToPosition(0, 100);
        }

        notifyFrameDrawnWhenReady();
        scheduleHomeSnapshotCapture(150);

        new Thread(() -> {
            try {
                ensureAccessibilityServiceEnabled();
                updateWifiStatus();
                updateBluetoothStatus();
            } catch (Throwable ignored) {
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sIsResumed = false;
        sHasWindowFocus = false;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        sHasWindowFocus = hasFocus;
        if (hasFocus) {
            notifyFrameDrawnWhenReady();
            mainHandler.postDelayed(this::notifyFrameDrawnWhenReady, 40);
            scheduleHomeSnapshotCapture(150);
        }
    }

    private void ensureAccessibilityServiceEnabled() {
        try {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            Settings.Secure.putString(getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    "com.cleantv.launcher/.HomeButtonAccessibilityService");
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sInstance == this) {
            sInstance = null;
        }
        sHasWindowFocus = false;
        try {
            unregisterReceiver(timeTickReceiver);
        } catch (Throwable ignored) {
        }
        try {
            unregisterReceiver(packageReceiver);
        } catch (Throwable ignored) {
        }
    }

    private List<String> resolveDefaultDockPackages() {
        List<String> resolved = new ArrayList<>();
        Set<String> chosen = new HashSet<>();

        // Step 1: For each curated dock slot, find the first matching package installed
        // on this device
        for (String[] candidates : DOCK_SLOT_CANDIDATES) {
            for (String candidate : candidates) {
                if (appMap.containsKey(candidate) && !chosen.contains(candidate)) {
                    resolved.add(candidate);
                    chosen.add(candidate);
                    break;
                }
            }
        }

        // Step 2: If fewer than 6 apps resolved, fill remaining slots with other
        // installed apps
        if (resolved.size() < 6) {
            for (AppItem item : allInstalledApps) {
                if (resolved.size() >= 6)
                    break;
                if (!chosen.contains(item.packageName)
                        && !item.packageName.toLowerCase().contains("instagram")
                        && !item.packageName.equals(getPackageName())) {
                    resolved.add(item.packageName);
                    chosen.add(item.packageName);
                }
            }
        }

        return resolved;
    }

    private void loadPreferences() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Load dock packages
        dockPackageList.clear();
        String saved = sp.getString(KEY_DOCK_PACKAGES, null);
        if (saved != null && !saved.trim().isEmpty()) {
            String[] parts = saved.split(",");
            for (String p : parts) {
                if (!p.trim().isEmpty())
                    dockPackageList.add(p.trim());
            }
        }

        // Validate that saved dock contains valid installed apps on this device.
        // If empty, or if fewer than 2 saved apps actually exist in appMap (which
        // happens when transferring
        // prefs or defaults across different Android TV vs Fire TV platforms),
        // re-resolve defaults!
        int validInstalledCount = 0;
        for (String pkg : dockPackageList) {
            if (appMap.containsKey(pkg)) {
                validInstalledCount++;
            }
        }

        if (dockPackageList.isEmpty() || validInstalledCount < 2 || !sp.getBoolean("v3_dock_updated", false)) {
            dockPackageList.clear();
            dockPackageList.addAll(resolveDefaultDockPackages());
            saveDockPackages();
            sp.edit().putBoolean("v3_dock_updated", true).apply();
        }

        // Load theme category, rotation interval, theme index and auto-rotation
        String themeCat = sp.getString(KEY_THEME_CATEGORY, AnimatedWaveBackgroundView.ThemeCategory.DYNAMIC_ABSTRACT.name());
        long interval = sp.getLong(KEY_ROTATION_INTERVAL, AnimatedWaveBackgroundView.DEFAULT_ROTATION_INTERVAL_MS);
        int themeIdx = sp.getInt(KEY_THEME_INDEX, 0);
        boolean autoRotate = sp.getBoolean(KEY_AUTO_ROTATE, true);
        if (animatedBackground != null) {
            animatedBackground.setCategoryByName(themeCat);
            animatedBackground.setRotationIntervalMs(interval);
            animatedBackground.setThemeIndex(themeIdx);
            animatedBackground.setAutoRotateEnabled(autoRotate);
        }
    }

    private void saveDockPackages() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dockPackageList.size(); i++) {
            sb.append(dockPackageList.get(i));
            if (i < dockPackageList.size() - 1)
                sb.append(",");
        }
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sp.edit().putString(KEY_DOCK_PACKAGES, sb.toString()).apply();
    }

    private void setupTopBar() {
        // Focus animation for high contrast and dynamic feedback on all pills
        View.OnFocusChangeListener pillFocusListener = (v, hasFocus) -> {
            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                if (hasFocus) {
                    tv.animate().scaleX(1.08f).scaleY(1.08f).setDuration(160)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                    if (outfitTypeface != null) {
                        tv.setTypeface(outfitTypeface, Typeface.BOLD);
                    }
                } else {
                    tv.animate().scaleX(1.0f).scaleY(1.0f).setDuration(140)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                    if (outfitTypeface != null) {
                        tv.setTypeface(outfitTypeface, Typeface.NORMAL);
                    }
                }
            }
        };

        updateProfilePill();
        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> showProfileSelectionDialog());
            btnProfile.setOnFocusChangeListener(pillFocusListener);
        }

        if (btnTheme != null) {
            btnTheme.setOnClickListener(v -> showThemeDialog());
            btnTheme.setOnFocusChangeListener(pillFocusListener);
        }

        if (btnInputs != null) {
            btnInputs.setOnClickListener(v -> showInputsDialog());
            btnInputs.setOnFocusChangeListener(pillFocusListener);
        }

        if (btnBluetooth != null) {
            btnBluetooth.setOnClickListener(v -> showBluetoothDialog());
            btnBluetooth.setOnFocusChangeListener(pillFocusListener);
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> showSettingsDialog());
            btnSettings.setOnFocusChangeListener(pillFocusListener);
        }

        btnWeather.setOnClickListener(v -> showWeatherDialog());

        // Horizontal pill chaining: Weather -> Profile -> Inputs -> Bluetooth ->
        // Settings
        btnWeather.setNextFocusRightId(R.id.btnProfile);
        if (btnProfile != null) {
            btnProfile.setNextFocusLeftId(R.id.btnWeather);
            btnProfile.setNextFocusRightId(R.id.btnTheme);
        }
        if (btnTheme != null) {
            btnTheme.setNextFocusLeftId(R.id.btnProfile);
            btnTheme.setNextFocusRightId(R.id.btnInputs);
        }
        if (btnInputs != null) {
            btnInputs.setNextFocusLeftId(R.id.btnTheme);
            btnInputs.setNextFocusRightId(R.id.btnBluetooth);
        }
        if (btnBluetooth != null) {
            btnBluetooth.setNextFocusLeftId(R.id.btnInputs);
            btnBluetooth.setNextFocusRightId(R.id.btnSettings);
        }
        if (btnSettings != null) {
            btnSettings.setNextFocusLeftId(R.id.btnBluetooth);
            btnSettings.setNextFocusRightId(R.id.btnSettings);
        }

        // Down from top bar pills routes to dock
        View.OnKeyListener pillDownListener = (v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (lastFocusedDockView != null) {
                    lastFocusedDockView.requestFocus();
                    return true;
                } else if (dockContainer.getChildCount() > 0) {
                    dockContainer.getChildAt(0).requestFocus();
                    return true;
                }
            }
            return false;
        };
        btnWeather.setOnKeyListener(pillDownListener);
        if (btnProfile != null)
            btnProfile.setOnKeyListener(pillDownListener);
        if (btnTheme != null)
            btnTheme.setOnKeyListener(pillDownListener);
        if (btnInputs != null)
            btnInputs.setOnKeyListener(pillDownListener);
        if (btnBluetooth != null)
            btnBluetooth.setOnKeyListener(pillDownListener);
        if (btnSettings != null)
            btnSettings.setOnKeyListener(pillDownListener);
    }

    private void updateProfilePill() {
        if (btnProfile != null) {
            String p = WatchNextManager.getCurrentProfile(this);
            String av = WatchNextManager.getProfileAvatar(this, p);
            final float density = getResources().getDisplayMetrics().density;
            int badgeSize = (int) (18 * density);
            Drawable monogram = WatchNextManager.createMonogramDrawable(this, p, av, badgeSize, outfitTypeface);
            btnProfile.setCompoundDrawablesWithIntrinsicBounds(monogram, null, null, null);
            btnProfile.setCompoundDrawablePadding((int) (8 * density));
            btnProfile.setText(p + " ▾");
        }
    }

    private void updateWeatherViews(String location, String condition, String icon, int tempF, int tempC) {
        if (tvWeatherLocation != null) {
            tvWeatherLocation.setText(location + " ▾");
        }
        if (tvWeatherTempF != null) {
            tvWeatherTempF.setText(tempF + "°F");
        }
        if (tvWeatherTempC != null) {
            tvWeatherTempC.setText("/ " + tempC + "°C");
        }
        if (ivWeatherIcon != null) {
            int iconRes = WeatherManager.getWeatherDrawableRes(icon);
            ivWeatherIcon.setImageResource(iconRes);
        }
        if (tvWeatherCondition != null) {
            tvWeatherCondition.setText(condition);
        }
    }

    private void setupWeather() {
        WeatherManager.WeatherInfo info = WeatherManager.getCachedWeather(this);
        updateWeatherViews(info.location, info.condition, info.icon, info.tempF, info.tempC);
        refreshWeather();
    }

    private void refreshWeather() {
        WeatherManager.fetchWeatherAsync(this, new WeatherManager.WeatherCallback() {
            @Override
            public void onWeatherUpdated(String location, String condition, String icon, int tempF, int tempC) {
                updateWeatherViews(location, condition, icon, tempF, tempC);
            }

            @Override
            public void onWeatherFailed(String error) {
                // Preserves cached weather
            }
        });
    }

    private void showWeatherDialog() {
        String[] options = new String[] {
                "Auto-Detect Location (Network)",
                "Refresh Weather Now",
                "San Jose, CA",
                "San Francisco, CA",
                "Santa Clara, CA",
                "Sunnyvale, CA",
                "Mountain View, CA",
                "Fremont, CA",
                "Oakland, CA",
                "Los Angeles, CA",
                "Seattle, WA",
                "New York, NY",
                "London, UK",
                "Tokyo, Japan",
                "Search Custom City..."
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Weather Location");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    Toast.makeText(this, "Detecting location from network...", Toast.LENGTH_SHORT).show();
                    WeatherManager.detectLocationFromNetwork(this, new WeatherManager.LocationDetectCallback() {
                        @Override
                        public void onSuccess(String location, double lat, double lon) {
                            Toast.makeText(MainActivity.this, "Location set to " + location, Toast.LENGTH_SHORT).show();
                            refreshWeather();
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(MainActivity.this, "Detection failed: " + error, Toast.LENGTH_LONG).show();
                        }
                    });
                    break;
                case 1:
                    refreshWeather();
                    Toast.makeText(this, "Refreshing weather...", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    WeatherManager.setCustomLocation(this, "San Jose, CA", 37.3388, -121.8916);
                    refreshWeather();
                    break;
                case 3:
                    WeatherManager.setCustomLocation(this, "San Francisco, CA", 37.7749, -122.4194);
                    refreshWeather();
                    break;
                case 4:
                    WeatherManager.setCustomLocation(this, "Santa Clara, CA", 37.3541, -121.9552);
                    refreshWeather();
                    break;
                case 5:
                    WeatherManager.setCustomLocation(this, "Sunnyvale, CA", 37.3688, -122.0363);
                    refreshWeather();
                    break;
                case 6:
                    WeatherManager.setCustomLocation(this, "Mountain View, CA", 37.3861, -122.0839);
                    refreshWeather();
                    break;
                case 7:
                    WeatherManager.setCustomLocation(this, "Fremont, CA", 37.5485, -121.9886);
                    refreshWeather();
                    break;
                case 8:
                    WeatherManager.setCustomLocation(this, "Oakland, CA", 37.8044, -122.2712);
                    refreshWeather();
                    break;
                case 9:
                    WeatherManager.setCustomLocation(this, "Los Angeles, CA", 34.0522, -118.2437);
                    refreshWeather();
                    break;
                case 10:
                    WeatherManager.setCustomLocation(this, "Seattle, WA", 47.6062, -122.3321);
                    refreshWeather();
                    break;
                case 11:
                    WeatherManager.setCustomLocation(this, "New York, NY", 40.7128, -74.0060);
                    refreshWeather();
                    break;
                case 12:
                    WeatherManager.setCustomLocation(this, "London, UK", 51.5074, -0.1278);
                    refreshWeather();
                    break;
                case 13:
                    WeatherManager.setCustomLocation(this, "Tokyo, Japan", 35.6762, 139.6503);
                    refreshWeather();
                    break;
                case 14:
                    showCitySearchDialog();
                    break;
            }
        });
        builder.setNegativeButton("Cancel", null);
        showTrackedBuilder(builder);
    }

    private void showCitySearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Search Custom City");

        final float density = getResources().getDisplayMetrics().density;
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padH = (int) (24 * density);
        int padV = (int) (14 * density);
        container.setPadding(padH, padV, padH, padV);

        TextView tvDesc = new TextView(this);
        tvDesc.setText("Type a city name to search (e.g. Chicago, Paris, Austin, Miami):");
        tvDesc.setTextSize(13f);
        tvDesc.setTextColor(0xFFE0E0E0);
        tvDesc.setPadding(0, 0, 0, (int) (10 * density));
        if (outfitTypeface != null) tvDesc.setTypeface(outfitTypeface);
        container.addView(tvDesc);

        final EditText input = new EditText(this);
        input.setHint("City name...");
        // Crucial requirement: High-contrast dark text on white typing background
        input.setTextColor(0xFF111115);
        input.setHintTextColor(0xFF757580);
        input.setTextSize(17f);
        input.setSingleLine(true);
        if (outfitTypeface != null) input.setTypeface(outfitTypeface);

        // Explicit white card background so the typing area is always crisp and visible
        android.graphics.drawable.GradientDrawable inputCardBg = new android.graphics.drawable.GradientDrawable();
        inputCardBg.setColor(Color.WHITE);
        inputCardBg.setCornerRadius(10f * density);
        inputCardBg.setStroke((int) (2 * density), 0xFFB0B0C0);
        input.setBackground(inputCardBg);
        int inputPadH = (int) (16 * density);
        int inputPadV = (int) (12 * density);
        input.setPadding(inputPadH, inputPadV, inputPadH, inputPadV);

        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Search", (dialog, which) -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) {
                Toast.makeText(this, "Searching for " + query + "...", Toast.LENGTH_SHORT).show();
                WeatherManager.searchCityAsync(query, new WeatherManager.CitySearchCallback() {
                    @Override
                    public void onResults(java.util.List<WeatherManager.CityResult> results) {
                        if (results == null || results.isEmpty()) {
                            Toast.makeText(MainActivity.this, "No matching cities found", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String[] cityNames = new String[results.size()];
                        for (int i = 0; i < results.size(); i++) {
                            cityNames[i] = results.get(i).getDisplayName();
                        }
                        AlertDialog.Builder subB = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                                .setTitle("Select City")
                                .setItems(cityNames, (d, sel) -> {
                                    WeatherManager.CityResult chosen = results.get(sel);
                                    WeatherManager.setCustomLocation(MainActivity.this, chosen.getDisplayName(), chosen.lat, chosen.lon);
                                    refreshWeather();
                                    Toast.makeText(MainActivity.this, "Weather updated for " + chosen.getDisplayName(), Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Cancel", null);
                        showTrackedBuilder(subB);
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(MainActivity.this, "Search failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = showTrackedBuilder(builder);
        input.requestFocus();
    }

    private void updateTimeAndGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Good morning";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Good afternoon";
        } else {
            greeting = "Good evening";
        }
        tvGreeting.setText(greeting);

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());

        Date now = new Date();
        tvClock.setText(timeFormat.format(now));
        tvDate.setText(dateFormat.format(now));
        scheduleHomeSnapshotCapture(80);
    }

    private void updateWifiStatus() {
        try {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo wi = wm.getConnectionInfo();
                if (wi != null && wi.getSSID() != null) {
                    String ssid = wi.getSSID().replace("\"", "");
                    if (!ssid.isEmpty() && !ssid.contains("unknown")) {
                        currentWifiSsid = ssid;
                        return;
                    }
                }
            }
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo net = cm.getActiveNetworkInfo();
                if (net != null && net.isConnected()) {
                    currentWifiSsid = "Connected";
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
        currentWifiSsid = "Connected";
    }

    private void loadInstalledApps() {
        allInstalledApps.clear();
        appMap.clear();

        PackageManager pm = getPackageManager();
        Intent leanbackIntent = new Intent(Intent.ACTION_MAIN);
        leanbackIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);

        List<ResolveInfo> activities = pm.queryIntentActivities(leanbackIntent, 0);
        if (activities == null || activities.isEmpty()) {
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            activities = pm.queryIntentActivities(launcherIntent, 0);
        }

        for (ResolveInfo ri : activities) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName()))
                continue;

            CharSequence label = ri.loadLabel(pm);
            String title = label != null ? label.toString() : pkg;

            // Exclude Instagram completely as requested
            if (pkg.toLowerCase().contains("instagram") || title.toLowerCase().contains("instagram")) {
                continue;
            }

            // Normalize Apple TV title so it displays and sorts cleanly under 'A'
            if (pkg.contains("appletv") || title.contains("") || title.equalsIgnoreCase("tv")) {
                title = "Apple TV";
            }

            Drawable banner = ri.activityInfo.loadBanner(pm);
            Drawable icon = ri.activityInfo.loadIcon(pm);

            AppItem item = new AppItem(pkg, ri.activityInfo.name, title, banner, icon, this);
            allInstalledApps.add(item);
            appMap.put(pkg, item);
        }

        // Sort all installed apps strictly alphabetically from A to Z like an Android
        // phone
        Collections.sort(allInstalledApps, (a, b) -> {
            String nameA = a.label != null ? a.label : a.packageName;
            String nameB = b.label != null ? b.label : b.packageName;
            return nameA.compareToIgnoreCase(nameB);
        });
    }

    private void populateDock() {
        dockContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        final float density = getResources().getDisplayMetrics().density;

        final ViewOutlineProvider roundedOutline = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 28f);
            }
        };

        for (int i = 0; i < dockPackageList.size(); i++) {
            String pkg = dockPackageList.get(i);
            if (pkg.toLowerCase().contains("instagram"))
                continue;
            AppItem app = appMap.get(pkg);
            if (app == null)
                continue;

            View tile = inflater.inflate(R.layout.item_dock_tile, dockContainer, false);
            tile.setOutlineProvider(roundedOutline);
            tile.setClipToOutline(true);

            ImageView ivBanner = tile.findViewById(R.id.ivBanner);
            TextView tvLabel = tile.findViewById(R.id.tvLabel);

            final int index = i;
            final boolean isThisMoving = isDockMovingMode && (movingDockIndex == index);

            if (isThisMoving) {
                android.graphics.drawable.GradientDrawable moveBg = new android.graphics.drawable.GradientDrawable();
                moveBg.setColor(0xE01E1E2E);
                moveBg.setCornerRadius(28f);
                moveBg.setStroke((int) (3 * density), 0xFF00E5FF);
                tile.setBackground(moveBg);
                tile.setTranslationZ(36f);
                tile.setScaleX(1.15f);
                tile.setScaleY(1.15f);
                ivBanner.setImageDrawable(app.banner != null ? app.banner : app.icon);
                tvLabel.setText("MOVE");
                tvLabel.setTextColor(0xFF00E5FF);
                tvLabel.setVisibility(View.VISIBLE);
                if (outfitTypeface != null)
                    tvLabel.setTypeface(outfitTypeface, Typeface.BOLD);
            } else {
                if (app.banner != null) {
                    ivBanner.setImageDrawable(app.banner);
                    tvLabel.setVisibility(View.GONE);
                } else {
                    ivBanner.setImageDrawable(app.icon);
                    tvLabel.setText(app.label);
                    tvLabel.setVisibility(View.VISIBLE);
                }
            }

            tile.setOnFocusChangeListener((v, hasFocus) -> {
                if (isDockMovingMode && movingDockIndex == index) {
                    v.setTranslationZ(36f);
                    v.setScaleX(1.15f);
                    v.setScaleY(1.15f);
                    tvFocusedAppName
                            .setText("Moving: " + app.label + " (Use Left / Right remote arrows, Select to finish)");
                    return;
                }
                if (hasFocus) {
                    lastFocusedDockView = v;
                    lastFocusedDockIndex = index;
                    v.setTranslationZ(24f);
                    v.animate().scaleX(1.18f).scaleY(1.18f).translationZ(24f).setDuration(220)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                    tvFocusedAppName.setText(app.label);

                    int[] loc = new int[2];
                    v.getLocationOnScreen(loc);
                    float cx = loc[0] + (v.getWidth() / 2f);
                    float cy = loc[1] + (v.getHeight() / 2f);
                    animatedBackground.setAmbientGlow(app.themeColor, cx, cy);

                    // Silky smooth scroll to top when focused on dock
                    mainScrollView.smoothScrollToPosition(0, 420);
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(180)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                }
            });

            tile.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (isDockMovingMode && movingDockIndex == index) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            if (movingDockIndex > 0) {
                                String item = dockPackageList.remove(movingDockIndex);
                                dockPackageList.add(movingDockIndex - 1, item);
                                movingDockIndex--;
                                saveDockPackages();
                                populateDock();
                                populateContinueWatchingShelf();
                                if (dockContainer.getChildCount() > movingDockIndex) {
                                    dockContainer.getChildAt(movingDockIndex).requestFocus();
                                }
                            }
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            if (movingDockIndex < dockPackageList.size() - 1) {
                                String item = dockPackageList.remove(movingDockIndex);
                                dockPackageList.add(movingDockIndex + 1, item);
                                movingDockIndex++;
                                saveDockPackages();
                                populateDock();
                                populateContinueWatchingShelf();
                                if (dockContainer.getChildCount() > movingDockIndex) {
                                    dockContainer.getChildAt(movingDockIndex).requestFocus();
                                }
                            }
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
                                || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER || keyCode == KeyEvent.KEYCODE_BACK) {
                            stopDockMoveMode();
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            stopDockMoveMode();
                            return true;
                        }
                    }

                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (containerContinueWatching != null
                                && containerContinueWatching.getVisibility() == View.VISIBLE) {
                            if (filterPillContainer != null && filterPillContainer.getChildCount() > 0) {
                                View targetPill = filterPillContainer.getChildAt(0);
                                String appName = app.label.toLowerCase();
                                for (int p = 0; p < filterPillContainer.getChildCount(); p++) {
                                    View child = filterPillContainer.getChildAt(p);
                                    if (child.getTag() != null
                                            && appName.contains(child.getTag().toString().toLowerCase())) {
                                        targetPill = child;
                                        break;
                                    }
                                }
                                targetPill.requestFocus();
                                mainScrollView.smoothScrollToPosition((int) (160 * density), 380);
                                return true;
                            } else if (shelfWatchNext != null && shelfWatchNext.getChildCount() > 0) {
                                int target = Math.min(index, shelfWatchNext.getChildCount() - 1);
                                shelfWatchNext.getChildAt(target).requestFocus();
                                mainScrollView.smoothScrollToPosition((int) (210 * density), 380);
                                return true;
                            }
                        } else if (appGrid.getChildCount() > 0) {
                            int targetCol = Math.min(index, Math.min(5, appGrid.getChildCount() - 1));
                            View gridTarget = appGrid.getChildAt(targetCol);
                            if (gridTarget != null) {
                                gridTarget.requestFocus();
                                return true;
                            }
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (btnProfile != null)
                            btnProfile.requestFocus();
                        else
                            btnInputs.requestFocus();
                        return true;
                    }
                }
                return false;
            });

            tile.setTag(new AppTag(app, index));
            tile.setOnClickListener(v -> {
                if (isDockMovingMode) {
                    stopDockMoveMode();
                } else {
                    launchApp(app.packageName);
                }
            });
            tile.setOnLongClickListener(v -> {
                if (isDockMovingMode) {
                    stopDockMoveMode();
                } else {
                    showAppOptionsDialog(app, index);
                }
                return true;
            });

            dockContainer.addView(tile);
        }
    }

    private void startDockMoveMode(int index) {
        if (index < 0 || index >= dockPackageList.size())
            return;
        isDockMovingMode = true;
        movingDockIndex = index;
        populateDock();
        if (dockContainer.getChildCount() > movingDockIndex) {
            dockContainer.getChildAt(movingDockIndex).requestFocus();
        }
        Toast.makeText(this, "Move mode: Press Left or Right to shift, press Select to finalize", Toast.LENGTH_LONG)
                .show();
    }

    private void stopDockMoveMode() {
        if (!isDockMovingMode)
            return;
        isDockMovingMode = false;
        int target = movingDockIndex;
        movingDockIndex = -1;
        saveDockPackages();
        populateDock();
        populateContinueWatchingShelf();
        if (target >= 0 && dockContainer.getChildCount() > target) {
            dockContainer.getChildAt(target).requestFocus();
        }
        Toast.makeText(this, "Dock order saved", Toast.LENGTH_SHORT).show();
    }

    private static final Set<String> warmStreamingApps = new HashSet<>();

    private void playWatchNextItem(WatchNextItem item) {
        if (item == null)
            return;
        try {
            HomeButtonAccessibilityService.cancelPendingHomeLaunches();
            Log.i(TAG, "playWatchNextItem: title=" + item.showTitle + " pkg=" + item.packageName + " intentUri="
                    + item.intentUri);
            WatchNextManager.recordAppWatch(this, item.packageName);
            final Intent playbackIntent = WatchNextManager.buildPlaybackIntent(this, item);
            if (playbackIntent != null) {
                boolean wasWarm = warmStreamingApps.contains(item.packageName);
                warmStreamingApps.add(item.packageName);
                Log.i(TAG, "playWatchNextItem: launching playback intent " + playbackIntent + " (wasWarm=" + wasWarm + ")");
                startActivity(playbackIntent);
                if (!wasWarm && "com.disney.disneyplus".equals(item.packageName)) {
                    mainHandler.postDelayed(() -> {
                        if (!isMainActivityFocused()) {
                            try {
                                startActivity(playbackIntent);
                            } catch (Throwable ignored) {
                            }
                        }
                    }, 3200);
                }
                return;
            }
            Log.i(TAG, "playWatchNextItem: fallback to launchApp " + item.packageName);
            launchApp(item.packageName);
        } catch (Throwable t) {
            Log.e(TAG, "playWatchNextItem error", t);
            launchApp(item.packageName);
        }
    }

    private void showWatchItemOptionsDialog(WatchNextItem item) {
        if (item == null)
            return;
        List<String> options = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();

        options.add("Resume Playback (" + item.showTitle + ")");
        actions.add(() -> playWatchNextItem(item));

        AppItem app = appMap.get(item.packageName);
        String appLabel = app != null ? app.label : item.appBadge;
        options.add("Open " + appLabel);
        actions.add(() -> launchApp(item.packageName));

        options.add("Edit Show / Change Episode");
        actions.add(() -> showEditShowDialog(item));

        options.add("Add Show to Continue Watching");
        actions.add(this::showAddShowDialog);

        options.add("Remove from Continue Watching");
        actions.add(() -> {
            WatchNextManager.dismissItem(this, item.id);
            populateContinueWatchingShelf();
            Toast.makeText(this, "Removed from Continue Watching", Toast.LENGTH_SHORT).show();
        });

        options.add("Reset Continue Watching to Default");
        actions.add(() -> {
            WatchNextManager.resetAllContinueWatching(this);
            activeWatchFilterKey = "ALL";
            populateContinueWatchingShelf();
            Toast.makeText(this, "Continue Watching reset to defaults", Toast.LENGTH_SHORT).show();
        });

        if (app != null) {
            options.add("App Options (Restart, Reset, Uninstall)");
            actions.add(() -> showAppOptionsDialog(app, -1));
        }

        CharSequence[] items = options.toArray(new CharSequence[0]);
        AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        b.setTitle(item.showTitle);
        b.setItems(items, (dialog, which) -> {
            if (which >= 0 && which < actions.size()) {
                actions.get(which).run();
            }
        });
        b.setNegativeButton("Cancel", null);
        showTrackedBuilder(b);
    }

    private void populateContinueWatchingShelf() {
        if (shelfWatchNext == null || containerContinueWatching == null) return;
        shelfWatchNext.removeAllViews();
        // Ensure legacy synthetic mock shows from older versions are completely purged and seeded with canonical UUID deep links
        SharedPreferences sp = getSharedPreferences("CleanTvWatchNextPrefs", Context.MODE_PRIVATE);
        if (!sp.getBoolean("v8_uuid_deep_links", false)) {
            WatchNextManager.resetAllContinueWatching(this);
            sp.edit().putBoolean("v8_uuid_deep_links", true).apply();
        }

        List<WatchNextItem> allItems = WatchNextManager.loadAllWatchNext(this, appMap, dockPackageList);
        if (allItems == null || allItems.isEmpty()) {
            containerContinueWatching.setVisibility(View.GONE);
            return;
        }
        containerContinueWatching.setVisibility(View.VISIBLE);

        populateWatchFilterPills(allItems);

        List<WatchNextItem> items = WatchNextManager.loadWatchNext(this, appMap, activeWatchFilterKey, dockPackageList);
        if (items == null || items.isEmpty()) {
            items = allItems;
        }
        watchNextItems.addAll(items);

        LayoutInflater inflater = LayoutInflater.from(this);
        final float density = getResources().getDisplayMetrics().density;
        final float cornerRadiusPx = 14f * density;
        final ViewOutlineProvider cardOutline = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        };

        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            WatchNextItem item = items.get(i);
            View cardView = inflater.inflate(R.layout.item_watch_next_card, shelfWatchNext, false);

            View cardRoot = cardView.findViewById(R.id.cardRoot);
            View cardContainer = cardView.findViewById(R.id.cardContainer);
            ImageView ivPoster = cardView.findViewById(R.id.ivPoster);
            TextView tvAppBadge = cardView.findViewById(R.id.tvAppBadge);
            TextView tvNewEpisodeBadge = cardView.findViewById(R.id.tvNewEpisodeBadge);
            TextView tvShowTitle = cardView.findViewById(R.id.tvShowTitle);
            TextView tvEpisodeInfo = cardView.findViewById(R.id.tvEpisodeInfo);
            View viewProgressFill = cardView.findViewById(R.id.viewProgressFill);
            View viewFocusRing = cardView.findViewById(R.id.viewFocusRing);

            cardContainer.setOutlineProvider(cardOutline);
            cardContainer.setClipToOutline(true);

            AppItem app = (appMap != null && item.packageName != null) ? appMap.get(item.packageName) : null;
            Drawable poster = item.getPoster(this, app);
            if (poster != null) {
                ivPoster.setImageDrawable(poster);
            } else if (item.backdrop != null) {
                ivPoster.setImageDrawable(item.backdrop);
            }

            if (tvAppBadge != null) {
                tvAppBadge.setText(item.appBadge != null ? item.appBadge : "");
            }

            if (tvNewEpisodeBadge != null) {
                if (item.isNewEpisode) {
                    tvNewEpisodeBadge.setVisibility(View.VISIBLE);
                } else {
                    tvNewEpisodeBadge.setVisibility(View.GONE);
                }
            }

            if (tvShowTitle != null) tvShowTitle.setText(item.showTitle);
            if (tvEpisodeInfo != null) tvEpisodeInfo.setText(item.episodeInfo);

            if (outfitTypeface != null) {
                if (tvShowTitle != null) tvShowTitle.setTypeface(outfitTypeface, Typeface.BOLD);
                if (tvEpisodeInfo != null) tvEpisodeInfo.setTypeface(outfitTypeface);
                if (tvAppBadge != null) tvAppBadge.setTypeface(outfitTypeface, Typeface.BOLD);
                if (tvNewEpisodeBadge != null) tvNewEpisodeBadge.setTypeface(outfitTypeface, Typeface.BOLD);
            }

            // Progress bar
            if (item.progressPercent > 0 && viewProgressFill != null) {
                viewProgressFill.setBackgroundColor(item.brandColor != 0 ? item.brandColor : Color.parseColor("#00E5FF"));
                int totalBarWidth = (int) (226 * density);
                int fillWidth = Math.max((int) (4 * density), (totalBarWidth * item.progressPercent) / 100);
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) viewProgressFill.getLayoutParams();
                if (lp != null) {
                    lp.width = fillWidth;
                    viewProgressFill.setLayoutParams(lp);
                }
            } else {
                View pContainer = cardView.findViewById(R.id.progressContainer);
                if (pContainer != null) pContainer.setVisibility(View.GONE);
            }

            cardRoot.setTag(item);

            cardRoot.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    v.setTranslationZ(28f);
                    v.animate().scaleX(1.08f).scaleY(1.08f).translationZ(28f).setDuration(220)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                    if (viewFocusRing != null) viewFocusRing.setVisibility(View.VISIBLE);

                    // Dynamic ambient glow matching show brand color
                    int[] loc = new int[2];
                    v.getLocationOnScreen(loc);
                    float cx = loc[0] + (v.getWidth() / 2f);
                    float cy = loc[1] + (v.getHeight() / 2f);
                    animatedBackground.setAmbientGlow(item.brandColor, cx, cy);

                    // Smooth scroll mainScrollView to position shelf cleanly
                    int targetY = (int) (210 * density);
                    mainScrollView.smoothScrollToPosition(targetY, 360);

                    // Auto scroll HorizontalScrollView to keep focused card centered
                    scrollWatchNext.smoothScrollTo(Math.max(0, v.getLeft() - (int) (60 * density)), 0);
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(180)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                    v.setTranslationZ(0f);
                    if (viewFocusRing != null) viewFocusRing.setVisibility(View.GONE);
                }
            });

            // D-Pad Navigation for shelf card
            cardRoot.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER
                            || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                        playWatchNextItem(item);
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_MENU) {
                        showWatchItemOptionsDialog(item);
                        return true;
                    }
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (filterPillContainer != null && filterPillContainer.getChildCount() > 0) {
                            View target = filterPillContainer.getChildAt(0);
                            for (int p = 0; p < filterPillContainer.getChildCount(); p++) {
                                View pv = filterPillContainer.getChildAt(p);
                                if (pv.getTag() != null && activeWatchFilterKey.equalsIgnoreCase(pv.getTag().toString())) {
                                    target = pv;
                                    break;
                                }
                            }
                            target.requestFocus();
                            return true;
                        } else if (lastFocusedDockView != null) {
                            lastFocusedDockView.requestFocus();
                            mainScrollView.smoothScrollToPosition(0, 420);
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (appGrid.getChildCount() > 0) {
                            int gridTarget = Math.min(index, Math.min(5, appGrid.getChildCount() - 1));
                            View gv = appGrid.getChildAt(gridTarget);
                            if (gv != null) gv.requestFocus();
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (index > 0 && shelfWatchNext.getChildCount() > index - 1) {
                            shelfWatchNext.getChildAt(index - 1).requestFocus();
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (index + 1 < shelfWatchNext.getChildCount()) {
                            shelfWatchNext.getChildAt(index + 1).requestFocus();
                            return true;
                        }
                    }
                }
                return false;
            });

            cardRoot.setOnClickListener(v -> playWatchNextItem(item));
            cardRoot.setOnLongClickListener(v -> {
                showWatchItemOptionsDialog(item);
                return true;
            });

            shelfWatchNext.addView(cardView);
        }
    }

    private void populateWatchFilterPills(List<WatchNextItem> allItems) {
        if (filterPillContainer == null)
            return;
        filterPillContainer.removeAllViews();
        List<WatchNextManager.AppFilter> filters = WatchNextManager.getAppFilters(this, allItems, dockPackageList);
        if (filters == null || filters.isEmpty()) {
            if (scrollWatchFilters != null)
                scrollWatchFilters.setVisibility(View.GONE);
            return;
        }
        if (scrollWatchFilters != null)
            scrollWatchFilters.setVisibility(View.VISIBLE);

        final float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < filters.size(); i++) {
            final WatchNextManager.AppFilter filter = filters.get(i);
            final boolean isSelected = activeWatchFilterKey.equalsIgnoreCase(filter.key);

            TextView pill = new TextView(this);
            pill.setText(filter.label + " (" + filter.count + ")");
            pill.setTextSize(12f);
            pill.setFocusable(true);
            pill.setClickable(true);
            pill.setGravity(android.view.Gravity.CENTER);
            pill.setTag(filter.key);
            if (outfitTypeface != null) {
                pill.setTypeface(outfitTypeface, isSelected ? Typeface.BOLD : Typeface.NORMAL);
            }

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (int) (30 * density));
            lp.setMarginEnd((int) (8 * density));
            pill.setLayoutParams(lp);
            pill.setPadding((int) (14 * density), 0, (int) (14 * density), 0);

            applyFilterPillStyle(pill, isSelected, filter.brandColor);

            pill.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    v.setTranslationZ(8f);
                    v.animate().scaleX(1.04f).scaleY(1.04f).setDuration(160).start();
                    applyFilterPillStyle(pill, true, filter.brandColor);
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(140).start();
                    v.setTranslationZ(0f);
                    applyFilterPillStyle(pill, activeWatchFilterKey.equalsIgnoreCase(filter.key), filter.brandColor);
                }
            });

            pill.setOnClickListener(v -> {
                activeWatchFilterKey = filter.key;
                populateContinueWatchingShelf();
                if (filterPillContainer != null) {
                    for (int p = 0; p < filterPillContainer.getChildCount(); p++) {
                        View child = filterPillContainer.getChildAt(p);
                        if (child.getTag() != null && activeWatchFilterKey.equalsIgnoreCase(child.getTag().toString())) {
                            child.requestFocus();
                            break;
                        }
                    }
                }
            });

            pill.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (lastFocusedDockView != null) {
                            lastFocusedDockView.requestFocus();
                        } else if (dockContainer.getChildCount() > 0) {
                            dockContainer.getChildAt(0).requestFocus();
                        }
                        mainScrollView.smoothScrollToPosition(0, 420);
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (shelfWatchNext != null && shelfWatchNext.getChildCount() > 0) {
                            shelfWatchNext.getChildAt(0).requestFocus();
                            return true;
                        }
                    }
                }
                return false;
            });

            filterPillContainer.addView(pill);
        }
    }

    private void applyFilterPillStyle(TextView pill, boolean highlighted, int brandColor) {
        final float density = getResources().getDisplayMetrics().density;
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setCornerRadius(15f * density);
        if (highlighted) {
            gd.setColor(brandColor != 0 ? brandColor : Color.parseColor("#00E5FF"));
            pill.setTextColor(Color.WHITE);
        } else {
            gd.setColor(Color.parseColor("#1C1C28"));
            gd.setStroke((int) (1 * density), Color.parseColor("#35FFFFFF"));
            pill.setTextColor(Color.parseColor("#C8C8DC"));
        }
        pill.setBackground(gd);
    }

    private void showCustomShowManagerDialog() {
        List<String> options = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();

        options.add("Add Show to Continue Watching");
        actions.add(this::showAddShowDialog);

        options.add("Reset Continue Watching to Default");
        actions.add(() -> {
            WatchNextManager.resetAllContinueWatching(this);
            activeWatchFilterKey = "ALL";
            populateContinueWatchingShelf();
            Toast.makeText(this, "Continue Watching reset to defaults", Toast.LENGTH_SHORT).show();
        });

        List<WatchNextItem> allItems = WatchNextManager.loadAllWatchNext(this, appMap, dockPackageList);
        for (WatchNextItem item : allItems) {
            options.add("Edit: " + item.showTitle + " (" + item.appBadge + ")");
            actions.add(() -> showEditShowDialog(item));
        }

        CharSequence[] items = options.toArray(new CharSequence[0]);
        AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        b.setTitle("Manage Continue Watching");
        b.setItems(items, (dialog, which) -> {
            if (which >= 0 && which < actions.size()) {
                actions.get(which).run();
            }
        });
        b.setNegativeButton("Close", null);
        showTrackedBuilder(b);
    }

    private void showAddShowDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Add Show to Continue Watching");

        final float density = getResources().getDisplayMetrics().density;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * density);
        layout.setPadding(pad, pad, pad, pad);

        TextView tvAppLabel = new TextView(this);
        tvAppLabel.setText("Select Streaming App:");
        tvAppLabel.setTextColor(0xFFE0E0E0);
        tvAppLabel.setTextSize(13f);
        if (outfitTypeface != null) tvAppLabel.setTypeface(outfitTypeface);
        layout.addView(tvAppLabel);

        final String[] appOptions = new String[]{"Max", "Disney+", "Apple TV+", "Peacock", "Hulu", "Prime Video", "Netflix"};
        final String[] appPkgs = new String[]{"com.wbd.stream", "com.disney.disneyplus", "com.apple.atve.amazon.appletv", "com.peacock.peacockfiretv", "com.hulu.plus", "com.amazon.amazonvideo.livingroom", "com.netflix.ninja"};
        final int[] appColors = new int[]{Color.parseColor("#8028F5"), Color.parseColor("#1B45F5"), Color.parseColor("#00E5FF"), Color.parseColor("#00B074"), Color.parseColor("#1CE783"), Color.parseColor("#00A8E1"), Color.parseColor("#E50914")};

        final int[] selectedAppIdx = new int[]{0};
        TextView btnPickApp = new TextView(this);
        btnPickApp.setText("Service: " + appOptions[0] + " ▾");
        btnPickApp.setTextColor(Color.WHITE);
        btnPickApp.setTextSize(15f);
        btnPickApp.setPadding(0, (int) (6 * density), 0, (int) (14 * density));
        btnPickApp.setFocusable(true);
        btnPickApp.setClickable(true);
        btnPickApp.setOnClickListener(v -> {
            AlertDialog.Builder appB = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
                    .setTitle("Select App")
                    .setItems(appOptions, (d, sel) -> {
                        selectedAppIdx[0] = sel;
                        btnPickApp.setText("Service: " + appOptions[sel] + " ▾");
                    });
            showTrackedBuilder(appB);
        });if(outfitTypeface!=null)btnPickApp.setTypeface(outfitTypeface,Typeface.BOLD);layout.addView(btnPickApp);

    TextView tvTitleLabel = new TextView(
            this);tvTitleLabel.setText("Show or Movie Title:");tvTitleLabel.setTextColor(0xFFE0E0E0);tvTitleLabel.setTextSize(13f);if(outfitTypeface!=null)tvTitleLabel.setTypeface(outfitTypeface);layout.addView(tvTitleLabel);

    EditText etTitle = createDialogInput("e.g. The Penguin, Succession...");layout.addView(etTitle);

    TextView tvEpLabel = new TextView(
            this);tvEpLabel.setText("Episode Info:");tvEpLabel.setTextColor(0xFFE0E0E0);tvEpLabel.setTextSize(13f);tvEpLabel.setPadding(0,(int)(10*density),0,0);if(outfitTypeface!=null)tvEpLabel.setTypeface(outfitTypeface);layout.addView(tvEpLabel);

    EditText etEp = createDialogInput("e.g. S1 E4 • Cent'Anni");layout.addView(etEp);

    builder.setView(layout);builder.setPositiveButton("Add Show",(dialog,which)->
    {
        String title = etTitle.getText().toString().trim();
        String ep = etEp.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        int idx = selectedAppIdx[0];
        WatchNextManager.saveCustomShow(this, appPkgs[idx], appOptions[idx].toUpperCase(), title,
                ep.isEmpty() ? "Resume Playback" : ep, 50, appColors[idx]);
        activeWatchFilterKey = "ALL";
        populateContinueWatchingShelf();
        Toast.makeText(this, "Added \"" + title + "\" to Continue Watching", Toast.LENGTH_SHORT).show();
    });builder.setNegativeButton("Cancel",null);
    AlertDialog dialog = showTrackedBuilder(builder);etTitle.requestFocus();
    }

    private void showEditShowDialog(WatchNextItem item) {
        if (item == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Edit " + item.showTitle);

        final float density = getResources().getDisplayMetrics().density;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * density);
        layout.setPadding(pad, pad, pad, pad);

        TextView tvTitleLabel = new TextView(this);
        tvTitleLabel.setText("Show Title:");
        tvTitleLabel.setTextColor(0xFFE0E0E0);
        tvTitleLabel.setTextSize(13f);
        if (outfitTypeface != null) tvTitleLabel.setTypeface(outfitTypeface);
        layout.addView(tvTitleLabel);

        EditText etTitle = createDialogInput(item.showTitle);
        etTitle.setText(item.showTitle);
        layout.addView(etTitle);

        TextView tvEpLabel = new TextView(this);
        tvEpLabel.setText("Episode Info:");
        tvEpLabel.setTextColor(0xFFE0E0E0);
        tvEpLabel.setTextSize(13f);
        tvEpLabel.setPadding(0, (int) (10 * density), 0, 0);
        if (outfitTypeface != null) tvEpLabel.setTypeface(outfitTypeface);
        layout.addView(tvEpLabel);

        EditText etEp = createDialogInput(item.episodeInfo);
        etEp.setText(item.episodeInfo);
        layout.addView(etEp);

        TextView tvPctLabel = new TextView(this);
        tvPctLabel.setText("Progress (%):");
        tvPctLabel.setTextColor(0xFFE0E0E0);
        tvPctLabel.setTextSize(13f);
        tvPctLabel.setPadding(0, (int) (10 * density), 0, 0);
        if (outfitTypeface != null) tvPctLabel.setTypeface(outfitTypeface);
        layout.addView(tvPctLabel);

        EditText etPct = createDialogInput(String.valueOf(item.progressPercent));
        etPct.setText(String.valueOf(item.progressPercent));
        etPct.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        layout.addView(etPct);

        builder.setView(layout);
        builder.setPositiveButton("Save Changes", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            String newEp = etEp.getText().toString().trim();
            int newPct = item.progressPercent;
            try {
                newPct = Math.max(0, Math.min(100, Integer.parseInt(etPct.getText().toString().trim())));
            } catch (Throwable ignored) {
            }

            if (!newTitle.isEmpty()) {
                WatchNextManager.editShow(this, item.id, newTitle, newEp.isEmpty() ? item.episodeInfo : newEp, newPct);
                populateContinueWatchingShelf();
                Toast.makeText(this, "Updated " + newTitle, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = showTrackedBuilder(builder);
        etTitle.requestFocus();
    }

    private EditText createDialogInput(String hint) {
        final float density = getResources().getDisplayMetrics().density;
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setTextColor(0xFF111115);
        input.setHintTextColor(0xFF757580);
        input.setTextSize(16f);
        input.setSingleLine(true);
        if (outfitTypeface != null) input.setTypeface(outfitTypeface);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.WHITE);
        bg.setCornerRadius(10f * density);
        bg.setStroke((int) (2 * density), 0xFFB0B0C0);
        input.setBackground(bg);
        int padH = (int) (14 * density);
        int padV = (int) (10 * density);
        input.setPadding(padH, padV, padH, padV);
        return input;
    }

    private void populateAllAppsGrid() {
        appGrid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        final float density = getResources().getDisplayMetrics().density;
        final float cornerRadiusPx = 18f * density;
        final ViewOutlineProvider cardOutline = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        };

        for (int i = 0; i < allInstalledApps.size(); i++) {
            AppItem app = allInstalledApps.get(i);
            View itemRoot = inflater.inflate(R.layout.item_grid_tile, appGrid, false);

            View gridCard = itemRoot.findViewById(R.id.gridCard);
            ImageView ivGridBanner = itemRoot.findViewById(R.id.ivGridBanner);
            ImageView ivGridIcon = itemRoot.findViewById(R.id.ivGridIcon);
            TextView tvLabel = itemRoot.findViewById(R.id.tvGridLabel);

            gridCard.setOutlineProvider(cardOutline);
            gridCard.setClipToOutline(true);

            // Phone-style app drawer: always display the squircle app icon
            Drawable displayIcon = app.icon != null ? app.icon : app.banner;
            ivGridIcon.setImageDrawable(displayIcon);
            ivGridIcon.setVisibility(View.VISIBLE);
            ivGridBanner.setVisibility(View.GONE);

            tvLabel.setText(app.label);
            if (outfitTypeface != null) {
                tvLabel.setTypeface(outfitTypeface);
            }

            final int col = i % 6;
            final int row = i / 6;

            itemRoot.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    v.setTranslationZ(24f);
                    gridCard.animate().scaleX(1.18f).scaleY(1.18f).translationZ(24f).setDuration(220)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                    tvLabel.setTextColor(Color.WHITE);
                    if (outfitTypeface != null) {
                        tvLabel.setTypeface(outfitTypeface, Typeface.BOLD);
                    }

                    // Silky smooth vertical glide with physics decelerate curve
                    int topInScrollView = 0;
                    View curr = v;
                    while (curr != null && curr != mainScrollView) {
                        topInScrollView += curr.getTop();
                        if (curr.getParent() instanceof View) {
                            curr = (View) curr.getParent();
                        } else {
                            break;
                        }
                    }
                    int targetY = Math.max(0, topInScrollView - 140);
                    mainScrollView.smoothScrollToPosition(targetY, 400);

                    int[] loc = new int[2];
                    v.getLocationOnScreen(loc);
                    float cx = loc[0] + (v.getWidth() / 2f);
                    float cy = loc[1] + (v.getHeight() / 2f);
                    animatedBackground.setAmbientGlow(app.themeColor, cx, cy);
                } else {
                    gridCard.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0f).setDuration(180)
                            .setInterpolator(new DecelerateInterpolator(1.6f)).start();
                    v.setTranslationZ(0f);
                    tvLabel.setTextColor(getResources().getColor(R.color.text_muted));
                    if (outfitTypeface != null) {
                        tvLabel.setTypeface(outfitTypeface, Typeface.NORMAL);
                    }
                }
            });

            final int itemIndex = i;
            // Deterministic D-Pad navigation for 6-column grid items:
            itemRoot.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP && row == 0) {
                        if (containerContinueWatching != null
                                && containerContinueWatching.getVisibility() == View.VISIBLE
                                && shelfWatchNext != null
                                && shelfWatchNext.getChildCount() > 0) {
                            int target = Math.min(col, shelfWatchNext.getChildCount() - 1);
                            shelfWatchNext.getChildAt(target).requestFocus();
                            return true;
                        } else {
                            int dockCol = Math.min(col, dockContainer.getChildCount() - 1);
                            if (dockCol >= 0) {
                                dockContainer.getChildAt(dockCol).requestFocus();
                                mainScrollView.smoothScrollToPosition(0, 420);
                                return true;
                            }
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (itemIndex + 1 < appGrid.getChildCount() && col < 5) {
                            appGrid.getChildAt(itemIndex + 1).requestFocus();
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (itemIndex - 1 >= 0 && col > 0) {
                            appGrid.getChildAt(itemIndex - 1).requestFocus();
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (itemIndex + 6 < appGrid.getChildCount()) {
                            appGrid.getChildAt(itemIndex + 6).requestFocus();
                            return true;
                        } else if (itemIndex < appGrid.getChildCount() - 1 && (itemIndex / 6) < ((appGrid.getChildCount() - 1) / 6)) {
                            appGrid.getChildAt(appGrid.getChildCount() - 1).requestFocus();
                            return true;
                        }
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && row > 0) {
                        if (itemIndex - 6 >= 0) {
                            appGrid.getChildAt(itemIndex - 6).requestFocus();
                            return true;
                        }
                    }
                }
                return false;
            });

            itemRoot.setTag(new AppTag(app, -1));
            itemRoot.setOnClickListener(v -> launchApp(app.packageName));
            itemRoot.setOnLongClickListener(v -> {
                showAppOptionsDialog(app, -1);
                return true;
            });

            appGrid.addView(itemRoot);
        }
    }

    private void launchApp(String pkg) {
        try {
            HomeButtonAccessibilityService.onExternalAppLaunching();
            Intent intent = null;
            AppItem app = appMap.get(pkg);
            PackageManager pm = getPackageManager();

            // 1. Try Leanback launch intent directly from PackageManager
            intent = pm.getLeanbackLaunchIntentForPackage(pkg);

            // 2. Try explicit ComponentName from indexed Leanback activities
            if (intent == null && app != null && app.activityName != null) {
                Intent compIntent = new Intent(Intent.ACTION_MAIN);
                compIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
                compIntent.setComponent(new ComponentName(pkg, app.activityName));
                if (pm.resolveActivity(compIntent, 0) != null) {
                    intent = compIntent;
                } else {
                    compIntent = new Intent(Intent.ACTION_MAIN);
                    compIntent.setComponent(new ComponentName(pkg, app.activityName));
                    if (pm.resolveActivity(compIntent, 0) != null) {
                        intent = compIntent;
                    }
                }
            }

            // 3. Fallback to standard touch launcher intent
            if (intent == null) {
                intent = pm.getLaunchIntentForPackage(pkg);
            }

            if (intent != null) {
                WatchNextManager.recordAppWatch(this, pkg);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Unable to launch " + pkg, Toast.LENGTH_SHORT).show();
            }
        } catch (Throwable t) {
            Toast.makeText(this, "Launch error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void restartApp(String pkg) {
        try {
            AppItem app = appMap.get(pkg);
            String label = app != null ? app.label : pkg;
            Toast.makeText(this, "Restarting " + label + "...", Toast.LENGTH_SHORT).show();

            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.killBackgroundProcesses(pkg);
            }
            try {
                Runtime.getRuntime().exec(new String[]{"am", "force-stop", pkg});
            } catch (Throwable ignored) {
            }

            mainHandler.postDelayed(() -> launchApp(pkg), 450);
        } catch (Throwable t) {
            Toast.makeText(this, "Error restarting: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openAppDetails(String pkg) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + pkg));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable t) {
            Toast.makeText(this, "Could not open settings: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uninstallApp(String pkg) {
        try {
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + pkg));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable t) {
            try {
                Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
                intent.setData(Uri.parse("package:" + pkg));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Throwable t2) {
                Toast.makeText(this, "Could not uninstall: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProfileSelectionDialog() {
        final float density = getResources().getDisplayMetrics().density;
        List<String> profiles = WatchNextManager.getProfilesList(this);
        String current = WatchNextManager.getCurrentProfile(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padH = (int) (26 * density);
        int padV = (int) (22 * density);
        container.setPadding(padH, padV, padH, padV);
        container.setClipChildren(false);
        container.setClipToPadding(false);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xF0181A24);
        bg.setCornerRadius(22f * density);
        bg.setStroke((int) (1.5f * density), 0x30FFFFFF);
        container.setBackground(bg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("User Profiles");
        tvTitle.setTextSize(21f);
        tvTitle.setTextColor(Color.WHITE);
        if (outfitTypeface != null) tvTitle.setTypeface(outfitTypeface, Typeface.BOLD);
        container.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("Select your profile for personalized watchlist and settings");
        tvSub.setTextSize(13f);
        tvSub.setTextColor(0xFF9E9EA8);
        tvSub.setPadding(0, 4, 0, (int) (16 * density));
        if (outfitTypeface != null) tvSub.setTypeface(outfitTypeface);
        container.addView(tvSub);

        ScrollView sv = new ScrollView(this);
        sv.setClipChildren(false);
        sv.setClipToPadding(false);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (320 * density));
        sv.setLayoutParams(svLp);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setClipChildren(false);
        list.setClipToPadding(false);

        AlertDialog dialog = trackDialog(builder.setView(container).create());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (580 * density), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }

        View focusedFirst = null;

        for (String p : profiles) {
            final String profileName = p;
            final boolean isActive = profileName.equalsIgnoreCase(current);

            LinearLayout tile = new LinearLayout(this);
            tile.setOrientation(LinearLayout.HORIZONTAL);
            tile.setGravity(android.view.Gravity.CENTER_VERTICAL);
            tile.setFocusable(true);
            tile.setClickable(true);
            int pH = (int) (16 * density);
            int pV = (int) (12 * density);
            tile.setPadding(pH, pV, pH, pV);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, (int) (4 * density), 0, (int) (4 * density));
            tile.setLayoutParams(lp);

            android.graphics.drawable.GradientDrawable defaultBg = new android.graphics.drawable.GradientDrawable();
            defaultBg.setColor(isActive ? 0x2800E5FF : 0x22FFFFFF);
            defaultBg.setCornerRadius(16f * density);
            defaultBg.setStroke((int) (1.5f * density), isActive ? 0x6600E5FF : 0x25FFFFFF);

            android.graphics.drawable.GradientDrawable focusedBg = new android.graphics.drawable.GradientDrawable();
            focusedBg.setColor(0xFFFFFFFF);
            focusedBg.setCornerRadius(16f * density);
            focusedBg.setStroke((int) (2.5f * density), 0xFFFFFFFF);

            tile.setBackground(defaultBg);

            // User Avatar Icon
            ImageView ivAvatar = new ImageView(this);
            int avatarPx = (int) (48 * density);
            LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(avatarPx, avatarPx);
            ivLp.setMarginEnd((int) (14 * density));
            ivLp.rightMargin = (int) (14 * density);
            ivAvatar.setLayoutParams(ivLp);

            String iconId = WatchNextManager.getProfileIcon(this, profileName);
            String themeId = WatchNextManager.getProfileAvatar(this, profileName);
            ivAvatar.setImageDrawable(WatchNextManager.createProfileAvatarDrawable(
                    this, profileName, iconId, themeId, avatarPx, outfitTypeface));
            tile.addView(ivAvatar);

            // Text Info
            LinearLayout textCol = new LinearLayout(this);
            textCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
            textCol.setLayoutParams(colLp);

            TextView tvName = new TextView(this);
            tvName.setText(profileName);
            tvName.setTextSize(16f);
            tvName.setTextColor(Color.WHITE);
            if (outfitTypeface != null) tvName.setTypeface(outfitTypeface, Typeface.BOLD);
            textCol.addView(tvName);

            TextView tvStatus = new TextView(this);
            tvStatus.setText(isActive ? "Active Profile • Currently Watching" : "Switch to " + profileName);
            tvStatus.setTextSize(12f);
            tvStatus.setTextColor(isActive ? 0xFF00E5FF : 0xFF8E8E98);
            if (outfitTypeface != null) tvStatus.setTypeface(outfitTypeface);
            textCol.addView(tvStatus);

            tile.addView(textCol);

            // Active Badge Pill if active
            if (isActive) {
                TextView badge = new TextView(this);
                badge.setText("ACTIVE");
                badge.setTextSize(11f);
                badge.setTextColor(0xFF00E5FF);
                int bH = (int) (10 * density);
                int bV = (int) (4 * density);
                badge.setPadding(bH, bV, bH, bV);
                android.graphics.drawable.GradientDrawable bBg = new android.graphics.drawable.GradientDrawable();
                bBg.setColor(0x3300E5FF);
                bBg.setCornerRadius(10f * density);
                bBg.setStroke((int) (1f * density), 0x8800E5FF);
                badge.setBackground(bBg);
                if (outfitTypeface != null) badge.setTypeface(outfitTypeface, Typeface.BOLD);
                tile.addView(badge);
            }

            tile.setClipChildren(false);
            tile.setClipToPadding(false);
            tile.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    tile.setBackground(focusedBg);
                    tvName.setTextColor(0xFF0A0A0E);
                    tvStatus.setTextColor(0xFF383842);
                    tile.setTranslationZ(4f * density);
                    ivAvatar.setScaleX(1.0f);
                    ivAvatar.setScaleY(1.0f);
                } else {
                    tile.setBackground(defaultBg);
                    tvName.setTextColor(Color.WHITE);
                    tvStatus.setTextColor(isActive ? 0xFF00E5FF : 0xFF8E8E98);
                    tile.setTranslationZ(0f);
                    ivAvatar.setScaleX(1.0f);
                    ivAvatar.setScaleY(1.0f);
                }
            });

            tile.setOnClickListener(v -> {
                dialog.dismiss();
                if (!isActive) {
                    WatchNextManager.setCurrentProfile(MainActivity.this, profileName);
                    updateProfilePill();
                    activeWatchFilterKey = "ALL";
                    populateContinueWatchingShelf();
                    Toast.makeText(MainActivity.this, "Switched profile to " + profileName, Toast.LENGTH_SHORT).show();
                }
            });

            list.addView(tile);
            if (isActive || focusedFirst == null) {
                focusedFirst = tile;
            }
        }

        // Section divider
        View divider = new View(this);
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * density));
        divLp.setMargins(0, (int) (10 * density), 0, (int) (10 * density));
        divider.setLayoutParams(divLp);
        divider.setBackgroundColor(0x22FFFFFF);
        list.addView(divider);

        // Customize Profile Option
        View btnCustomize = createDialogTile("Customize Profile Avatar",
                "Choose character art, colors, or initials for " + current, () -> {
            dialog.dismiss();
            showAvatarSelectionDialog(current);
        });
        list.addView(btnCustomize);

        // Add Profile Option
        View btnAdd = createDialogTile("Add New User Profile",
                "Create a profile with independent watchlist and history", () -> {
            dialog.dismiss();
            showAddProfileDialog();
        });
        list.addView(btnAdd);

        sv.addView(list);
        container.addView(sv);

        dialog.show();
        if (focusedFirst != null) {
            focusedFirst.requestFocus();
        }
    }

    private void showAvatarSelectionDialog(String profile) {
        final float density = getResources().getDisplayMetrics().density;
        String curIcon = WatchNextManager.getProfileIcon(this, profile);
        String curTheme = WatchNextManager.getProfileAvatar(this, profile);

        final String[] selectedIcon = new String[]{curIcon};
        final String[] selectedTheme = new String[]{curTheme};

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);

        LinearLayout outerContainer = new LinearLayout(this);
        outerContainer.setOrientation(LinearLayout.VERTICAL);
        outerContainer.setClipChildren(true);
        outerContainer.setClipToPadding(true);
        int pad = (int) (16 * density);
        outerContainer.setPadding(pad, pad, pad, 0);

        // 1. Live Preview Card (permanently pinned at top)
        LinearLayout previewCard = new LinearLayout(this);
        previewCard.setOrientation(LinearLayout.HORIZONTAL);
        previewCard.setGravity(android.view.Gravity.CENTER_VERTICAL);
        int cardPad = (int) (14 * density);
        previewCard.setPadding(cardPad, cardPad, cardPad, cardPad);
        GradientDrawable previewBg = new GradientDrawable();
        previewBg.setColor(0xFF141620);
        previewBg.setCornerRadius(18 * density);
        previewBg.setStroke((int) (1.5f * density), 0x5500E5FF);
        previewCard.setBackground(previewBg);

        ImageView ivLivePreview = new ImageView(this);
        int previewSize = (int) (76 * density);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(previewSize, previewSize);
        ivLp.setMarginEnd((int) (18 * density));
        ivLp.rightMargin = (int) (18 * density);
        ivLivePreview.setLayoutParams(ivLp);
        ivLivePreview.setImageDrawable(WatchNextManager.createProfileAvatarDrawable(
                this, profile, selectedIcon[0], selectedTheme[0], previewSize, outfitTypeface));
        previewCard.addView(ivLivePreview);

        LinearLayout previewTextCol = new LinearLayout(this);
        previewTextCol.setOrientation(LinearLayout.VERTICAL);

        TextView tvPreviewTitle = new TextView(this);
        tvPreviewTitle.setText("Customize " + profile);
        tvPreviewTitle.setTextColor(0xFFFFFFFF);
        tvPreviewTitle.setTextSize(17f);
        if (outfitTypeface != null) tvPreviewTitle.setTypeface(outfitTypeface, Typeface.BOLD);
        previewTextCol.addView(tvPreviewTitle);

        TextView tvPreviewSub = new TextView(this);
        tvPreviewSub.setText(getCharacterDescription(selectedIcon[0]));
        tvPreviewSub.setTextColor(0xFF00E5FF);
        tvPreviewSub.setTextSize(12f);
        if (outfitTypeface != null) tvPreviewSub.setTypeface(outfitTypeface);
        previewTextCol.addView(tvPreviewSub);

        previewCard.addView(previewTextCol);
        outerContainer.addView(previewCard);

        final Runnable[] prevSelectedUpdater = new Runnable[1];

        Runnable refreshPreview = () -> {
            ivLivePreview.setImageDrawable(WatchNextManager.createProfileAvatarDrawable(
                    MainActivity.this, profile, selectedIcon[0], selectedTheme[0], previewSize, outfitTypeface));
            tvPreviewSub.setText(getCharacterDescription(selectedIcon[0]));
        };

        ScrollView sv = new ScrollView(this);
        sv.setClipChildren(true);
        sv.setClipToPadding(true);
        sv.setFillViewport(true);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        svLp.topMargin = (int) (10 * density);
        sv.setLayoutParams(svLp);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setClipChildren(false);
        root.setClipToPadding(false);
        root.setPadding(0, 0, 0, (int) (40 * density));

        // Group into 10 distinct horizontal category rows from G:\My Drive\temp\TV-Launcher
        String[] categories = new String[]{
                "Landscapes",
                "Cities",
                "Space",
                "Animals",
                "Cars",
                "Pop Culture",
                "Superheroes",
                "Sci-Fi",
                "Monsters",
                "Abstract"
        };

        final List<List<View>> allCategoryTiles = new ArrayList<>();
        final List<View> allCategoryHeaders = new ArrayList<>();
        final List<HorizontalScrollView> allCategoryHsvs = new ArrayList<>();
        View[] initialFocusHolder = new View[1];
        final AlertDialog[] dialogHolder = new AlertDialog[1];
        final Button[] btnSaveHolder = new Button[1];

        for (String cat : categories) {
            // Category Section Header
            LinearLayout catHeader = new LinearLayout(this);
            catHeader.setOrientation(LinearLayout.HORIZONTAL);
            catHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams catHdrLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            catHdrLp.topMargin = (int) (16 * density);
            catHdrLp.bottomMargin = (int) (4 * density);
            catHeader.setLayoutParams(catHdrLp);

            TextView tvCatTitle = new TextView(this);
            tvCatTitle.setText(cat.toUpperCase(java.util.Locale.ROOT));
            tvCatTitle.setTextColor(0xFF00E5FF);
            tvCatTitle.setTextSize(13f);
            tvCatTitle.setLetterSpacing(0.08f);
            if (outfitTypeface != null) tvCatTitle.setTypeface(outfitTypeface, Typeface.BOLD);
            catHeader.addView(tvCatTitle);

            root.addView(catHeader);
            allCategoryHeaders.add(catHeader);

            // Horizontal Scroll Row
            HorizontalScrollView hsvRow = new HorizontalScrollView(this);
            hsvRow.setClipChildren(false);
            hsvRow.setClipToPadding(false);
            hsvRow.setHorizontalScrollBarEnabled(false);

            LinearLayout catRow = new LinearLayout(this);
            catRow.setOrientation(LinearLayout.HORIZONTAL);
            catRow.setClipChildren(false);
            catRow.setClipToPadding(false);
            int catVPad = (int) (14 * density);
            int catHPad = (int) (10 * density);
            catRow.setPadding(catHPad, catVPad, catHPad, catVPad);

            List<View> rowTiles = new ArrayList<>();

            for (WatchNextManager.AvatarCharacter character : WatchNextManager.AVATAR_CHARACTERS) {
                if (!character.category.equalsIgnoreCase(cat)) continue;

                LinearLayout charTile = new LinearLayout(this);
                charTile.setOrientation(LinearLayout.VERTICAL);
                charTile.setGravity(android.view.Gravity.CENTER);
                charTile.setFocusable(true);
                charTile.setClickable(true);
                int tPad = (int) (10 * density);
                charTile.setPadding(tPad, tPad, tPad, tPad);
                LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(
                        (int) (100 * density), (int) (122 * density));
                tLp.setMarginEnd((int) (12 * density));
                tLp.rightMargin = (int) (12 * density);
                charTile.setLayoutParams(tLp);

                ImageView ivChar = new ImageView(this);
                int charPx = (int) (64 * density);
                ivChar.setLayoutParams(new LinearLayout.LayoutParams(charPx, charPx));
                WatchNextManager.loadAvatarIntoViewAsync(
                        MainActivity.this, profile, character.id, selectedTheme[0], charPx, outfitTypeface, ivChar);
                charTile.addView(ivChar);

                TextView tvCharLabel = new TextView(this);
                tvCharLabel.setText(character.label);
                tvCharLabel.setTextColor(0xDDFFFFFF);
                tvCharLabel.setTextSize(11f);
                tvCharLabel.setGravity(android.view.Gravity.CENTER);
                tvCharLabel.setSingleLine(true);
                LinearLayout.LayoutParams lblLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lblLp.topMargin = (int) (6 * density);
                tvCharLabel.setLayoutParams(lblLp);
                if (outfitTypeface != null) tvCharLabel.setTypeface(outfitTypeface, Typeface.BOLD);
                charTile.addView(tvCharLabel);

                Runnable updateTile = () -> {
                    boolean isSelected = character.id.equalsIgnoreCase(selectedIcon[0]);
                    boolean hasFocus = charTile.hasFocus();

                    charTile.setClipChildren(false);
                    charTile.setClipToPadding(false);
                    GradientDrawable bg = new GradientDrawable();
                    if (hasFocus) {
                        bg.setColor(0x45FFFFFF);
                        bg.setStroke((int) (3f * density), 0xFFFFFFFF);
                        ivChar.setScaleX(1.0f);
                        ivChar.setScaleY(1.0f);
                        tvCharLabel.setTextColor(0xFFFFFFFF);
                    } else if (isSelected) {
                        bg.setColor(0x2800E5FF);
                        bg.setStroke((int) (2.5f * density), 0xFF00E5FF);
                        ivChar.setScaleX(1.0f);
                        ivChar.setScaleY(1.0f);
                        tvCharLabel.setTextColor(0xFF00E5FF);
                    } else {
                        bg.setColor(0x18FFFFFF);
                        bg.setStroke((int) (1f * density), 0x22FFFFFF);
                        ivChar.setScaleX(1.0f);
                        ivChar.setScaleY(1.0f);
                        tvCharLabel.setTextColor(0xBBFFFFFF);
                    }
                    bg.setCornerRadius(14 * density);
                    charTile.setBackground(bg);
                };

                if (character.id.equalsIgnoreCase(selectedIcon[0])) {
                    prevSelectedUpdater[0] = updateTile;
                    initialFocusHolder[0] = charTile;
                }

                charTile.setTag(new Object[]{character, updateTile});

                charTile.setOnClickListener(v -> {
                    selectedIcon[0] = character.id;
                    WatchNextManager.setProfileIcon(MainActivity.this, profile, selectedIcon[0]);
                    updateProfilePill();
                    Toast.makeText(MainActivity.this, profile + "'s avatar updated to " + character.label, Toast.LENGTH_SHORT).show();
                    if (dialogHolder[0] != null) {
                        dialogHolder[0].dismiss();
                    }
                });

                updateTile.run();
                catRow.addView(charTile);
                rowTiles.add(charTile);
            }

            hsvRow.addView(catRow);
            root.addView(hsvRow);
            allCategoryHsvs.add(hsvRow);
            allCategoryTiles.add(rowTiles);
        }

        // Setup predictable focus navigation and natural viewport scrolling:
        for (int r = 0; r < allCategoryTiles.size(); r++) {
            final int rowIndex = r;
            final List<View> thisRow = allCategoryTiles.get(r);
            final View thisCatHeader = allCategoryHeaders.get(r);
            final HorizontalScrollView thisHsv = allCategoryHsvs.get(r);

            for (int c = 0; c < thisRow.size(); c++) {
                final int colIndex = c;
                final View tile = thisRow.get(c);
                Object[] tagData = (Object[]) tile.getTag();
                final WatchNextManager.AvatarCharacter character = (WatchNextManager.AvatarCharacter) tagData[0];
                final Runnable updateTile = (Runnable) tagData[1];

                tile.setOnFocusChangeListener((v, hasFocus) -> {
                    updateTile.run();
                    if (hasFocus) {
                        Runnable prev = prevSelectedUpdater[0];
                        selectedIcon[0] = character.id;
                        prevSelectedUpdater[0] = updateTile;
                        if (prev != null && prev != updateTile) {
                            prev.run();
                        }
                        refreshPreview.run();

                        // Smooth horizontal scroll within this category row
                        thisHsv.post(() -> {
                            if (colIndex == 0) {
                                thisHsv.smoothScrollTo(0, 0);
                            } else {
                                int targetX = tile.getLeft() - (thisHsv.getWidth() - tile.getWidth()) / 2;
                                thisHsv.smoothScrollTo(Math.max(0, targetX), 0);
                            }
                        });

                        // Natural vertical scrolling: only adjust if row is not fully visible
                        sv.post(() -> {
                            int svHeight = sv.getHeight();
                            if (svHeight <= 0) return;
                            int scrollY = sv.getScrollY();
                            int rowTop = thisCatHeader.getTop() - (int) (8 * density);
                            int rowBottom = thisHsv.getBottom() + (int) (12 * density);

                            if (rowBottom > scrollY + svHeight) {
                                int targetY = rowBottom - svHeight;
                                sv.smoothScrollTo(0, Math.max(0, targetY));
                            } else if (rowTop < scrollY) {
                                int targetY = rowTop;
                                sv.smoothScrollTo(0, Math.max(0, targetY));
                            }
                        });
                    }
                });

                tile.setOnKeyListener((v, keyCode, event) -> {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            if (rowIndex + 1 < allCategoryTiles.size()) {
                                View target = allCategoryTiles.get(rowIndex + 1).get(0);
                                if (target != null) {
                                    target.requestFocus();
                                    return true;
                                }
                            } else if (btnSaveHolder[0] != null) {
                                btnSaveHolder[0].requestFocus();
                                return true;
                            }
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                            if (rowIndex > 0) {
                                View target = allCategoryTiles.get(rowIndex - 1).get(0);
                                if (target != null) {
                                    target.requestFocus();
                                    return true;
                                }
                            }
                            return true; // Keep focus on row 0 when pressing up, do not jump out
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                            if (colIndex > 0) {
                                View target = thisRow.get(colIndex - 1);
                                if (target != null) {
                                    target.requestFocus();
                                    return true;
                                }
                            }
                            return true; // Stay on first item
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                            if (colIndex + 1 < thisRow.size()) {
                                View target = thisRow.get(colIndex + 1);
                                if (target != null) {
                                    target.requestFocus();
                                    return true;
                                }
                            }
                            return true; // Stay on last item
                        }
                    }
                    return false;
                });
            }
        }

        sv.addView(root);
        outerContainer.addView(sv);
        builder.setView(outerContainer);

        builder.setPositiveButton("Save Avatar", (d, w) -> {
            WatchNextManager.setProfileIcon(this, profile, selectedIcon[0]);
            updateProfilePill();
            Toast.makeText(this, profile + "'s avatar updated", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = showTrackedBuilder(builder);
        dialogHolder[0] = dialog;

        btnSaveHolder[0] = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (btnSaveHolder[0] != null) {
            btnSaveHolder[0].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (!allCategoryTiles.isEmpty()) {
                        List<View> lastRow = allCategoryTiles.get(allCategoryTiles.size() - 1);
                        if (!lastRow.isEmpty()) {
                            lastRow.get(0).requestFocus();
                            return true;
                        }
                    }
                }
                return false;
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (880 * density), (int) (540 * density));
        }

        if (initialFocusHolder[0] == null && !allCategoryTiles.isEmpty() && !allCategoryTiles.get(0).isEmpty()) {
            initialFocusHolder[0] = allCategoryTiles.get(0).get(0);
        }
        if (initialFocusHolder[0] != null) {
            initialFocusHolder[0].requestFocus();
        }
    }

    private String getCharacterDescription(String iconId) {
        if (iconId != null) {
            for (WatchNextManager.AvatarCharacter c : WatchNextManager.AVATAR_CHARACTERS) {
                if (c.id.equalsIgnoreCase(iconId)) return c.category + "  •  " + c.description;
            }
        }
        return "Personalized visual profile avatar";
    }

    private void showAddProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Add New User Profile");
        final float density = getResources().getDisplayMetrics().density;
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * density);
        layout.setPadding(pad, pad, pad, pad);

        TextView label = new TextView(this);
        label.setText("Profile Name (e.g. Jessica, Alex):");
        label.setTextColor(0xFFE0E0E0);
        label.setTextSize(13f);
        if (outfitTypeface != null) label.setTypeface(outfitTypeface);
        layout.addView(label);

        EditText et = createDialogInput("Profile Name");
        layout.addView(et);

        builder.setView(layout);
        builder.setPositiveButton("Create Profile", (d, w) -> {
            String name = et.getText().toString().trim();
            if (!name.isEmpty()) {
                WatchNextManager.addProfile(this, name);
                WatchNextManager.setCurrentProfile(this, name);
                updateProfilePill();
                activeWatchFilterKey = "ALL";
                populateContinueWatchingShelf();
                Toast.makeText(this, "Created profile " + name, Toast.LENGTH_SHORT).show();
                showAvatarSelectionDialog(name);
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = showTrackedBuilder(builder);
        et.requestFocus();
    }

    private void showInputsDialog() {
        List<TvInputHelper.InputItem> inputs = TvInputHelper.getAvailableInputs(this);
        CharSequence[] items = new CharSequence[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            items[i] = inputs.get(i).label;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle(R.string.dialog_select_input);
        builder.setItems(items, (dialog, which) -> {
            TvInputHelper.switchToInput(MainActivity.this, inputs.get(which));
        });
        builder.setNegativeButton("Cancel", null);
        showTrackedBuilder(builder);
    }

    private String getIntervalLabel(long ms) {
        if (ms == 60_000L) return "1 Minute";
        if (ms == 300_000L) return "5 Minutes (Default)";
        if (ms == 900_000L) return "15 Minutes";
        if (ms == 1_800_000L) return "30 Minutes";
        if (ms == 3_600_000L) return "1 Hour";
        if (ms <= 0L) return "Off (Static)";
        return (ms / 60_000L) + " Minutes";
    }

    private long getNextInterval(long cur) {
        long[] arr = AnimatedWaveBackgroundView.ROTATION_INTERVALS;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == cur) {
                return arr[(i + 1) % arr.length];
            }
        }
        return AnimatedWaveBackgroundView.DEFAULT_ROTATION_INTERVAL_MS;
    }

    private LinearLayout createSubActionButton(String label, Runnable action, float density) {
        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(android.view.Gravity.CENTER);
        btn.setFocusable(true);
        btn.setClickable(true);
        int vPad = (int) (10 * density);
        int hPad = (int) (14 * density);
        btn.setPadding(hPad, vPad, hPad, vPad);

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(13f);
        if (outfitTypeface != null) tv.setTypeface(outfitTypeface, Typeface.BOLD);
        btn.addView(tv);

        GradientDrawable defBg = new GradientDrawable();
        defBg.setColor(0x22FFFFFF);
        defBg.setCornerRadius(10 * density);
        defBg.setStroke((int) (1f * density), 0x33FFFFFF);
        btn.setBackground(defBg);

        btn.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                btn.setBackgroundColor(0xFFFFFFFF);
                tv.setTextColor(0xFF090A10);
            } else {
                GradientDrawable b = new GradientDrawable();
                b.setColor(0x22FFFFFF);
                b.setCornerRadius(10 * density);
                b.setStroke((int) (1f * density), 0x33FFFFFF);
                btn.setBackground(b);
                tv.setTextColor(Color.WHITE);
            }
        });

        btn.setOnClickListener(v -> {
            if (action != null) action.run();
        });

        return btn;
    }

    private void showThemeDialog() {
        final float density = getResources().getDisplayMetrics().density;
        final AlertDialog[] dialogHolder = new AlertDialog[1];
        AnimatedWaveBackgroundView.ThemeCategory curCat = animatedBackground.getCategory();
        int curIdx = animatedBackground.getCurrentThemeIndex();

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle("Wallpaper & Ambient Themes");

        ScrollView sv = new ScrollView(this);
        sv.setClipChildren(true);
        sv.setClipToPadding(false);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (18 * density);
        root.setPadding(pad, pad, pad, pad);

        // Header
        TextView tvCatHeader = new TextView(this);
        tvCatHeader.setText("THEME SELECTION");
        tvCatHeader.setTextColor(0xFF00E5FF);
        tvCatHeader.setTextSize(12f);
        if (outfitTypeface != null) tvCatHeader.setTypeface(outfitTypeface, Typeface.BOLD);
        tvCatHeader.setPadding(0, 0, 0, (int) (8 * density));
        root.addView(tvCatHeader);

        // 4 Theme Category Cards
        AnimatedWaveBackgroundView.ThemeCategory[] categories = AnimatedWaveBackgroundView.ThemeCategory.values();
        List<View> catCards = new ArrayList<>();

        for (AnimatedWaveBackgroundView.ThemeCategory cat : categories) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setFocusable(true);
            card.setClickable(true);
            int cPad = (int) (12 * density);
            card.setPadding(cPad, cPad, cPad, cPad);
            LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cLp.bottomMargin = (int) (10 * density);
            card.setLayoutParams(cLp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvTitle = new TextView(this);
            tvTitle.setText(cat.title);
            tvTitle.setTextSize(15f);
            tvTitle.setTextColor(Color.WHITE);
            if (outfitTypeface != null) tvTitle.setTypeface(outfitTypeface, Typeface.BOLD);
            LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            topRow.addView(tvTitle, tLp);

            TextView tvBadge = new TextView(this);
            tvBadge.setText("ACTIVE");
            tvBadge.setTextSize(11f);
            tvBadge.setTextColor(0xFF00E5FF);
            if (outfitTypeface != null) tvBadge.setTypeface(outfitTypeface, Typeface.BOLD);
            topRow.addView(tvBadge);

            card.addView(topRow);

            TextView tvDesc = new TextView(this);
            tvDesc.setText(cat.description);
            tvDesc.setTextSize(12f);
            tvDesc.setTextColor(0xFF9E9EA8);
            tvDesc.setPadding(0, (int) (4 * density), 0, 0);
            if (outfitTypeface != null) tvDesc.setTypeface(outfitTypeface);
            card.addView(tvDesc);

            catCards.add(card);

            Runnable updateCard = () -> {
                boolean isCur = (cat == animatedBackground.getCategory());
                boolean hasFocus = card.hasFocus();
                tvBadge.setVisibility(isCur ? View.VISIBLE : View.GONE);
                if (hasFocus) {
                    card.setBackgroundColor(0xFFFFFFFF);
                    tvTitle.setTextColor(0xFF090A10);
                    tvDesc.setTextColor(0xFF383842);
                    tvBadge.setTextColor(0xFF0091EA);
                } else {
                    GradientDrawable bg = new GradientDrawable();
                    bg.setColor(isCur ? 0x2800E5FF : 0x1AFFFFFF);
                    bg.setCornerRadius(12 * density);
                    bg.setStroke((int) (1.2f * density), isCur ? 0xCC00E5FF : 0x22FFFFFF);
                    card.setBackground(bg);
                    tvTitle.setTextColor(Color.WHITE);
                    tvDesc.setTextColor(0xFF9E9EA8);
                    tvBadge.setTextColor(0xFF00E5FF);
                }
            };

            card.setOnFocusChangeListener((v, hasFocus) -> updateCard.run());
            updateCard.run();

            card.setOnClickListener(v -> {
                animatedBackground.setCategory(cat);
                getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                        .edit().putString(KEY_THEME_CATEGORY, cat.name()).apply();
                Toast.makeText(this, "Theme switched to " + cat.title, Toast.LENGTH_SHORT).show();
                if (dialogHolder[0] != null) {
                    dialogHolder[0].dismiss();
                }
                showThemeDialog();
            });

            root.addView(card);
        }

        // Section Divider
        View div = new View(this);
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (1 * density));
        dLp.setMargins(0, (int) (8 * density), 0, (int) (14 * density));
        div.setLayoutParams(dLp);
        div.setBackgroundColor(0x22FFFFFF);
        root.addView(div);

        // Slideshow Interval Selector
        LinearLayout intervalTile = new LinearLayout(this);
        intervalTile.setOrientation(LinearLayout.HORIZONTAL);
        intervalTile.setGravity(android.view.Gravity.CENTER_VERTICAL);
        intervalTile.setFocusable(true);
        intervalTile.setClickable(true);
        int iPad = (int) (12 * density);
        intervalTile.setPadding(iPad, iPad, iPad, iPad);
        LinearLayout.LayoutParams intLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        intLp.bottomMargin = (int) (12 * density);
        intervalTile.setLayoutParams(intLp);

        TextView tvIntLabel = new TextView(this);
        tvIntLabel.setText("Slideshow Rotation Interval");
        tvIntLabel.setTextSize(14f);
        tvIntLabel.setTextColor(Color.WHITE);
        if (outfitTypeface != null) tvIntLabel.setTypeface(outfitTypeface, Typeface.BOLD);
        LinearLayout.LayoutParams intLblLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        intervalTile.addView(tvIntLabel, intLblLp);

        TextView tvIntValue = new TextView(this);
        tvIntValue.setText(getIntervalLabel(animatedBackground.getRotationIntervalMs()));
        tvIntValue.setTextSize(13f);
        tvIntValue.setTextColor(0xFF00E5FF);
        if (outfitTypeface != null) tvIntValue.setTypeface(outfitTypeface, Typeface.BOLD);
        intervalTile.addView(tvIntValue);

        intervalTile.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                intervalTile.setBackgroundColor(0xFFFFFFFF);
                tvIntLabel.setTextColor(0xFF090A10);
                tvIntValue.setTextColor(0xFF0091EA);
            } else {
                GradientDrawable b = new GradientDrawable();
                b.setColor(0x22FFFFFF);
                b.setCornerRadius(12 * density);
                b.setStroke((int) (1.2f * density), 0x33FFFFFF);
                intervalTile.setBackground(b);
                tvIntLabel.setTextColor(Color.WHITE);
                tvIntValue.setTextColor(0xFF00E5FF);
            }
        });
        GradientDrawable bDef = new GradientDrawable();
        bDef.setColor(0x22FFFFFF);
        bDef.setCornerRadius(12 * density);
        bDef.setStroke((int) (1.2f * density), 0x33FFFFFF);
        intervalTile.setBackground(bDef);

        intervalTile.setOnClickListener(v -> {
            long curInt = animatedBackground.getRotationIntervalMs();
            long nextInt = getNextInterval(curInt);
            animatedBackground.setRotationIntervalMs(nextInt);
            animatedBackground.setAutoRotateEnabled(nextInt > 0);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putLong(KEY_ROTATION_INTERVAL, nextInt)
                    .putBoolean(KEY_AUTO_ROTATE, nextInt > 0).apply();
            tvIntValue.setText(getIntervalLabel(nextInt));
            Toast.makeText(this, "Slideshow interval: " + getIntervalLabel(nextInt), Toast.LENGTH_SHORT).show();
        });

        root.addView(intervalTile);

        // Slideshow controls if in Photo mode (Nature, Space, Cities)
        if (curCat != AnimatedWaveBackgroundView.ThemeCategory.DYNAMIC_ABSTRACT) {
            int count = animatedBackground.getCurrentWallpaperCount();
            int curPhotoIdx = animatedBackground.getCurrentWallpaperIndex() + 1;

            TextView tvCount = new TextView(this);
            tvCount.setText("Curated 4K Collection: " + count + " Sharp Landscapes & Astrophotography");
            tvCount.setTextSize(12f);
            tvCount.setTextColor(0xFFBBBBCC);
            tvCount.setPadding(0, 0, 0, (int) (8 * density));
            if (outfitTypeface != null) tvCount.setTypeface(outfitTypeface);
            root.addView(tvCount);

            LinearLayout photoNavRow = new LinearLayout(this);
            photoNavRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            pLp.bottomMargin = (int) (10 * density);
            photoNavRow.setLayoutParams(pLp);

            LinearLayout btnPrev = createSubActionButton("Previous 4K Image", () -> {
                animatedBackground.previousWallpaper();
                Toast.makeText(this, "Showing previous 4K wallpaper", Toast.LENGTH_SHORT).show();
            }, density);
            LinearLayout.LayoutParams btnPrevLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            btnPrevLp.setMarginEnd((int) (8 * density));
            btnPrevLp.rightMargin = (int) (8 * density);
            btnPrev.setLayoutParams(btnPrevLp);
            photoNavRow.addView(btnPrev);

            LinearLayout btnNext = createSubActionButton("Next 4K Image", () -> {
                animatedBackground.nextWallpaper();
                Toast.makeText(this, "Showing next 4K wallpaper", Toast.LENGTH_SHORT).show();
            }, density);
            LinearLayout.LayoutParams btnNextLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            btnNext.setLayoutParams(btnNextLp);
            photoNavRow.addView(btnNext);

            root.addView(photoNavRow);
        } else {
            // Dynamic Abstract Palettes
            TextView tvPalHeader = new TextView(this);
            tvPalHeader.setText("ABSTRACT FLUID PALETTES");
            tvPalHeader.setTextColor(0xFF00E5FF);
            tvPalHeader.setTextSize(12f);
            if (outfitTypeface != null) tvPalHeader.setTypeface(outfitTypeface, Typeface.BOLD);
            tvPalHeader.setPadding(0, 0, 0, (int) (8 * density));
            root.addView(tvPalHeader);

            for (int i = 0; i < AnimatedWaveBackgroundView.THEMES.length; i++) {
                final int themeIdx = i;
                AnimatedWaveBackgroundView.ThemeScheme t = AnimatedWaveBackgroundView.THEMES[i];
                boolean isCur = (i == curIdx);

                LinearLayout pCard = new LinearLayout(this);
                pCard.setOrientation(LinearLayout.HORIZONTAL);
                pCard.setGravity(android.view.Gravity.CENTER_VERTICAL);
                pCard.setFocusable(true);
                pCard.setClickable(true);
                int cPadH = (int) (16 * density);
                int cPadV = (int) (12 * density);
                pCard.setPadding(cPadH, cPadV, cPadH, cPadV);
                LinearLayout.LayoutParams pcLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, (int) (52 * density));
                pcLp.bottomMargin = (int) (8 * density);
                pCard.setLayoutParams(pcLp);

                View swatch = new View(this);
                int sPx = (int) (28 * density);
                LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(sPx, sPx);
                sLp.setMarginEnd((int) (12 * density));
                sLp.rightMargin = (int) (12 * density);
                swatch.setLayoutParams(sLp);
                GradientDrawable sBg = new GradientDrawable(
                        GradientDrawable.Orientation.TL_BR,
                        new int[]{t.accentColor, t.secondaryColor}
                );
                sBg.setShape(GradientDrawable.OVAL);
                swatch.setBackground(sBg);
                pCard.addView(swatch);

                TextView tvPName = new TextView(this);
                tvPName.setText(t.name);
                tvPName.setTextColor(Color.WHITE);
                tvPName.setTextSize(13f);
                if (outfitTypeface != null) tvPName.setTypeface(outfitTypeface);
                pCard.addView(tvPName, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvPActive = new TextView(this);
                tvPActive.setText("ACTIVE");
                tvPActive.setTextColor(0xFF00E5FF);
                tvPActive.setTextSize(11f);
                tvPActive.setVisibility(isCur ? View.VISIBLE : View.GONE);
                if (outfitTypeface != null) tvPActive.setTypeface(outfitTypeface, Typeface.BOLD);
                pCard.addView(tvPActive);

                Runnable updatePCard = () -> {
                    boolean hasFocus = pCard.hasFocus();
                    boolean cur = (themeIdx == animatedBackground.getCurrentThemeIndex());
                    tvPActive.setVisibility(cur ? View.VISIBLE : View.GONE);
                    if (hasFocus) {
                        pCard.setBackgroundColor(0xFFFFFFFF);
                        tvPName.setTextColor(0xFF090A10);
                        tvPActive.setTextColor(0xFF0091EA);
                    } else {
                        GradientDrawable bg = new GradientDrawable();
                        bg.setColor(0x1AFFFFFF);
                        bg.setCornerRadius(10 * density);
                        bg.setStroke((int) (1f * density), cur ? 0xAA00E5FF : 0x22FFFFFF);
                        pCard.setBackground(bg);
                        tvPName.setTextColor(Color.WHITE);
                        tvPActive.setTextColor(0xFF00E5FF);
                    }
                };

                pCard.setOnFocusChangeListener((v, hasFocus) -> updatePCard.run());
                updatePCard.run();

                pCard.setOnClickListener(v -> {
                    animatedBackground.setThemeIndex(themeIdx);
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                            .edit().putInt(KEY_THEME_INDEX, themeIdx).apply();
                    Toast.makeText(this, "Palette set to " + t.name, Toast.LENGTH_SHORT).show();
                    for (int c = 0; c < root.getChildCount(); c++) {
                        root.getChildAt(c).invalidate();
                    }
                });

                root.addView(pCard);
            }
        }

        sv.addView(root);
        builder.setView(sv);
        builder.setNegativeButton("Done", null);
        AlertDialog dialog = showTrackedBuilder(builder);
        dialogHolder[0] = dialog;
    }

    private void showCustomizeDialog() {
        String[] options = new String[]{"Add App to Dock", "Reset Dock to Default", "Re-order Current Dock Apps"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        builder.setTitle(R.string.dialog_customize_dock);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showAddAppListDialog();
            } else if (which == 1) {
                dockPackageList.clear();
                dockPackageList.addAll(resolveDefaultDockPackages());
                saveDockPackages();
                populateDock();
                populateContinueWatchingShelf();
                Toast.makeText(this, "Dock reset to default", Toast.LENGTH_SHORT).show();
            } else if (which == 2) {
                Toast.makeText(this, "Long-press any dock app or press Menu to move/remove it", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("Done", null);
        showTrackedBuilder(builder);
    }

    private void showAddAppListDialog() {
        List<AppItem> candidates = new ArrayList<>();
        for (AppItem app : allInstalledApps) {
            if (!dockPackageList.contains(app.packageName)) {
                candidates.add(app);
            }
        }

        if (candidates.isEmpty()) {
            Toast.makeText(this, "All apps are already in your dock", Toast.LENGTH_SHORT).show();
            return;
        }

        CharSequence[] names = new CharSequence[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            names[i] = candidates.get(i).label;
        }

        AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        b.setTitle("Select App to Add to Dock");
        b.setItems(names, (dialog, which) -> {
            AppItem chosen = candidates.get(which);
            dockPackageList.add(chosen.packageName);
            saveDockPackages();
            populateDock();
            populateContinueWatchingShelf();
            Toast.makeText(MainActivity.this, chosen.label + " added to dock", Toast.LENGTH_SHORT).show();
        });
        b.setNegativeButton("Cancel", null);
        showTrackedBuilder(b);
    }

    private void promptAddToDock(AppItem app) {
        if (dockPackageList.contains(app.packageName)) {
            Toast.makeText(this, app.label + " is already in dock", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        b.setTitle(app.label);
        b.setMessage("Add " + app.label + " to the dock?");
        b.setPositiveButton("Add", (dialog, which) -> {
            dockPackageList.add(app.packageName);
            saveDockPackages();
            populateDock();
            populateContinueWatchingShelf();
            Toast.makeText(this, app.label + " added to dock", Toast.LENGTH_SHORT).show();
        });
        b.setNegativeButton("Cancel", null);
        showTrackedBuilder(b);
    }

    private void showAppOptionsDialog(AppItem app, int dockIndex) {
        List<String> options = new ArrayList<>();
        final List<Runnable> actions = new ArrayList<>();

        options.add("Open");
        actions.add(() -> launchApp(app.packageName));

        options.add("Restart App");
        actions.add(() -> restartApp(app.packageName));

        options.add("Reset / App Info (Clear Data & Cache)");
        actions.add(() -> openAppDetails(app.packageName));

        options.add("Uninstall App");
        actions.add(() -> uninstallApp(app.packageName));

        if (dockIndex >= 0) {
            options.add("Move in Dock (Shift with Left / Right)");
            actions.add(() -> startDockMoveMode(dockIndex));

            options.add("Remove from Dock");
            actions.add(() -> {
                dockPackageList.remove(dockIndex);
                saveDockPackages();
                populateDock();
                populateContinueWatchingShelf();
                Toast.makeText(this, app.label + " removed from dock", Toast.LENGTH_SHORT).show();
            });
        } else {
            if (dockPackageList.contains(app.packageName)) {
                options.add("Remove from Dock");
                actions.add(() -> {
                    dockPackageList.remove(app.packageName);
                    saveDockPackages();
                    populateDock();
                    populateContinueWatchingShelf();
                    Toast.makeText(this, app.label + " removed from dock", Toast.LENGTH_SHORT).show();
                });
            } else {
                options.add("Add to Dock");
                actions.add(() -> {
                    dockPackageList.add(app.packageName);
                    saveDockPackages();
                    populateDock();
                    populateContinueWatchingShelf();
                    Toast.makeText(this, app.label + " added to dock", Toast.LENGTH_SHORT).show();
                });
            }
        }

        CharSequence[] items = options.toArray(new CharSequence[0]);
        AlertDialog.Builder b = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        b.setTitle(app.label);
        b.setItems(items, (dialog, which) -> {
            if (which >= 0 && which < actions.size()) {
                actions.get(which).run();
            }
        });
        b.setNegativeButton("Cancel", null);
        showTrackedBuilder(b);
    }

    private void updateBluetoothStatus() {
        if (btnBluetooth == null) return;
        boolean btOn = false;
        try {
            android.media.AudioManager am = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (am != null && am.isBluetoothA2dpOn()) {
                btOn = true;
            }
        } catch (Throwable ignored) {
        }
        final boolean isBtOn = btOn;
        runOnUiThread(() -> {
            if (btnBluetooth != null) {
                btnBluetooth.setText("Bluetooth");
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        Log.i("RemoteKey", "dispatchKeyEvent: keyCode=" + keyCode + " (" + KeyEvent.keyCodeToString(keyCode) + ") action=" + action);

        if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (action == KeyEvent.ACTION_DOWN) {
                dismissAllDialogsAndResetToHome();
            }
            return true;
        }
        if (action == KeyEvent.ACTION_DOWN) {
            HomeButtonAccessibilityService.onUserInteractedWithMainActivity();
        }
        if (HomeButtonAccessibilityService.isAppShortcutKey(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN) {
                HomeButtonAccessibilityService.handleAppShortcutKeyIfMatched(this, keyCode);
            }
            return true;
        }
        if (HomeButtonAccessibilityService.isInputKey(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN) {
                showInputsDialog();
            }
            return true;
        }
        if (HomeButtonAccessibilityService.isAccountKey(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN) {
                showProfileSelectionDialog();
            }
            return true;
        }
        if (isDockMovingMode && keyCode == KeyEvent.KEYCODE_BACK) {
            if (action == KeyEvent.ACTION_UP) {
                stopDockMoveMode();
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (action == KeyEvent.ACTION_UP) {
                View focused = getCurrentFocus();
                if (handleMenuKeyOnView(focused)) {
                    return true;
                }
                openOriginalTvSettings();
            }
            return true;
        }
        if (HomeButtonAccessibilityService.isSettingsKey(keyCode)) {
            if (action == KeyEvent.ACTION_DOWN) {
                openOriginalTvSettings();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean handleMenuKeyOnView(View focused) {
        if (focused == null) return false;
        Object tag = focused.getTag();
        if (tag instanceof AppTag) {
            AppTag at = (AppTag) tag;
            showAppOptionsDialog(at.app, at.dockIndex);
            return true;
        }
        if (tag instanceof WatchNextItem) {
            showWatchItemOptionsDialog((WatchNextItem) tag);
            return true;
        }
        if (focused.getParent() instanceof View) {
            Object parentTag = ((View) focused.getParent()).getTag();
            if (parentTag instanceof AppTag) {
                AppTag at = (AppTag) parentTag;
                showAppOptionsDialog(at.app, at.dockIndex);
                return true;
            }
            if (parentTag instanceof WatchNextItem) {
                showWatchItemOptionsDialog((WatchNextItem) parentTag);
                return true;
            }
        }
        return false;
    }

    public static boolean isFocusedOnCardOrDialog() {
        MainActivity inst = sInstance;
        if (inst == null) return false;
        if (inst.hasActiveDialogs()) return true;
        View focused = inst.getCurrentFocus();
        if (focused != null && (focused.getTag() instanceof WatchNextItem || focused.getTag() instanceof AppTag)) return true;
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (HomeButtonAccessibilityService.isAppShortcutKey(keyCode)) {
            HomeButtonAccessibilityService.handleAppShortcutKeyIfMatched(this, keyCode);
            return true;
        }
        if (isDockMovingMode && keyCode == KeyEvent.KEYCODE_BACK) {
            stopDockMoveMode();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            View focused = getCurrentFocus();
            if (handleMenuKeyOnView(focused)) {
                return true;
            }
            openSettings();
            return true;
        }
        if (HomeButtonAccessibilityService.isSettingsKey(keyCode)) {
            openSettings();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
        final float density = getResources().getDisplayMetrics().density;

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padH = (int) (26 * density);
        int padV = (int) (20 * density);
        container.setPadding(padH, padV, padH, padV);
        container.setClipChildren(true);
        container.setClipToPadding(true);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xF0181A24);
        bg.setCornerRadius(22f * density);
        bg.setStroke((int) (1.5f * density), 0x30FFFFFF);
        container.setBackground(bg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Settings");
        tvTitle.setTextSize(22f);
        tvTitle.setTextColor(Color.WHITE);
        if (outfitTypeface != null) tvTitle.setTypeface(outfitTypeface, Typeface.BOLD);
        container.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("Configure theme, network, user profiles, TV system settings, and launcher preferences");
        tvSub.setTextSize(13f);
        tvSub.setTextColor(0xFF9E9EA8);
        tvSub.setPadding(0, 4, 0, (int) (14 * density));
        if (outfitTypeface != null) tvSub.setTypeface(outfitTypeface);
        container.addView(tvSub);

        AlertDialog dialog = trackDialog(builder.setView(container).create());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (580 * density), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }

        android.widget.ScrollView sv = new android.widget.ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        sv.setClipChildren(true);
        sv.setClipToPadding(true);
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (int) (380 * density));
        svLp.setMargins(0, (int) (6 * density), 0, (int) (6 * density));
        sv.setLayoutParams(svLp);

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(0, (int) (4 * density), 0, (int) (16 * density));
        sv.addView(listContainer);
        container.addView(sv);

        // 1. Theme & Ambient Wallpaper
        View btnThemeTile = createDialogTile("Theme & Ambient Wallpaper",
                "Cycle dynamic gradients or enable auto-theme rotation every 5 min", () -> {
            dialog.dismiss();
            showThemeDialog();
        });
        listContainer.addView(btnThemeTile);

        // 2. Wi-Fi & Network
        String wifiDesc = "Connected: " + (currentWifiSsid != null ? currentWifiSsid : "Wi-Fi") + " • Manage connections";
        View btnWifiTile = createDialogTile("Wi-Fi & Network Settings", wifiDesc, () -> {
            dialog.dismiss();
            openWifiSettings();
        });
        listContainer.addView(btnWifiTile);

        // 3. User Profiles & 3D Character Avatars
        String curProf = WatchNextManager.getCurrentProfile(this);
        View btnAvatarTile = createDialogTile("Customize User Avatar (" + curProf + ")",
                "Choose rich 3D character portraits (Superhero, Cyberpunk, Gamer, Astronaut, Fox, Ninja...)", () -> {
            dialog.dismiss();
            showAvatarSelectionDialog(curProf);
        });
        listContainer.addView(btnAvatarTile);

        View btnProfileTile = createDialogTile("Switch or Add User Profiles",
                "Current: " + curProf + " • Switch between profiles or create new user", () -> {
            dialog.dismiss();
            showProfileSelectionDialog();
        });
        listContainer.addView(btnProfileTile);

        // 4. TV System Settings
        View btnTvSettingsTile = createDialogTile("TV System Settings",
                "Open TV's native settings for Display, Audio, Device, and Accounts", () -> {
            dialog.dismiss();
            openOriginalTvSettings();
        });
        listContainer.addView(btnTvSettingsTile);

        // 5. Manage Continue Watching
        View btnShowsTile = createDialogTile("Continue Watching Shows",
                "Add, edit, or reset shows for " + curProf + " across Disney+, Max, and streaming apps", () -> {
            dialog.dismiss();
            showCustomShowManagerDialog();
        });
        listContainer.addView(btnShowsTile);

        // 6. Bluetooth & Audio
        View btnBtTile = createDialogTile("Bluetooth & Audio",
                "Pair headphones, wireless speakers, game controllers, or remotes", () -> {
            dialog.dismiss();
            showBluetoothDialog();
        });
        listContainer.addView(btnBtTile);

        // 7. Weather Location
        View btnWeatherTile = createDialogTile("Weather Location",
                "Change city, search locations, or switch between °F and °C", () -> {
            dialog.dismiss();
            showWeatherDialog();
        });
        listContainer.addView(btnWeatherTile);

        dialog.show();
        btnThemeTile.requestFocus();
    }

    private void openWifiSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable t) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.amazon.tv.settings.v2", "com.amazon.tv.settings.v2.wifi.WifiActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Throwable t2) {
                openOriginalTvSettings();
            }
        }
    }

    private void openOriginalTvSettings() {
        HomeButtonAccessibilityService.onExternalAppLaunching();
        if (!HomeButtonAccessibilityService.isFireTvDevice()) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
                return;
            } catch (Throwable ignored) {
            }
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
                return;
            } catch (Throwable ignored) {
            }
        }
        // 1. Direct explicit Fire TV MainSettingsActivity
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.amazon.tv.launcher", "com.amazon.tv.launcher.ui.MainSettingsActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        } catch (Throwable ignored) {
        }
        // 2. Direct Fire TV Settings V2 (Device / System)
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.amazon.tv.settings.v2", "com.amazon.tv.settings.v2.tv.device.DeviceActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        } catch (Throwable ignored) {
        }
        // 3. Android TV Leanback settings
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        } catch (Throwable ignored) {
        }
        // 4. Standard Android Settings action
        try {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        } catch (Throwable ignored) {
        }
        // 5. Fallback: Quick Settings
        try {
            Intent intent = new Intent("android.settings.QUICK_LAUNCH_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable t) {
            Toast.makeText(this, "Could not open TV settings: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettings() {
        showSettingsDialog();
    }

    private void showBluetoothDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);

        final float density = getResources().getDisplayMetrics().density;
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padH = (int) (28 * density);
        int padV = (int) (22 * density);
        container.setPadding(padH, padV, padH, padV);
        container.setClipChildren(false);
        container.setClipToPadding(false);

        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(0xF0181A24);
        bg.setCornerRadius(22f * density);
        bg.setStroke((int) (1.5f * density), 0x30FFFFFF);
        container.setBackground(bg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("Bluetooth & Audio");
        tvTitle.setTextSize(21f);
        tvTitle.setTextColor(Color.WHITE);
        if (outfitTypeface != null) tvTitle.setTypeface(outfitTypeface, Typeface.BOLD);
        container.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText("Select your wireless headphones, earbuds, or pair new accessories");
        tvSub.setTextSize(13f);
        tvSub.setTextColor(0xFF9E9EA8);
        tvSub.setPadding(0, 4, 0, (int) (16 * density));
        if (outfitTypeface != null) tvSub.setTypeface(outfitTypeface);
        container.addView(tvSub);

        AlertDialog dialog = trackDialog(builder.setView(container).create());
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout((int) (580 * density), android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }

        // Action 1: Pair New Device
        View btnPair = createDialogTile("Pair New Device",
                "Discover nearby Bluetooth headphones, earbuds, or accessories", () -> {
            dialog.dismiss();
            launchAddAccessory();
        });
        container.addView(btnPair);

        // List Bonded Devices
        try {
            android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                java.util.Set<android.bluetooth.BluetoothDevice> bonded = adapter.getBondedDevices();
                if (bonded != null && !bonded.isEmpty()) {
                    for (android.bluetooth.BluetoothDevice dev : bonded) {
                        String name = dev.getName();
                        if (name == null || name.trim().isEmpty()) name = dev.getAddress();
                        final String devName = name;

                        View btnDev = createDialogTile(devName, "Paired Device • Tap to connect", () -> {
                            dialog.dismiss();
                            connectOrManageBluetoothDevice(dev);
                        });
                        container.addView(btnDev);
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        // Action 3: Remotes & Accessories Settings
        View btnAllSettings = createDialogTile("System Accessories Settings",
                "Open TV settings for pairing, unpairing, or volume", () -> {
            dialog.dismiss();
            openSettings();
        });
        container.addView(btnAllSettings);

        dialog.show();
        btnPair.requestFocus();
    }

    private View createDialogTile(String title, String subtitle, Runnable action) {
        final float density = getResources().getDisplayMetrics().density;
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setFocusable(true);
        tile.setClickable(true);
        int pH = (int) (18 * density);
        int pV = (int) (12 * density);
        tile.setPadding(pH, pV, pH, pV);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, (int) (4 * density), 0, (int) (4 * density));
        tile.setLayoutParams(lp);

        android.graphics.drawable.GradientDrawable defaultBg = new android.graphics.drawable.GradientDrawable();
        defaultBg.setColor(0x22FFFFFF);
        defaultBg.setCornerRadius(14f * density);
        defaultBg.setStroke((int) (1 * density), 0x25FFFFFF);

        android.graphics.drawable.GradientDrawable focusedBg = new android.graphics.drawable.GradientDrawable();
        focusedBg.setColor(0xFFFFFFFF);
        focusedBg.setCornerRadius(14f * density);
        focusedBg.setStroke((int) (2 * density), 0xFFFFFFFF);

        tile.setBackground(defaultBg);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(14f);
        tvTitle.setTextColor(Color.WHITE);
        if (outfitTypeface != null) tvTitle.setTypeface(outfitTypeface, Typeface.BOLD);
        tile.addView(tvTitle);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextSize(11f);
        tvSub.setTextColor(0xFF8E8E98);
        tvSub.setPadding(0, (int) (2 * density), 0, 0);
        if (outfitTypeface != null) tvSub.setTypeface(outfitTypeface);
        tile.addView(tvSub);

        tile.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                tile.setBackground(focusedBg);
                tvTitle.setTextColor(0xFF0A0A0E);
                tvSub.setTextColor(0xFF383842);
                tile.setTranslationZ(4f * density);
            } else {
                tile.setBackground(defaultBg);
                tvTitle.setTextColor(Color.WHITE);
                tvSub.setTextColor(0xFF8E8E98);
                tile.setTranslationZ(0f);
            }
        });

        tile.setOnClickListener(v -> action.run());
        return tile;
    }

    private void connectOrManageBluetoothDevice(android.bluetooth.BluetoothDevice dev) {
        try {
            android.bluetooth.BluetoothAdapter adapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                adapter.getProfileProxy(this, new android.bluetooth.BluetoothProfile.ServiceListener() {
                    @Override
                    public void onServiceConnected(int profile, android.bluetooth.BluetoothProfile proxy) {
                        try {
                            if (profile == android.bluetooth.BluetoothProfile.A2DP) {
                                java.lang.reflect.Method connectMethod = proxy.getClass().getMethod("connect", android.bluetooth.BluetoothDevice.class);
                                connectMethod.invoke(proxy, dev);
                                Toast.makeText(MainActivity.this, "Connecting to " + dev.getName() + "...", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Throwable t) {
                            launchAddAccessory();
                        } finally {
                            adapter.closeProfileProxy(profile, proxy);
                        }
                    }

                    @Override
                    public void onServiceDisconnected(int profile) {}
                }, android.bluetooth.BluetoothProfile.A2DP);
                return;
            }
        } catch (Throwable ignored) {
        }
        launchAddAccessory();
    }

    private void launchAddAccessory() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new android.content.ComponentName("com.android.tv.settings", "com.android.tv.settings.accessories.AddAccessoryActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable t) {
            openSettings();
        }
    }
}
