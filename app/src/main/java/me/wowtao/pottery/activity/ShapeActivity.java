package me.wowtao.pottery.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import com.umeng.update.UmengUpdateAgent;
import me.wowtao.pottery.R;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.fragment.ShapeFragment;

public class ShapeActivity extends BaseActivity {
    private ShapeFragment shapeFragment;
    private boolean isFirst = true;

    public ShapeFragment getShapeFragment() {
        return shapeFragment;
    }

    private boolean isChoose = false;
    private View splashView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UmengUpdateAgent.update(this);
        setContentView(R.layout.activity_shape);
        shapeFragment = (ShapeFragment) getFragmentManager().findFragmentById(R.id.shape_fragment);
        splashView = findViewById(R.id.splash_image);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getIntExtra("fromFinish", 0) == 1) {
            splashView.setVisibility(View.GONE);
            Wowtao.getGlManager().background.setTexture(ShapeActivity.this, R.drawable.main_activity_background);
            Wowtao.getGlManager().background.position = "right";
            Wowtao.getGlManager().getTable().setTexture(ShapeActivity.this, R.drawable.table);
            Wowtao.getGlManager().shadow.setTexture(ShapeActivity.this, R.drawable.shadow);
            Wowtao.getGlManager().getPottery().reset();
            shapeFragment.getGLView().onResume();
            shapeFragment.glManagerResume();
        } else {
            if (isFirst) {
                isFirst = false;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Wowtao.getGlManager().init(ShapeActivity.this.getApplicationContext());
                        Wowtao.getGlManager().background.setTexture(ShapeActivity.this, R.drawable.main_activity_background);
                        Wowtao.getGlManager().background.position = "right";
                        Wowtao.getGlManager().getTable().setTexture(ShapeActivity.this, R.drawable.table);
                        Wowtao.getGlManager().shadow.setTexture(ShapeActivity.this, R.drawable.shadow);
                        shapeFragment.getGLView().onResume();
                        while (!Wowtao.getGlManager().isDrew || !Wowtao.getGlManager().alreadyInitGL) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        shapeFragment.glManagerResume();

                        final Animation animation = new AlphaAnimation(1, 0);
                        animation.setDuration(1000);
                        ShapeActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                splashView.startAnimation(animation);
                                splashView.setVisibility(View.GONE);
                            }
                        });
                    }
                }).start();

            }
        }
        if (Wowtao.getGlManager().background != null) {
            Wowtao.getGlManager().background.position = "right";
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (isChoose) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                getFragmentManager().popBackStack();
                getFragmentManager().popBackStack();
                isChoose = false;
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return shapeFragment.onKeyDown(keyCode) || closeApp(keyCode, event);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        boolean isBackFromFinish = intent.getBooleanExtra("backToMain", false);
        if (isBackFromFinish) {
            getFragmentManager().popBackStack();
            getFragmentManager().popBackStack();
            shapeFragment.returnToHomePage();
            Wowtao.getGlManager().getPottery().reset();
        }
        super.onNewIntent(intent);
    }

}
