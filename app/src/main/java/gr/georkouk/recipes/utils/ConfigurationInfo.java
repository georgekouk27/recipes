package gr.georkouk.recipes.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;

import gr.georkouk.recipes.R;

public class ConfigurationInfo {

    public static int[] findScreenDimens(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();

        DisplayMetrics outMetrics = new DisplayMetrics();

        display.getMetrics(outMetrics);

        int[] screenDimens = new int[2];
        screenDimens[0] = outMetrics.widthPixels;
        screenDimens[1] = outMetrics.heightPixels;

        return screenDimens;
    }

    public static boolean onPhoneLandscape(Context context) {
        return !context.getResources().getBoolean(R.bool.isTablet)
                && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

    }

    public static boolean onPhonePortrait(Context context) {
        return !context.getResources().getBoolean(R.bool.isTablet)
                && context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public static void hideSystemUI(@NonNull Activity activity) {
        int newUiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();

        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        if (Build.VERSION.SDK_INT >= 19) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

}
