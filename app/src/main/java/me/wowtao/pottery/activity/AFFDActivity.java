package me.wowtao.pottery.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import me.wowtao.pottery.R;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.gl.FFDView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class AFFDActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_affd);
        final FFDView ffdView = (FFDView) findViewById(R.id.glview);
        try {
            ffdView.setObjFileStream(new FileInputStream(new File(getExternalCacheDir(), "test.obj")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ((EditText) findViewById(R.id.controlPointNumber)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String input = s.toString();
                if (input.length() == 0) {
                    return;
                }
                try {
                    final int dup = Integer.parseInt(input);
                    ffdView.setControlPointNumber(dup);
                } catch (Exception e) {
                    System.out.println("dup input error");
                }
            }
        });
        ((EditText) findViewById(R.id.dup)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                final String input = s.toString();
                if (input.length() == 0) {
                    return;
                }
                try {
                    final int dup = Integer.parseInt(input);
                    ffdView.setDup(dup);
                } catch (Exception e) {
                    System.out.println("dup input error");
                }
            }
        });

        findViewById(R.id.restore).setOnClickListener(v -> ffdView.restore());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wowtao.getGlManager().background.setTexture(this, R.drawable.main_activity_background);
    }
}
