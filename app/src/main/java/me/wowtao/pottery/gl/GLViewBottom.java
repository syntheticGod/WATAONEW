
package me.wowtao.pottery.gl;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.TypedArray;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import me.wowtao.pottery.R;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.utils.PotteryTextureManager;

public class GLViewBottom extends GLSurfaceView {

    /**
     * the touch point position
     */
    float lastX = 0, lastY = 0;

    /**
     * this construction is used for construct from xml;
     */
    public GLViewBottom(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setDebugFlags(GLSurfaceView.DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
        setTransparent(context, attrs);
        setRenderer(getRenderer(context));
    }

    private void setTransparent(Context c, AttributeSet attrs) {
        TypedArray typedArray = c.obtainStyledAttributes(attrs, R.styleable.GLView);
        boolean isTransparent = false;

        try {
            isTransparent = typedArray.getBoolean(R.styleable.GLView_is_transparent, false);
        } finally {
            typedArray.recycle();
        }
        if (isTransparent) {
            this.setZOrderOnTop(true);
            this.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            this.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        }
    }

    private GLRendererBottom getRenderer(Context context) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configInfo = activityManager.getDeviceConfigurationInfo();

        int reqGlEsVersion = configInfo.reqGlEsVersion;

        if (reqGlEsVersion >= 0x20000) {
            this.setEGLContextClientVersion(2);
            return getRenderer();
        } else {
            throw new RuntimeException("can not run below GLES 2.0");
        }
    }

    GLRendererBottom getRenderer() {
        return new GLRendererBottom();
    }

    public boolean onTouchEvent(final MotionEvent event) {
        handleChangeGesture(event);
        return true;
    }

    void handleChangeGesture(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            lastX = event.getX();
            lastY = event.getY();
        } else if (action == MotionEvent.ACTION_MOVE) {
            float deltaX = event.getX() - lastX;
            float deltaY = event.getY() - lastY;
            Wowtao.getGlManager().changeGesture(deltaX, deltaY);
            lastX = event.getX();
            lastY = event.getY();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PotteryTextureManager.setIsInvalidGL();
    }

}


