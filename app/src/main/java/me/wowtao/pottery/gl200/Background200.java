package me.wowtao.pottery.gl200;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import me.wowtao.pottery.gl.Background;
import me.wowtao.pottery.shader.BackgroundShader;
import me.wowtao.pottery.utils.ShaderUtil;

import java.io.InputStream;

import static android.opengl.GLES31.*;

public class Background200 extends Background {

    public Background200() {
        super();
        isNormalBufferDirty.value = false;
    }

    private BackgroundShader shader;

    public void createShader(Resources res) {
        shader = new BackgroundShader("vertex_background.glsl", "frag_background.glsl", res);
    }

    @Override
    public void draw() {
        shader.useProgram();
        shader.setVertexAttributeOffset(vertexBufferOffset, normalBufferOffset, texCoordinateBufferOffset);
        if (textureName == 0) {
            int[] temp = new int[1];
            glGenTextures(1, temp, 0);
            textureName = temp[0];
            glBindTexture(GL_TEXTURE_2D, textureName);
            InputStream is = context.getResources().openRawResource(resId);
            texture = BitmapFactory.decodeStream(is);
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, texture, 0);
            texture.recycle();
            texture = null;
            System.gc();
        }

        ShaderUtil.setTexParameter(textureName);
        shader.setTexture(0);
        shader.drawVBO(indicesBuffer.capacity(), indicesBufferOffset);
    }

    public void setLum(float f) {
        shader.setLum(f);
    }

    public void setEyeLookAt(float d) {
        shader.setLookAt(d);
    }
}
