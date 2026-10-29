package com.cleantv.launcher;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AppItem {
    public final String packageName;
    public final String activityName;
    public final String label;
    public final Drawable banner;
    public final Drawable icon;
    public final int themeColor;

    public AppItem(String packageName, String activityName, String label, Drawable banner, Drawable icon, Context context) {
        this.packageName = packageName;
        this.activityName = activityName;
        this.label = label;
        this.icon = icon;

        if (packageName.contains("youtube") && context != null) {
            this.banner = createDarkYouTubeBanner(context);
        } else {
            this.banner = banner;
        }

        this.themeColor = computeThemeColor(packageName, this.banner != null ? this.banner : icon);
    }

    private static Drawable createDarkYouTubeBanner(Context context) {
        try {
            int w = 320;
            int h = 180;
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);

            // Sleek deep dark gradient
            Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setShader(new LinearGradient(0, 0, 0, h,
                    new int[]{Color.parseColor("#1C1C1C"), Color.parseColor("#0C0C0C")},
                    null, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, bgPaint);

            // Red YouTube Play Badge
            Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            redPaint.setColor(Color.parseColor("#FF0033"));
            RectF badgeRect = new RectF(52, 58, 118, 122);
            canvas.drawRoundRect(badgeRect, 18, 18, redPaint);

            // White Play Triangle
            Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            whitePaint.setColor(Color.WHITE);
            Path triangle = new Path();
            triangle.moveTo(80, 75);
            triangle.lineTo(80, 105);
            triangle.lineTo(96, 90);
            triangle.close();
            canvas.drawPath(triangle, whitePaint);

            // Clean bold "YouTube" wordmark
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(36);
            textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            canvas.drawText("YouTube", 132, 103, textPaint);

            return new BitmapDrawable(context.getResources(), bmp);
        } catch (Throwable t) {
            return null;
        }
    }

    private static int computeThemeColor(String pkg, Drawable d) {
        if (pkg.contains("youtube")) return Color.parseColor("#FF0033");
        if (pkg.contains("disney")) return Color.parseColor("#1B45F5");
        if (pkg.contains("wbd") || pkg.contains("max") || pkg.contains("hbo")) return Color.parseColor("#8028F5");
        if (pkg.contains("peacock")) return Color.parseColor("#00B074");
        if (pkg.contains("apple")) return Color.parseColor("#E0E0E6");
        if (pkg.contains("amazon") || pkg.contains("prime")) return Color.parseColor("#00A8E1");
        if (pkg.contains("netflix")) return Color.parseColor("#E50914");
        if (pkg.contains("hulu")) return Color.parseColor("#1CE783");
        if (pkg.contains("fubo")) return Color.parseColor("#FA4E28");
        if (pkg.contains("tubi")) return Color.parseColor("#FF3D3D");
        if (pkg.contains("pluto")) return Color.parseColor("#FFD200");

        if (d instanceof BitmapDrawable) {
            Bitmap b = ((BitmapDrawable) d).getBitmap();
            if (b != null && !b.isRecycled()) {
                try {
                    int pixel = b.getPixel(b.getWidth() / 2, b.getHeight() / 2);
                    if (Color.alpha(pixel) > 100) return pixel;
                } catch (Throwable ignored) {
                }
            }
        }
        return Color.parseColor("#6C5CE7");
    }
}
