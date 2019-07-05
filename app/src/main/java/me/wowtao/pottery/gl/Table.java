package me.wowtao.pottery.gl;

import android.content.Context;
import android.content.res.Resources;

public class Table extends GLMeshObject {
	
	protected int textureName = 0;
	protected float angleForRotate = 0.0f;
	protected float angleForSensor = 0.0f;
	protected Resources resources;
	protected int resId;
	protected Context context;

	public Table(Context context, String fileName){
		this.resources = context.getResources();
		LoadObj(context, fileName);
		updateBuffers();
	}

	public void draw() {
		
	}

	public void setAngleForRotate(float rotateAngle) {
		this.angleForRotate = rotateAngle;
	}

	public void setAngleRotateY(float potteryCurrentAngleRotateX) {
		this.angleForSensor = potteryCurrentAngleRotateX;
	}


	public void setTexture(Context context, int id) {
		resId = id;
		this.context = context;
		this.textureName = 0;
	}
}
