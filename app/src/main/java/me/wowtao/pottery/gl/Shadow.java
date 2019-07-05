package me.wowtao.pottery.gl;


import android.content.Context;

public class Shadow extends GLMeshObject{

	protected int textureName;
	protected float angleForSensor = 0.0f;
	protected Context context;
	protected int resId;

	public void setAngleRotateY(float angleForSensor) {
		this.angleForSensor = angleForSensor;
	}

	public Shadow(Context context, String fileName){
		LoadObj(context, fileName);
		updateBuffers();
	}

	public void draw() {
	}

	public void setTexture(Context context, int id) {
		this.context = context;
		this.resId = id;
		textureName = 0;
	}

}
