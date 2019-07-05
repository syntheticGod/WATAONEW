package me.wowtao.pottery.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.utils.ShaderUtil;

/**
 * Created by ac on 15-3-10.
 *
 */
public class BaseActivity extends Activity {

    private static final boolean DEBUG = true;
    private long time;

    boolean checkInternet() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//         出现应用级异常时的处理
        if (DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, final Throwable e) {
                    e.printStackTrace();
                    Intent crashedIntent = new Intent(BaseActivity.this, ErrorActivity.class);
                    crashedIntent.putExtra("errorinfo", Log.getStackTraceString(e));
                    crashedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                    crashedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    BaseActivity.this.startActivity(crashedIntent);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wowtao.resumeGLManager(getApplicationContext());
        ShaderUtil.clearMem();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    boolean closeApp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - time > 2500) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                finishAffinity();
                System.exit(0);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
