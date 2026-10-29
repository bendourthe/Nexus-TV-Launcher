package com.cleantv.launcher;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

public class AnimatedWaveBackgroundView extends View {

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
        public final float hueShift;       // Hue rotation in degrees
        public final float satMultiplier;  // Saturation multiplier
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
            // 0. Liquid Silk Waves (Obsidian & Violet) - Smooth horizontal liquid glass flow
            new ThemeScheme("Liquid Silk (Obsidian Violet)", AbstractShape.SILK_RIBBONS,
                    0f, 1.0f, Color.parseColor("#B388FF"), Color.parseColor("#7C4DFF"), 1.0f),

            // 1. Cascading Liquid Stream (Emerald Green) - Vertical cascading fluid streams
            new ThemeScheme("Cascading Stream (Emerald Green)", AbstractShape.AURORA_CURTAINS,
                    210f, 1.30f, Color.parseColor("#00F5A0"), Color.parseColor("#00D9F9"), 1.0f),

            // 2. Deep Liquid Surge (Crimson Ember) - Broad smooth surging liquid waves, zero twirling
            new ThemeScheme("Liquid Surge (Crimson Ember)", AbstractShape.COSMIC_VORTEX,
                    60f, 1.35f, Color.parseColor("#FF2A55"), Color.parseColor("#FF7A00"), 1.0f),

            // 3. Oceanic Liquid Drift (Deep Sapphire Blue) - Smooth diagonal laminar liquid glass flow
            new ThemeScheme("Oceanic Drift (Sapphire Blue)", AbstractShape.GEOMETRIC_PRISMS,
                    310f, 1.35f, Color.parseColor("#00A6FF"), Color.parseColor("#0051FF"), 1.05f),

            // 4. Warm Fluid Contours (Solar Amber) - Broad rolling sinusoidal liquid dunes
            new ThemeScheme("Fluid Contours (Solar Amber)", AbstractShape.TOPOGRAPHIC_DUNES,
                    110f, 1.30f, Color.parseColor("#FFB703"), Color.parseColor("#FB8500"), 1.0f),

            // 5. Concentric Fluid Ripples (Electric Magenta) - Smooth concentric circular water ripples
            new ThemeScheme("Fluid Ripples (Electric Magenta)", AbstractShape.CYBER_SYNTH_GRID,
                    340f, 1.35f, Color.parseColor("#FF007F"), Color.parseColor("#9D4EDD"), 1.0f)
    };

    // 24x16 mesh grid for real-time hardware-accelerated deformation
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

    // Theme state
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

    // Auto rotate every 5 minutes (300,000 ms)
    public static final long AUTO_ROTATE_DELAY_MS = 300000;
    private boolean autoRotateEnabled = true;
    private final Handler autoRotateHandler = new Handler(Looper.getMainLooper());
    private final Runnable autoRotateRunnable = new Runnable() {
        @Override
        public void run() {
            if (autoRotateEnabled) {
                cycleToNextTheme();
            }
            autoRotateHandler.postDelayed(this, AUTO_ROTATE_DELAY_MS);
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

        // Seamless 2*PI phase cycle over 20 seconds for ultra-smooth, hypnotic liquid motion
        continuousAnimator = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2.0));
        continuousAnimator.setDuration(20000);
        continuousAnimator.setRepeatCount(ValueAnimator.INFINITE);
        continuousAnimator.setRepeatMode(ValueAnimator.RESTART);
        continuousAnimator.setInterpolator(new LinearInterpolator());
        continuousAnimator.addUpdateListener(anim -> {
            phase = (float) anim.getAnimatedValue();
            updateMesh();
            invalidate();
        });
        continuousAnimator.start();
    }

    public void setAmbientGlow(int color, float focusX, float focusY) {
        // Spotlight dots completely disabled
    }

    public void cycleToNextTheme() {
        setThemeIndex((targetThemeIndex + 1) % THEMES.length);
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

        // Smooth 3.0-second cross-fade between shapes, animations, and color schemes
        transitionAnimator = ValueAnimator.ofFloat(0f, 1f);
        transitionAnimator.setDuration(3000);
        transitionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        transitionAnimator.addUpdateListener(anim -> {
            themeTransitionProgress = (float) anim.getAnimatedValue();
            curHue = prevHue + (target.hueShift - prevHue) * themeTransitionProgress;
            curSat = prevSat + (target.satMultiplier - prevSat) * themeTransitionProgress;
            curAccent = blendColors(prevAccent, target.accentColor, themeTransitionProgress);
            curSecondary = blendColors(prevSecondary, target.secondaryColor, themeTransitionProgress);
            curAmp = prevAmp + (target.waveAmplitude - prevAmp) * themeTransitionProgress;

            updateColorFilter();
            invalidate();
        });
        transitionAnimator.start();
    }

    public int getCurrentThemeIndex() {
        return targetThemeIndex;
    }

    public ThemeScheme getCurrentTheme() {
        return THEMES[targetThemeIndex];
    }

    public boolean isAutoRotateEnabled() {
        return autoRotateEnabled;
    }

    public void setAutoRotateEnabled(boolean enabled) {
        this.autoRotateEnabled = enabled;
        autoRotateHandler.removeCallbacks(autoRotateRunnable);
        if (enabled) {
            autoRotateHandler.postDelayed(autoRotateRunnable, AUTO_ROTATE_DELAY_MS);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (continuousAnimator != null && !continuousAnimator.isRunning()) {
            continuousAnimator.start();
        }
        autoRotateHandler.removeCallbacks(autoRotateRunnable);
        if (autoRotateEnabled) {
            autoRotateHandler.postDelayed(autoRotateRunnable, AUTO_ROTATE_DELAY_MS);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (continuousAnimator != null) continuousAnimator.cancel();
        if (transitionAnimator != null) transitionAnimator.cancel();
        autoRotateHandler.removeCallbacks(autoRotateRunnable);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) return;

        if (rawWallpaper != null) {
            scaledWallpaper = Bitmap.createScaledBitmap(rawWallpaper, w, h, true);
        }

        int index = 0;
        for (int y = 0; y <= MESH_HEIGHT; y++) {
            float fy = (float) h * y / MESH_HEIGHT;
            for (int x = 0; x <= MESH_WIDTH; x++) {
                float fx = (float) w * x / MESH_WIDTH;
                origVerts[index * 2] = fx;
                origVerts[index * 2 + 1] = fy;
                index++;
            }
        }
        updateColorFilter();
        updateMesh();
    }

    private void updateColorFilter() {
        if (Math.abs(curHue) < 1f && Math.abs(curSat - 1.0f) < 0.05f) {
            wallpaperPaint.setColorFilter(null);
        } else {
            ColorMatrix cm = new ColorMatrix();
            adjustHue(cm, curHue);
            ColorMatrix satMatrix = new ColorMatrix();
            satMatrix.setSaturation(curSat);
            cm.postConcat(satMatrix);
            wallpaperPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        }
    }

    private void updateMesh() {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float amp = curAmp;
        AbstractShape currentShape = THEMES[currentThemeIndex].shape;
        AbstractShape targetShape = THEMES[targetThemeIndex].shape;
        float morphProgress = themeTransitionProgress;

        int index = 0;
        for (int y = 0; y <= MESH_HEIGHT; y++) {
            float normY = (float) y / MESH_HEIGHT;
            float envY = (y == 0 || y == MESH_HEIGHT) ? 0f : (float) Math.sin(normY * Math.PI);
            for (int x = 0; x <= MESH_WIDTH; x++) {
                float ox = origVerts[index * 2];
                float oy = origVerts[index * 2 + 1];

                if (x == 0 || x == MESH_WIDTH || y == 0 || y == MESH_HEIGHT) {
                    animVerts[index * 2] = ox;
                    animVerts[index * 2 + 1] = oy;
                    index++;
                    continue;
                }

                float normX = (float) x / MESH_WIDTH;
                float envX = (float) Math.sin(normX * Math.PI);
                // Smooth window envelope: 0 at all 4 screen borders, 0.42 in the interior
                float edgePin = envX * envY * 0.42f;

                // Compute displacement for previous shape
                float[] dPrev = computeShapeDisplacement(currentShape, normX, normY, ox, oy, w, h, phase, amp);
                // Compute displacement for target shape
                float[] dTarget = computeShapeDisplacement(targetShape, normX, normY, ox, oy, w, h, phase, amp);

                // Smooth morph between shapes during 3s transition
                float dx = (dPrev[0] * (1f - morphProgress) + dTarget[0] * morphProgress) * edgePin;
                float dy = (dPrev[1] * (1f - morphProgress) + dTarget[1] * morphProgress) * edgePin;

                animVerts[index * 2] = ox + dx;
                animVerts[index * 2 + 1] = oy + dy;
                index++;
            }
        }
    }

    /**
     * Computes the mathematical displacement vector for each vertex.
     * All harmonic phase multipliers are STRICTLY INTEGERS (1, 2, 3), ensuring:
     * sin(theta + k * 2*PI) == sin(theta + 0) and cos(theta + k * 2*PI) == cos(theta + 0).
     * This mathematical invariance guarantees zero frame skipping or discontinuity when the cycle loops.
     */
    private float[] computeShapeDisplacement(AbstractShape shape, float normX, float normY, float ox, float oy, int w, int h, float p, float amp) {
        float dx = 0f;
        float dy = 0f;

        switch (shape) {
            case SILK_RIBBONS:
                // Smooth horizontal undulating liquid silk currents (Obsidian Violet)
                dx = amp * (55f * (float) Math.sin((normY * 2.8) + p)
                        + 25f * (float) Math.cos((normX * 3.6) - p));
                dy = amp * (65f * (float) Math.cos((normX * 2.2) + p)
                        + 30f * (float) Math.sin((normY * 2.8) - 2.0 * p));
                break;

            case AURORA_CURTAINS:
                // Gentle vertical cascading liquid streams (Emerald Green)
                dx = amp * (35f * (float) Math.sin((normX * 4.2) + p));
                dy = amp * (70f * (float) Math.sin((normX * 2.5) - p)
                        + 35f * (float) Math.cos((normY * 3.2) + 2.0 * p));
                break;

            case COSMIC_VORTEX:
                // Broad, smooth liquid tidal surge (Crimson Red) - pure flowing lines, zero twirling
                dx = amp * (65f * (float) Math.sin((normX * 2.2 + normY * 1.5) * Math.PI * 0.8 + p)
                        + 28f * (float) Math.cos((normY * 3.0) - p));
                dy = amp * (75f * (float) Math.cos((normX * 1.8 - normY * 2.0) * Math.PI * 0.8 - p)
                        + 30f * (float) Math.sin((normX * 2.4) + 2.0 * p));
                break;

            case GEOMETRIC_PRISMS:
                // Smooth diagonal laminar liquid glass flow (Deep Sapphire Blue)
                float streamPhase = (normX * 1.8f + normY * 1.4f) * (float) Math.PI - p;
                float crossDrift = (normX * 1.2f - normY * 1.6f) * (float) Math.PI + p;
                dx = amp * (60f * (float) Math.cos(streamPhase) + 25f * (float) Math.sin(crossDrift));
                dy = amp * (70f * (float) Math.sin(streamPhase) - 30f * (float) Math.cos(crossDrift));
                break;

            case TOPOGRAPHIC_DUNES:
                // Warm rolling fluid contour waves (Solar Amber)
                dx = amp * (45f * (float) Math.sin((normY * 2.6) + p) + 20f * (float) Math.cos((normX * 2.0) - p));
                dy = amp * (75f * (float) Math.sin((normX * 1.8) + p) * (float) Math.sin(normY * Math.PI)
                        + 30f * (float) Math.cos((normY * 3.4) - 2.0 * p));
                break;

            case CYBER_SYNTH_GRID:
                // Smooth concentric circular liquid ripples (Electric Magenta) - zero rotation
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

        if (scaledWallpaper != null && !scaledWallpaper.isRecycled()) {
            canvas.drawBitmapMesh(scaledWallpaper, MESH_WIDTH, MESH_HEIGHT, animVerts, 0, null, 0, wallpaperPaint);
        }
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
