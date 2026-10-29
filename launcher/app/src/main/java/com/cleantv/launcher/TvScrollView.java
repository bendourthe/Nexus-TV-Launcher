package com.cleantv.launcher;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;

public class TvScrollView extends ScrollView {

    private ValueAnimator activeScrollAnimator;

    public TvScrollView(Context context) {
        super(context);
        init();
    }

    public TvScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TvScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSmoothScrollingEnabled(true);
        setOverScrollMode(OVER_SCROLL_NEVER);
    }

    @Override
    protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
        // Return 0 to prevent Android's default abrupt jumping when child views gain focus
        return 0;
    }

    public void smoothScrollToPosition(int targetY, int durationMs) {
        if (activeScrollAnimator != null && activeScrollAnimator.isRunning()) {
            activeScrollAnimator.cancel();
        }
        int currentY = getScrollY();
        if (currentY == targetY) return;

        activeScrollAnimator = ValueAnimator.ofInt(currentY, targetY);
        activeScrollAnimator.setDuration(durationMs > 0 ? durationMs : 420);
        activeScrollAnimator.setInterpolator(new DecelerateInterpolator(1.8f));
        activeScrollAnimator.addUpdateListener(anim -> {
            scrollTo(0, (Integer) anim.getAnimatedValue());
        });
        activeScrollAnimator.start();
    }
}
