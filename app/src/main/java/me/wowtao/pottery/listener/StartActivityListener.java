package me.wowtao.pottery.listener;


import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.Serializable;

public class StartActivityListener implements OnClickListener {

	private Intent intent;
	private Context context;

	public StartActivityListener(Context context, Class<?> clazz,Object... objects) {
		this.context = context;
		intent = new Intent(context, clazz);
		for (int i = 0; i < objects.length; i+=2) {
			intent.putExtra((String)objects[i], (Serializable)objects[i+1]);
		}
	}

	@Override
	public void onClick(View v) {
		context.startActivity(intent);
	}

}
