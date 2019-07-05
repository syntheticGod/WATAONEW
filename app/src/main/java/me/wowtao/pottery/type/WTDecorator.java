package me.wowtao.pottery.type;

import android.graphics.Bitmap;

public class WTDecorator extends WTObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5128086078955266986L;
	
	private float width;
	public int temp;
	public String resourceId;
    public Long id;

	private Bitmap original;

	private int scale = 50;

	public WTDecorator() {
	}

	public WTDecorator(float width) {
		this.width = width;
	}


	public float getWidth() {
		return width;
	}
	
	public void setWidth(float f){
		this.width = f;
	}
	
	public void setOriginal(Bitmap bitmap) {
		this.original = bitmap;
	}
	
	public Bitmap getOriginal(){
		return original;
	}

	public void setScale(int progress) {
		this.scale = progress;
	}
	
	public int getScale(){
		return this.scale;
	}
}
