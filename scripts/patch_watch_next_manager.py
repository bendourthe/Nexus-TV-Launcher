import re

path = r'launcher/app/src/main/java/com/cleantv/launcher/WatchNextManager.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Load generated AVATAR_CHARACTERS
with open(r'launcher/app/src/main/res/avatar_characters_generated.txt', 'r', encoding='utf-8') as f:
    new_avatar_code = f.read()

# Replace AVATAR_CHARACTERS definition
avatar_regex = r'public static final List<AvatarCharacter> AVATAR_CHARACTERS = Arrays\.asList\([\s\S]*?\n    \);'
if not re.search(avatar_regex, content):
    raise Exception("Could not find AVATAR_CHARACTERS regex in WatchNextManager.java")

content = re.sub(avatar_regex, new_avatar_code.strip(), content, count=1)

# 2. Add isPlausibleShowTitle method
plausible_title_method = '''
    public static boolean isPlausibleShowTitle(String text) {
        if (text == null) return false;
        String t = text.trim();
        if (t.length() < 2 || t.length() > 65) return false;
        String lower = t.toLowerCase();
        if (lower.contains("remove from") || lower.contains("add to") || lower.contains("continue watching")
                || lower.contains("watch next") || lower.contains("resume") || lower.contains("dismiss")
                || lower.contains("option") || lower.contains("manage") || lower.contains("clear")
                || lower.contains("delete") || lower.contains("details") || lower.contains("episodes")
                || lower.contains("more info") || lower.contains("see all") || lower.contains("switch")
                || lower.contains("restart") || lower.contains("suggested") || lower.contains("extras")
                || lower.contains("audio description") || lower.contains("trailer") || lower.contains("search by")
                || lower.contains("sign in") || lower.contains("sign out") || lower.contains("loading")) {
            return false;
        }
        return true;
    }
'''

if "public static boolean isPlausibleShowTitle" not in content:
    # Insert right before saveDynamicWatchlistShow
    target = "public static void saveDynamicWatchlistShow("
    content = content.replace(target, plausible_title_method + "\n    " + target, 1)

# 3. Update saveDynamicWatchlistShow validation
old_save = "if (context == null || title == null || title.trim().isEmpty()) return;"
new_save = "if (context == null || title == null || !isPlausibleShowTitle(title)) return;"
content = content.replace(old_save, new_save)

# 4. In loadAllWatchNext, filter with isPlausibleShowTitle
old_custom_loop = '''        List<WatchNextItem> customItems = loadCustomItems(context, appMap);
        for (WatchNextItem ci : customItems) {
            if (!dismissed.contains(ci.id) && isWatchlistApp(ci.packageName) && !isYouTube(ci.packageName, ci.appBadge, ci.showTitle)) {
                rawList.add(ci);
            }
        }'''
new_custom_loop = '''        List<WatchNextItem> customItems = loadCustomItems(context, appMap);
        for (WatchNextItem ci : customItems) {
            if (!dismissed.contains(ci.id) && isPlausibleShowTitle(ci.showTitle) && isWatchlistApp(ci.packageName) && !isYouTube(ci.packageName, ci.appBadge, ci.showTitle)) {
                rawList.add(ci);
            }
        }'''
content = content.replace(old_custom_loop, new_custom_loop)

# 5. In loadCustomItems, filter with isPlausibleShowTitle and clean corrupted items
old_custom_items = '''            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.optString("id");
                String pkg = obj.optString("packageName");
                String badge = obj.optString("appBadge");
                String title = obj.optString("showTitle");

                if (isYouTube(pkg, badge, title)) continue;'''

new_custom_items = '''            JSONArray arr = new JSONArray(raw);
            JSONArray cleanedArr = new JSONArray();
            boolean hadCorrupted = false;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id = obj.optString("id");
                String pkg = obj.optString("packageName");
                String badge = obj.optString("appBadge");
                String title = obj.optString("showTitle");

                if (!isPlausibleShowTitle(title)) {
                    hadCorrupted = true;
                    continue;
                }
                if (isYouTube(pkg, badge, title)) {
                    hadCorrupted = true;
                    continue;
                }
                cleanedArr.put(obj);'''
content = content.replace(old_custom_items, new_custom_items)

# Also persist cleanedArr if hadCorrupted
old_list_end = '''            }
        } catch (Throwable t) {
            Log.e(TAG, "loadCustomItems error", t);
        }'''
new_list_end = '''            }
            if (hadCorrupted) {
                sp.edit().putString(storageKey, cleanedArr.toString()).apply();
            }
        } catch (Throwable t) {
            Log.e(TAG, "loadCustomItems error", t);
        }'''
content = content.replace(old_list_end, new_list_end)

# 6. Update buildBenAuthenticShowsSeed
old_s1 = '''            // 1. VisionQuest (Disney+ Ben Profile)
            JSONObject s1 = new JSONObject();
            s1.put("id", "disney_ben_visionquest");
            s1.put("packageName", disneyPkg);
            s1.put("appBadge", "DISNEY+");
            s1.put("showTitle", "VisionQuest");
            s1.put("episodeInfo", "S1:E3 Reboot");
            s1.put("progressPercent", 45);
            s1.put("brandColor", Color.parseColor("#1B45F5"));
            s1.put("intentUri", resolveDefaultIntentUri(disneyPkg, "VisionQuest", "S1:E3 Reboot"));
            arr.put(s1);

            // 2. Bleach: Thousand-Year Blood War (Disney+ Ben Profile)
            JSONObject s2 = new JSONObject();
            s2.put("id", "disney_ben_bleach");
            s2.put("packageName", disneyPkg);
            s2.put("appBadge", "DISNEY+");
            s2.put("showTitle", "Bleach: Thousand-Year Blood War");
            s2.put("episodeInfo", "S4:E9 (Sub) THE BLADE");
            s2.put("progressPercent", 80);
            s2.put("brandColor", Color.parseColor("#1B45F5"));
            s2.put("intentUri", resolveDefaultIntentUri(disneyPkg, "Bleach: Thousand-Year Blood War", "S4:E9 (Sub) THE BLADE"));
            arr.put(s2);'''

new_s1 = '''            // 1. VisionQuest (Disney+ Ben Profile)
            JSONObject s1 = new JSONObject();
            s1.put("id", "disney_ben_visionquest");
            s1.put("packageName", disneyPkg);
            s1.put("appBadge", "DISNEY+");
            s1.put("showTitle", "VisionQuest");
            s1.put("episodeInfo", "S1:E4 Malware");
            s1.put("progressPercent", 45);
            s1.put("brandColor", Color.parseColor("#1B45F5"));
            s1.put("intentUri", "https://www.disneyplus.com/series/visionquest");
            arr.put(s1);

            // 2. Bleach: Thousand-Year Blood War (Disney+ Ben Profile)
            JSONObject s2 = new JSONObject();
            s2.put("id", "disney_ben_bleach");
            s2.put("packageName", disneyPkg);
            s2.put("appBadge", "DISNEY+");
            s2.put("showTitle", "Bleach: Thousand-Year Blood War");
            s2.put("episodeInfo", "S4:E9 (Sub) THE BLADE");
            s2.put("progressPercent", 80);
            s2.put("brandColor", Color.parseColor("#1B45F5"));
            s2.put("intentUri", "https://www.disneyplus.com/series/bleach-thousand-year-blood-war");
            arr.put(s2);'''
content = content.replace(old_s1, new_s1)

# 7. Update resolveDefaultIntentUri for Disney+
old_disney = '''        // Disney+
        if (p.contains("disney")) {
            return "https://www.disneyplus.com/search?q=" + encodedTitle;
        }'''

new_disney = '''        // Disney+
        if (p.contains("disney")) {
            String lowerTitle = title.trim().toLowerCase();
            if (lowerTitle.contains("visionquest") || lowerTitle.contains("vision quest")) {
                return "https://www.disneyplus.com/series/visionquest";
            }
            if (lowerTitle.contains("bleach")) {
                return "https://www.disneyplus.com/series/bleach-thousand-year-blood-war";
            }
            if (lowerTitle.contains("star wars") || lowerTitle.contains("mandalorian")) {
                return "https://www.disneyplus.com/series/the-mandalorian";
            }
            if (lowerTitle.contains("loki")) {
                return "https://www.disneyplus.com/series/loki";
            }
            if (lowerTitle.contains("percy jackson")) {
                return "https://www.disneyplus.com/series/percy-jackson-and-the-olympians";
            }
            String slug = lowerTitle.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            if (!slug.isEmpty()) {
                return "https://www.disneyplus.com/series/" + slug;
            }
            return "disneyplus://disneyplus.com/search?query=" + encodedTitle;
        }'''
content = content.replace(old_disney, new_disney)

# 8. In buildPlaybackIntent, attach intent extras
old_build_intent = '''                if (item.packageName != null && !item.packageName.isEmpty()) {
                    parsed.setPackage(item.packageName);
                }
                parsed.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);'''

new_build_intent = '''                if (item.packageName != null && !item.packageName.isEmpty()) {
                    parsed.setPackage(item.packageName);
                }
                parsed.putExtra("query", item.showTitle);
                parsed.putExtra("q", item.showTitle);
                parsed.putExtra("searchTerm", item.showTitle);
                parsed.putExtra(SearchManager.QUERY, item.showTitle);
                parsed.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);'''
content = content.replace(old_build_intent, new_build_intent)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Successfully patched WatchNextManager.java!")
