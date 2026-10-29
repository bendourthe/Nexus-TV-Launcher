package com.cleantv.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileSyncManager {
    private static final String TAG = "ProfileSyncManager";
    private static final String PREFS_NAME = "CleanTvWatchNextPrefs";
    public static final String KEY_SYNC_UPDATED_AT = "profile_sync_updated_at";
    private static final String KEY_KNOWN_PEERS = "profile_sync_known_peers";
    private static final int SYNC_PORT = 49878;

    private static final List<String> DEFAULT_KNOWN_PEERS = Arrays.asList(
            "10.0.0.169",
            "10.0.0.96"
    );

    private static volatile boolean sStarted = false;
    private static volatile Context sAppContext;
    private static final ExecutorService sIoExecutor = Executors.newCachedThreadPool();

    public static synchronized void start(Context context) {
        if (context == null) return;
        sAppContext = context.getApplicationContext();
        ensureInitialMigrationTimestamp(sAppContext);
        if (!sStarted) {
            sStarted = true;
            startUdpListener();
            startTcpListener();
        }
        requestAndBroadcastSync(sAppContext);
    }

    private static void ensureInitialMigrationTimestamp(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long ts = sp.getLong(KEY_SYNC_UPDATED_AT, -1L);
            boolean isAmazon = Build.MANUFACTURER != null && Build.MANUFACTURER.toLowerCase().contains("amazon");
            if (ts < 0L || (isAmazon && ts == 1000L)) {
                long seedTs = isAmazon ? 2000L : 1000L;
                sp.edit().putLong(KEY_SYNC_UPDATED_AT, seedTs).apply();
                Log.i(TAG, "Initialized profile sync timestamp=" + seedTs + " (isAmazon=" + isAmazon + ")");
            }
        } catch (Throwable ignored) {
        }
    }

    public static void onLocalProfileChanged(Context context) {
        if (context == null) return;
        Context appCtx = context.getApplicationContext();
        sAppContext = appCtx;
        SharedPreferences sp = appCtx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long now = System.currentTimeMillis();
        sp.edit().putLong(KEY_SYNC_UPDATED_AT, now).apply();
        broadcastProfileStateAsync(appCtx);
    }

    public static void requestAndBroadcastSync(Context context) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        sIoExecutor.execute(() -> {
            try {
                String syncPayload = buildSyncPayload(appCtx, "PROFILE_SYNC_AND_REQ");
                sendToAllPeers(appCtx, syncPayload);
            } catch (Throwable t) {
                Log.w(TAG, "requestAndBroadcastSync error: " + t.getMessage());
            }
        });
    }

    public static void broadcastProfileStateAsync(Context context) {
        if (context == null) return;
        final Context appCtx = context.getApplicationContext();
        sIoExecutor.execute(() -> {
            try {
                String payload = buildSyncPayload(appCtx, "PROFILE_SYNC");
                sendToAllPeers(appCtx, payload);
            } catch (Throwable t) {
                Log.w(TAG, "broadcastProfileStateAsync error: " + t.getMessage());
            }
        });
    }

    private static String buildSyncPayload(Context context, String msgType) throws Exception {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        ensureInitialMigrationTimestamp(context);
        long updatedAt = sp.getLong(KEY_SYNC_UPDATED_AT, 0L);
        String currentProfile = WatchNextManager.getCurrentProfile(context);
        List<String> profiles = WatchNextManager.getProfilesList(context);

        JSONObject root = new JSONObject();
        root.put("type", msgType);
        root.put("updatedAt", updatedAt);
        root.put("currentProfile", currentProfile);

        JSONArray profArr = new JSONArray();
        JSONObject iconsObj = new JSONObject();
        JSONObject themesObj = new JSONObject();

        for (String p : profiles) {
            profArr.put(p);
            String keySuffix = p.toLowerCase().trim();
            String iconId = WatchNextManager.getProfileIcon(context, p);
            String themeId = WatchNextManager.getProfileAvatar(context, p);
            if (iconId != null) {
                iconsObj.put(keySuffix, iconId);
            }
            if (themeId != null) {
                themesObj.put(keySuffix, themeId);
            }
        }

        root.put("profiles", profArr);
        root.put("icons", iconsObj);
        root.put("themes", themesObj);
        return root.toString();
    }

    private static void handleIncomingPayload(Context context, String rawJson, String senderIp) {
        if (context == null || rawJson == null || rawJson.trim().isEmpty()) return;
        try {
            String localIp = getLocalIpv4Address();
            if (senderIp != null && (senderIp.equals("127.0.0.1") || senderIp.equals(localIp))) {
                return;
            }
            if (senderIp != null && !senderIp.isEmpty()) {
                recordPeerIp(context, senderIp);
            }

            JSONObject obj = new JSONObject(rawJson.trim());
            String type = obj.optString("type", "");
            if (!type.startsWith("PROFILE_SYNC")) return;

            long remoteTs = obj.optLong("updatedAt", 0L);
            SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            long localTs = sp.getLong(KEY_SYNC_UPDATED_AT, 0L);

            Log.i(TAG, "Received " + type + " from " + senderIp + " (remoteTs=" + remoteTs + ", localTs=" + localTs + ")");

            if (remoteTs > localTs) {
                applyRemoteState(context, sp, obj, remoteTs);
            } else if (localTs > remoteTs && senderIp != null && !senderIp.isEmpty()) {
                // Our state is newer than the peer's state; send our state back to the peer!
                String reply = buildSyncPayload(context, "PROFILE_SYNC");
                sendDirectToIp(senderIp, reply);
            }
        } catch (Throwable t) {
            Log.w(TAG, "handleIncomingPayload error: " + t.getMessage());
        }
    }

    private static boolean doesLocalStateMatch(Context context, JSONObject remote) {
        try {
            String remoteCur = remote.optString("currentProfile", "");
            if (!remoteCur.equals(WatchNextManager.getCurrentProfile(context))) {
                return false;
            }
            JSONObject icons = remote.optJSONObject("icons");
            if (icons != null) {
                Iterator<String> keys = icons.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    String rIcon = icons.optString(k, "");
                    String lIcon = WatchNextManager.getProfileIcon(context, k);
                    if (!rIcon.equalsIgnoreCase(lIcon)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Throwable t) {
            return true;
        }
    }

    private static void applyRemoteState(Context context, SharedPreferences sp, JSONObject obj, long remoteTs) {
        try {
            SharedPreferences.Editor editor = sp.edit();
            JSONArray profArr = obj.optJSONArray("profiles");
            if (profArr != null && profArr.length() > 0) {
                List<String> merged = new ArrayList<>(WatchNextManager.getProfilesList(context));
                for (int i = 0; i < profArr.length(); i++) {
                    String p = profArr.optString(i, "").trim();
                    if (!p.isEmpty() && !merged.contains(p)) {
                        merged.add(p);
                    }
                }
                JSONArray saveArr = new JSONArray();
                for (String p : merged) {
                    saveArr.put(p);
                }
                editor.putString(WatchNextManager.KEY_PROFILES_LIST, saveArr.toString());
            }

            String remoteCurrent = obj.optString("currentProfile", "").trim();
            if (!remoteCurrent.isEmpty()) {
                editor.putString(WatchNextManager.KEY_CURRENT_PROFILE, remoteCurrent);
            }

            JSONObject iconsObj = obj.optJSONObject("icons");
            if (iconsObj != null) {
                Iterator<String> it = iconsObj.keys();
                while (it.hasNext()) {
                    String k = it.next().toLowerCase().trim();
                    String v = iconsObj.optString(k, "").trim();
                    if (!k.isEmpty() && !v.isEmpty()) {
                        editor.putString(WatchNextManager.KEY_PROFILE_ICON_PREFIX + k, v);
                    }
                }
            }

            JSONObject themesObj = obj.optJSONObject("themes");
            if (themesObj != null) {
                Iterator<String> it = themesObj.keys();
                while (it.hasNext()) {
                    String k = it.next().toLowerCase().trim();
                    String v = themesObj.optString(k, "").trim();
                    if (!k.isEmpty() && !v.isEmpty()) {
                        editor.putString(WatchNextManager.KEY_PROFILE_AVATAR_PREFIX + k, v);
                    }
                }
            }

            editor.putLong(KEY_SYNC_UPDATED_AT, remoteTs);
            editor.apply();

            Log.i(TAG, "Applied synced profile state: currentProfile=" + remoteCurrent
                    + ", icons=" + (iconsObj != null ? iconsObj.toString() : "{}")
                    + ", updatedAt=" + remoteTs);
            MainActivity.onProfileSyncedFromPeer();
        } catch (Throwable t) {
            Log.w(TAG, "applyRemoteState error: " + t.getMessage());
        }
    }

    private static void sendToAllPeers(Context context, String payload) {
        byte[] data = payload.getBytes(StandardCharsets.UTF_8);
        String localIp = getLocalIpv4Address();

        Set<String> targets = new HashSet<>(DEFAULT_KNOWN_PEERS);
        targets.addAll(getSavedPeers(context));

        // 1. Send UDP broadcast + unicast
        try (DatagramSocket sock = new DatagramSocket()) {
            sock.setBroadcast(true);
            try {
                DatagramPacket bcast = new DatagramPacket(
                        data, data.length, InetAddress.getByName("255.255.255.255"), SYNC_PORT);
                sock.send(bcast);
            } catch (Throwable ignored) {
            }
            if (localIp != null && localIp.contains(".")) {
                String subnetPrefix = localIp.substring(0, localIp.lastIndexOf('.') + 1);
                try {
                    DatagramPacket subBcast = new DatagramPacket(
                            data, data.length, InetAddress.getByName(subnetPrefix + "255"), SYNC_PORT);
                    sock.send(subBcast);
                } catch (Throwable ignored) {
                }
            }
            for (String ip : targets) {
                if (ip == null || ip.equals(localIp)) continue;
                try {
                    DatagramPacket pkt = new DatagramPacket(
                            data, data.length, InetAddress.getByName(ip), SYNC_PORT);
                    sock.send(pkt);
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Also send direct TCP to known peers so router multicast/UDP filtering never blocks sync
        for (String ip : targets) {
            if (ip == null || ip.equals(localIp)) continue;
            sendDirectTcpToIp(ip, payload);
        }
    }

    private static void sendDirectToIp(String ip, String payload) {
        try (DatagramSocket sock = new DatagramSocket()) {
            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            DatagramPacket pkt = new DatagramPacket(data, data.length, InetAddress.getByName(ip), SYNC_PORT);
            sock.send(pkt);
        } catch (Throwable ignored) {
        }
        sendDirectTcpToIp(ip, payload);
    }

    private static void sendDirectTcpToIp(String ip, String payload) {
        sIoExecutor.execute(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(ip, SYNC_PORT), 1200);
                socket.setSoTimeout(1200);
                OutputStream os = socket.getOutputStream();
                os.write((payload + "\n").getBytes(StandardCharsets.UTF_8));
                os.flush();
            } catch (Throwable ignored) {
            }
        });
    }

    private static void startUdpListener() {
        Thread t = new Thread(() -> {
            while (sStarted) {
                try (DatagramSocket socket = new DatagramSocket(null)) {
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(SYNC_PORT));
                    byte[] buf = new byte[8192];
                    while (sStarted) {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        String json = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                        String senderIp = packet.getAddress() != null ? packet.getAddress().getHostAddress() : "";
                        handleIncomingPayload(sAppContext, json, senderIp);
                    }
                } catch (Throwable e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }, "CleanTvProfileUdpSync");
        t.setDaemon(true);
        t.start();
    }

    private static void startTcpListener() {
        Thread t = new Thread(() -> {
            while (sStarted) {
                try (ServerSocket serverSocket = new ServerSocket()) {
                    serverSocket.setReuseAddress(true);
                    serverSocket.bind(new InetSocketAddress(SYNC_PORT));
                    while (sStarted) {
                        Socket client = serverSocket.accept();
                        sIoExecutor.execute(() -> {
                            try (Socket c = client) {
                                c.setSoTimeout(2000);
                                BufferedReader reader = new BufferedReader(
                                        new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
                                String line = reader.readLine();
                                String senderIp = c.getInetAddress() != null ? c.getInetAddress().getHostAddress() : "";
                                if (line != null) {
                                    handleIncomingPayload(sAppContext, line, senderIp);
                                }
                            } catch (Throwable ignored) {
                            }
                        });
                    }
                } catch (Throwable e) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }, "CleanTvProfileTcpSync");
        t.setDaemon(true);
        t.start();
    }

    private static Set<String> getSavedPeers(Context context) {
        if (context == null) return Collections.emptySet();
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return new HashSet<>(sp.getStringSet(KEY_KNOWN_PEERS, Collections.emptySet()));
    }

    private static void recordPeerIp(Context context, String ip) {
        if (context == null || ip == null || ip.isEmpty()) return;
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> peers = new HashSet<>(sp.getStringSet(KEY_KNOWN_PEERS, Collections.emptySet()));
        if (peers.add(ip)) {
            sp.edit().putStringSet(KEY_KNOWN_PEERS, peers).apply();
        }
    }

    private static String getLocalIpv4Address() {
        try {
            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }
}

