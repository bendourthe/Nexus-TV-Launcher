package com.cleantv.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class WatchNextItem {
    public final String id;
    public final String packageName;
    public final String appBadge;
    public final String showTitle;
    public final String episodeInfo;
    public final boolean isNewEpisode;
    public final int progressPercent;
    public final int brandColor;
    public final String intentUri;
    public final Drawable backdrop;
    public final int drawableResId;
    public final Bitmap posterBitmap;

    public WatchNextItem(String id, String packageName, String appBadge, String showTitle,
                          String episodeInfo, boolean isNewEpisode, int progressPercent,
                          int brandColor, String intentUri, Drawable backdrop) {
        this(id, packageName, appBadge, showTitle, episodeInfo, isNewEpisode, progressPercent, brandColor, intentUri, backdrop, 0, null);
    }

    public WatchNextItem(String id, String packageName, String appBadge, String showTitle,
                          String episodeInfo, boolean isNewEpisode, int progressPercent,
                          int brandColor, String intentUri, Drawable backdrop, int drawableResId) {
        this(id, packageName, appBadge, showTitle, episodeInfo, isNewEpisode, progressPercent, brandColor, intentUri, backdrop, drawableResId, null);
    }

    public WatchNextItem(String id, String packageName, String appBadge, String showTitle,
                          String episodeInfo, boolean isNewEpisode, int progressPercent,
                          int brandColor, String intentUri, Drawable backdrop, int drawableResId, Bitmap posterBitmap) {
        this.id = id;
        this.packageName = packageName;
        this.appBadge = appBadge;
        this.showTitle = showTitle;
        this.episodeInfo = episodeInfo;
        this.isNewEpisode = isNewEpisode;
        this.progressPercent = progressPercent;
        this.brandColor = brandColor;
        this.intentUri = intentUri;
        this.backdrop = backdrop;
        this.drawableResId = drawableResId;
        this.posterBitmap = posterBitmap;
    }

    public Drawable getPoster(Context context) {
        return getPoster(context, null);
    }

    public Drawable getPoster(Context context, AppItem app) {
        // 1. Photographic bitmap thumbnail (from EPG, notification, or network)
        if (posterBitmap != null && context != null) {
            return new BitmapDrawable(context.getResources(), posterBitmap);
        }

        // 2. Bundled photographic high-resolution 16:9 still
        if (drawableResId != 0 && context != null) {
            try {
                return context.getResources().getDrawable(drawableResId);
            } catch (Throwable ignored) {
            }
        }

        // 3. Explicit backdrop drawable if set
        if (backdrop != null) {
            return backdrop;
        }

        // 4. App thumbnail / banner as backup
        if (app != null && context != null) {
            if (app.banner != null) {
                Drawable brandedBanner = createAppBrandedBackdrop(context, app.banner, brandColor, true);
                if (brandedBanner != null) return brandedBanner;
                return app.banner;
            } else if (app.icon != null) {
                Drawable brandedIcon = createAppBrandedBackdrop(context, app.icon, brandColor, false);
                if (brandedIcon != null) return brandedIcon;
                return app.icon;
            }
        }

        // 5. Branded cinematic gradient fallback
        return generateCinematicPoster(context, showTitle, brandColor, appBadge);
    }

    public static Drawable createAppBrandedBackdrop(Context context, Drawable appDrawable, int brandColor, boolean isBanner) {
        if (context == null || appDrawable == null) return null;
        try {
            int w = 500;
            int h = 292; // 16:9 ratio
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            // Rich multi-stop dark cinematic gradient
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            int darkTint = Color.argb(255,
                    Math.max(12, Color.red(brandColor) / 5),
                    Math.max(12, Color.green(brandColor) / 5),
                    Math.max(18, Color.blue(brandColor) / 5));
            bgPaint.setShader(new LinearGradient(0, 0, w, h,
                    new int[]{darkTint, Color.parseColor("#0C0C14"), Color.parseColor("#06060A")},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, bgPaint);

            // Central ambient soft glow behind the logo
            Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glowPaint.setColor(Color.argb(45, Color.red(brandColor), Color.green(brandColor), Color.blue(brandColor)));
            canvas.drawCircle(w * 0.5f, h * 0.44f, 120f, glowPaint);

            // Draw app banner or icon centered
            if (isBanner) {
                int bannerW = (int) (w * 0.62f);
                int bannerH = (int) (bannerW * (180f / 320f));
                int left = (w - bannerW) / 2;
                int top = (h - bannerH) / 2 - 12;
                appDrawable.setBounds(left, top, left + bannerW, top + bannerH);
                appDrawable.draw(canvas);
            } else {
                int iconSize = 110;
                int left = (w - iconSize) / 2;
                int top = (h - iconSize) / 2 - 12;
                appDrawable.setBounds(left, top, left + iconSize, top + iconSize);
                appDrawable.draw(canvas);
            }

            return new BitmapDrawable(context.getResources(), bmp);
        } catch (Throwable t) {
            return null;
        }
    }

    public static Drawable generateCinematicPoster(Context context, String title, int brandColor, String iconSymbol) {
        if (context == null) return null;
        try {
            int w = 500;
            int h = 292; // 16:9 ratio
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            // Rich multi-stop dark cinematic gradient
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            int darkTint = Color.argb(255,
                    Math.max(12, Color.red(brandColor) / 5),
                    Math.max(12, Color.green(brandColor) / 5),
                    Math.max(18, Color.blue(brandColor) / 5));
            bgPaint.setShader(new LinearGradient(0, 0, w, h,
                    new int[]{darkTint, Color.parseColor("#0C0C14"), Color.parseColor("#06060A")},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, bgPaint);

            // Ambient glow
            Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            glowPaint.setColor(Color.argb(35, Color.red(brandColor), Color.green(brandColor), Color.blue(brandColor)));
            canvas.drawCircle(w * 0.5f, h * 0.42f, 130f, glowPaint);

            // Monogram or symbol
            if (iconSymbol != null && !iconSymbol.isEmpty()) {
                Paint symbolPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                symbolPaint.setColor(Color.argb(45, 255, 255, 255));
                symbolPaint.setTextSize(72f);
                symbolPaint.setTextAlign(Paint.Align.CENTER);
                symbolPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                canvas.drawText(iconSymbol, w * 0.5f, h * 0.50f, symbolPaint);
            }

            return new BitmapDrawable(context.getResources(), bmp);
        } catch (Throwable t) {
            return null;
        }
    }
}
