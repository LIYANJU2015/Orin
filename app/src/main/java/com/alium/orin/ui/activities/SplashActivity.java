package com.alium.orin.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alium.orin.App;
import com.alium.orin.R;
import com.alium.orin.util.LogUtil;
import com.alium.orin.util.Util;
import com.githang.statusbar.StatusBarCompat;

/**
 * Created by liyanju on 2017/12/2.
 */

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarCompat.setStatusBarColor(this,
                ContextCompat.getColor(this, R.color.colorPrimaryDark));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);
    }

    @Override
    protected void onResume() {
        super.onResume();

        LogUtil.v("xx", " sIsColdLaunch " + App.sIsColdLaunch);
        if (!App.sIsColdLaunch) {
            startMain();
            return;
        }

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.splash_linear);
        linearLayout.setAlpha(0);
        linearLayout.animate().alpha(1).setDuration(3000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        startMain();
                    }
                }).setInterpolator(new AccelerateInterpolator())
                .start();
    }

    private void startMain() {
        finish();
        App.sIsColdLaunch = false;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
