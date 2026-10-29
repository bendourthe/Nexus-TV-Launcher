package com.cleantv.launcher;



import android.accessibilityservice.AccessibilityService;

import android.accessibilityservice.AccessibilityServiceInfo;

import android.content.BroadcastReceiver;

import android.content.Context;

import android.content.Intent;

import android.content.IntentFilter;

import android.os.Handler;

import android.os.Looper;

import android.util.Log;

import android.view.KeyEvent;

import android.view.accessibility.AccessibilityEvent;

import android.view.accessibility.AccessibilityNodeInfo;



import java.util.Arrays;

import java.util.HashSet;

import java.util.Set;



public class HomeButtonAccessibilityService extends AccessibilityService {

    private static final String TAG = "CleanHomeService";

    private static final String GOOGLE_LAUNCHER_PKG = "com.google.android.apps.tv.launcherx";

    private static final String SETUP_WRAITH_PKG = "com.google.android.tungsten.setupwraith";

    private static final String FIRETV_LAUNCHER_PKG = "com.amazon.tv.launcher";

    private static final String FIRETV_HOMESTARTER_PKG = "com.amazon.firehomestarter";

    private static final long DEBOUNCE_DELAY_MS = 100;



    private static final Set<String> WATCHLIST_KEYWORDS = new HashSet<>(Arrays.asList(

            "continue watching", "keep watching", "resume", "recently watched", "jump back in", "up next"));

    private static final Set<String> IGNORED_TITLES = new HashSet<>(Arrays.asList(

            "home", "search", "settings", "profiles", "profile", "explore", "browse", "movies", "series",

            "shows", "originals", "new", "live", "more", "back", "cancel", "ok", "play", "details",

            "audio", "subtitles", "restart", "episodes", "trailer", "terms", "privacy", "sign in"));



    private long lastHarvestTime = 0;

    private long lastLaunchTime = 0;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private BroadcastReceiver screenReceiver;

    private BroadcastReceiver globalKeyReceiver;



    private static volatile HomeButtonAccessibilityService sInstance;

    private static volatile android.graphics.Bitmap sHomeSnapshot;

    private android.view.WindowManager windowManager;

    private android.widget.FrameLayout overlayContainer;

    private AnimatedWaveBackgroundView overlayWaveView;

    private android.widget.ImageView overlaySnapshotView;

    private boolean overlayAttached = false;

    private final Runnable overlayTimeoutRunnable = this::hideMaskForce;



    private static final long MIN_MASK_HOLD_MS = 180L;

    private static final long MAX_MASK_HOLD_MS = 420L;

    private static final long MIN_WAKE_MASK_HOLD_MS = 1100L;

    private static final long MAX_WAKE_MASK_HOLD_MS = 2400L;

    private volatile boolean isWakeMaskActive = false;

    private volatile long lastWakeSignalTime = 0L;

    private volatile long lastHomeTriggerTime = 0L;

    private volatile long lastMaskShowTime = 0L;

    private volatile long lastCleanLaunchDispatchTime = 0L;

    private volatile long lastAppShortcutLaunchTime = 0L;

    private volatile String lastActiveWindowPkg = "";

    private volatile String lastActiveWindowClass = "";

    private volatile boolean serviceRunning = false;

    private Thread logcatHomeWatcherThread;

    private Thread localUdpHomeWatcherThread;

    private java.net.DatagramSocket localUdpSocket;



    private static final long[] RETRY_DELAYS_MS = new long[] { 80, 180, 320, 500, 720, 960, 1220, 1500 };

    private int retryIndex = 0;

    private final Runnable homeLaunchRetryRunnable = new Runnable() {

        @Override

        public void run() {

            if (!isScreenInteractive()) {

                return;

            }

            long elapsed = System.currentTimeMillis() - lastHomeTriggerTime;

            if (elapsed >= MIN_MASK_HOLD_MS

                    && MainActivity.isMainActivityFocused()

                    && !isStockLauncherActiveWindow()) {

                hideMaskIfReady();

                return;

            }

            launchCleanLauncher();

            if (retryIndex < RETRY_DELAYS_MS.length) {

                long delay = RETRY_DELAYS_MS[retryIndex++];

                mainHandler.postDelayed(this, delay);

            } else {

                hideMaskIfReady();

            }

        }

    };



    public static void updateHomeSnapshot(android.graphics.Bitmap bmp) {

        if (bmp == null || bmp.isRecycled())

            return;

        sHomeSnapshot = bmp;

        HomeButtonAccessibilityService svc = sInstance;

        if (svc != null) {

            svc.mainHandler.post(() -> {

                if (svc.overlaySnapshotView != null && sHomeSnapshot != null && !sHomeSnapshot.isRecycled()) {

                    svc.overlaySnapshotView.setImageBitmap(sHomeSnapshot);

                    svc.overlaySnapshotView.setVisibility(android.view.View.VISIBLE);

                }

            });

        }

    }



    public static void onMainActivityResumed() {

        HomeButtonAccessibilityService svc = sInstance;

        if (svc != null) {

            svc.mainHandler.post(svc::hideMaskIfReady);

        }

    }



    public static void onMainActivityFrameDrawnAndFocused() {

        HomeButtonAccessibilityService svc = sInstance;

        if (svc != null) {

            svc.mainHandler.post(svc::hideMaskIfReady);

        }

    }



    public static void onUserInteractedWithMainActivity() {

        HomeButtonAccessibilityService svc = sInstance;

        if (svc != null) {

            svc.mainHandler.post(() -> {

                svc.mainHandler.removeCallbacks(svc.homeLaunchRetryRunnable);

                svc.mainHandler.removeCallbacks(svc.wakeRedirectRunnable);

                svc.hideMaskForce();

            });

        }

    }



    public static void onExternalAppLaunching() {

        HomeButtonAccessibilityService svc = sInstance;

        if (svc != null) {

            svc.mainHandler.post(() -> {

                svc.mainHandler.removeCallbacks(svc.homeLaunchRetryRunnable);

                svc.mainHandler.removeCallbacks(svc.wakeRedirectRunnable);

                svc.hideMaskForce();

            });

        }

    }



    public static void onMainActivityActive() {

        onMainActivityFrameDrawnAndFocused();

    }



    public static void cancelPendingHomeLaunches() {

        onUserInteractedWithMainActivity();

    }



    public static void dismissOverlay() {

        onUserInteractedWithMainActivity();

    }



    public static boolean isFireTvDevice() {

        String manuf = android.os.Build.MANUFACTURER != null ? android.os.Build.MANUFACTURER.toLowerCase() : "";

        String brand = android.os.Build.BRAND != null ? android.os.Build.BRAND.toLowerCase() : "";

        String model = android.os.Build.MODEL != null ? android.os.Build.MODEL.toLowerCase() : "";

        return manuf.contains("amazon") || brand.contains("amazon") || model.contains("aft");

    }



    public static String[] getAppShortcutPackageCandidates(int keyCode) {

        switch (keyCode) {

            case 265: // Hisense Netflix button (0x109)

                return new String[] { "com.netflix.ninja", "com.netflix.mediaclient" };

            case 293: // Hisense YouTube button (0x125)

                return new String[] { "com.google.android.youtube.tv", "com.amazon.firetv.youtube", "com.google.android.youtube" };

            case 294: // Hisense Sling TV button (0x126)

                return new String[] { "com.sling" };

            case 295: // Hisense Prime Video button (0x127)

                return new String[] { "com.amazon.amazonvideo.livingroom", "com.amazon.firebat", "com.amazon.avod" };

            case 296: // Hisense Hulu button (0x128)

                return new String[] { "com.hulu.livingroomplus", "com.hulu.plus" };

            case 289: // Hisense Vudu / Fandango button (0x121)

            case 304: // Hisense Vudu button (0x130)

            case 360: // Hisense Vudu button (0x168)

                return new String[] { "air.com.vudu.air.DownloaderTablet", "com.fandango.fandangonow.android.tv" };

            case 303: // Hisense Google Play button (0x12f)

                return new String[] { "com.android.vending" };

            case 305: // Hisense TikiLive button (0x131)

                return new String[] { "com.tikilive.live" };

            case 337: // Hisense Hotstar button (0x151)

                return new String[] { "in.startv.hotstar" };

            case 362: // Hisense Tubi button (0x16a)

            case 606: // Hisense Tubi button (0x25e)

                return new String[] { "com.tubitv" };

            case 363: // Hisense Disney+ button (0x16b)

            case 607: // Hisense Disney+ button (0x25f)

                return new String[] { "com.disney.disneyplus" };

            case 364: // Hisense Peacock button (0x16c)

            case 608: // Hisense Peacock button (0x260)

                return new String[] { "com.peacocktv.peacockandroid", "com.peacock.peacockfiretv" };

            case 611: // Hisense YouTube Music button (0x263)

            case 691: // Hisense YouTube Music button (0x2b3)

                return new String[] { "com.google.android.youtube.tvmusic", "com.google.android.youtube.tv" };

            case 661: // Hisense Spotify button (0x295)

                return new String[] { "com.spotify.tv.android", "com.spotify.music" };

            case 697: // Hisense SonyLIV button (0x2b9)

                return new String[] { "com.sonyliv" };

            default:

                return null;

        }

    }



    public static boolean isAppShortcutKey(int keyCode) {

        return getAppShortcutPackageCandidates(keyCode) != null;

    }



    public static boolean handleAppShortcutKeyIfMatched(Context context, int keyCode) {

        String[] candidates = getAppShortcutPackageCandidates(keyCode);

        if (candidates == null || candidates.length == 0 || context == null) {

            return false;

        }

        onExternalAppLaunching();

        android.content.pm.PackageManager pm = context.getPackageManager();

        for (String pkg : candidates) {

            try {

                Intent launchIntent = pm.getLeanbackLaunchIntentForPackage(pkg);

                if (launchIntent == null) {

                    launchIntent = pm.getLaunchIntentForPackage(pkg);

                }

                if (launchIntent != null) {

                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    context.startActivity(launchIntent);

                    Log.i(TAG, "Launched app shortcut key " + keyCode + " -> " + pkg);

                    return true;

                }

            } catch (Throwable t) {

                Log.w(TAG, "Failed launching " + pkg + " for key " + keyCode + ": " + t.getMessage());

            }

        }

        // If the app is not installed, open its Play Store listing rather than Settings

        String primaryPkg = candidates[0];

        try {

            Intent marketIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=" + primaryPkg));

            marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(marketIntent);

            return true;

        } catch (Throwable ignored) {

        }

        return true;

    }



    public static boolean isAccountKey(int keyCode) {

        return keyCode == 288 // KEYCODE_PROFILE_SWITCH (Account / User button on Google TV / Hisense remotes)

                || keyCode == 258 // KEYCODE_TV_MEDIA_CONTEXT_MENU / User key

                || keyCode == 264; // KEYCODE_PAIRING

    }



    public static boolean isInputKey(int keyCode) {

        return keyCode == KeyEvent.KEYCODE_TV_INPUT // 178

                || keyCode == 171 // KEYCODE_WINDOW (Input button on Android TV)

                || keyCode == 165 // KEYCODE_INFO

                || (keyCode >= 243 && keyCode <= 255); // KEYCODE_TV_INPUT_HDMI_1 .. KEYCODE_TV_INPUT_VGA_1

    }



    public static boolean isSettingsKey(int keyCode) {

        if (isAppShortcutKey(keyCode) || isAccountKey(keyCode) || isInputKey(keyCode)) {

            return false;

        }

        return keyCode == KeyEvent.KEYCODE_SETTINGS // 176
                || keyCode == 83 // KEYCODE_NOTIFICATION (Hisense physical remote settings gear button)

                || keyCode == 357 // Virtual remote SETTINGS key on Android TV / Hisense

                || keyCode == KeyEvent.KEYCODE_MENU // 82

                || keyCode == 256 // KEYCODE_TV_CONTENTS_MENU

                || keyCode == 257 // KEYCODE_TV_MEDIA_CONTEXT_MENU

                || keyCode == KeyEvent.KEYCODE_HELP // 259

                || keyCode == 284 // KEYCODE_ALL_APPS / Quick Settings

                || keyCode == 285 // KEYCODE_MEDIA_TOP_MENU

                || keyCode == 286

                || keyCode == 287

                || keyCode == 289

                || keyCode == 290

                || keyCode == 662 // Hisense settings receiver key

                || keyCode == 663 // Hisense settings receiver key

                || keyCode == 664 // Hisense settings receiver key

                || keyCode == 699 // Hisense dedicated Menu/Settings key (0x2bb)

                || keyCode == 700 // Hisense SmartSense settings key

                || keyCode == 709 // Hisense SmartSense tools key

                || keyCode == 710 // Hisense SmartSense menu key

                || keyCode == 711

                || keyCode == 712;

    }



    @Override

    protected void onServiceConnected() {

        super.onServiceConnected();

        sInstance = this;

        windowManager = (android.view.WindowManager) getSystemService(WINDOW_SERVICE);

        Log.i(TAG, "onServiceConnected: Configuring accessibility service info...");

        AccessibilityServiceInfo info = getServiceInfo();

        if (info == null) {

            info = new AccessibilityServiceInfo();

        }

        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

                | AccessibilityEvent.TYPE_WINDOWS_CHANGED

                | AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS

                | AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;

        info.notificationTimeout = 0;

        setServiceInfo(info);

        Log.i(TAG, "onServiceConnected: Key event and window state filters active!");



        registerScreenReceiver();

        registerGlobalKeyReceiver();

        ensureOverlayAttached();

        ProfileSyncManager.start(this);

        startFastHomeSignalListeners();

    }



    private void startFastHomeSignalListeners() {

        serviceRunning = true;

        // 1. Localhost UDP listener on 127.0.0.1:49877 so clean_home_helper.sh can trigger mask in <1ms

        if (localUdpHomeWatcherThread == null || !localUdpHomeWatcherThread.isAlive()) {

            localUdpHomeWatcherThread = new Thread(() -> {

                try {

                    localUdpSocket = new java.net.DatagramSocket(null);

                    localUdpSocket.setReuseAddress(true);

                    localUdpSocket.bind(new java.net.InetSocketAddress("127.0.0.1", 49877));

                    byte[] buf = new byte[128];

                    while (serviceRunning) {

                        java.net.DatagramPacket pkt = new java.net.DatagramPacket(buf, buf.length);

                        localUdpSocket.receive(pkt);

                        fastHomeTriggerFromSignal("udp_49877");

                    }

                } catch (Throwable ignored) {

                }

            }, "CleanHomeUdpSignal");

            localUdpHomeWatcherThread.setDaemon(true);

            localUdpHomeWatcherThread.start();

        }



        // 2. Direct in-process logcat reader (enabled via READ_LOGS permission):

        // - On Fire TV: catches OZ-DCS:CallbackBaseManager_KeyPress KEYCODE_HOME at t=0ms (94ms before HomeActivity_vNext starts!)

        // - On Hisense TV: catches KeyHandleBroadcastDelegate / AppStartupUtils for dedicated remote app shortcut buttons!

        if (logcatHomeWatcherThread == null || !logcatHomeWatcherThread.isAlive()) {

            logcatHomeWatcherThread = new Thread(() -> {

                final boolean fireTv = isFireTvDevice();

                while (serviceRunning) {

                    Process proc = null;

                    try {

                        ProcessBuilder pb = fireTv

                                ? new ProcessBuilder("logcat", "-T", "1", "-v", "brief", "*:I")

                                : new ProcessBuilder("logcat", "-T", "1", "-v", "brief",

                                        "AppStartupUtils:D", "KeyHandleBroadcastDelegate:D", "*:S");

                        proc = pb.redirectErrorStream(true).start();

                        java.io.BufferedReader reader = new java.io.BufferedReader(

                                new java.io.InputStreamReader(proc.getInputStream()));

                        String line;

                        while (serviceRunning && (line = reader.readLine()) != null) {

                            if (fireTv) {

                                if (line.contains("USER_SCREEN_OFF")) {

                                    mainHandler.postAtFrontOfQueue(this::onScreenOffDetected);

                                } else if (line.contains("USER_SCREEN_ON")

                                        || line.contains("SCREEN_ON intent received")) {

                                    mainHandler.postAtFrontOfQueue(() -> onScreenWakeDetected("logcat_user_screen_on"));

                                } else if (line.contains("keyCode=KEYCODE_HOME")

                                        || line.contains("HOME_PRESSED")

                                        || line.contains("Intercept Key event HOME_KEY")

                                        || line.contains("Starting Home Launcher")) {

                                    fastHomeTriggerFromSignal("logcat_oz_dcs");

                                }

                            } else {

                                int defIdx = line.indexOf("default app ");

                                if (defIdx >= 0) {

                                    final String pkg = line.substring(defIdx + "default app ".length()).trim();

                                    if (!pkg.isEmpty()) {

                                        mainHandler.post(() -> launchShortcutPackageFromSignal(pkg));

                                    }

                                } else {

                                    int kcIdx = line.indexOf("receive IR keycode =");

                                    if (kcIdx >= 0) {

                                        String numStr = line.substring(kcIdx + "receive IR keycode =".length()).trim();

                                        try {

                                            final int kc = Integer.parseInt(numStr);

                                            if (isAppShortcutKey(kc)) {

                                                mainHandler.post(() -> handleAppShortcutKeyDebounced(kc));

                                            } else if (isAccountKey(kc)) {

                                                mainHandler.post(this::launchUserMenu);

                                            } else if (isSettingsKey(kc)) {

                                                mainHandler.post(this::launchSettings);

                                            }

                                        } catch (Throwable ignored) {

                                        }

                                    }

                                }

                            }

                        }

                    } catch (Throwable ignored) {

                    } finally {

                        if (proc != null) {

                            try {

                                proc.destroy();

                            } catch (Throwable ignored) {

                            }

                        }

                    }

                    try {

                        Thread.sleep(1500);

                    } catch (InterruptedException ie) {

                        break;

                    }

                }

            }, "CleanHomeLogcatWatcher");

            logcatHomeWatcherThread.setPriority(Thread.MAX_PRIORITY);

            logcatHomeWatcherThread.setDaemon(true);

            logcatHomeWatcherThread.start();

        }

    }



    private void handleAppShortcutKeyDebounced(int kc) {

        long now = System.currentTimeMillis();

        if (now - lastAppShortcutLaunchTime < 800) {

            return;

        }

        lastAppShortcutLaunchTime = now;

        if (isScreensaverOrDreamActive()) {

            wakeAndDismissScreensaver();

        }

        handleAppShortcutKeyIfMatched(this, kc);

    }



    private void launchShortcutPackageFromSignal(String pkg) {

        long now = System.currentTimeMillis();

        if (now - lastAppShortcutLaunchTime < 800) {

            return;

        }

        lastAppShortcutLaunchTime = now;

        if (isScreensaverOrDreamActive()) {

            wakeAndDismissScreensaver();

        }

        onExternalAppLaunching();

        android.content.pm.PackageManager pm = getPackageManager();

        String[] fallbacks;

        if (pkg.contains("netflix")) {

            fallbacks = new String[] { pkg, "com.netflix.ninja", "com.netflix.mediaclient" };

        } else if (pkg.contains("youtube.tvmusic")) {

            fallbacks = new String[] { pkg, "com.google.android.youtube.tvmusic", "com.google.android.youtube.tv" };

        } else if (pkg.contains("youtube")) {

            fallbacks = new String[] { pkg, "com.google.android.youtube.tv", "com.google.android.youtube" };

        } else if (pkg.contains("amazonvideo") || pkg.contains("firebat") || pkg.contains("avod")) {

            fallbacks = new String[] { pkg, "com.amazon.amazonvideo.livingroom", "com.amazon.firebat", "com.amazon.avod" };

        } else if (pkg.contains("peacock")) {

            fallbacks = new String[] { pkg, "com.peacocktv.peacockandroid", "com.peacock.peacockfiretv" };

        } else {

            fallbacks = new String[] { pkg };

        }

        for (String candidate : fallbacks) {

            try {

                Intent launchIntent = pm.getLeanbackLaunchIntentForPackage(candidate);

                if (launchIntent == null) {

                    launchIntent = pm.getLaunchIntentForPackage(candidate);

                }

                if (launchIntent != null) {

                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                    startActivity(launchIntent);

                    Log.i(TAG, "Launched Hisense remote shortcut app -> " + candidate);

                    return;

                }

            } catch (Throwable ignored) {

            }

        }

    }



    private void fastHomeTriggerFromSignal(String source) {

        long now = System.currentTimeMillis();

        if (now - lastHomeTriggerTime < 220) {

            return;

        }

        lastHomeTriggerTime = now;

        mainHandler.postAtFrontOfQueue(() -> {

            showMask();

            MainActivity.requestResetToHomeFromService();

            launchCleanLauncher();

            mainHandler.removeCallbacks(homeLaunchRetryRunnable);

            retryIndex = 0;

            mainHandler.postDelayed(homeLaunchRetryRunnable, RETRY_DELAYS_MS[retryIndex++]);

        });

    }



    private void triggerHomeTransition(String reason) {

        Log.i(TAG, "REQUEST_SHELL_HOME_LAUNCH: " + reason);

        lastHomeTriggerTime = System.currentTimeMillis();

        MainActivity.requestResetToHomeFromService();

        if (isScreensaverOrDreamActive()) {

            hideMaskForce();

            wakeAndDismissScreensaver();

            return;

        }

        if (isFireTvDevice() || isStockLauncherActiveWindow()) {

            showMask();

        }

        mainHandler.removeCallbacks(homeLaunchRetryRunnable);

        retryIndex = 0;

        if (isScreenInteractive()) {

            launchCleanLauncher();

            mainHandler.postDelayed(homeLaunchRetryRunnable, RETRY_DELAYS_MS[retryIndex++]);

        }

    }



    private void onScreenOffDetected() {

        mainHandler.removeCallbacks(homeLaunchRetryRunnable);

        mainHandler.removeCallbacks(wakeRedirectRunnable);

        mainHandler.removeCallbacks(overlayTimeoutRunnable);

        isWakeMaskActive = true;

        MainActivity.requestResetToHomeFromService();

        if (isFireTvDevice()) {

            // Pre-stage the Clean TV Home Screen snapshot on Layer 29 while screen is off on Fire TV

            // so that when the TV turns back on, the stock launcher at Layer 2 is 100% occluded from frame 0.

            showMaskWithoutTimeout();

        }

    }



    private void onScreenWakeDetected(String source) {

        long now = System.currentTimeMillis();

        if (now - lastWakeSignalTime < 900) {

            return;

        }

        lastWakeSignalTime = now;

        isWakeMaskActive = true;

        Log.i(TAG, "REQUEST_SHELL_HOME_LAUNCH: screen_wake_" + source);

        lastHomeTriggerTime = now;

        lastMaskShowTime = now;

        MainActivity.requestResetToHomeFromService();

        if (isFireTvDevice()) {

            showMaskWithoutTimeout();

            mainHandler.removeCallbacks(overlayTimeoutRunnable);

            mainHandler.postDelayed(overlayTimeoutRunnable, MAX_WAKE_MASK_HOLD_MS);

            mainHandler.postDelayed(this::hideMaskIfReady, MIN_WAKE_MASK_HOLD_MS + 20);

        }

        mainHandler.removeCallbacks(wakeRedirectRunnable);

        mainHandler.post(wakeRedirectRunnable);

    }



    private final Runnable wakeRedirectRunnable = new Runnable() {

        private int attempts = 0;

        @Override

        public void run() {

            if (!isScreenInteractive()) {

                attempts = 0;

                return;

            }

            long elapsed = System.currentTimeMillis() - lastHomeTriggerTime;

            long minHold = isWakeMaskActive ? MIN_WAKE_MASK_HOLD_MS : MIN_MASK_HOLD_MS;

            if (elapsed >= minHold && MainActivity.isMainActivityFocused() && !isStockLauncherActiveWindow()) {

                hideMaskIfReady();

                attempts = 0;

                return;

            }

            checkAndRedirectCurrentWindow();

            launchCleanLauncher();

            attempts++;

            if (attempts < 15) {

                long nextDelay = (attempts < 6) ? 80 : (attempts < 10 ? 180 : 350);

                mainHandler.postDelayed(this, nextDelay);

            } else {

                attempts = 0;

                hideMaskIfReady();

            }

        }

    };



    private void registerScreenReceiver() {

        if (screenReceiver == null) {

            screenReceiver = new BroadcastReceiver() {

                @Override

                public void onReceive(Context context, Intent intent) {

                    if (intent == null) return;

                    String action = intent.getAction();

                    Log.i(TAG, "screenReceiver action: " + action);

                    if (Intent.ACTION_SCREEN_OFF.equals(action)) {

                        onScreenOffDetected();

                    } else if (Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_USER_PRESENT.equals(action)) {

                        onScreenWakeDetected("broadcast");

                    }

                }

            };

            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_SCREEN_ON);

            filter.addAction(Intent.ACTION_SCREEN_OFF);

            filter.addAction(Intent.ACTION_USER_PRESENT);

            registerReceiver(screenReceiver, filter);

        }

    }



    private void registerGlobalKeyReceiver() {

        if (globalKeyReceiver == null) {

            globalKeyReceiver = new BroadcastReceiver() {

                @Override

                public void onReceive(Context context, Intent intent) {

                    if (intent == null) return;

                    String action = intent.getAction();

                    Log.i(TAG, "globalKeyReceiver intercepted action: " + action);

                    if ("amazon.intent.action.HOME_PRESSED".equals(action)

                            || "com.amazon.tv.action.HOME_PRESSED".equals(action)) {

                        Log.i(TAG, "Hardware HOME_PRESSED broadcast intercepted! Instant launch...");

                        triggerHomeTransition("broadcast_home_pressed");

                        return;

                    }

                    KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                    if (event != null) {

                        int kc = event.getKeyCode();

                        Log.i(TAG, "globalKeyReceiver KeyEvent: keyCode=" + kc + " action=" + event.getAction());

                        if (kc == KeyEvent.KEYCODE_HOME) {

                            if (event.getAction() == KeyEvent.ACTION_DOWN) {

                                triggerHomeTransition("global_button_home");

                            }

                            return;

                        }

                        if (isAppShortcutKey(kc)) {

                            if (event.getAction() == KeyEvent.ACTION_DOWN) {

                                handleAppShortcutKeyIfMatched(HomeButtonAccessibilityService.this, kc);

                            }

                            return;

                        }

                        if (isInputKey(kc)) {

                            if (event.getAction() == KeyEvent.ACTION_DOWN) {

                                launchInputsMenu();

                            }

                            return;

                        }

                        if (isAccountKey(kc)) {

                            if (event.getAction() == KeyEvent.ACTION_DOWN) {

                                launchUserMenu();

                            }

                            return;

                        }

                        if (isSettingsKey(kc)) {

                            if (event.getAction() == KeyEvent.ACTION_DOWN) {

                                launchSettings();

                            }

                            return;

                        }

                    }

                    if ("com.hisense.android.tv.APPLICATION_MENU".equals(action)

                            || "com.hisense.tv.action.QUICK_SETTINGS".equals(action)) {

                        launchSettings();

                    }

                    if ("com.hisense.INTERCEPT_BUTTON_OPERATING".equals(action)) {

                        int kc = intent.getIntExtra("keycode", 0);

                        if (isAppShortcutKey(kc)) {

                            handleAppShortcutKeyIfMatched(HomeButtonAccessibilityService.this, kc);

                        } else if (isInputKey(kc)) {

                            launchInputsMenu();

                        } else if (isAccountKey(kc)) {

                            launchUserMenu();

                        } else if (isSettingsKey(kc)) {

                            launchSettings();

                        }

                    }

                }

            };

            IntentFilter filter = new IntentFilter();

            filter.addAction("android.intent.action.GLOBAL_BUTTON");

            filter.addAction("amazon.intent.action.HOME_PRESSED");

            filter.addAction("com.amazon.tv.action.HOME_PRESSED");

            filter.addAction("com.google.android.apps.tv.launcherx.CONFIGURE_GLOBAL_BUTTON");

            filter.addAction("com.hisense.android.tv.APPLICATION_MENU");

            filter.addAction("com.hisense.tv.action.QUICK_SETTINGS");

            filter.addAction("com.hisense.INTERCEPT_BUTTON_OPERATING");

            try {

                registerReceiver(globalKeyReceiver, filter);

            } catch (Throwable t) {

                Log.w(TAG, "Failed to register globalKeyReceiver: " + t.getMessage());

            }

        }

    }



    public static boolean isStockLauncherPackage(String p) {

        if (p == null) return false;

        return FIRETV_LAUNCHER_PKG.equals(p)

                || GOOGLE_LAUNCHER_PKG.equals(p)

                || FIRETV_HOMESTARTER_PKG.equals(p)

                || SETUP_WRAITH_PKG.equals(p)

                || "com.hisense.tv.customerlauncher".equals(p)

                || "com.android.tv.settings.system.FallbackHome".equals(p);

    }



    private boolean isScreensaverPkgOrClass(CharSequence pkg, CharSequence cls) {

        String p = pkg != null ? pkg.toString().toLowerCase() : "";

        String c = cls != null ? cls.toString().toLowerCase() : "";

        return p.contains("backdrop")

                || p.contains("screensaver")

                || p.contains("dreams")

                || p.contains("somnambulator")

                || p.contains("bueller.photos")

                || c.contains("dream")

                || c.contains("screensaver")

                || c.contains("backdrop")

                || c.contains("ambient")

                || c.contains("somnambulator");

    }



    private boolean isScreensaverOrDreamActive() {

        try {

            if (isScreensaverPkgOrClass(lastActiveWindowPkg, lastActiveWindowClass)) {

                return true;

            }

            AccessibilityNodeInfo root = getRootInActiveWindow();

            if (root != null) {

                CharSequence pkg = root.getPackageName();

                CharSequence cls = root.getClassName();

                root.recycle();

                if (isScreensaverPkgOrClass(pkg, cls)) {

                    return true;

                }

            }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

                java.util.List<android.view.accessibility.AccessibilityWindowInfo> windows = getWindows();

                if (windows != null) {

                    for (android.view.accessibility.AccessibilityWindowInfo win : windows) {

                        if (win == null) continue;

                        CharSequence title = win.getTitle();

                        if (title != null && isScreensaverPkgOrClass(title, title)) {

                            return true;

                        }

                        AccessibilityNodeInfo wRoot = win.getRoot();

                        if (wRoot != null) {

                            CharSequence wPkg = wRoot.getPackageName();

                            CharSequence wCls = wRoot.getClassName();

                            wRoot.recycle();

                            if (isScreensaverPkgOrClass(wPkg, wCls)) {

                                return true;

                            }

                        }

                    }

                }

            }

        } catch (Throwable ignored) {

        }

        return false;

    }



    @SuppressWarnings("deprecation")

    private void wakeAndDismissScreensaver() {

        Log.i(TAG, "Waking and dismissing active screensaver/dream...");

        hideMaskForce();

        try {

            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);

            if (pm != null) {

                android.os.PowerManager.WakeLock wl = pm.newWakeLock(

                        android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK

                                | android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP

                                | android.os.PowerManager.ON_AFTER_RELEASE,

                        "cleantv:dream_wakeup");

                wl.acquire(1500);

            }

        } catch (Throwable ignored) {

        }

        try {

            Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

            closeDialogs.putExtra("reason", "homekey");

            sendBroadcast(closeDialogs);

        } catch (Throwable ignored) {

        }

        lastActiveWindowPkg = "";

        lastActiveWindowClass = "";

        MainActivity.requestResetToHomeFromService();

        launchCleanLauncher();

        mainHandler.postDelayed(this::launchCleanLauncher, 90);

        mainHandler.postDelayed(this::launchCleanLauncher, 220);

        mainHandler.postDelayed(this::launchCleanLauncher, 420);

    }



    private boolean isStockLauncherActiveWindow() {

        try {

            AccessibilityNodeInfo root = getRootInActiveWindow();

            if (root != null) {

                CharSequence pkg = root.getPackageName();

                CharSequence cls = root.getClassName();

                boolean isStock = pkg != null && isTargetHomeScreen(pkg, cls);

                root.recycle();

                return isStock;

            }

        } catch (Throwable ignored) {

        }

        return false;

    }



    private boolean checkAndRedirectCurrentWindow() {

        if (!isScreenInteractive()) return false;

        try {

            AccessibilityNodeInfo root = getRootInActiveWindow();

            if (root != null) {

                CharSequence pkg = root.getPackageName();

                CharSequence cls = root.getClassName();

                if (pkg != null) {

                    if (isTargetHomeScreen(pkg, cls)) {

                        Log.i(TAG, "Stock launcher detected on wake: " + pkg + ". Bypassing immediately...");

                        triggerHomeTransition("wake_stock_detected");

                        root.recycle();

                        return true;

                    }

                }

                root.recycle();

            }

        } catch (Throwable t) {

            Log.w(TAG, "checkAndRedirectCurrentWindow error: " + t.getMessage());

        }

        return false;

    }



    private boolean isScreenInteractive() {

        try {

            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(Context.POWER_SERVICE);

            return pm == null || pm.isInteractive();

        } catch (Throwable t) {

            return true;

        }

    }



    private boolean isTargetHomeScreen(CharSequence pkg, CharSequence cls) {

        if (!isScreenInteractive()) return false;

        if (pkg == null) return false;

        String p = pkg.toString();

        String c = cls != null ? cls.toString() : "";

        String cLower = c.toLowerCase();

        String pLower = p.toLowerCase();



        // If the window is related to sleep, daydream, screensaver, ambient, or power, NEVER intercept!

        if (cLower.contains("sleep") || cLower.contains("ambient") || cLower.contains("dream")

                || cLower.contains("saver") || cLower.contains("standby") || cLower.contains("power")

                || pLower.contains("screensaver") || pLower.contains("dream") || pLower.contains("backdrop")) {

            return false;

        }



        // If the window is explicitly a settings activity or panel, NEVER intercept!

        if (c.contains("Settings") || c.contains("settings") || c.contains("QuickSettings")

                || c.contains("HudActivity") || c.contains("Preference")) {

            return false;

        }

        if (p.contains("settings") || p.equals("com.amazon.tv.settings.v2") || p.equals("com.android.tv.settings")) {

            return false;

        }



        if (SETUP_WRAITH_PKG.equals(p)) return true;

        if (FIRETV_HOMESTARTER_PKG.equals(p)) return true;

        if ("com.hisense.tv.customerlauncher".equals(p)) return true;

        if ("com.android.tv.settings.system.FallbackHome".equals(p)) return true;

        if (FIRETV_LAUNCHER_PKG.equals(p)) {

            if (c.contains("Search") || c.contains("search") || c.contains("Details") || c.contains("detail")) {

                return false;

            }

            return true;

        }

        if (GOOGLE_LAUNCHER_PKG.equals(p)) {

            return c.contains("HomeActivity") || c.contains("VanillaModeHomeActivity");

        }

        return false;

    }



    @Override

    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event == null) return;

        int eventType = event.getEventType();

        CharSequence pkgName = event.getPackageName();

        CharSequence className = event.getClassName();

        String pkgStr = pkgName != null ? pkgName.toString() : "";

        String clsStr = className != null ? className.toString() : "";



        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && !pkgStr.isEmpty()) {

            lastActiveWindowPkg = pkgStr;

            lastActiveWindowClass = clsStr;

        }



        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

                || eventType == AccessibilityEvent.TYPE_WINDOWS_CHANGED) {

            if (isScreensaverPkgOrClass(pkgName, className)) {

                hideMaskForce();

                return;

            }

            if (isTargetHomeScreen(pkgName, className)) {

                Log.i(TAG, "isTargetHomeScreen triggered for " + pkgName + " / " + className);

                showMask();

                long now = System.currentTimeMillis();

                if (now - lastLaunchTime > DEBOUNCE_DELAY_MS) {

                    lastLaunchTime = now;

                    triggerHomeTransition("window_state_" + pkgStr);

                }

            } else if (getPackageName().equals(pkgStr) && MainActivity.isMainActivityFocused()) {

                hideMaskIfReady();

            }

        }



        // Dynamic Watchlist Harvester for Streaming Apps (Disney+, HBO Max, Apple TV, Prime Video)

        if (WatchNextManager.isWatchlistApp(pkgStr)) {

            if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED

                    || eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED

                    || eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {

                harvestWatchlistFromActiveWindow(pkgStr);

            }

        }

    }



    private void harvestWatchlistFromActiveWindow(String pkg) {

        long now = System.currentTimeMillis();

        if (now - lastHarvestTime < 1400) return; // Debounce while browsing

        lastHarvestTime = now;



        try {

            AccessibilityNodeInfo root = getRootInActiveWindow();

            if (root == null) return;

            harvestNodesRecursive(root, pkg, false, 0);

            root.recycle();

        } catch (Throwable t) {

            Log.w(TAG, "harvestWatchlist error: " + t.getMessage());

        }

    }



    private void harvestNodesRecursive(AccessibilityNodeInfo node, String pkg, boolean inWatchlistSection, int depth) {

        if (node == null || depth > 18) return;



        CharSequence textSeq = node.getText();

        CharSequence descSeq = node.getContentDescription();

        String text = textSeq != null ? textSeq.toString().trim() : "";

        String desc = descSeq != null ? descSeq.toString().trim() : "";



        boolean sectionDetected = inWatchlistSection;

        if (!inWatchlistSection) {

            String combined = (text + " " + desc).toLowerCase();

            for (String kw : WATCHLIST_KEYWORDS) {

                if (combined.contains(kw)) {

                    sectionDetected = true;

                    break;

                }

            }

        }



        if (sectionDetected) {

            String candidate = !desc.isEmpty() ? desc : text;

            if (isPlausibleShowTitle(candidate)) {

                parseAndSaveShow(pkg, candidate);

            }

        }



        int childCount = node.getChildCount();

        for (int i = 0; i < childCount; i++) {

            AccessibilityNodeInfo child = node.getChild(i);

            if (child != null) {

                try {

                    harvestNodesRecursive(child, pkg, sectionDetected, depth + 1);

                } finally {

                    child.recycle();

                }

            }

        }

    }



    private boolean isPlausibleShowTitle(String text) {

        if (text == null || text.length() < 2 || text.length() > 65) return false;

        String lower = text.toLowerCase().trim();

        if (IGNORED_TITLES.contains(lower)) return false;

        for (String kw : WATCHLIST_KEYWORDS) {

            if (lower.equals(kw) || lower.contains(kw)) return false;

        }

        if (lower.contains("minute") || lower.contains("hour") || lower.contains("skip")

                || lower.contains("loading") || lower.contains("press select") || lower.contains("press enter")

                || lower.contains("terms") || lower.contains("privacy") || lower.contains("sign in") || lower.contains("sign out")

                || lower.contains("remove from") || lower.contains("add to") || lower.contains("watch next")

                || lower.contains("resume") || lower.contains("dismiss") || lower.contains("option")

                || lower.contains("manage") || lower.contains("clear") || lower.contains("delete")

                || lower.contains("details") || lower.contains("episodes") || lower.contains("more info")

                || lower.contains("see all") || lower.contains("switch") || lower.contains("restart")

                || lower.contains("suggested") || lower.contains("extras") || lower.contains("audio description")

                || lower.contains("trailer") || lower.contains("search by") || lower.contains("action/adventure")) {

            return false;

        }

        return true;

    }



    private void parseAndSaveShow(String pkg, String raw) {

        String title = raw;

        String ep = "Continue Watching";



        if (raw.contains(" - ")) {

            String[] parts = raw.split(" - ", 2);

            title = parts[0].trim();

            ep = parts[1].trim();

        } else if (raw.contains(", Season") || raw.contains(", S")) {

            String[] parts = raw.split(", (?=S)", 2);

            title = parts[0].trim();

            ep = parts[1].trim();

        } else if (raw.contains(": Season") || raw.contains(": S")) {

            String[] parts = raw.split(": (?=S)", 2);

            title = parts[0].trim();

            ep = parts[1].trim();

        }



        if (title.isEmpty()) return;



        WatchNextManager.saveDynamicWatchlistShow(this, pkg, title, ep);

    }



    private void ensureOverlayAttached() {

        try {

            if (windowManager == null) {

                windowManager = (android.view.WindowManager) getSystemService(WINDOW_SERVICE);

            }

            if (windowManager == null) return;

            if (overlayContainer == null) {

                overlayContainer = new android.widget.FrameLayout(this);

                overlayContainer.setBackgroundColor(0xFF080B12);

                int themeIdx = getSharedPreferences("CleanTvLauncherPrefs", MODE_PRIVATE)

                        .getInt("theme_index", 0);

                overlayWaveView = new AnimatedWaveBackgroundView(this);

                overlayWaveView.setThemeIndex(themeIdx);

                overlayContainer.addView(overlayWaveView, new android.widget.FrameLayout.LayoutParams(

                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,

                        android.view.ViewGroup.LayoutParams.MATCH_PARENT));



                overlaySnapshotView = new android.widget.ImageView(this);

                overlaySnapshotView.setScaleType(android.widget.ImageView.ScaleType.FIT_XY);

                if (sHomeSnapshot != null && !sHomeSnapshot.isRecycled()) {

                    overlaySnapshotView.setImageBitmap(sHomeSnapshot);

                    overlaySnapshotView.setVisibility(android.view.View.VISIBLE);

                } else {

                    overlaySnapshotView.setVisibility(android.view.View.GONE);

                }

                overlayContainer.addView(overlaySnapshotView, new android.widget.FrameLayout.LayoutParams(

                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,

                        android.view.ViewGroup.LayoutParams.MATCH_PARENT));

                overlayContainer.setVisibility(android.view.View.GONE);

            }

            if (!overlayAttached) {

                int type = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1

                        ? android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY

                        : android.view.WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;

                android.view.WindowManager.LayoutParams lp = new android.view.WindowManager.LayoutParams(

                        android.view.WindowManager.LayoutParams.MATCH_PARENT,

                        android.view.WindowManager.LayoutParams.MATCH_PARENT,

                        type,

                        android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE

                                | android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

                                | android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

                                | android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,

                        android.graphics.PixelFormat.OPAQUE);

                windowManager.addView(overlayContainer, lp);

                overlayAttached = true;

            }

        } catch (Throwable t) {

            Log.w(TAG, "ensureOverlayAttached error: " + t.getMessage());

            overlayAttached = false;

        }

    }



    private void showMaskWithoutTimeout() {

        try {

            if (isScreensaverOrDreamActive()) {

                return;

            }

            ensureOverlayAttached();

            updateOverlayTheme();

            if (overlaySnapshotView != null && sHomeSnapshot != null && !sHomeSnapshot.isRecycled()) {

                overlaySnapshotView.setImageBitmap(sHomeSnapshot);

                overlaySnapshotView.setVisibility(android.view.View.VISIBLE);

            }

            if (overlayContainer != null && overlayContainer.getVisibility() != android.view.View.VISIBLE) {

                lastMaskShowTime = System.currentTimeMillis();

                overlayContainer.setVisibility(android.view.View.VISIBLE);

                overlayContainer.invalidate();

            }

        } catch (Throwable t) {

            Log.w(TAG, "showMaskWithoutTimeout error: " + t.getMessage());

        }

    }



    private void showMask() {

        long now = System.currentTimeMillis();

        long minHold = isWakeMaskActive ? MIN_WAKE_MASK_HOLD_MS : MIN_MASK_HOLD_MS;

        long maxHold = isWakeMaskActive ? MAX_WAKE_MASK_HOLD_MS : (isFireTvDevice() ? 8500L : MAX_MASK_HOLD_MS);

        boolean alreadyActiveInWindow = overlayContainer != null

                && overlayContainer.getVisibility() == android.view.View.VISIBLE

                && (now - lastMaskShowTime) <= maxHold;

        showMaskWithoutTimeout();

        if (!alreadyActiveInWindow) {

            lastMaskShowTime = now;

            mainHandler.removeCallbacks(overlayTimeoutRunnable);

            mainHandler.postDelayed(overlayTimeoutRunnable, maxHold);

            mainHandler.postDelayed(this::hideMaskIfReady, minHold + 15);

        }

    }



    private void updateOverlayTheme() {

        try {

            if (overlayWaveView != null) {

                int themeIdx = getSharedPreferences("CleanTvLauncherPrefs", MODE_PRIVATE)

                        .getInt("theme_index", 0);

                overlayWaveView.setThemeIndex(themeIdx);

            }

        } catch (Throwable ignored) {

        }

    }



    private void hideMaskIfReady() {

        try {

            if (overlayContainer == null || overlayContainer.getVisibility() == android.view.View.GONE) {

                return;

            }

            long now = System.currentTimeMillis();

            long sinceShow = now - lastMaskShowTime;

            long minHold = isWakeMaskActive ? MIN_WAKE_MASK_HOLD_MS : MIN_MASK_HOLD_MS;

            long maxHold = isWakeMaskActive ? MAX_WAKE_MASK_HOLD_MS : (isFireTvDevice() ? 8500L : MAX_MASK_HOLD_MS);

            if (sinceShow < minHold) {

                mainHandler.postDelayed(this::hideMaskIfReady, (minHold - sinceShow) + 15);

                return;

            }

            if (sinceShow < maxHold && (!MainActivity.isMainActivityFocused() || isStockLauncherActiveWindow())) {

                launchCleanLauncher();

                mainHandler.postDelayed(this::hideMaskIfReady, 60);

                return;

            }

            hideMaskForce();

        } catch (Throwable ignored) {

        }

    }



    private void hideMaskForce() {

        try {

            isWakeMaskActive = false;

            mainHandler.removeCallbacks(overlayTimeoutRunnable);

            if (overlayContainer != null && overlayContainer.getVisibility() != android.view.View.GONE) {

                overlayContainer.setVisibility(android.view.View.GONE);

            }

        } catch (Throwable ignored) {

        }

    }



    private void removeOverlay() {

        try {

            mainHandler.removeCallbacks(overlayTimeoutRunnable);

            if (overlayAttached && overlayContainer != null && windowManager != null) {

                windowManager.removeViewImmediate(overlayContainer);

                overlayAttached = false;

            }

        } catch (Throwable t) {

            overlayAttached = false;

        }

    }



    @Override

    public void onInterrupt() {

        Log.i(TAG, "onInterrupt");

        hideMaskForce();

    }



    @Override

    public void onDestroy() {

        super.onDestroy();

        serviceRunning = false;

        sInstance = null;

        if (localUdpSocket != null) {

            try {

                localUdpSocket.close();

            } catch (Throwable ignored) {

            }

            localUdpSocket = null;

        }

        removeOverlay();

        if (screenReceiver != null) {

            try {

                unregisterReceiver(screenReceiver);

            } catch (Throwable ignored) {

            }

            screenReceiver = null;

        }

        if (globalKeyReceiver != null) {

            try {

                unregisterReceiver(globalKeyReceiver);

            } catch (Throwable ignored) {

            }

            globalKeyReceiver = null;

        }

    }



    @Override

    protected boolean onKeyEvent(KeyEvent event) {

        int keyCode = event.getKeyCode();

        int action = event.getAction();

        Log.i(TAG, "onKeyEvent: keyCode=" + keyCode + " (" + KeyEvent.keyCodeToString(keyCode) + ") action=" + action);



        if (keyCode == KeyEvent.KEYCODE_HOME) {

            if (isScreensaverOrDreamActive() || !isFireTvDevice()) {

                if (action == KeyEvent.ACTION_DOWN) {

                    Log.i(TAG, "KEYCODE_HOME on Hisense or during Screensaver -> waking dream & launching Clean TV...");

                    wakeAndDismissScreensaver();

                }

                // Let PhoneWindowManager also process KEYCODE_HOME so DreamManagerService stops Daydream

                // and launches the default CATEGORY_HOME activity (com.cleantv.launcher/.MainActivity).

                return false;

            }

            if (action == KeyEvent.ACTION_DOWN) {

                Log.i(TAG, "KEYCODE_HOME ACTION_DOWN intercepted on Fire TV! Direct launch...");

                triggerHomeTransition("onKeyEvent_home");

            }

            return true; // Consume KEYCODE_HOME on Fire TV so PhoneWindowManager does not run stock launcher

        }



        if (isAppShortcutKey(keyCode)) {

            if (action == KeyEvent.ACTION_DOWN) {

                Log.i(TAG, "Dedicated app shortcut button (" + keyCode + ") pressed! Launching app...");

                if (isScreensaverOrDreamActive()) {

                    wakeAndDismissScreensaver();

                }

                handleAppShortcutKeyIfMatched(this, keyCode);

            }

            return true;

        }



        if (isInputKey(keyCode)) {

            if (action == KeyEvent.ACTION_DOWN) {

                Log.i(TAG, "Input key (" + keyCode + ") captured! Launching inputs menu...");

                launchInputsMenu();

            }

            return true;

        }



        if (isAccountKey(keyCode)) {

            if (action == KeyEvent.ACTION_DOWN) {

                Log.i(TAG, "Account key (" + keyCode + ") captured! Launching user menu...");

                launchUserMenu();

            }

            return true;

        }



        if (isSettingsKey(keyCode)) {

            if (keyCode == KeyEvent.KEYCODE_MENU) {

                try {

                    AccessibilityNodeInfo root = getRootInActiveWindow();

                    if (root != null) {

                        CharSequence pkg = root.getPackageName();

                        root.recycle();

                        if (getPackageName().equals(pkg != null ? pkg.toString() : null)) {

                            if (MainActivity.isFocusedOnCardOrDialog()) {

                                return super.onKeyEvent(event);

                            }

                        }

                    }

                } catch (Throwable ignored) {

                }

            }

            if (action == KeyEvent.ACTION_DOWN) {

                Log.i(TAG, "Settings key (" + keyCode + ") captured! Launching system settings...");

                launchSettings();

            }

            return true;

        }

        return super.onKeyEvent(event);

    }



    public void launchInputsMenu() {

        if (MainActivity.isMainActivityFocused()) {

            MainActivity.requestShowInputsMenuFromService();

            return;

        }

        try {

            Intent intent = new Intent(this, MainActivity.class);

            intent.setAction(Intent.ACTION_MAIN);

            intent.addCategory(Intent.CATEGORY_HOME);

            intent.putExtra("EXTRA_OPEN_INPUTS_MENU", true);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

        } catch (Throwable t) {

            Log.w(TAG, "launchInputsMenu failed: " + t.getMessage());

        }

    }



    public void launchUserMenu() {

        if (MainActivity.isMainActivityFocused()) {

            MainActivity.requestShowUserMenuFromService();

            return;

        }

        try {

            Intent intent = new Intent(this, MainActivity.class);

            intent.setAction(Intent.ACTION_MAIN);

            intent.addCategory(Intent.CATEGORY_HOME);

            intent.putExtra("EXTRA_OPEN_USER_MENU", true);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(intent);

        } catch (Throwable t) {

            Log.w(TAG, "launchUserMenu failed: " + t.getMessage());

        }

    }



        private static long sLastSvcSettingsLaunch = 0;

    public void launchSettings() {
        long now = android.os.SystemClock.uptimeMillis();
        if (now - sLastSvcSettingsLaunch < 1000) {
            return;
        }
        sLastSvcSettingsLaunch = now;

        onExternalAppLaunching();
        if (MainActivity.isMainActivityFocused()) {
            MainActivity.requestOpenSettingsFromService();
            return;
        }
        if (!isFireTvDevice()) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            } catch (Throwable ignored) {
            }
            try {
                Intent intent = new Intent();
                intent.setComponent(new android.content.ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
            } catch (Throwable ignored) {
            }
            try {
                Intent intent = new Intent();
                intent.setComponent(new android.content.ComponentName("com.android.tv.settings", "com.android.tv.settings.system.QuickSettingsActivity"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
                return;
            } catch (Throwable ignored) {
            }
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
                return;
            } catch (Throwable ignored) {
            }
        }
        try {
            Intent intent = new Intent();
            intent.setComponent(new android.content.ComponentName("com.amazon.tv.launcher", "com.amazon.tv.launcher.ui.MainSettingsActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Intent intent = new Intent();
            intent.setComponent(new android.content.ComponentName("com.amazon.tv.settings.v2", "com.amazon.tv.settings.v2.tv.device.DeviceActivity"));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        } catch (Throwable ignored) {
        }
        try {
            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Throwable t) {
            try {
                Intent intent = new Intent();
                intent.setComponent(new android.content.ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings"));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Throwable ignored) {
            }
        }
    }

    private void launchCleanLauncher() {

        if (!isScreenInteractive()) {

            Log.i(TAG, "Screen is non-interactive / sleep mode. Skipping launchCleanLauncher.");

            return;

        }

        long now = System.currentTimeMillis();

        if (now - lastCleanLaunchDispatchTime < 95) {

            return;

        }

        lastCleanLaunchDispatchTime = now;

        try {

            Intent intent = new Intent(this, MainActivity.class);

            intent.setAction(Intent.ACTION_MAIN);

            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK

                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

                    | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

            try {

                android.app.ActivityOptions opts = android.app.ActivityOptions.makeCustomAnimation(this, 0, 0);

                startActivity(intent, opts.toBundle());

            } catch (Throwable optEx) {

                startActivity(intent);

            }

            Log.i(TAG, "Clean TV Launcher dispatched immediately with zero animation delay");

        } catch (Throwable e) {

            Log.e(TAG, "Failed to launch MainActivity", e);

            try {

                Intent fallback = new Intent(this, MainActivity.class);

                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

                startActivity(fallback);

            } catch (Throwable ignored) {

            }

        }

    }

}

