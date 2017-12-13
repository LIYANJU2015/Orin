package com.alium.orin.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.admodule.AdModule;
import com.admodule.adfb.IFacebookAd;
import com.alium.orin.App;
import com.alium.orin.R;
import com.alium.orin.util.LogUtil;
import com.alium.orin.util.StatReportUtils;
import com.alium.orin.util.Util;
import com.githang.statusbar.StatusBarCompat;

/**
 * Created by liyanju on 2017/12/2.
 */

public class SplashActivity extends AppCompatActivity implements IFacebookAd.FacebookAdListener{

    private ViewGroup adContainerFrame;

    private TextView countDownTV;

    private CountDownTimer countDownTimer  = new CountDownTimer(6*1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            LogUtil.v("xx", "onTick millisUntilFinished " + millisUntilFinished);
            countDownTV.setText(String.valueOf(millisUntilFinished / 1000) + " SKIP");
        }

        @Override
        public void onFinish() {
            countDownTV.setText("0 SKIP");
            startMain();
        }
    };

    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_layout);

        LogUtil.v("xx", " sIsColdLaunch " + App.sIsColdLaunch);
        if (!App.sIsColdLaunch) {
            startMain();
            return;
        }

        adContainerFrame = (ViewGroup) findViewById(R.id.ad_container_frame);
        countDownTV = (TextView)findViewById(R.id.count_down_tv);
        countDownTV.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startMain();
            }
        });

        AdModule.getInstance().getFacebookAd().setLoadListener(this);
        AdModule.getInstance().getFacebookAd().loadAd(false, "1305172892959949_1308865969257308");

        linearLayout = (LinearLayout) findViewById(R.id.splash_linear);
        linearLayout.setAlpha(0);
        linearLayout.animate().alpha(1).setDuration(3500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        LogUtil.v("XX", "onAnimationEnd >>>");
                        linearLayout.animate().alpha(0).setDuration(100)
                                .setListener(null).start();
                        if (adView != null) {
                            adContainerFrame.removeAllViews();
                            adContainerFrame.addView(adView);
                            startCountDown();
                            StatReportUtils.trackCustomEvent("splash_page", "ad show");
                        } else {
                            startMain();
                        }
                    }
                }).setInterpolator(new AccelerateInterpolator())
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        AdModule.getInstance().getFacebookAd().cancelLoadListener();
    }

    private void startCountDown() {
        countDownTV.setText("6 SKIP");
        countDownTV.setVisibility(View.VISIBLE);
        countDownTimer.cancel();
        countDownTimer.start();
    }

    private View adView;

    @Override
    public void onLoadedAd(View view) {
        adView = view;
    }

    @Override
    public void onStartLoadAd(View view) {

    }

    @Override
    public void onLoadAdFailed(int i, String s) {

    }

    private void startMain() {
        finish();
        App.sIsColdLaunch = false;
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
