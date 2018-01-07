package com.alium.orin.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * Created by liyanju on 2017/12/8.
 */

public class InstallReferrerReceiver extends BroadcastReceiver {

    private static final String TARGET = "&referrer=";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String referrer = intent.getStringExtra("referrer");
            if (referrer == null) {
                return;
            }
            Log.e("Referrer:::::", referrer);
            StatReportUtils.trackCustomEvent("ReferrerReceiver", " referrer : " + referrer);

            if (referrer.contains(TARGET)) {
                String referrer2 = referrer.substring(referrer.indexOf(TARGET) + TARGET.length(), referrer.length());
                FacebookReport.logSentReferrer(referrer2);
            } else {
                FacebookReport.logSentReferrer(referrer);
            }

            if (referrer.contains("utm_campaign=")) {
                String referrer2 = referrer.substring(referrer.indexOf("utm_campaign="), referrer.length());
                FacebookReport.logSentReferrer2(referrer2);
            }

            String source = Util.parseRefererSource(referrer);
            if ("facebook".equals(source)) {
                FacebookReport.logSentReferrerFacebook(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
