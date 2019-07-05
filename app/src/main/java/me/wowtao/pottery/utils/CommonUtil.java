package me.wowtao.pottery.utils;

import android.content.Context;
import android.widget.Toast;

import java.lang.reflect.Field;

public class CommonUtil {
	public static int getResId(String name, Class<?> c){
		try {
			Field idField = c.getDeclaredField(name);
			return idField.getInt(idField);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

    public static void makeToast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }
}
