package me.wowtao.pottery.fragment;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import me.wowtao.pottery.R;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.activity.*;
import me.wowtao.pottery.adapter.GeneralGridViewAdapter;
import me.wowtao.pottery.gl.GLView;
import me.wowtao.pottery.gl.Pottery;
import me.wowtao.pottery.gl200.Pottery200;
import me.wowtao.pottery.listener.GridViewChooseListener;
import me.wowtao.pottery.listener.StartActivityListener;
import me.wowtao.pottery.type.WTMode;
import me.wowtao.pottery.utils.PotteryTextureManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShapeFragment extends Fragment {

    private GLView glView;
    public WTMode mode;
    private View marketButton;
    private View accountButton;
    private View createButton;
    private View logoButton;
    private View nextButton;
    private GridView gridView;
    private View chooseClassicView;
    private boolean classicIsShowed = false;
    private View shapeMenuBar;

    public boolean needSave = true;
    private AnimatorUpdateListener animatorUpdateListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //initialize for home page
        mode = WTMode.VIEW;
        final View view = inflater.inflate(R.layout.fragment_shape, container, false);
        glView = (GLView) view.findViewById(R.id.pottery);
        glView.setFingerPoint(view.findViewById(R.id.finger_point));
        glView.onPause();
        Wowtao.getGlManager().alreadyInitGL = false;

        glView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    beginShape(event);
                    return false;
                } else {
                    return false;
                }
            }
        });
        marketButton = view.findViewById(R.id.market_button);
        marketButton.setOnClickListener(new StartActivityListener(getActivity(), IdeaMarketActivity.class) {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "您已进入哇陶创意集市", Toast.LENGTH_SHORT).show();
                super.onClick(v);
            }
        });
        accountButton = view.findViewById(R.id.account_button);
        accountButton.setOnClickListener(new StartActivityListener(getActivity(), CollectAndOrderActivity.class));


        createButton = view.findViewById(R.id.create_button);
        createButton.setOnClickListener(new myOnClickListener());
        logoButton = view.findViewById(R.id.logo);


        View returnToHomePageButton = view.findViewById(R.id.back_to_home_page_button);
        returnToHomePageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                returnToHomePage();
            }
        });
        View resetButton = view.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (currentMijiId == -1) {
                    Wowtao.getGlManager().getPottery().reset();
                } else {
                    loadMiji(currentMijiId);
                }
            }
        });
        View classicButton = view.findViewById(R.id.classic_button);
        classicButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (classicIsShowed) {
                    hideClassic();
                } else {
                    showClassic();
                }
            }
        });


        nextButton = view.findViewById(R.id.next);
        nextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DecorateActivity.class);
                ((ShapeActivity) getActivity()).getShapeFragment().needSave = false;
                getActivity().overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
                startActivity(intent);
            }
        });

        gridView = (GridView) view.findViewById(R.id.classicSample);
        GeneralGridViewAdapter adapter = new GeneralGridViewAdapter(getActivity());
        gridView.setAdapter(adapter);

        chooseClassicView = view.findViewById(R.id.choose_classic);

        chooseClassicView.post(new Runnable() {
            @Override
            public void run() {
                int height = chooseClassicView.getHeight();
                chooseClassicView.setTag(height);
                ((LayoutParams) chooseClassicView.getLayoutParams()).topMargin = -height;
            }
        });


        final GestureDetector gd = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {

                float x = e2.getX() - e1.getX();
                float y = e2.getY() - e1.getY();
                //限制必须得划过屏幕的1/6才能算划过
                float y_limit = Wowtao.screenHeightPixel / 6;
                float x_abs = Math.abs(x);
                float y_abs = Math.abs(y);
                if (x_abs <= y_abs) {
                    //gesture down or up
                    if (y > y_limit || y < -y_limit) {
                        if (y < 0) {
                            hideClassic();
                        }
                    }
                }
                return true;
            }
        });

        gridView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gd.onTouchEvent(event);
            }
        });

        view.findViewById(R.id.choose_classic_cancle_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                hideClassic();
            }
        });


        shapeMenuBar = view.findViewById(R.id.menu_bar);

        animatorUpdateListener = animation -> {
            shapeMenuBar.setAlpha((Float) animation.getAnimatedValue());
            shapeMenuBar.requestLayout();
            nextButton.setAlpha((Float) animation.getAnimatedValue());
            nextButton.requestLayout();
        };


        view.findViewById(R.id.ffd).setOnClickListener(new StartActivityListener(getActivity(), AFFDActivity.class) {
            @Override
            public void onClick(View v) {
                final File file = new File(getActivity().getExternalCacheDir(), "test.obj");
                Wowtao.getGlManager().getPottery().toOBJFile(file, false);
                super.onClick(v);
            }
        });

        return view;
    }


    private void beginShape(MotionEvent event) {
        float x = 0;
        float y = 0;
        int screenWidth = 0;
        int screenHeight = 0;
        if (event != null) {
            screenWidth = Wowtao.screenWidthPixel;
            screenHeight = Wowtao.screenHeightPixel;
            x = event.getX();
            y = event.getY();
        }
        if (event == null || x > screenWidth / 2 && y > screenHeight / 3) {
            Wowtao.getGlManager().mode = WTMode.SHAPE;
            glView.setMode(WTMode.SHAPE);
            if (mode == WTMode.VIEW) {
                new Thread() {
                    @Override
                    public void run() {
                        long startTime = System.currentTimeMillis();
                        long elapseTime = 0;
                        do {
                            float rate = elapseTime / 700.0f;
                            float eyeOffset = 0.18f * (1.0f - rate);
                            Wowtao.getGlManager().setEyeOffset(eyeOffset);
                            Wowtao.getGlManager().rotateSpeed = 0.24f - eyeOffset;
                            elapseTime = System.currentTimeMillis() - startTime;
                            try {
                                Thread.sleep(40);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } while (elapseTime < 700);
                        Wowtao.getGlManager().setEyeOffset(0);
                    }
                }.start();

                this.mode = WTMode.SHAPE;
                int[] location = new int[2];
                logoButton.getLocationOnScreen(location);
                int distance = location[0] + logoButton.getWidth();
                animateOut(marketButton, 0, distance);
                animateOut(accountButton, 20, distance);
                animateOut(logoButton, 40, distance);
                animateOut(createButton, 80, distance);

                //show shape menu
                shapeMenuBar.setVisibility(View.VISIBLE);
                nextButton.setVisibility(View.VISIBLE);
                ValueAnimator shapeMenuBarAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
                shapeMenuBarAnimator.addUpdateListener(animatorUpdateListener);
                shapeMenuBarAnimator.setDuration(1000).start();
            }
        }
    }

    private String[] fileNames = {"01.txt", "02.txt", "03.txt",
            "04.txt", "05.txt", "10.txt",
            "07.txt", "08.txt", "09.txt"};
    private float[] heights = {5, 6, 10,
            6, 10, 3,
            23, 13.2f, 14};

    private int currentMijiId = -1;

    public void loadMiji(int id) {
        currentMijiId = id;
        String fileName = fileNames[id];
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open(fileName)));
            float height = Float.parseFloat(reader.readLine());
            float realHeight = heights[id] / 8.0f;
            float ratio = realHeight / height;

            if (Pottery.VERTICAL_PRECISION != 50) {
                throw new RuntimeException("Pottery.VERTICAL_PRECISION must be 50");
            }
            float[] bases = new float[50];
            for (int i = 49; i >= 0; --i) {
                bases[49 - i] = Float.parseFloat(reader.readLine());
                bases[49 - i] *= ratio;
            }
            Wowtao.getGlManager().getPottery().setShape(bases, realHeight);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert reader != null;
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        hideClassic();
    }

    private void showClassic() {
        int height = (Integer) chooseClassicView.getTag();
        Animation animation = new TranslateAnimation(0, 0, -height, 0);
        animation.setDuration(500);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        chooseClassicView.startAnimation(animation);
        LayoutParams layoutParams = (LayoutParams) chooseClassicView.getLayoutParams();
        layoutParams.topMargin = 0;
        chooseClassicView.setLayoutParams(layoutParams);
        classicIsShowed = true;
        gridView.setOnItemClickListener(new GridViewChooseListener(this, getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        needSave = true;
        if (Wowtao.getGlManager().alreadyInit && Wowtao.getGlManager().alreadyInitGL) {
            Wowtao.getGlManager().getPottery().switchShader(Pottery200.CLAY);
            PotteryTextureManager.setBaseTexture(getResources(), R.drawable.clay);

            glManagerResume();
            glView.onResume();
        }
    }

    public void glManagerResume() {
        Wowtao.getGlManager().popPottery();
        Wowtao.getGlManager().mode = mode;
        PotteryTextureManager.setBaseTexture(getResources(), R.drawable.clay);
        Wowtao.getGlManager().getPottery().switchShader(Pottery200.CLAY);

        Wowtao.getGlManager().background.setTexture(getActivity(), R.drawable.main_activity_background);
        Wowtao.getGlManager().getTable().setTexture(getActivity(), R.drawable.table);
        Wowtao.getGlManager().shadow.setTexture(getActivity(), R.drawable.shadow);

        if (mode == WTMode.SHAPE) {
            Wowtao.getGlManager().setEyeOffset(0.0f);
            Wowtao.getGlManager().rotateSpeed = 0.24f;
        } else {
            Wowtao.getGlManager().setEyeOffset(0.3f);
            Wowtao.getGlManager().rotateSpeed = 0.06f;
        }
        Wowtao.getGlManager().changeGesture(0, 20);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        glView.onPause();

    }

    private class myOnClickListener implements OnClickListener {
        @Override
        public void onClick(View arg0) {
            beginShape(null);
        }

    }

    private void animateOut(final View view, int startTime, int distance) {
        Animation animation = new TranslateAnimation(0, -distance, 0, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(500);
        animation.setStartOffset(startTime);
        view.startAnimation(animation);
        view.setTag(distance);
        view.setVisibility(View.INVISIBLE);
    }

    public void returnToHomePage() {
        if (classicIsShowed) {
            hideClassic();
            return;
        }
        new Thread() {
            @Override
            public void run() {
                Wowtao.getGlManager().mode = WTMode.VIEW;
                long startTime = System.currentTimeMillis();
                long elapseTime = 0;
                do {
                    float eyeOffset = 0.18f * elapseTime / 700.0f;
                    Wowtao.getGlManager().setEyeOffset(eyeOffset);
                    Wowtao.getGlManager().rotateSpeed = 0.24f - eyeOffset;
                    elapseTime = System.currentTimeMillis() - startTime;
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } while (elapseTime < 700);
            }
        }.start();
        mode = WTMode.VIEW;

        animateIn(marketButton, 0);
        animateIn(accountButton, 20);
        animateIn(logoButton, 40);
        animateIn(createButton, 80);
        //hide shape menu
        ValueAnimator shapeMenuBarAnimator = ValueAnimator.ofFloat(1.0f, 0.0f);
        shapeMenuBarAnimator.addUpdateListener(animatorUpdateListener);
        shapeMenuBarAnimator.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                shapeMenuBar.setVisibility(View.GONE);
                nextButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }
        });
        shapeMenuBarAnimator.setDuration(1000).start();
    }

    private void animateIn(View view, int startTime) {
        Object tag = view.getTag();
        if (tag == null) {
            return;
        }
        Animation animation = new TranslateAnimation(-(Integer) tag, 0, 0, 0);
        animation.setInterpolator(new AccelerateInterpolator());
        animation.setDuration(500);
        animation.setStartOffset(startTime);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    private void hideClassic() {
        if (!classicIsShowed) {
            return;
        }
        final int height = (Integer) chooseClassicView.getTag();
        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.addUpdateListener(new AnimatorUpdateListener() {
            private IntEvaluator mEvaluator = new IntEvaluator();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = (Integer) animation.getAnimatedValue();
                float fraction = currentValue / 100.0f;
                ((LayoutParams) chooseClassicView.getLayoutParams()).topMargin = mEvaluator.evaluate(fraction, 0, -height);
                chooseClassicView.requestLayout();
            }
        });
        animator.setDuration(500).start();
        classicIsShowed = false;
    }


    public boolean onKeyDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_BACK && classicIsShowed) {
            if (((LayoutParams) chooseClassicView.getLayoutParams()).topMargin >= 0) {
                hideClassic();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && mode == WTMode.SHAPE) {
            returnToHomePage();
            return true;
        } else {
            return false;
        }
    }

    public GLView getGLView() {
        return glView;
    }
}

