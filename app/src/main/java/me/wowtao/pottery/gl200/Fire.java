package me.wowtao.pottery.gl200;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.opengl.GLUtils;
import me.wowtao.pottery.gl.Background;
import me.wowtao.pottery.shader.BackgroundShader;
import me.wowtao.pottery.shader.WTShader;
import me.wowtao.pottery.utils.ShaderUtil;

import java.io.InputStream;

import static android.opengl.GLES32.*;

public class Fire extends Background {

    public Fire() {
        super();
        isNormalBufferDirty.value = false;
    }

    private WTShader shader;
    private boolean needReload = false;

    public void createShader(Resources res) {
        shader = new BackgroundShader("vertex_background.glsl", "frag_background.glsl", res);
    }

    @Override
    public void draw() {
        shader.useProgram();
        shader.setVertexAttributeOffset(vertexBufferOffset, normalBufferOffset, texCoordinateBufferOffset);
        if (textureName == 0 || needReload) {
            if (textureName == 0) {
                int[] temp = new int[1];
                glGenTextures(1, temp, 0);
                textureName = temp[0];
            }
            glBindTexture(GL_TEXTURE_2D, textureName);
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, texture, 0);
            needReload = false;
        }
        ShaderUtil.setTexParameter(textureName);
        shader.setTexture(0);
        shader.drawVBO(indicesBuffer.capacity(), indicesBufferOffset);
    }

    public void genPosition(float left, float right, float bottom,
                            float top, float near, float far) {
        this.top = top * 3 / near;
        this.bottom = bottom * 3 / near;
        this.left = left * 3 / near;
        this.right = right * 3 / near;
        vertices = new float[]{
                this.left, this.bottom, -3,
                this.right, this.bottom, -3,
                this.right, this.top, -3,
                this.left, this.top, -3
        };
        updateVertexBuffer();
        this.originalVertices = vertices.clone();
    }


    private static Options op = new Options();

    public void changeTexture(Activity activity, int mainActivityBackground) {
        InputStream is = activity.getResources().openRawResource(mainActivityBackground);
        op.inBitmap = texture;
        op.inMutable = true;
        op.inSampleSize = 1;
        texture = BitmapFactory.decodeStream(is, null, op);
        needReload = true;
    }

    @Override
    public void setTexture(Activity activity, int mainActivityBackground) {
        InputStream is = activity.getResources().openRawResource(mainActivityBackground);
        Options op = new Options();
        op.inMutable = true;
        texture = BitmapFactory.decodeStream(is, null, op);
        textureName = 0;
    }

}
