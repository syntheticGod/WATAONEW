package me.wowtao.pottery.gl200;

import android.content.Context;
import me.wowtao.pottery.gl.Table;
import me.wowtao.pottery.shader.TableFireShader;
import me.wowtao.pottery.shader.TableShader;
import me.wowtao.pottery.utils.ShaderUtil;

public class Table200 extends Table {

    private TableShader shader;
    private TableFireShader fireShader;

    public static final int FIRE = 1;
    public static final int COMMON = 0;
    private int currentShader = COMMON;

    public Table200(Context context, String fileName) {
        super(context, fileName);
    }

    public void createShader(float offset) {
        shader = new TableShader("vertex_table.glsl", "frag_table.glsl", this.resources, offset);
        fireShader = new TableFireShader("vertex_table.glsl", "frag_table.glsl", this.resources, offset);
    }

    @Override
    public void draw() {
        if (currentShader == COMMON) {
            drawWithShader(shader);
        } else {
            drawWithShader(fireShader);
        }
        super.draw();
    }


    private void drawWithShader(TableShader shader) {
        shader.useProgram();
        shader.setVertexAttributeOffset(vertexBufferOffset, normalBufferOffset, texCoordinateBufferOffset);

        textureName = ShaderUtil.loadTexture(context, textureName, resId);
        ShaderUtil.setTexParameter(textureName);
        shader.reloadModelMatrix();
        shader.translate(0f, -1.9f, -6.4f);
        shader.rotate(angleForSensor, 1.0f, 0.0f, 0.0f);
        shader.rotate(angleForRotate, 0f, 1f, 0f);
        shader.scale(0.7f, 0.4f, 0.7f);
        shader.drawVBO(indicesBuffer.capacity(), indicesBufferOffset);
    }


    public void setOffset(float d) {
        if (shader != null) {
            shader.setLookAt(d);
        }
    }

    public void switchShader(int shader) {
        currentShader = shader;
    }


    public void setLum(float f) {
        fireShader.setLum(f);
    }

}