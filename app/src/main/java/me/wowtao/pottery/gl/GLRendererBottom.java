package me.wowtao.pottery.gl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import me.wowtao.pottery.Wowtao;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GLRendererBottom implements GLSurfaceView.Renderer {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Wowtao.getGlManager().initForGLBottom();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        Wowtao.getGlManager().changeFrustum(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Wowtao.getGlManager().drawBottom();
    }
}
