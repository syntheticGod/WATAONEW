package me.wowtao.pottery.view;
import me.wowtao.pottery.R;


import android.app.Dialog;
import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

public class CustomProgressDialog extends Dialog {
	private ProgressBar pb;
	private TextView tvMsg;
	private int maxWendu;

	public CustomProgressDialog(Context context, int strMessage) {
		this(context, R.style.CustomProgressDialog, strMessage);
	}

	public CustomProgressDialog(Context context, int theme, int wendu) {
		super(context, theme);
		this.setContentView(R.layout.progress);
		tvMsg = (TextView) this.findViewById(R.id.id_tv_loadingmsg);
		pb = (ProgressBar) findViewById(R.id.loadingImageView);
		pb.setMax(100);
		this.maxWendu = wendu;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		if (!hasFocus) {
			dismiss();
		}
	}

	public void setProgress(Integer integer) {
		pb.setProgress(integer);
		int wendu = (int) (integer / 100f * maxWendu);
//		if (integer <= 25) {
//			wendu = (int) (integer * 4 / 100f * maxWendu);
//		}else if (integer >= 75) {
//			wendu = (int) ((100 - integer) * 4 / 100f * maxWendu);
//		}else{
//			wendu = maxWendu;
//		}
		tvMsg.setText("当前温度为:"+ wendu + "℃");
	}
}