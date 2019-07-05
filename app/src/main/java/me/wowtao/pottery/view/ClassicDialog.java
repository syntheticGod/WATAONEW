package me.wowtao.pottery.view;
import me.wowtao.pottery.R;


import android.app.Dialog;
import android.content.Context;

public class ClassicDialog extends Dialog {
	public ClassicDialog(Context context, int layout) {
		this(context, R.style.transparent, layout);
	}

	public ClassicDialog(Context context, int theme, int layout) {
		super(context, theme);
		this.setContentView(layout);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus) {
			dismiss();
		}
	}
	
}