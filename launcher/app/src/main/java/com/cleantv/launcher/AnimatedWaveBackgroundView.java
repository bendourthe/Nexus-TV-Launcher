package com.cleantv.launcher;







import android.animation.ValueAnimator;



import android.content.Context;



import android.content.res.AssetManager;



import android.graphics.Bitmap;



import android.graphics.BitmapFactory;



import android.graphics.Canvas;



import android.graphics.Color;



import android.graphics.ColorMatrix;



import android.graphics.ColorMatrixColorFilter;



import android.graphics.LinearGradient;



import android.graphics.Paint;



import android.graphics.Rect;



import android.graphics.RectF;



import android.graphics.Shader;



import android.os.Handler;



import android.os.Looper;



import android.util.AttributeSet;



import android.util.Log;



import android.view.View;



import android.view.animation.AccelerateDecelerateInterpolator;



import android.view.animation.LinearInterpolator;







import java.io.InputStream;



import java.util.ArrayList;
import java.util.Arrays;



import java.util.Collections;



import java.util.List;



import java.util.concurrent.ExecutorService;



import java.util.concurrent.Executors;







public class AnimatedWaveBackgroundView extends View {

    private static final String[] NATURE_WALLPAPERS = {
        "wallpapers/nature/nature_01.jpg", "wallpapers/nature/nature_02.jpg", "wallpapers/nature/nature_03.jpg",
        "wallpapers/nature/nature_04.jpg", "wallpapers/nature/nature_05.jpg", "wallpapers/nature/nature_06.jpg",
        "wallpapers/nature/nature_07.jpg", "wallpapers/nature/nature_08.jpg", "wallpapers/nature/nature_09.jpg",
        "wallpapers/nature/nature_10.jpg", "wallpapers/nature/nature_11.jpg", "wallpapers/nature/nature_12.jpg",
        "wallpapers/nature/nature_13.jpg", "wallpapers/nature/nature_14.jpg", "wallpapers/nature/nature_15.jpg",
        "wallpapers/nature/nature_16.jpg", "wallpapers/nature/nature_17.jpg", "wallpapers/nature/nature_18.jpg",
        "wallpapers/nature/nature_19.jpg", "wallpapers/nature/nature_20.jpg", "wallpapers/nature/nature_21.jpg",
        "wallpapers/nature/nature_22.jpg", "wallpapers/nature/nature_23.jpg", "wallpapers/nature/nature_24.jpg",
        "wallpapers/nature/nature_25.jpg", "wallpapers/nature/nature_26.jpg", "wallpapers/nature/nature_27.jpg",
        "wallpapers/nature/nature_28.jpg", "wallpapers/nature/nature_29.jpg", "wallpapers/nature/nature_30.jpg",
        "wallpapers/nature/nature_31.jpg", "wallpapers/nature/nature_32.jpg", "wallpapers/nature/nature_33.jpg",
        "wallpapers/nature/nature_34.jpg", "wallpapers/nature/nature_35.jpg", "wallpapers/nature/nature_36.jpg",
        "wallpapers/nature/nature_37.jpg", "wallpapers/nature/nature_38.jpg", "wallpapers/nature/nature_39.jpg",
        "wallpapers/nature/nature_40.jpg", "wallpapers/nature/nature_41.jpg", "wallpapers/nature/nature_42.jpg",
        "wallpapers/nature/nature_43.jpg", "wallpapers/nature/nature_44.jpg", "wallpapers/nature/nature_45.jpg",
        "wallpapers/nature/nature_46.jpg", "wallpapers/nature/nature_47.jpg", "wallpapers/nature/nature_48.jpg",
        "wallpapers/nature/nature_49.jpg", "wallpapers/nature/nature_50.jpg", "wallpapers/nature/nature_51.jpg",
        "wallpapers/nature/nature_52.jpg", "wallpapers/nature/nature_53.jpg", "wallpapers/nature/nature_54.jpg",
        "wallpapers/nature/nature_55.jpg"
    };

    private static final String[] SPACE_WALLPAPERS = {
        "wallpapers/space/space_01.jpg", "wallpapers/space/space_02.jpg", "wallpapers/space/space_03.jpg",
        "wallpapers/space/space_04.jpg", "wallpapers/space/space_05.jpg", "wallpapers/space/space_06.jpg",
        "wallpapers/space/space_07.jpg", "wallpapers/space/space_08.jpg", "wallpapers/space/space_09.jpg",
        "wallpapers/space/space_10.jpg", "wallpapers/space/space_11.jpg", "wallpapers/space/space_12.jpg",
        "wallpapers/space/space_13.jpg", "wallpapers/space/space_14.jpg", "wallpapers/space/space_15.jpg",
        "wallpapers/space/space_16.jpg", "wallpapers/space/space_17.jpg", "wallpapers/space/space_18.jpg",
        "wallpapers/space/space_19.jpg", "wallpapers/space/space_20.jpg", "wallpapers/space/space_21.jpg",
        "wallpapers/space/space_22.jpg", "wallpapers/space/space_23.jpg", "wallpapers/space/space_24.jpg",
        "wallpapers/space/space_25.jpg", "wallpapers/space/space_26.jpg", "wallpapers/space/space_27.jpg"
    };

    private static final String[] CITIES_WALLPAPERS = {
        "wallpapers/cities/cities_01.jpg", "wallpapers/cities/cities_02.jpg", "wallpapers/cities/cities_03.jpg",
        "wallpapers/cities/cities_04.jpg", "wallpapers/cities/cities_05.jpg", "wallpapers/cities/cities_06.jpg",
        "wallpapers/cities/cities_07.jpg", "wallpapers/cities/cities_08.jpg", "wallpapers/cities/cities_09.jpg",
        "wallpapers/cities/cities_10.jpg", "wallpapers/cities/cities_11.jpg", "wallpapers/cities/cities_12.jpg",
        "wallpapers/cities/cities_13.jpg", "wallpapers/cities/cities_14.jpg", "wallpapers/cities/cities_15.jpg",
        "wallpapers/cities/cities_16.jpg", "wallpapers/cities/cities_17.jpg", "wallpapers/cities/cities_18.jpg",
        "wallpapers/cities/cities_19.jpg", "wallpapers/cities/cities_20.jpg", "wallpapers/cities/cities_21.jpg",
        "wallpapers/cities/cities_22.jpg", "wallpapers/cities/cities_23.jpg", "wallpapers/cities/cities_24.jpg",
        "wallpapers/cities/cities_25.jpg", "wallpapers/cities/cities_26.jpg", "wallpapers/cities/cities_27.jpg",
        "wallpapers/cities/cities_28.jpg", "wallpapers/cities/cities_29.jpg", "wallpapers/cities/cities_30.jpg"
    };

    private static final String TAG = "AnimatedBg";







    public enum ThemeCategory {



        DYNAMIC_ABSTRACT("Dynamic Abstract", "Hypnotic fluid mesh waves with vibrant color shifts"),



        NATURE("Nature Landscapes", "Slideshow of breathtaking 4K mountain, lake, and forest vistas"),



        SPACE("Deep Space & Cosmos", "Slideshow of breathtaking 4K nebulae, galaxies, and planetary astrophotography"),



        CITIES("World City Skylines", "Slideshow of breathtaking 4K twilight and night cityscapes");







        public final String title;



        public final String description;







        ThemeCategory(String title, String description) {



            this.title = title;



            this.description = description;



        }



    }







    public static final long[] ROTATION_INTERVALS = new long[]{



            60_000L,        // 1 Minute



            300_000L,       // 5 Minutes (Default)



            900_000L,       // 15 Minutes



            1_800_000L,     // 30 Minutes



            3_600_000L,     // 1 Hour



            0L              // Static / Off



    };







    public static final String[] INTERVAL_LABELS = new String[]{



            "1 Minute", "5 Minutes (Default)", "15 Minutes", "30 Minutes", "1 Hour", "Off (Static)"



    };







    public enum AbstractShape {



        SILK_RIBBONS("Liquid Silk Wave"),



        AURORA_CURTAINS("Cascading Liquid Stream"),



        COSMIC_VORTEX("Deep Liquid Surge"),



        GEOMETRIC_PRISMS("Oceanic Liquid Drift"),



        TOPOGRAPHIC_DUNES("Warm Fluid Contours"),



        CYBER_SYNTH_GRID("Concentric Fluid Ripples");







        public final String displayName;







        AbstractShape(String name) {



            this.displayName = name;



        }



    }







    public static class ThemeScheme {



        public final String name;



        public final AbstractShape shape;



        public final float hueShift;



        public final float satMultiplier;



        public final int accentColor;



        public final int secondaryColor;



        public final float waveAmplitude;







        public ThemeScheme(String name, AbstractShape shape, float hue, float sat, int accent, int secondary, float amp) {



            this.name = name;



            this.shape = shape;



            this.hueShift = hue;



            this.satMultiplier = sat;



            this.accentColor = accent;



            this.secondaryColor = secondary;



            this.waveAmplitude = amp;



        }



    }







    public static final ThemeScheme[] THEMES = new ThemeScheme[]{



            new ThemeScheme("Liquid Silk (Obsidian Violet)", AbstractShape.SILK_RIBBONS,



                    0f, 1.0f, Color.parseColor("#B388FF"), Color.parseColor("#7C4DFF"), 1.0f),



            new ThemeScheme("Cascading Stream (Emerald Green)", AbstractShape.AURORA_CURTAINS,



                    210f, 1.30f, Color.parseColor("#00F5A0"), Color.parseColor("#00D9F9"), 1.0f),



            new ThemeScheme("Liquid Surge (Crimson Ember)", AbstractShape.COSMIC_VORTEX,



                    60f, 1.35f, Color.parseColor("#FF2A55"), Color.parseColor("#FF7A00"), 1.0f),



            new ThemeScheme("Oceanic Drift (Sapphire Blue)", AbstractShape.GEOMETRIC_PRISMS,



                    310f, 1.35f, Color.parseColor("#00A6FF"), Color.parseColor("#0051FF"), 1.05f),



            new ThemeScheme("Fluid Contours (Solar Amber)", AbstractShape.TOPOGRAPHIC_DUNES,



                    110f, 1.30f, Color.parseColor("#FFB703"), Color.parseColor("#FB8500"), 1.0f),



            new ThemeScheme("Fluid Ripples (Electric Magenta)", AbstractShape.CYBER_SYNTH_GRID,



                    340f, 1.35f, Color.parseColor("#FF007F"), Color.parseColor("#9D4EDD"), 1.0f)



    };







    // Mesh deformation for Abstract Theme



    private static final int MESH_WIDTH = 24;



    private static final int MESH_HEIGHT = 16;



    private static final int VERTEX_COUNT = (MESH_WIDTH + 1) * (MESH_HEIGHT + 1);







    private final float[] origVerts = new float[VERTEX_COUNT * 2];



    private final float[] animVerts = new float[VERTEX_COUNT * 2];







    private Bitmap rawWallpaper;



    private Bitmap scaledWallpaper;



    private final Paint wallpaperPaint = new Paint(Paint.FILTER_BITMAP_FLAG);







    private ValueAnimator continuousAnimator;



    private ValueAnimator transitionAnimator;



    private float phase = 0f;







    // Abstract theme state



    private int currentThemeIndex = 0;



    private int targetThemeIndex = 0;



    private float curHue = THEMES[0].hueShift;



    private float curSat = THEMES[0].satMultiplier;



    private int curAccent = THEMES[0].accentColor;



    private int curSecondary = THEMES[0].secondaryColor;



    private float curAmp = THEMES[0].waveAmplitude;







    private float prevHue = curHue;



    private float prevSat = curSat;



    private int prevAccent = curAccent;



    private int prevSecondary = curSecondary;



    private float prevAmp = curAmp;



    private float themeTransitionProgress = 1f;







    // Mode & Category



    private ThemeCategory currentCategory = ThemeCategory.DYNAMIC_ABSTRACT;







    // Wallpaper Photo Collections



    private final List<String> natureWallpapers = new ArrayList<>();



    private final List<String> spaceWallpapers = new ArrayList<>();



    private final List<String> citiesWallpapers = new ArrayList<>();



    private int currentPhotoIndex = 0;







    private Bitmap currentPhotoBitmap = null;



    private Bitmap prevPhotoBitmap = null;



    private float photoCrossfadeProgress = 1.0f;



    private ValueAnimator photoCrossfadeAnim;







    private final Paint photoPaint = new Paint(Paint.FILTER_BITMAP_FLAG);



    private final Paint prevPhotoPaint = new Paint(Paint.FILTER_BITMAP_FLAG);



    private final Paint overlayPaint = new Paint();



    private Paint bottomGradientPaint;



    private Paint topGradientPaint;







    // Slideshow Timing



    public static final long DEFAULT_ROTATION_INTERVAL_MS = 300000L; // 5 minutes



    private long rotationIntervalMs = DEFAULT_ROTATION_INTERVAL_MS;



    private boolean autoRotateEnabled = true;







    private final Handler mainHandler = new Handler(Looper.getMainLooper());



    private final ExecutorService decodeExecutor = Executors.newSingleThreadExecutor();







    private final Runnable autoRotateRunnable = new Runnable() {



        @Override



        public void run() {



            if (autoRotateEnabled && rotationIntervalMs > 0) {



                cycleToNextTheme();



                mainHandler.postDelayed(this, rotationIntervalMs);



            }



        }



    };







    public AnimatedWaveBackgroundView(Context context) {



        super(context);



        init();



    }







    public AnimatedWaveBackgroundView(Context context, AttributeSet attrs) {



        super(context, attrs);



        init();



    }







    public AnimatedWaveBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {



        super(context, attrs, defStyleAttr);



        init();



    }







    private void init() {



        try {



            rawWallpaper = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper);



        } catch (Throwable ignored) {



        }







        discoverPhotoWallpapers();







        continuousAnimator = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2.0));



        continuousAnimator.setDuration(24000);



        continuousAnimator.setRepeatCount(ValueAnimator.INFINITE);



        continuousAnimator.setRepeatMode(ValueAnimator.RESTART);



        continuousAnimator.setInterpolator(new LinearInterpolator());



        continuousAnimator.addUpdateListener(anim -> {



            phase = (float) anim.getAnimatedValue();



            if (currentCategory == ThemeCategory.DYNAMIC_ABSTRACT) {



                updateMesh();



            }



            invalidate();



        });



        continuousAnimator.start();







        scheduleNextRotation();



    }







    public void discoverPhotoWallpapers() {

        AssetManager am = getContext().getAssets();

        natureWallpapers.clear();

        spaceWallpapers.clear();

        citiesWallpapers.clear();



        scanAssetCategory(am, "wallpapers/nature", natureWallpapers);

        scanAssetCategory(am, "wallpapers/space", spaceWallpapers);

        scanAssetCategory(am, "wallpapers/cities", citiesWallpapers);



        // Fallback to static manifest if dynamic scanning returns 0 (common on TV APKs)

        if (natureWallpapers.isEmpty()) {

            natureWallpapers.addAll(Arrays.asList(NATURE_WALLPAPERS));

        }

        if (spaceWallpapers.isEmpty()) {

            spaceWallpapers.addAll(Arrays.asList(SPACE_WALLPAPERS));

        }

        if (citiesWallpapers.isEmpty()) {

            citiesWallpapers.addAll(Arrays.asList(CITIES_WALLPAPERS));

        }



        Log.i(TAG, "Wallpapers discovered - Nature: " + natureWallpapers.size()

                + ", Space: " + spaceWallpapers.size() + ", Cities: " + citiesWallpapers.size());

    }



    private void scanAssetCategory(AssetManager am, String folder, List<String> target) {

        try {

            String[] files = am.list(folder);

            if (files != null && files.length > 0) {

                for (String f : files) {

                    String lower = f.toLowerCase();

                    if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {

                        target.add(folder + "/" + f);

                    }

                }

                Collections.sort(target);

            }

        } catch (Throwable t) {

            Log.w(TAG, "Failed scanning " + folder + ": " + t.getMessage());

        }

    }



    public ThemeCategory getCategory() {



        return currentCategory;



    }







    public void setCategory(ThemeCategory category) {



        if (category == null) category = ThemeCategory.DYNAMIC_ABSTRACT;



        this.currentCategory = category;







        int w = getWidth();



        int h = getHeight();







        if (category == ThemeCategory.DYNAMIC_ABSTRACT) {



            if (scaledWallpaper == null && rawWallpaper != null && w > 0 && h > 0) {



                initMesh(w, h);



            }



            invalidate();



        } else {



            currentPhotoIndex = 0;



            if (w > 0 && h > 0) {



                loadPhotoAsync(category, currentPhotoIndex, true);



            }



        }



        rescheduleRotation();



    }







    public void setCategoryByName(String name) {



        if (name == null) return;



        for (ThemeCategory c : ThemeCategory.values()) {



            if (c.name().equalsIgnoreCase(name)) {



                setCategory(c);



                return;



            }



        }



    }







    public List<String> getListForCategory(ThemeCategory cat) {



        switch (cat) {



            case NATURE: return natureWallpapers;



            case SPACE: return spaceWallpapers;



            case CITIES: return citiesWallpapers;



            default: return Collections.emptyList();



        }



    }







    public int getCurrentWallpaperCount() {



        List<String> list = getListForCategory(currentCategory);



        return list != null ? list.size() : 0;



    }







    public int getCurrentWallpaperIndex() {



        return currentPhotoIndex;



    }







    public void nextWallpaper() {



        if (currentCategory == ThemeCategory.DYNAMIC_ABSTRACT) {



            cycleToNextTheme();



        } else {



            List<String> list = getListForCategory(currentCategory);



            if (list == null || list.isEmpty()) return;



            currentPhotoIndex = (currentPhotoIndex + 1) % list.size();



            loadPhotoAsync(currentCategory, currentPhotoIndex, true);



        }



    }







    public void previousWallpaper() {



        if (currentCategory == ThemeCategory.DYNAMIC_ABSTRACT) {



            int prev = (targetThemeIndex - 1 + THEMES.length) % THEMES.length;



            setThemeIndex(prev);



        } else {



            List<String> list = getListForCategory(currentCategory);



            if (list == null || list.isEmpty()) return;



            currentPhotoIndex = (currentPhotoIndex - 1 + list.size()) % list.size();



            loadPhotoAsync(currentCategory, currentPhotoIndex, true);



        }



    }







    public void cycleToNextTheme() {



        if (currentCategory == ThemeCategory.DYNAMIC_ABSTRACT) {



            setThemeIndex((targetThemeIndex + 1) % THEMES.length);



        } else {



            nextWallpaper();



        }



    }







    private void loadPhotoAsync(ThemeCategory category, int index, boolean animateCrossfade) {



        List<String> list = getListForCategory(category);



        if (list == null || list.isEmpty()) {



            discoverPhotoWallpapers();



            list = getListForCategory(category);



            if (list == null || list.isEmpty()) return;



        }







        int safeIdx = Math.abs(index % list.size());



        String assetPath = list.get(safeIdx);







        decodeExecutor.execute(() -> {



            try {



                Context ctx = getContext();



                if (ctx == null) return;



                AssetManager am = ctx.getAssets();







                BitmapFactory.Options boundsOpts = new BitmapFactory.Options();



                boundsOpts.inJustDecodeBounds = true;



                InputStream is;
                try {
                    try {
                    is = am.open(assetPath);
                } catch (Throwable t1) {
                    is = am.open(assetPath.replace('/', '\\'));
                }
                } catch (Throwable t1) {
                    is = am.open(assetPath.replace('/', '\\'));
                }



                BitmapFactory.decodeStream(is, null, boundsOpts);



                is.close();







                int targetW = Math.max(getWidth(), 1920);



                int targetH = Math.max(getHeight(), 1080);







                int sample = 1;



                while ((boundsOpts.outWidth / (sample * 2)) >= targetW && (boundsOpts.outHeight / (sample * 2)) >= targetH) {



                    sample *= 2;



                }







                BitmapFactory.Options decodeOpts = new BitmapFactory.Options();



                decodeOpts.inSampleSize = sample;



                decodeOpts.inPreferredConfig = Bitmap.Config.RGB_565;







                try {
                    is = am.open(assetPath);
                } catch (Throwable t1) {
                    is = am.open(assetPath.replace('/', '\\'));
                }



                Bitmap decoded = BitmapFactory.decodeStream(is, null, decodeOpts);



                is.close();







                if (decoded != null) {



                    mainHandler.post(() -> {



                        if (currentCategory != category) {



                            decoded.recycle();



                            return;



                        }







                        if (prevPhotoBitmap != null && !prevPhotoBitmap.isRecycled() && prevPhotoBitmap != currentPhotoBitmap) {



                            prevPhotoBitmap.recycle();



                        }



                        prevPhotoBitmap = currentPhotoBitmap;



                        currentPhotoBitmap = decoded;







                        if (animateCrossfade && prevPhotoBitmap != null) {



                            if (photoCrossfadeAnim != null && photoCrossfadeAnim.isRunning()) {



                                photoCrossfadeAnim.cancel();



                            }



                            photoCrossfadeProgress = 0f;



                            photoCrossfadeAnim = ValueAnimator.ofFloat(0f, 1f);



                            photoCrossfadeAnim.setDuration(2200);



                            photoCrossfadeAnim.setInterpolator(new AccelerateDecelerateInterpolator());



                            photoCrossfadeAnim.addUpdateListener(anim -> {



                                photoCrossfadeProgress = (float) anim.getAnimatedValue();



                                invalidate();



                            });



                            photoCrossfadeAnim.start();



                        } else {



                            photoCrossfadeProgress = 1.0f;



                            invalidate();



                        }



                    });



                }



            } catch (Throwable t) {



                Log.w(TAG, "Error loading wallpaper " + assetPath + ": " + t.getMessage());



            }



        });



    }







    public void setAmbientGlow(int color, float focusX, float focusY) {



        // Subtle ambient responsiveness



    }







    public void setThemeIndex(int newIndex) {



        if (newIndex < 0 || newIndex >= THEMES.length) return;



        if (newIndex == targetThemeIndex && themeTransitionProgress >= 1f) return;







        currentThemeIndex = targetThemeIndex;



        targetThemeIndex = newIndex;



        ThemeScheme target = THEMES[newIndex];







        prevHue = curHue;



        prevSat = curSat;



        prevAccent = curAccent;



        prevSecondary = curSecondary;



        prevAmp = curAmp;







        if (transitionAnimator != null && transitionAnimator.isRunning()) {



            transitionAnimator.cancel();



        }







        transitionAnimator = ValueAnimator.ofFloat(0f, 1f);



        transitionAnimator.setDuration(2500);



        transitionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());



        transitionAnimator.addUpdateListener(anim -> {



            themeTransitionProgress = (float) anim.getAnimatedValue();



            curHue = prevHue + (target.hueShift - prevHue) * themeTransitionProgress;



            curSat = prevSat + (target.satMultiplier - prevSat) * themeTransitionProgress;



            curAccent = blendColors(prevAccent, target.accentColor, themeTransitionProgress);



            curSecondary = blendColors(prevSecondary, target.secondaryColor, themeTransitionProgress);



            curAmp = prevAmp + (target.waveAmplitude - prevAmp) * themeTransitionProgress;







            ColorMatrix cm = new ColorMatrix();



            adjustHue(cm, curHue);



            ColorMatrix satMatrix = new ColorMatrix();



            satMatrix.setSaturation(curSat);



            cm.postConcat(satMatrix);



            wallpaperPaint.setColorFilter(new ColorMatrixColorFilter(cm));



            invalidate();



        });



        transitionAnimator.start();



    }







    public int getCurrentThemeIndex() {



        return targetThemeIndex;



    }







    public boolean isAutoRotateEnabled() {



        return autoRotateEnabled;



    }







    public void setAutoRotateEnabled(boolean enabled) {



        this.autoRotateEnabled = enabled;



        rescheduleRotation();



    }







    public long getRotationIntervalMs() {



        return rotationIntervalMs;



    }







    public void setRotationIntervalMs(long ms) {



        this.rotationIntervalMs = ms;



        rescheduleRotation();



    }







    private void rescheduleRotation() {



        mainHandler.removeCallbacks(autoRotateRunnable);



        scheduleNextRotation();



    }







    private void scheduleNextRotation() {



        if (autoRotateEnabled && rotationIntervalMs > 0) {



            mainHandler.postDelayed(autoRotateRunnable, rotationIntervalMs);



        }



    }







    @Override



    protected void onSizeChanged(int w, int h, int oldw, int oldh) {



        super.onSizeChanged(w, h, oldw, oldh);



        if (w > 0 && h > 0) {



            initGradients(w, h);



            if (currentCategory == ThemeCategory.DYNAMIC_ABSTRACT) {



                initMesh(w, h);



            } else {



                if (currentPhotoBitmap == null) {



                    loadPhotoAsync(currentCategory, currentPhotoIndex, false);



                }



            }



        }



    }







    private void initGradients(int w, int h) {



        if (w <= 0 || h <= 0) return;



        LinearGradient bg = new LinearGradient(0, h * 0.45f, 0, h,



                new int[]{0x00000000, 0x88000000, 0xD0000000},



                new float[]{0.0f, 0.55f, 1.0f}, Shader.TileMode.CLAMP);



        bottomGradientPaint = new Paint();



        bottomGradientPaint.setShader(bg);







        LinearGradient tg = new LinearGradient(0, 0, 0, h * 0.28f,



                new int[]{0xB0000000, 0x50000000, 0x00000000},



                new float[]{0.0f, 0.60f, 1.0f}, Shader.TileMode.CLAMP);



        topGradientPaint = new Paint();



        topGradientPaint.setShader(tg);



    }







    private void initMesh(int w, int h) {



        if (rawWallpaper == null || w <= 0 || h <= 0) return;



        scaledWallpaper = Bitmap.createScaledBitmap(rawWallpaper, w, h, true);







        int index = 0;



        for (int y = 0; y <= MESH_HEIGHT; y++) {



            float fy = (float) y / MESH_HEIGHT * h;



            for (int x = 0; x <= MESH_WIDTH; x++) {



                float fx = (float) x / MESH_WIDTH * w;



                origVerts[index * 2] = fx;



                origVerts[index * 2 + 1] = fy;



                animVerts[index * 2] = fx;



                animVerts[index * 2 + 1] = fy;



                index++;



            }



        }







        ColorMatrix cm = new ColorMatrix();



        adjustHue(cm, curHue);



        ColorMatrix satMatrix = new ColorMatrix();



        satMatrix.setSaturation(curSat);



        cm.postConcat(satMatrix);



        wallpaperPaint.setColorFilter(new ColorMatrixColorFilter(cm));



    }







    private void updateMesh() {



        if (scaledWallpaper == null || scaledWallpaper.isRecycled()) return;



        int w = getWidth();



        int h = getHeight();



        if (w <= 0 || h <= 0) return;







        AbstractShape curShape = THEMES[currentThemeIndex].shape;



        AbstractShape targetShape = THEMES[targetThemeIndex].shape;







        int index = 0;



        for (int y = 0; y <= MESH_HEIGHT; y++) {



            float normY = (float) y / MESH_HEIGHT;



            for (int x = 0; x <= MESH_WIDTH; x++) {



                float normX = (float) x / MESH_WIDTH;



                float ox = origVerts[index * 2];



                float oy = origVerts[index * 2 + 1];







                float[] dCur = computeDeformation(curShape, normX, normY, phase, prevAmp);



                float[] dTar = computeDeformation(targetShape, normX, normY, phase, curAmp);







                float dx = dCur[0] + (dTar[0] - dCur[0]) * themeTransitionProgress;



                float dy = dCur[1] + (dTar[1] - dCur[1]) * themeTransitionProgress;







                animVerts[index * 2] = ox + dx;



                animVerts[index * 2 + 1] = oy + dy;



                index++;



            }



        }



    }







    private float[] computeDeformation(AbstractShape shape, float normX, float normY, float p, float amp) {



        float dx = 0f;



        float dy = 0f;







        switch (shape) {



            case SILK_RIBBONS:



                dx = amp * (55f * (float) Math.sin((normY * 2.8) + p)



                        + 25f * (float) Math.cos((normX * 1.8) - 1.5 * p));



                dy = amp * (30f * (float) Math.cos((normX * 2.2) + p)



                        + 15f * (float) Math.sin((normY * 2.0) - p));



                break;



            case AURORA_CURTAINS:



                dx = amp * (35f * (float) Math.sin((normX * 4.2) + p));



                dy = amp * (70f * (float) Math.sin((normX * 2.5) - p)



                        + 35f * (float) Math.cos((normY * 3.2) + 2.0 * p));



                break;



            case COSMIC_VORTEX:



                dx = amp * (65f * (float) Math.sin((normX * 2.2 + normY * 1.5) * Math.PI * 0.8 + p)



                        + 28f * (float) Math.cos((normY * 3.0) - p));



                dy = amp * (75f * (float) Math.cos((normX * 1.8 - normY * 2.0) * Math.PI * 0.8 - p)



                        + 30f * (float) Math.sin((normX * 2.4) + 2.0 * p));



                break;



            case GEOMETRIC_PRISMS:



                float streamPhase = (normX * 1.8f + normY * 1.4f) * (float) Math.PI - p;



                float crossDrift = (normX * 1.2f - normY * 1.6f) * (float) Math.PI + p;



                dx = amp * (60f * (float) Math.cos(streamPhase) + 25f * (float) Math.sin(crossDrift));



                dy = amp * (70f * (float) Math.sin(streamPhase) - 30f * (float) Math.cos(crossDrift));



                break;



            case TOPOGRAPHIC_DUNES:



                dx = amp * (45f * (float) Math.sin((normY * 2.6) + p) + 20f * (float) Math.cos((normX * 2.0) - p));



                dy = amp * (75f * (float) Math.sin((normX * 1.8) + p) * (float) Math.sin(normY * Math.PI)



                        + 30f * (float) Math.cos((normY * 3.4) - 2.0 * p));



                break;



            case CYBER_SYNTH_GRID:



                float distX = normX - 0.5f;



                float distY = normY - 0.5f;



                float distCenter = (float) Math.sqrt(distX * distX + distY * distY);



                float ripple = (float) Math.sin(distCenter * 7.0 - 2.0 * p);



                dx = amp * 40f * distX * ripple;



                dy = amp * 40f * distY * ripple;



                break;



        }







        return new float[]{dx, dy};



    }







    @Override



    protected void onDraw(Canvas canvas) {



        super.onDraw(canvas);



        int w = getWidth();



        int h = getHeight();



        if (w <= 0 || h <= 0) return;







        if (currentCategory == ThemeCategory.DYNAMIC_ABSTRACT) {



            if (scaledWallpaper != null && !scaledWallpaper.isRecycled()) {



                canvas.drawBitmapMesh(scaledWallpaper, MESH_WIDTH, MESH_HEIGHT, animVerts, 0, null, 0, wallpaperPaint);



            }



            return;



        }







        // Draw Photo Slideshow for Nature, Space, Cities



        // 1. Draw previous photo during crossfade



        if (photoCrossfadeProgress < 1.0f && prevPhotoBitmap != null && !prevPhotoBitmap.isRecycled()) {



            prevPhotoPaint.setAlpha(255);



            drawCoverBitmap(canvas, prevPhotoBitmap, w, h, prevPhotoPaint);



        }







        // 2. Draw current photo



        if (currentPhotoBitmap != null && !currentPhotoBitmap.isRecycled()) {



            int alpha = (int) (255 * Math.min(1.0f, Math.max(0.0f, photoCrossfadeProgress)));



            photoPaint.setAlpha(alpha);



            drawCoverBitmap(canvas, currentPhotoBitmap, w, h, photoPaint);



        }







        // 3. Elegant contrast vignette overlays



        canvas.drawColor(0x33000000); // 20% overall dark wash for crystal clear icon/text contrast



        if (topGradientPaint != null) {



            canvas.drawRect(0, 0, w, h * 0.28f, topGradientPaint);



        }



        if (bottomGradientPaint != null) {



            canvas.drawRect(0, h * 0.45f, w, h, bottomGradientPaint);



        }



    }







    private void drawCoverBitmap(Canvas canvas, Bitmap bmp, int viewW, int viewH, Paint paint) {



        int bw = bmp.getWidth();



        int bh = bmp.getHeight();



        if (bw <= 0 || bh <= 0) return;







        float scale = Math.max((float) viewW / bw, (float) viewH / bh);



        // Subtle slow Ken Burns drift (scale gently between 1.0f and 1.025f)



        float drift = 1.0f + 0.02f * (float) (0.5 + 0.5 * Math.sin(phase * 0.5));



        scale *= drift;







        float scaledW = bw * scale;



        float scaledH = bh * scale;



        float left = (viewW - scaledW) / 2f;



        float top = (viewH - scaledH) / 2f;







        Rect src = new Rect(0, 0, bw, bh);



        RectF dst = new RectF(left, top, left + scaledW, top + scaledH);



        canvas.drawBitmap(bmp, src, dst, paint);



    }







    private static void adjustHue(ColorMatrix cm, float value) {



        value = cleanValue(value, 360f) / 180f * (float) Math.PI;



        if (value == 0) return;



        float cosVal = (float) Math.cos(value);



        float sinVal = (float) Math.sin(value);



        float lumR = 0.213f;



        float lumG = 0.715f;



        float lumB = 0.072f;



        float[] mat = new float[]{



                lumR + cosVal * (1 - lumR) + sinVal * (-lumR), lumG + cosVal * (-lumG) + sinVal * (-lumG), lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0, 0,



                lumR + cosVal * (-lumR) + sinVal * (0.143f), lumG + cosVal * (1 - lumG) + sinVal * (0.140f), lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0, 0,



                lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)), lumG + cosVal * (-lumG) + sinVal * (lumG), lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0, 0,



                0, 0, 0, 1, 0,



                0, 0, 0, 0, 1



        };



        cm.postConcat(new ColorMatrix(mat));



    }







    private static float cleanValue(float p_val, float p_limit) {



        return Math.min(p_limit, Math.max(-p_limit, p_val));



    }







    private static int blendColors(int from, int to, float ratio) {



        float inverse = 1f - ratio;



        int r = (int) (Color.red(from) * inverse + Color.red(to) * ratio);



        int g = (int) (Color.green(from) * inverse + Color.green(to) * ratio);



        int b = (int) (Color.blue(from) * inverse + Color.blue(to) * ratio);



        int a = (int) (Color.alpha(from) * inverse + Color.alpha(to) * ratio);



        return Color.argb(a, r, g, b);



    }



}



