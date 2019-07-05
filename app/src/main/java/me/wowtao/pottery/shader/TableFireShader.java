package me.wowtao.pottery.shader;

import android.content.res.Resources;
import android.opengl.GLES20;

public class TableFireShader extends TableShader {

    //light parameter
    {
        ambientL = new float[]{0.8f, 0.1f, 0.1f, 1.0f};
        diffuseL = new float[]{0.8f, 0.1f, 0.1f, 1.0f};
        lightPosition = new float[]{0.0f, 15.0f, -5f, 1.0f};
        ambientM = new float[]{0.9f, 0.9f, 0.7f, 1.0f};
        diffuseM = new float[]{0.9f, 0.9f, 0.9f, 1.0f};
    }

    //material parameter
    private float[] ambientMB = new float[]{0.9f, 0.9f, 0.7f, 1.0f};
    private float[] diffuseMB = new float[]{0.9f, 0.9f, 0.9f, 1.0f};

    public TableFireShader(String vertexShader, String fragShader, Resources resources, float offset) {

        super(vertexShader, fragShader, resources, offset);

        this.uAmbientLHandle = GLES20.glGetUniformLocation(program, "uAmbientL");
        checkParameterHandle(uAmbientLHandle, "uAmbientL");
        GLES20.glUniform4fv(uAmbientLHandle, 1, ambientL, 0);

        this.uDiffuseLHandle = GLES20.glGetUniformLocation(program, "uDiffuseL");
        checkParameterHandle(uDiffuseLHandle, "uDiffuseL");
        GLES20.glUniform4fv(uDiffuseLHandle, 1, diffuseL, 0);

        this.uLightPositionHandle = GLES20.glGetUniformLocation(program, "uLightPosition");
        checkParameterHandle(uLightPositionHandle, "uLightPosition");
        GLES20.glUniform4fv(uLightPositionHandle, 1, lightPosition, 0);

        this.uAmbientMHandle = GLES20.glGetUniformLocation(program, "uAmbientM");
        checkParameterHandle(uAmbientMHandle, "uAmbientM");
        GLES20.glUniform4fv(uAmbientMHandle, 1, ambientM, 0);

        this.uDiffuseMHandle = GLES20.glGetUniformLocation(program, "uDiffuseM");
        checkParameterHandle(uDiffuseMHandle, "uDiffuseM");
        GLES20.glUniform4fv(uDiffuseMHandle, 1, diffuseM, 0);
    }

    private boolean needReloadM = false;

    @Override
    protected void updateM() {
        if (needReloadM) {

            GLES20.glUniform4fv(uAmbientMHandle, 1, ambientM, 0);
            GLES20.glUniform4fv(uDiffuseMHandle, 1, diffuseM, 0);

            needReloadM = false;
        }
    }

    @Override
    public void setLum(float radio) {
        for (int i = 0; i < 3; ++i) {
            ambientM[i] = ambientMB[i] * radio;
            diffuseM[i] = diffuseMB[i] * radio;
        }

        needReloadM = true;
    }

}
