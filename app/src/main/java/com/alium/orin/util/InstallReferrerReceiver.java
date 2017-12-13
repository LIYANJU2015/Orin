package com.alium.orin.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tencent.stat.StatService;

/**
 * Created by liyanju on 2017/12/8.
 */

public class InstallReferrerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
        if (referrer == null) {
            return;
        }
        Log.e("Referrer:::::", referrer);
        StatReportUtils.trackCustomEvent("ReferrerReceiver", " referrer : " + referrer);
        FacebookReport.logSentReferrer(referrer);

        String source = Util.parseRefererSource(referrer);
        if ("facebook".equals(source)) {
            FacebookReport.logSentReferrerFacebook(source);
        }
    }
}
