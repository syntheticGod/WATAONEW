package me.wowtao.pottery.gl;

import ac.affd_android.affdview.GL.ACGLSurfaceView;
import android.content.Context;
import android.util.AttributeSet;
import me.wowtao.pottery.Wowtao;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES31.*;

/**
 * Created by ac on 11/17/16.
 * todo some describe
 */
public class FFDView extends ACGLSurfaceView {
    public FFDView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FFDView(Context context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        super.onSurfaceCreated(unused, config);
        Wowtao.getGlManager().initForGLFFD();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        Wowtao.getGlManager().drawBackground();
        super.onDrawFrame(unused);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        super.onSurfaceChanged(unused, width, height);
        Wowtao.getGlManager().changeFrustum(width, height);
    }

}
