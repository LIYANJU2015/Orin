package com.alium.orin.util;

import android.os.Bundle;

import com.alium.orin.App;
import com.facebook.appevents.AppEventsLogger;

/**
 * Created by liyanju on 2017/12/13.
 */

public class FacebookReport {

    public static void logSentReferrer(String referrer)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("cus_referrer", referrer);
        logger.logEvent("ReferrerReceiver",bundle);
    }

    public static void logAppLinkDataFetched(String linkeData) {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("linkeData", linkeData);
        logger.logEvent("ReferrerReceiver3",bundle);
    }

    public static void logSentReferrer2(String referrer)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        Bundle bundle = new Bundle();
        bundle.putString("utm_campaign", referrer);
        logger.logEvent("ReferrerReceiver2",bundle);
    }

    public static void logSentReferrerFacebook(String referrer)  {
        AppEventsLogger logger = AppEventsLogger.newLogger(App.sContext);
        logger.logEvent("ReferrerReceiver_facebook");
    }
}
