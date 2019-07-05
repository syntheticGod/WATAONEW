package me.wowtao.pottery.gl200;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import me.wowtao.pottery.R;
import me.wowtao.pottery.gl.Pottery;
import me.wowtao.pottery.shader.*;
import me.wowtao.pottery.utils.PotteryTextureManager;

import java.io.IOException;
import java.io.InputStream;

import static android.opengl.GLES32.*;

public class Pottery200 extends Pottery {
    public static final int CLAY = 0;
    public static final int DRY_CLAY = 1;
    public static final int CI = 2;
    public static final int FIRE = 3;

    private int shaderType;
    private PotteryShader clayShader;
    private PotteryShader ciShader;
    private PotteryShader dryClayShader;
    private PotteryShader fireShader;

    public Pottery200() {
        super();
    }

    public void createShader(float offset, Resources resources) {
        generateCubeMap(resources);

        ciShader = new PotteryShaderCi("vertex_pottery.glsl", "frag_pottery_ci.glsl", resources, offset);
        ciShader.useProgram();
        ciShader.setTextureCube();

        clayShader = new PotteryShaderClay("vertex_pottery.glsl", "frag_pottery.glsl", resources, offset);
        clayShader.useProgram();
        clayShader.setTextureCube();

        dryClayShader = new PotteryShaderDryClay("vertex_pottery.glsl", "frag_pottery_dry_clay.glsl", resources, offset);
        dryClayShader.useProgram();
        dryClayShader.setTextureCube();

        fireShader = new PotteryShaderFire("vertex_pottery.glsl", "frag_pottery_dry_clay.glsl", resources, offset);
        fireShader.useProgram();
        fireShader.setTextureCube();
    }

    @Override
    public void draw() {
        switch (shaderType) {
            case CLAY:
                drawWithShader(clayShader);
                break;

            case DRY_CLAY:
                drawWithShader(dryClayShader);
                break;

            case CI:
                drawWithShader(ciShader);
                break;

            case FIRE:
                drawWithShader(fireShader);
                break;

            default:
                break;
        }
    }

    private void drawWithShader(PotteryShader shader) {
        shader.useProgram();


        shader.setVertexAttributeOffset(vertexBufferOffset, normalBufferOffset, texCoordinateBufferOffset);

        shader.reloadModelMatrix();
        shader.translate(0f, -1.85f, -6.4f);
        shader.rotate(angleForSensor, 1.0f, 0.0f, 0.0f);
        shader.rotate(angleForRotate, 0f, 1f, 0f);
        PotteryTextureManager.loadTexture();

        shader.setTexture(0);
        shader.drawVBO(indicesBuffer.capacity(), indicesBufferOffset);
    }

    public void setOffset(float ratio) {
        if (dryClayShader != null) {
            clayShader.setLookAt(ratio);
            ciShader.setLookAt(ratio);
            dryClayShader.setLookAt(ratio);
        }
    }

    //加载立方图纹理
    static int generateCubeMap(Resources resources) {

        int[] cubeMapResourceIds = new int[]{
                R.drawable.light, R.drawable.light, R.drawable.light,
                R.drawable.light, R.drawable.light, R.drawable.light
        };

        int[] ids = new int[1];
        glGenTextures(1, ids, 0);
        int cubeMapTextureId = ids[0];

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeMapTextureId);
        glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameterf(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_REPEAT);

        for (int face = 0; face < 6; face++) {
            InputStream is = resources.openRawResource(cubeMapResourceIds[face]);
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(is);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e("CubeMap", "Could not decode texture for face " + Integer.toString(face));
                }
            }
            GLUtils.texImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + face, 0, bitmap, 0);
            bitmap.recycle();
        }

        glActiveTexture(GL_TEXTURE0);
        return cubeMapTextureId;
    }

    public void switchShader(int i) {
        shaderType = i;
    }

    public void setLum(float radio) {
        fireShader.setLum(radio);
    }

    public float getWidth(float height) {
        float radio = height / 8.0f / currentHeight;
        float[] temp = new float[VERTICAL_PRECISION];
        float max = 0f;
        for (int i = 0; i < radii.length; ++i) {
            float f = radii[i] * radio;
            temp[i] = f;
            if (max < f) {
                max = f;
            }
        }
        radii = temp;
        currentHeight = height / 8f;
        genVerticesFromBases();
        updateVertexBuffer();
        if (max < 0.375f) {
            max = 0.375f;
        }
        return max;
    }

    public double getPerimeter(int i) {
        if (i >= radii.length) {
            return radii[radii.length - 1] * 2 * Math.PI;
        } else {
            return radii[i] * 2 * Math.PI;
        }
    }
}

