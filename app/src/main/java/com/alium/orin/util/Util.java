package com.alium.orin.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.alium.orin.App;
import com.alium.orin.model.Album;
import com.kabouzeid.appthemehelper.util.TintHelper;
import com.alium.orin.R;

import java.net.URLDecoder;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Util {

    public static ExecutorService sExecutorService = Executors.newSingleThreadExecutor();

    public static final String RECOMMEND_PACKAGE_NAME1 = "com.gomusic.musicdownloader";
    public static final String RECOMMEND_PACKAGE_NAME2 = "com.gomusic.freedownloader";
    public static String sRecommendPageName = RECOMMEND_PACKAGE_NAME2;

    public static void initRecommend() {
        int i = new Random().nextInt(10);
        if (i % 2 == 0) {
            sRecommendPageName = RECOMMEND_PACKAGE_NAME1;
        } else {
            sRecommendPageName = RECOMMEND_PACKAGE_NAME2;
        }

    }

    public static boolean checkRecommendExist(Context context, String packageName) {
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        if (packages == null) {
            return false;
        }
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static void gotoGP(Context context, String packageName) {
        try {
            Intent launchIntent = new Intent();
            launchIntent.setPackage("com.android.vending");
            launchIntent.setData(Uri.parse("market://details?id=" + packageName));
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(launchIntent);
        } catch (android.content.ActivityNotFoundException anfe) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + packageName)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void runSingleThread(Runnable runnable) {
        sExecutorService.execute(runnable);
    }

    public static String parseRefererSource(String referer) {
        try {
            String newreferer = URLDecoder.decode(referer, "UTF-8");
            String target = "utm_source=";
            newreferer = newreferer.substring(newreferer.indexOf(target) + target.length(), newreferer.length());
            System.out.println("newreferer::" + newreferer);
            String source = newreferer.substring(0, newreferer.indexOf("&"));
            System.out.println("source::>>" + source);
            return source;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getActionBarSize(@NonNull Context context) {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) App.sContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display.getHeight();
    }

    public static int getScreenWhith() {
        WindowManager wm = (WindowManager) App.sContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display.getWidth();
    }

    public static Point getScreenSize(@NonNull Context c) {
        Display display = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarTranslucent(@NonNull Window window) {
        window.setFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public static void setAllowDrawUnderStatusBar(@NonNull Window window) {
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public static String getExtraDetailsWithDot(Activity activity, Album album) {
        return MusicUtil.getAlbumInfoString(activity, album);
    }

    public static String getExtraDetailsLineSpace(Activity activity, Album album) {
        return MusicUtil.getAlbumInfoStringLineSpace(activity, album);
    }

    public static void hideSoftKeyboard(@Nullable Activity activity) {
        if (activity != null) {
            View currentFocus = activity.getCurrentFocus();
            if (currentFocus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            }
        }
    }

    public static boolean isTablet(@NonNull final Resources resources) {
        return resources.getConfiguration().smallestScreenWidthDp >= 600;
    }

    public static boolean isLandscape(@NonNull final Resources resources) {
        return resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static Drawable resolveDrawable(@NonNull Context context, @AttrRes int drawableAttr) {
        TypedArray a = context.obtainStyledAttributes(new int[]{drawableAttr});
        Drawable drawable = a.getDrawable(0);
        a.recycle();
        return drawable;
    }

    public static int resolveDimensionPixelSize(@NonNull Context context, @AttrRes int dimenAttr) {
        TypedArray a = context.obtainStyledAttributes(new int[]{dimenAttr});
        int dimensionPixelSize = a.getDimensionPixelSize(0, 0);
        a.recycle();
        return dimensionPixelSize;
    }

    public static Drawable getVectorDrawable(@NonNull Context context, @DrawableRes int id) {
        return getVectorDrawable(context.getResources(), id, context.getTheme());
    }

    public static Drawable getVectorDrawable(@NonNull Resources res, @DrawableRes int resId, @Nullable Resources.Theme theme) {
        if (Build.VERSION.SDK_INT >= 21) {
            return res.getDrawable(resId, theme);
        }
        return VectorDrawableCompat.create(res, resId, theme);
    }

    public static Drawable getTintedVectorDrawable(@NonNull Context context, @DrawableRes int id, @ColorInt int color) {
        return TintHelper.createTintedDrawable(getVectorDrawable(context.getResources(), id, context.getTheme()), color);
    }

    public static Drawable getTintedVectorDrawable(@NonNull Resources res, @DrawableRes int resId, @Nullable Resources.Theme theme, @ColorInt int color) {
        return TintHelper.createTintedDrawable(getVectorDrawable(res, resId, theme), color);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isRTL(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = context.getResources().getConfiguration();
            return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        } else return false;
    }

    public static boolean isAllowedToDownloadMetadata(final Context context) {
        switch (PreferenceUtil.getInstance(context).autoDownloadImagesPolicy()) {
            case "always":
                return true;
            case "only_wifi":
                final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
                return netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI && netInfo.isConnectedOrConnecting();
            case "never":
            default:
                return false;
        }
    }

}