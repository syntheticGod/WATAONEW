package me.wowtao.pottery.gl;

import me.wowtao.pottery.Wowtao;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class GLRenderer extends GLRendererBottom {

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Wowtao.getGlManager().initForGL();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Wowtao.getGlManager().draw(gl);
    }
}
