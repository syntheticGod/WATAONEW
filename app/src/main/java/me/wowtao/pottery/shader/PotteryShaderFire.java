package me.wowtao.pottery.shader;

import android.content.res.Resources;
import android.opengl.GLES20;

public class PotteryShaderFire extends PotteryShader{
	
	//light parameter
	private static final float[] ambientL = new float[]{0.5f, 0.2f, 0.2f, 1.0f};
	private static final float[] diffuseL = new float[]{0.4f, 0.1f, 0.1f, 1.0f};
	private static final float[] specularL = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
	
	private static final float[] ambientL2 = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
	private static final float[] diffuseL2 = new float[]{0.0f, 0.0f, 0.0f, 1.0f};
	private static final float[] specularL2 = new float[]{0.0f, 0.0f, 0.0f, 1.0f};

	//material parameter
	private static final float[] ambientM = new float[]{0.6f, 0.6f, 0.6f, 1.0f};
	private static final float[] diffuseM = new float[]{0.8f, 0.8f, 0.6f, 1.0f};
	private static final float[] specularM = new float[]{0.7f, 0.7f, 0.7f, 1.0f};
	private static final float[] ambientMB = new float[]{0.6f, 0.6f, 0.6f, 1.0f};
	private static final float[] diffuseMB = new float[]{0.8f, 0.8f, 0.6f, 1.0f};
	private static final float[] specularMB = new float[]{0.7f, 0.7f, 0.7f, 1.0f};
	private static final float shininess = 50.0f;

	public PotteryShaderFire(String vertexShader, String fragShader, Resources resources, float offset) {
		super(vertexShader, fragShader, resources, offset);
		
		this.uAmbientLHandle = GLES20.glGetUniformLocation(program, "uAmbientL");
		checkParameterHandle(uAmbientLHandle, "uAmbientL");
		GLES20.glUniform4fv(uAmbientLHandle, 1, ambientL, 0);
		
		this.uDiffuseLHandle = GLES20.glGetUniformLocation(program, "uDiffuseL");
		checkParameterHandle(uDiffuseLHandle, "uDiffuseL");
		GLES20.glUniform4fv(uDiffuseLHandle, 1, diffuseL, 0);
		
		this.uSpecularLHandle = GLES20.glGetUniformLocation(program, "uSpecularL");
		checkParameterHandle(uSpecularLHandle, "uSpecularL");
		GLES20.glUniform4fv(uSpecularLHandle, 1, specularL, 0);
	
		this.uAmbientL2Handle = GLES20.glGetUniformLocation(program, "uAmbientL2");
		checkParameterHandle(uAmbientL2Handle, "uAmbientL2");
		GLES20.glUniform4fv(uAmbientL2Handle, 1, ambientL2, 0);
		
		this.uDiffuseL2Handle = GLES20.glGetUniformLocation(program, "uDiffuseL2");
		checkParameterHandle(uDiffuseL2Handle, "uDiffuseL2");
		GLES20.glUniform4fv(uDiffuseL2Handle, 1, diffuseL2, 0);
		
		this.uSpecularL2Handle = GLES20.glGetUniformLocation(program, "uSpecularL2");
		checkParameterHandle(uSpecularL2Handle, "uSpecularL2");
		GLES20.glUniform4fv(uSpecularL2Handle, 1, specularL2, 0);

		this.uAmbientMHandle = GLES20.glGetUniformLocation(program, "uAmbientM");
		checkParameterHandle(uAmbientMHandle, "uAmbientM");
		GLES20.glUniform4fv(uAmbientMHandle, 1, ambientM, 0);
		
		this.uDiffuseMHandle = GLES20.glGetUniformLocation(program, "uDiffuseM");
		checkParameterHandle(uDiffuseMHandle, "uDiffuseM");
		GLES20.glUniform4fv(uDiffuseMHandle, 1, diffuseM, 0);
		
		this.uSpecularMHandle = GLES20.glGetUniformLocation(program, "uSpecularM");
		checkParameterHandle(uSpecularMHandle, "uSpecularM");
		GLES20.glUniform4fv(uSpecularMHandle, 1, specularM, 0);
		
		this.uShininessHandle = GLES20.glGetUniformLocation(program, "uShininess");
		checkParameterHandle(uShininessHandle, "uShininess");
		GLES20.glUniform1f(uShininessHandle, shininess);
	}
	
	@Override
	protected void updateM() {
		if (needReloadM) {

			GLES20.glUniform4fv(uAmbientMHandle, 1, ambientM, 0);
			GLES20.glUniform4fv(uDiffuseMHandle, 1, diffuseM, 0);
			GLES20.glUniform4fv(uSpecularMHandle, 1, specularM, 0);
			
			needReloadM = false;
		}
	}
	
	public void setLum(float radio) {
		for (int i = 0; i < 3; ++i) {
			ambientM[i] = ambientMB[i] * radio;
			diffuseM[i] = diffuseMB[i] * radio;
			specularM[i] = specularMB[i] * radio;
		}
		
		needReloadM = true;
	}
}
