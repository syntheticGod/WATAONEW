package me.wowtao.pottery.shader;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class TableShader extends WTShader{
	
	//light parameter
	float[] ambientL = new float[]{0.6f, 0.6f, 0.6f, 1.0f};
	float[] diffuseL = new float[]{0.9f, 0.9f, 0.9f, 1.0f};
	static float[] lightPosition = new float[]{0.0f,15.0f,-5f, 1.0f};
	
	//material parameter
	float[] ambientM = new float[]{0.9f, 0.9f, 0.7f, 1.0f};
	float[] diffuseM = new float[]{0.9f, 0.9f, 0.9f, 1.0f};


	//vertex parameter handle
	private int uMVMatrixHandle;
	
	//frag parameter handle
	int uAmbientLHandle;
	int uDiffuseLHandle;
	int uLightPositionHandle;
	int uAmbientMHandle;
	int uDiffuseMHandle;

	public TableShader(String vertexShader, String fragShader, Resources resources, float offset) {
		super(vertexShader, fragShader, resources);
		
		useProgram();
		//get the location of variable in vertex shader
		this.aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
		checkParameterHandle(aPositionHandle, "aPosition");
		this.aTexCoordinateHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
		checkParameterHandle(aTexCoordinateHandle, "aTexCoord");
		this.aNormalHandle = GLES20.glGetAttribLocation(program, "aNormal");
		checkParameterHandle(aNormalHandle, "aNormal");
		GLES20.glEnableVertexAttribArray(aPositionHandle);
		GLES20.glEnableVertexAttribArray(aNormalHandle);
		GLES20.glEnableVertexAttribArray(aTexCoordinateHandle);
		
        this.uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        checkParameterHandle(uMVPMatrixHandle, "uMVPMatrix");
        this.uMVMatrixHandle = GLES20.glGetUniformLocation(program, "uMVMatrix");
        checkParameterHandle(uMVMatrixHandle, "uMVMatrix");
        
        //get the location of variable in frag shader
		this.uTextureHandle = GLES20.glGetUniformLocation(program, "uTexture");
		checkParameterHandle(uTextureHandle, "uTexture");
		
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
	
		//Position the eye behind the origin.
		final float eyeX = genOffsetX(offset);
		final float eyeY = 0.0f;
		final float eyeZ = genOffsetZ(offset);

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -1.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);
	}

	public void translate(float x, float y, float z) {
		Matrix.translateM(modelMatrix, 0, x, y, z);
	}

	public void rotate(float angle, float x, float y, float z) {
		Matrix.rotateM(modelMatrix, 0, angle, x, y, z);
	}
	
	public void scale(float x, float y, float z) {
		Matrix.scaleM(modelMatrix, 0, x, y, z);
	}

	@Override
	public void drawVBO(int indicesNum, int offset) {
		updateM();
		updateVBO();
		float[] multiplyMMResult = new float[16];
		Matrix.multiplyMM(multiplyMMResult, 0, viewMatrix, 0, modelMatrix, 0);
		GLES20.glUniformMatrix4fv(uMVMatrixHandle, 1, false, multiplyMMResult, 0);

		Matrix.multiplyMM(multiplyMMResult, 0, perspectiveMatrix, 0, multiplyMMResult, 0);
		GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, multiplyMMResult, 0);

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesNum, GLES20.GL_UNSIGNED_SHORT, offset);
	}

	public void reloadModelMatrix() {
		Matrix.setIdentityM(modelMatrix, 0);
	}

	public void setLum(float radio) {
		
	}

	protected void updateM() {
		
	}

}
