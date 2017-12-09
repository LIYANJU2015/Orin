package com.alium.orin.util;

import com.alium.orin.App;
import com.tencent.stat.StatService;

import java.util.Properties;

/**
 * Created by liyanju on 2017/12/9.
 */

public class StatReportUtils {

    public static void trackCustomKVEvent(String event, Properties prop) {
        StatService.trackCustomKVEvent(App.sContext, event, prop);
    }

    public static void trackCustomEvent(String event, String... values) {
        StatService.trackCustomEvent(App.sContext, event, values);
    }

    public static void trackCustomEndKVEvent(String event, Properties prop) {
        StatService.trackCustomBeginKVEvent(App.sContext, event, prop);
    }

    public static void trackCustomBeginKVEvent(String event, Properties prop) {
        StatService.trackCustomBeginKVEvent(App.sContext, event, prop);
    }
}
