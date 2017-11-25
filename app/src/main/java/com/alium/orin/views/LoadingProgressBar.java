package com.alium.orin.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by liyanju on 2017/11/25.
 */

public class LoadingProgressBar extends AppCompatImageView {

    private ObjectAnimator animator;

    public LoadingProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.VISIBLE) {
            animator =  ObjectAnimator.ofFloat(this, "Rotation", 0, 360f);
            animator.setRepeatCount(ObjectAnimator.INFINITE);
            animator.setRepeatMode(ObjectAnimator.RESTART);
            animator.setDuration(800);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        } else if (animator != null){
            animator.cancel();
        }
    }
}
