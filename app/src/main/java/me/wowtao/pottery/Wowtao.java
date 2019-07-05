package me.wowtao.pottery;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.chillax.service.landDivideServeice;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import me.wowtao.pottery.activity.DecorateActivity;
import me.wowtao.pottery.utils.GLManager;
import me.wowtao.pottery.utils.MySensor;


public class Wowtao extends Application {
    public static int screenWidthPixel;
    public static int screenHeightPixel;
    public static float density;

    static public GLManager getGlManager() {
        return glManager;
    }

    static public void resumeGLManager(Context context){
        if (glManager == null || !glManager.alreadyInit) {
            glManager = new GLManager();
            glManager.init(context);
            glManager.initForGL();
        }
    }

    private static GLManager glManager;

    @Override
    public void onCreate() {
        super.onCreate();

        MySensor.init(getApplicationContext());

        //initialize screen width and height
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        Wowtao.screenWidthPixel = dm.widthPixels;
        Wowtao.screenHeightPixel = dm.heightPixels;
        Wowtao.density = dm.density;

        Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
        editor.putBoolean(DecorateActivity.KEY_IS_SHOW_PWD_TIP, true).apply();

        Intent i = new Intent(this, landDivideServeice.class);
        startService(i);

        initImageLoader();
        glManager = new GLManager();
    }


    private void initImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory()
                .cacheOnDisc()
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader.getInstance().init(config);
    }
}
