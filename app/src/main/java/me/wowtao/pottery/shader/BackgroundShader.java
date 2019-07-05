package me.wowtao.pottery.shader;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class BackgroundShader extends WTShader{
	
	private int factorHandle;
	private float factor = 1;
	
	public BackgroundShader(String vertexShader, String fragShader, Resources resources) {
		super(vertexShader, fragShader, resources);
		this.aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
		checkParameterHandle(aPositionHandle, "aPosition");
		this.aTexCoordinateHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
		checkParameterHandle(aTexCoordinateHandle, "aTexCoord");
		GLES20.glEnableVertexAttribArray(aPositionHandle);
		GLES20.glEnableVertexAttribArray(aTexCoordinateHandle);
		
        this.uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        checkParameterHandle(uMVPMatrixHandle, "uMVPMatrix");
        
        //get the location of variable in frag shader
		this.uTextureHandle = GLES20.glGetUniformLocation(program, "uTexture");
		checkParameterHandle(uTextureHandle, "uTexture");
		
		useProgram();
		this.factorHandle = GLES20.glGetUniformLocation(program, "factor");
		GLES20.glUniform1f(factorHandle, factor);
		
		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 0.0f;

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
	
	@Override
	public void drawVBO(int indicesNum, int offset) {
		updateM();
		super.drawVBO(indicesNum, offset);
	}

	
	private boolean needReloadM = false;

	private void updateM() {
		if (needReloadM) {
			GLES20.glUniform1f(factorHandle, factor);
			needReloadM = false;
		}
	}

	public void setLum(float radio) {
		factor = radio;
		needReloadM = true;
	}

    public void setLookAt(float d) {
		//Position the eye behind the origin.
		final float eyeY = 0.0f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -1.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		Matrix.setLookAtM(viewMatrix, 0, d, eyeY, d, lookX, lookY, lookZ, upX, upY, upZ);
	}

}
