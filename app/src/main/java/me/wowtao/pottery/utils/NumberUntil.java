package me.wowtao.pottery.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

public class NumberUntil {
	public static Bitmap getNumberBitmap(Context context, float input){
		input = Math.round(input * 10)/10.0f;
		String inputStr = Float.toString(input);
		int width;
		if (inputStr.indexOf('.') == -1) {
			width = inputStr.length() * 31;
		}else{
			width = inputStr.length() * 31 - 12;
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, 43, Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bitmap);
		
		int currentWidth = 0;
		for (int i = 0; i < inputStr.length(); ++i) {
			char c = inputStr.charAt(i);
			Bitmap charBitmap = getCharBitmap(context, c);
			canvas.drawBitmap(charBitmap, currentWidth, 0, null);
			if (c == '.') {
				currentWidth += 19;
			}else{
				currentWidth += 31;
			}
		}
		
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		
		return bitmap;
	}
	
	public static Bitmap getNumberBitmapI(Context context, Integer input){
		String inputStr = input.toString();
		int width;
		if (inputStr.indexOf('.') == -1) {
			width = inputStr.length() * 31;
		}else{
			width = inputStr.length() * 31 - 12;
		}
		
		Bitmap bitmap = Bitmap.createBitmap(width, 43, Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bitmap);
		
		int currentWidth = 0;
		for (int i = 0; i < inputStr.length(); ++i) {
			char c = inputStr.charAt(i);
			Bitmap charBitmap = getCharBitmap(context, c);
			canvas.drawBitmap(charBitmap, currentWidth, 0, null);
			if (c == '.') {
				currentWidth += 19;
			}else{
				currentWidth += 31;
			}
		}
		
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		
		return bitmap;
	}

	private static Bitmap getCharBitmap(Context context, char c) {
		String fileName = "n" + c + ".png";
		InputStream is = null;
		
		try {
			is = context.getAssets().open(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return BitmapFactory.decodeStream(is);
	}
}
