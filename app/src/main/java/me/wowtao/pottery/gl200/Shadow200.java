package me.wowtao.pottery.gl200;

import android.content.Context;
import android.content.res.Resources;
import me.wowtao.pottery.gl.Shadow;
import me.wowtao.pottery.shader.ShadowShader;
import me.wowtao.pottery.utils.ShaderUtil;

public class Shadow200 extends Shadow {

    public Shadow200(Context context, String fileName) {
        super(context, fileName);
    }

    @Override
    public void draw() {
        shader.useProgram();
        shader.setVertexAttributeOffset(vertexBufferOffset, normalBufferOffset, texCoordinateBufferOffset);

        textureName = ShaderUtil.loadTexture(context, textureName, resId);
        ShaderUtil.setTexParameter(textureName);
        shader.reloadModelMatrix();
        shader.translate(0f, -1.9f, -6.4f);
        shader.rotate(angleForSensor, 1.0f, 0.0f, 0.0f);
        shader.rotate(135, 0, 1, 0);
        shader.scale(1.2f, 1.0f, 0.95f);
        shader.translate(0f, 0f, -0.5f);
        shader.drawVBO(indicesBuffer.capacity(), indicesBufferOffset);
    }

    private ShadowShader shader;

    public void createShader(float offset, Resources res) {
        shader = new ShadowShader("vertex_shadow.glsl", "frag_shadow.glsl", res, offset);
    }

    public void setOffset(float d) {
        if (shader != null) {
            shader.setLookAt(d);
        }
    }
}
