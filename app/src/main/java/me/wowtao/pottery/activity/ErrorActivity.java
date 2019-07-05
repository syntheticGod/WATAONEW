package me.wowtao.pottery.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import me.wowtao.pottery.R;

/**
 * Created by ac on 9/13/16.
 * todo some describe
 */
public class ErrorActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_activity);
        final String errorContent = getIntent().getStringExtra("errorinfo");
        ((TextView) findViewById(R.id.info)).setText(errorContent);
        findViewById(R.id.sendEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendIntent = new Intent(
                        Intent.ACTION_SEND);
                String subject = "Your App crashed! Fix it!";
                String body = "Yoddle" + '\n' + '\n' +
                        errorContent + '\n' +
                        '\n';
                // sendIntent.setType("text/plain");
                sendIntent.setType("message/rfc822");
                sendIntent.putExtra(Intent.EXTRA_EMAIL,
                        new String[]{"425719176@qq.com"});
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        body);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                        subject);
                sendIntent.setType("message/rfc822");
                ErrorActivity.this.startActivity(sendIntent);
                System.exit(0);
            }
        });
    }
}
