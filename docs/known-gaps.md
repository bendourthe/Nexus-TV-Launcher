# Known Gaps & Architectural Analysis: Cross-App Continue Watching on Android TV & Fire OS

## 1. Executive Summary

This document details the technical evaluation, operating system boundaries, and architectural findings regarding real-time, cross-app "Continue Watching" synchronization on Android TV (Hisense Google TV) and Amazon Fire OS (Fire TV Stick 4K Max).

Per product requirements, Continue Watching must reflect 100% live, authoritative in-progress media from third-party streaming providers (e.g., Netflix, Disney+, HBO Max, Apple TV, Prime Video, Peacock) without:
1. Static seed or placeholder data.
2. Visible automated foreground app switching or screen scraping ("cheating").

Because native Android OS security boundaries and commercial streaming application architectures prevent background third-party synchronization, the Continue Watching shelf has been cleanly removed from the Home screen. This preserves a minimalist, ultra-responsive launcher experience while cataloging potential future resolution vectors.

---

## 2. Technical Evaluation & Operating System Boundaries

### A. Android App Sandboxing & File System Isolation
Android enforces strict UID-based process and filesystem sandboxing. Each application executes within its own Linux process and private directory (`/data/data/<package_name>/`). A custom launcher running with standard application permissions cannot inspect or read SQLite databases, shared preferences, or cache directories of installed third-party apps.

### B. The Android TV Watch Next EPG Provider (`content://android.media.tv/watch_next_program`)
Android TV defines a standard system ContentProvider (`TvContract.WatchNextPrograms`) designed for cross-app media discovery.
- **Provider Status on Google TV:** Google TV exposes the content URI, but major streaming platforms (notably Netflix) refuse to publish playback records to Google's public provider to protect proprietary user engagement metrics and avoid feeding data to Google's recommendation engine.
- **Provider Status on Fire OS:** Amazon replaces the Android TV EPG framework with its own proprietary Amazon Home catalog. Querying `content://android.media.tv/watch_next_program` on Fire OS returns zero rows (`No result found.`), rendering the standard TV contract completely non-functional.

### C. Accessibility Service Node Traversal Limits
An `AccessibilityService` has elevated permissions to inspect on-screen UI node hierarchies (`AccessibilityNodeInfo`).
- **Foreground Restriction:** Accessibility services can only inspect views that are actively rendered in the active foreground window.
- **Background Inability:** An accessibility service cannot inspect off-screen, minimized, or background activities. To harvest an app's Continue Watching shelf via accessibility, the launcher would have to programmatically launch each streaming app, wait for network rendering, navigate the remote focus, scrape the titles, and force-kill the app. This creates intrusive screen flashing and disrupts the user experience.

### D. MediaSession & Notification Listener Bounds
The Android `NotificationListenerService` and `MediaSessionManager` permit observing media playback events.
- **Transient State:** Playback notifications and active media sessions only exist while an item is actively playing or paused in memory.
- **Historical Gaps:** Once a streaming app is closed or killed by low-memory killer (LMK), its media notification is destroyed. There is no historical query API to determine what was previously in progress without active playback.

### E. Cloud APIs & Authentication Barriers
Each commercial streaming provider manages watch history on proprietary cloud servers authenticated via private session cookies, Widevine DRM device tokens, and encrypted GraphQL/REST APIs. Accessing these services out-of-band would require:
- Storing decrypted user credentials inside the launcher.
- Re-implementing reverse-engineered private APIs subject to immediate breakage and rate limits.
- Bypassing 2FA and bot-detection mechanisms.

---

## 3. Resolution & Current Implementation

To uphold the core principles of the Clean TV Launcher (performance, minimalism, visual integrity, and zero artificial placeholders), the Continue Watching shelf has been retired from the active interface:
1. **Layout Simplification:** `containerContinueWatching` is set to `View.GONE` in `launcher/app/src/main/res/layout/activity_main.xml`.
2. **Controller Bypass:** `populateContinueWatchingShelf()` in `MainActivity.java` clears internal collections and exits immediately, eliminating background content provider observer load and periodic polling.
3. **Smooth Vertical D-Pad Flow:** Remote D-pad navigation flows seamlessly between the Top Menu Pills, the 6-app Dock, and the Alphabetical "All Apps" phone grid.
4. **Dock Channel Banners:** Custom high-definition 16:9 full-bleed banners have been implemented for all dock streaming channels (including Peacock and Apple TV) to ensure visual perfection.

---

## 4. Future Exploration Vectors for Next-Generation Implementations

Future engineering efforts may evaluate the following potential vectors if live synchronization is revisited:

| Vector | Architecture | Prerequisites / Trade-offs |
|---|---|---|
| **Trakt.tv / Simkl Scrobbler Integration** | Cloud Webhook / OAuth Sync | Requires the user to link their streaming accounts to a third-party tracking service (e.g., Trakt), which exposes an official REST API with authorized watch history. |
| **System-Level Privileged App (`priv-app`)** | Root / System ROM Placement | On rooted hardware, placing Clean TV Launcher in `/system/priv-app/` allows reading system logs (`logcat -b events`) and inter-app broadcasts, though streaming app databases remain encrypted. |
| **Local Network Packet Inspection** | Local VPN / MitM Proxy | Running a local pseudo-VPN service on Android to intercept telemetry pings. Non-viable without installing custom CA root certificates on the TV, which modern Android and streaming DRM strictly reject. |
