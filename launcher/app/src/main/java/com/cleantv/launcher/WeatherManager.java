package com.cleantv.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class WeatherManager {
    private static final String TAG = "WeatherManager";
    private static final String PREFS_NAME = "CleanTvWeatherPrefs";

    private static final String KEY_LOCATION = "weather_location";
    private static final String KEY_LAT = "weather_lat";
    private static final String KEY_LON = "weather_lon";
    private static final String KEY_TEMP_C = "weather_temp_c";
    private static final String KEY_TEMP_F = "weather_temp_f";
    private static final String KEY_CONDITION = "weather_condition";
    private static final String KEY_ICON = "weather_icon";
    private static final String KEY_TIMESTAMP = "weather_timestamp";

    private static final String KEY_IS_AUTO_LOCATION = "weather_is_auto_location";

    // Default fallback: San Jose, CA
    private static final String DEFAULT_LOCATION = "San Jose, CA";
    private static final double DEFAULT_LAT = 37.3388;
    private static final double DEFAULT_LON = -121.8916;

    public interface WeatherCallback {
        void onWeatherUpdated(String location, String condition, String icon, int tempF, int tempC);
        void onWeatherFailed(String error);
    }

    public interface LocationDetectCallback {
        void onSuccess(String location, double lat, double lon);
        void onError(String error);
    }

    public static class CityResult {
        public final String name;
        public final String admin1;
        public final String country;
        public final double lat;
        public final double lon;

        public CityResult(String name, String admin1, String country, double lat, double lon) {
            this.name = name;
            this.admin1 = admin1;
            this.country = country;
            this.lat = lat;
            this.lon = lon;
        }

        public String getDisplayName() {
            if (admin1 != null && !admin1.isEmpty()) {
                return name + ", " + admin1;
            }
            if (country != null && !country.isEmpty()) {
                return name + ", " + country;
            }
            return name;
        }
    }

    public interface CitySearchCallback {
        void onResults(java.util.List<CityResult> results);
        void onError(String error);
    }

    public static class WeatherInfo {
        public final String location;
        public final String condition;
        public final String icon;
        public final int tempF;
        public final int tempC;

        public WeatherInfo(String location, String condition, String icon, int tempF, int tempC) {
            this.location = location;
            this.condition = condition;
            this.icon = icon;
            this.tempF = tempF;
            this.tempC = tempC;
        }

        public String getFormattedText() {
            return location + " • " + tempF + "°F / " + tempC + "°C • " + condition;
        }
    }

    public static WeatherInfo getCachedWeather(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String location = sp.getString(KEY_LOCATION, DEFAULT_LOCATION);
        if ("Cupertino".equalsIgnoreCase(location)) {
            location = DEFAULT_LOCATION;
        }
        String condition = sp.getString(KEY_CONDITION, "Clear");
        String icon = sp.getString(KEY_ICON, "clear_day");
        int tempF = sp.getInt(KEY_TEMP_F, 68);
        int tempC = sp.getInt(KEY_TEMP_C, 20);
        return new WeatherInfo(location, condition, icon, tempF, tempC);
    }

    public static boolean isAutoLocation(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_IS_AUTO_LOCATION, true);
    }

    public static void setCustomLocation(Context context, String location, double lat, double lon) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_LOCATION, location != null ? location : DEFAULT_LOCATION)
                .putFloat(KEY_LAT, (float) (lat != 0 ? lat : DEFAULT_LAT))
                .putFloat(KEY_LON, (float) (lon != 0 ? lon : DEFAULT_LON))
                .putBoolean(KEY_IS_AUTO_LOCATION, false)
                .apply();
    }

    public static void setAutoLocation(Context context, String location, double lat, double lon) {
        SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sp.edit()
                .putString(KEY_LOCATION, location != null ? location : DEFAULT_LOCATION)
                .putFloat(KEY_LAT, (float) (lat != 0 ? lat : DEFAULT_LAT))
                .putFloat(KEY_LON, (float) (lon != 0 ? lon : DEFAULT_LON))
                .putBoolean(KEY_IS_AUTO_LOCATION, true)
                .apply();
    }

    public static void detectLocationFromNetwork(Context context, LocationDetectCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String locationName = detectLocationInternal(context);
            if (locationName != null) {
                SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                double lat = sp.getFloat(KEY_LAT, (float) DEFAULT_LAT);
                double lon = sp.getFloat(KEY_LON, (float) DEFAULT_LON);
                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess(locationName, lat, lon);
                });
            } else {
                mainHandler.post(() -> {
                    if (callback != null) callback.onError("Network location detection failed");
                });
            }
        }).start();
    }

    public static String detectLocationInternal(Context context) {
        String locationName = null;
        double lat = 0;
        double lon = 0;

        // Priority 1: Android System Wi-Fi Network Location Provider (accurate to local address)
        try {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                Location loc = null;
                try { loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER); } catch (Throwable ignored) {}
                if (loc == null) {
                    try { loc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER); } catch (Throwable ignored) {}
                }
                if (loc == null) {
                    try { loc = lm.getLastKnownLocation("fused"); } catch (Throwable ignored) {}
                }
                if (loc != null && (loc.getLatitude() != 0 || loc.getLongitude() != 0)) {
                    lat = loc.getLatitude();
                    lon = loc.getLongitude();
                    Log.i(TAG, "Device Wi-Fi location provider returned: " + lat + ", " + lon);
                    locationName = reverseGeocode(context, lat, lon);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "LocationManager detection error: " + t.getMessage());
        }

        // Priority 2: BigDataCloud client reverse lookup (reverse geocodes client IP without hardcoded coords)
        if (locationName == null || lat == 0) {
            try {
                String jsonStr = httpGet("https://api.bigdatacloud.net/data/reverse-geocode-client?localityLanguage=en");
                JSONObject obj = new JSONObject(jsonStr);
                double bLat = obj.optDouble("latitude", 0);
                double bLon = obj.optDouble("longitude", 0);
                String locality = obj.optString("locality");
                String city = obj.optString("city");
                String state = obj.optString("principalSubdivisionCode");
                if (state.startsWith("US-")) state = state.substring(3);
                if (state.isEmpty()) state = obj.optString("principalSubdivision");
                String chosen = (!locality.isEmpty()) ? locality : city;
                if (bLat != 0 && bLon != 0 && !chosen.isEmpty()) {
                    lat = bLat;
                    lon = bLon;
                    locationName = !state.isEmpty() ? chosen + ", " + state : chosen;
                }
            } catch (Throwable t) {
                Log.w(TAG, "BigDataCloud IP detection error: " + t.getMessage());
            }
        }

        // Priority 3: ip-api.com
        if (locationName == null || lat == 0) {
            try {
                String jsonStr = httpGet("http://ip-api.com/json/");
                JSONObject obj = new JSONObject(jsonStr);
                if ("success".equalsIgnoreCase(obj.optString("status"))) {
                    String city = obj.optString("city");
                    String region = obj.optString("region");
                    if (region.isEmpty()) region = obj.optString("regionName");
                    lat = obj.optDouble("lat", DEFAULT_LAT);
                    lon = obj.optDouble("lon", DEFAULT_LON);
                    locationName = !region.isEmpty() ? city + ", " + region : city;
                }
            } catch (Throwable t) {
                Log.w(TAG, "ip-api lookup error: " + t.getMessage());
            }
        }

        if (locationName != null && lat != 0 && lon != 0) {
            setAutoLocation(context, locationName, lat, lon);
            return locationName;
        }
        return null;
    }

    private static String reverseGeocode(Context context, double lat, double lon) {
        // Method 1: Native Android Geocoder
        try {
            if (Geocoder.isPresent()) {
                Geocoder gcd = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = gcd.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    String city = addr.getLocality();
                    if (city == null || city.isEmpty()) city = addr.getSubLocality();
                    String state = addr.getAdminArea();
                    if ("California".equalsIgnoreCase(state)) state = "CA";
                    if (city != null && !city.isEmpty()) {
                        return state != null && !state.isEmpty() ? city + ", " + state : city;
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Native Geocoder error: " + t.getMessage());
        }

        // Method 2: Nominatim OpenStreetMap
        try {
            String url = String.format(Locale.US,
                    "https://nominatim.openstreetmap.org/reverse?lat=%.6f&lon=%.6f&format=json",
                    lat, lon);
            String jsonStr = httpGet(url);
            JSONObject obj = new JSONObject(jsonStr);
            JSONObject address = obj.optJSONObject("address");
            if (address != null) {
                String city = address.optString("city");
                if (city.isEmpty()) city = address.optString("town");
                if (city.isEmpty()) city = address.optString("village");
                if (city.isEmpty()) city = address.optString("suburb");
                if (city.isEmpty()) city = address.optString("hamlet");
                String state = address.optString("state");
                if ("California".equalsIgnoreCase(state)) state = "CA";
                if (!city.isEmpty()) {
                    return !state.isEmpty() ? city + ", " + state : city;
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Nominatim reverse error: " + t.getMessage());
        }

        // Method 3: BigDataCloud reverse geocode
        try {
            String url = String.format(Locale.US,
                    "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=%.6f&longitude=%.6f&localityLanguage=en",
                    lat, lon);
            String jsonStr = httpGet(url);
            JSONObject obj = new JSONObject(jsonStr);
            String locality = obj.optString("locality");
            String city = obj.optString("city");
            String state = obj.optString("principalSubdivisionCode");
            if (state.startsWith("US-")) state = state.substring(3);
            if (state.isEmpty()) state = obj.optString("principalSubdivision");
            String chosen = (!locality.isEmpty()) ? locality : city;
            if (!chosen.isEmpty()) {
                return !state.isEmpty() ? chosen + ", " + state : chosen;
            }
        } catch (Throwable t) {
            Log.w(TAG, "BigDataCloud reverse error: " + t.getMessage());
        }

        return null;
    }

    public static void searchCityAsync(String query, CitySearchCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            try {
                String encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8");
                String urlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + encoded + "&count=6&language=en&format=json";
                String jsonStr = httpGet(urlStr);
                JSONObject root = new JSONObject(jsonStr);
                org.json.JSONArray array = root.optJSONArray("results");
                java.util.List<CityResult> list = new java.util.ArrayList<>();
                if (array != null) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        String name = obj.getString("name");
                        String admin1 = obj.optString("admin1", "");
                        String country = obj.optString("country", "");
                        double lat = obj.getDouble("latitude");
                        double lon = obj.getDouble("longitude");
                        list.add(new CityResult(name, admin1, country, lat, lon));
                    }
                }
                mainHandler.post(() -> {
                    if (callback != null) callback.onResults(list);
                });
            } catch (Throwable t) {
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(t.getMessage());
                });
            }
        }).start();
    }

    public static void fetchWeatherAsync(Context context, WeatherCallback callback) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        new Thread(() -> {
            try {
                SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                boolean isAuto = sp.getBoolean(KEY_IS_AUTO_LOCATION, true);

                // If in auto-location mode, detect the real Wi-Fi network location
                if (isAuto && (!sp.contains(KEY_LOCATION) || DEFAULT_LOCATION.equals(sp.getString(KEY_LOCATION, null)) || "Cupertino".equalsIgnoreCase(sp.getString(KEY_LOCATION, null)))) {
                    detectLocationInternal(context);
                }

                String location = sp.getString(KEY_LOCATION, DEFAULT_LOCATION);
                double lat = sp.getFloat(KEY_LAT, (float) DEFAULT_LAT);
                double lon = sp.getFloat(KEY_LON, (float) DEFAULT_LON);

                // Fetch weather from Open-Meteo API
                String weatherUrl = String.format(java.util.Locale.US,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current_weather=true",
                        lat, lon);
                String weatherJson = httpGet(weatherUrl);
                JSONObject root = new JSONObject(weatherJson);
                JSONObject current = root.getJSONObject("current_weather");

                double tempDoubleC = current.getDouble("temperature");
                int code = current.getInt("weathercode");
                int isDay = current.optInt("is_day", 1);

                int tempC = (int) Math.round(tempDoubleC);
                int tempF = (int) Math.round(tempDoubleC * 1.8 + 32.0);

                String[] conditionAndIcon = decodeWeatherCode(code, isDay);
                String condition = conditionAndIcon[0];
                String icon = conditionAndIcon[1];

                // Cache results
                sp.edit()
                        .putString(KEY_LOCATION, location)
                        .putString(KEY_CONDITION, condition)
                        .putString(KEY_ICON, icon)
                        .putInt(KEY_TEMP_F, tempF)
                        .putInt(KEY_TEMP_C, tempC)
                        .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                        .apply();

                String finalLocation = location;
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onWeatherUpdated(finalLocation, condition, icon, tempF, tempC);
                    }
                });

            } catch (Throwable t) {
                Log.e(TAG, "Failed to fetch weather: " + t.getMessage(), t);
                mainHandler.post(() -> {
                    if (callback != null) {
                        callback.onWeatherFailed(t.getMessage());
                    }
                });
            }
        }).start();
    }

    private static String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(6000);
        conn.setRequestProperty("User-Agent", "CleanTvLauncher/1.0");

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new Exception("HTTP error code: " + code);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        conn.disconnect();
        return sb.toString();
    }

    public static int getWeatherDrawableRes(String icon) {
        if (icon == null) return R.drawable.ic_weather_partly_cloudy;
        if (icon.equals("clear_day") || icon.contains("☀️") || icon.contains("Clear")) {
            return R.drawable.ic_weather_clear_day;
        }
        if (icon.equals("clear_night") || icon.contains("🌙")) {
            return R.drawable.ic_weather_clear_night;
        }
        if (icon.equals("cloudy") || icon.contains("☁️")) {
            return R.drawable.ic_weather_cloudy;
        }
        if (icon.equals("partly_cloudy") || icon.contains("🌤") || icon.contains("⛅")) {
            return R.drawable.ic_weather_partly_cloudy;
        }
        if (icon.equals("rain") || icon.contains("🌧") || icon.contains("🌦")) {
            return R.drawable.ic_weather_rain;
        }
        if (icon.equals("snow") || icon.contains("❄") || icon.contains("🌨")) {
            return R.drawable.ic_weather_snow;
        }
        if (icon.equals("thunder") || icon.contains("⛈")) {
            return R.drawable.ic_weather_thunder;
        }
        if (icon.equals("fog") || icon.contains("🌫")) {
            return R.drawable.ic_weather_fog;
        }
        return R.drawable.ic_weather_partly_cloudy;
    }

    private static String[] decodeWeatherCode(int code, int isDay) {
        boolean night = (isDay == 0);
        switch (code) {
            case 0:
                return night ? new String[]{"Clear Night", "clear_night"} : new String[]{"Clear", "clear_day"};
            case 1:
                return night ? new String[]{"Mainly Clear", "clear_night"} : new String[]{"Mainly Clear", "partly_cloudy"};
            case 2:
                return night ? new String[]{"Partly Cloudy", "clear_night"} : new String[]{"Partly Cloudy", "partly_cloudy"};
            case 3:
                return new String[]{"Overcast", "cloudy"};
            case 45:
            case 48:
                return new String[]{"Foggy", "fog"};
            case 51:
            case 53:
            case 55:
                return night ? new String[]{"Drizzle", "rain"} : new String[]{"Drizzle", "rain"};
            case 61:
            case 63:
            case 65:
                return new String[]{"Rain", "rain"};
            case 66:
            case 67:
                return new String[]{"Freezing Rain", "rain"};
            case 71:
            case 73:
            case 75:
            case 77:
                return new String[]{"Snow", "snow"};
            case 80:
            case 81:
            case 82:
                return night ? new String[]{"Rain Showers", "rain"} : new String[]{"Rain Showers", "rain"};
            case 85:
            case 86:
                return new String[]{"Snow Showers", "snow"};
            case 95:
            case 96:
            case 99:
                return new String[]{"Thunderstorm", "thunder"};
            default:
                return night ? new String[]{"Clear Night", "clear_night"} : new String[]{"Fair", "partly_cloudy"};
        }
    }
}
