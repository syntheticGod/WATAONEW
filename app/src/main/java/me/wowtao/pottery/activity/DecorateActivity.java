package me.wowtao.pottery.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import me.wowtao.pottery.R;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.gl.GLView;
import me.wowtao.pottery.gl200.Pottery200;
import me.wowtao.pottery.gl200.Table200;
import me.wowtao.pottery.type.WTDecorateTypeEnum;
import me.wowtao.pottery.type.WTDecorator;
import me.wowtao.pottery.type.WTMode;
import me.wowtao.pottery.utils.CommonUtil;
import me.wowtao.pottery.utils.PotteryTextureManager;
import me.wowtao.pottery.view.ClassicDialog;
import me.wowtao.pottery.view.CustomProgressDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecorateActivity extends BaseActivity {

    private GLView glView;
    private LinearLayout decoratorDisplayView;
    private View decoratorTypes;
    private View decoratorDisplayScrollView;
    private View nextButton;
    private View backButton;
    private ImageButton eraseButton;

    private long time;
    private float customerHeight;
    private View type1;
    private SeekBar changeSize;

    public static boolean isModify = false;
    private static final int REQUEST_MARK = 911;
    private String name;
    private int currentColor;
    private int currentFont;
    private int currentBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isFirst = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isDecorateFirst", true);
        initDecorateId();
        if (isFirst) {
            final ClassicDialog progressDialog = new ClassicDialog(this, R.layout.zuohua);
            progressDialog.findViewById(R.id.root).setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {
                    progressDialog.dismiss();
                    return false;
                }
            });
            android.view.WindowManager.LayoutParams params = progressDialog.getWindow().getAttributes();
            params.y = 0;
            params.gravity = Gravity.TOP;
            progressDialog.getWindow().setAttributes(params);
            progressDialog.show();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isDecorateFirst", false).commit();
        }

        isModify = false;
        setContentView(R.layout.activity_decorate);
        glView = (GLView) findViewById(R.id.pottery);

        decoratorDisplayView = (LinearLayout) findViewById(R.id.decorator);
        setDecorator(WTDecorateTypeEnum.CLASSIC);

        nextButton = findViewById(R.id.next);
        nextButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                next();
            }
        });

        backButton = findViewById(R.id.back_to_home_page_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(DecorateActivity.this).setMessage("您的作品即将丢失,是否继续?").setNegativeButton("否", null).setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
            }
        });

        eraseButton = (ImageButton) findViewById(R.id.erase);
        eraseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentView != null) {
                    currentView.setBackgroundResource(R.drawable.decorate_block);
                }
                PotteryTextureManager.currentDecorator = new WTDecorator(0.15f);
                PotteryTextureManager.isEraseMode = true;
                eraseButton.setImageResource(R.drawable.erase_chosed);
            }
        });

        type1 = findViewById(R.id.decorate_type_1);
        type1.setOnClickListener(new ChooseDecoratorType(WTDecorateTypeEnum.CUSTOM));

        RadioButton viewById = (RadioButton) findViewById(R.id.decorate_type_3);
        viewById.setChecked(true);
        viewById.setOnClickListener(new ChooseDecoratorType(WTDecorateTypeEnum.CLASSIC));
        findViewById(R.id.decorate_type_4).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DecorateActivity.this, BottomActivity.class);
                intent.putExtra("name", name)
                        .putExtra("color", currentColor)
                        .putExtra("font", currentFont)
                        .putExtra("back", currentBackground);
                startActivityForResult(intent, REQUEST_MARK);
            }
        });

        decoratorTypes = findViewById(R.id.decorate_type);
        decoratorDisplayScrollView = findViewById(R.id.decorator_scrollView);

        PotteryTextureManager.currentDecorator = null;
        changeSize = (SeekBar) findViewById(R.id.changeSize);
        changeSize.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            private int progress;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progress == -1) {
                    return;
                }

                if (currentView == null) {
                    return;
                }

                WTDecorator decorator = (WTDecorator) currentView.getTag();
                if (decorator == null) {
                    return;
                }

                Bitmap bitmap = decorator.getOriginal();
                if (bitmap == null) {
                    return;
                }

                //let rate change from 0.6 to 1.4
                float rate = 0.6f + progress / 125.0f;


                int totalWidth = (int) (bitmap.getWidth() * 3 / rate);
                Bitmap bitmap2 = Bitmap.createBitmap(totalWidth, bitmap.getHeight(), Config.ARGB_8888);
                bitmap2.eraseColor(Color.TRANSPARENT);
                Canvas c = new Canvas(bitmap2);
                int left = (bitmap2.getWidth() - bitmap.getWidth()) / 2;
                if (left < 0) {
                    left = 0;
                }
                c.drawBitmap(bitmap, left, 0, null);
                c.save(Canvas.ALL_SAVE_FLAG);
                c.restore();

                String id = decorator.resourceId;
                PotteryTextureManager.setPatternTexture(id, bitmap2);
                PotteryTextureManager.changePatternWidth(id, rate);
                PotteryTextureManager.reloadPattern();
                PotteryTextureManager.isTextureInvalid.value = true;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                if (fromUser) {
                    this.progress = progress;
                } else {
                    this.progress = -1;
                }
            }
        });

    }


    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - time > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    finishAffinity();
                }
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void next() {
        changeSize.setVisibility(View.GONE);
        startFire(780);
    }

    private boolean isFirst = true;
    private boolean notFired = true;

    private void startFire(final int wendu) {
        new AsyncTask<URL, Integer, String>() {
            CustomProgressDialog progressDialog = new CustomProgressDialog(DecorateActivity.this, wendu);
            private float oldSpeed = 0f;

            @Override
            protected void onPreExecute() {
                Wowtao.getGlManager().mode = WTMode.FIRE;

                Wowtao.getGlManager().getPottery().setLum(getFactor(0));
                Wowtao.getGlManager().background.setLum(getFactor(0));
                Wowtao.getGlManager().getTable().setLum(getFactor(0));
                //set pregressdialog position
                android.view.WindowManager.LayoutParams params = progressDialog.getWindow().getAttributes();
                params.y = 100;
                params.gravity = Gravity.TOP;
                progressDialog.getWindow().setAttributes(params);
                //set progressdialog content
                progressDialog.setTitle("正在烧制...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                //hide the menu;
                setMenuVisibility(false);
                Wowtao.getGlManager().background.setTexture(DecorateActivity.this, R.drawable.fire_bgk);
                Wowtao.getGlManager().getTable().switchShader(Table200.FIRE);
                Wowtao.getGlManager().getPottery().switchShader(Pottery200.FIRE);
                oldSpeed = Wowtao.getGlManager().rotateSpeed;
                Wowtao.getGlManager().rotateSpeed = 0;
            }

            @Override
            protected String doInBackground(URL... params) {
                int j = 9;
                for (int i = 1; i < 26; ++i) {
                    int id = CommonUtil.getResId("fire" + Integer.toString(i), R.raw.class);
                    Wowtao.getGlManager().fire.changeTexture(DecorateActivity.this, id);
                    publishProgress((i * 100 / 104));
                    Wowtao.getGlManager().getPottery().setLum(getFactor(i * j));
                    Wowtao.getGlManager().background.setLum(getFactor(i * j));
                    Wowtao.getGlManager().getTable().setLum(getFactor(i * j));
                    try {
                        Thread.sleep(40);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                for (int i = 26; i < 78; ++i) {
                    int id = CommonUtil.getResId("fire" + Integer.toString(i), R.raw.class);
                    Wowtao.getGlManager().fire.changeTexture(DecorateActivity.this, id);
                    publishProgress((i * 100 / 104));
                    try {
                        Thread.sleep(40);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                for (int i = 78; i < 105; ++i) {
                    int id = CommonUtil.getResId("fire" + Integer.toString(i), R.raw.class);
                    Wowtao.getGlManager().fire.changeTexture(DecorateActivity.this, id);
                    publishProgress((i * 100 / 104));
                    Wowtao.getGlManager().getPottery().setLum(getFactor((105 - i) * j));
                    Wowtao.getGlManager().background.setLum(getFactor((105 - i) * j));
                    Wowtao.getGlManager().getTable().setLum(getFactor((105 - i) * j));
                    try {
                        Thread.sleep(40);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return "ok";
            }

            private float getFactor(int i) {
                return i / 150.0f + 0.28f;
            }

            protected void onProgressUpdate(Integer... values) {
                progressDialog.setProgress(values[0]);
            }

            protected void onPostExecute(String result) {
                progressDialog.dismiss();
                if (notFired) {
                    setMenuVisibility(true);
                    Wowtao.getGlManager().getPottery().setLum(getFactor(-42));
                    Wowtao.getGlManager().background.setLum(getFactor(-42));
                    Wowtao.getGlManager().getTable().setLum(getFactor(-42));
                    Wowtao.getGlManager().mode = WTMode.INTERACT_VIEW_AND_DECORATE;
                    glView.setMode(WTMode.INTERACT_VIEW_AND_DECORATE);
                    Wowtao.getGlManager().getTable().setTexture(DecorateActivity.this, R.drawable.shangyoumuwen);
                    Wowtao.getGlManager().background.setTexture(DecorateActivity.this, R.drawable.decorate_background);
                    Wowtao.getGlManager().getPottery().switchShader(Pottery200.CI);
                    Wowtao.getGlManager().getTable().switchShader(Table200.COMMON);
                    PotteryTextureManager.setBaseTexture(DecorateActivity.this.getResources(), R.drawable.w);
                    notFired = false;
                    Wowtao.getGlManager().getPottery().setLum(1f);
                    Wowtao.getGlManager().background.setLum(1f);
                    Wowtao.getGlManager().getTable().setLum(1f);
                } else {
                    Wowtao.getGlManager().getPottery().setLum(getFactor(-42));
                    Wowtao.getGlManager().background.setLum(getFactor(-84));
                    Wowtao.getGlManager().getTable().setLum(getFactor(-42));
                    Wowtao.getGlManager().mode = WTMode.INTERACT_VIEW;
                    Intent intent = new Intent(DecorateActivity.this, PotteryFinishedActivity.class);
                    intent.putExtra("name", name)
                            .putExtra("color", currentColor)
                            .putExtra("font", currentFont)
                            .putExtra("back", currentBackground);

                    glView.onPause();
                    startActivity(intent);
                    DecorateActivity.this.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
                }
                Wowtao.getGlManager().rotateSpeed = oldSpeed;
            }
        }.execute();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wowtao.getGlManager().shadow.setTexture(this, R.drawable.shadow);
        Wowtao.getGlManager().rotateSpeed = 0.0f;
        Wowtao.getGlManager().setEyeOffset(0);
        Wowtao.getGlManager().fire.setTexture(DecorateActivity.this, R.raw.fire0);
        Wowtao.getGlManager().background.position = "center";
        Wowtao.getGlManager().fire.position = "center";
        if (isFirst) {
            PotteryTextureManager.changeBaseTexture(getResources(), R.drawable.clay);
            Wowtao.getGlManager().getPottery().switchShader(Pottery200.DRY_CLAY);
            Wowtao.getGlManager().getTable().setTexture(this, R.drawable.table);
            Wowtao.getGlManager().background.setTexture(this, R.drawable.decorate_background);
            startFire(1280);
            isFirst = false;

        } else {
            Wowtao.getGlManager().getTable().setTexture(DecorateActivity.this, R.drawable.shangyoumuwen);
            Wowtao.getGlManager().background.setTexture(DecorateActivity.this, R.drawable.decorate_background);
            PotteryTextureManager.changeBaseTexture(DecorateActivity.this.getResources(), R.drawable.w);
            Wowtao.getGlManager().getPottery().switchShader(Pottery200.CI);
        }
        if (glFlag) {
            glFlag = false;
        } else {
            glView.onResume();
        }
    }

    public static final String KEY_IS_SHOW_PWD_TIP = "custom_decorate";
    private static final int ZOOM_REQUEST_CODE = 10;
    private static final int IMAGE_REQUEST_CODE = 20;
    private static final int CAMERA_REQUEST_CODE = 30;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // 结果码不等于取消时候
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData());
                    glFlag = true;
                    glFlag2 = true;
                    break;
                case CAMERA_REQUEST_CODE:
                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                    File tempFile = new File(path, TEMP_PHOTO_FILE);
                    startPhotoZoom(Uri.fromFile(tempFile));
                    glFlag = true;
                    glFlag2 = true;
                    break;
                case ZOOM_REQUEST_CODE: // 图片缩放完成后
                    if (data != null) {
                        getImageToView();
                    }
                    glFlag = false;
                    glFlag2 = false;
                    break;

                case REQUEST_MARK:
                    name = data.getStringExtra("name");
                    currentColor = data.getIntExtra("color", 0);
                    currentFont = data.getIntExtra("font", 0);
                    currentBackground = data.getIntExtra("back", 0);
                    break;
            }
        } else {
            glFlag = false;
            glFlag2 = false;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private class ChooseDecoratorType implements OnClickListener {

        private WTDecorateTypeEnum type;

        ChooseDecoratorType(WTDecorateTypeEnum i) {
            type = i;
        }

        @Override
        public void onClick(View v) {
            eraseButton.setImageResource(R.drawable.erase);
            PotteryTextureManager.isEraseMode = false;
            switch (type) {
                case CLASSIC:
                    setDecorator(WTDecorateTypeEnum.CLASSIC);
                    break;
                case CUSTOM:
                    if (Wowtao.getGlManager().getPottery().getCurrentHeight() < 1.125f) {
                        type1.setEnabled(false);
                        new AlertDialog.Builder(DecorateActivity.this).setMessage("当前瓷器高度不足，无法贴上所选图案。").setNeutralButton("确定", null).show();
                    } else {
                        if (Wowtao.getGlManager().getPottery().getCurrentHeight() < 1.875f) {
                            customerHeight = Wowtao.getGlManager().getPottery().getCurrentHeight() - 0.1f;
                        } else {
                            customerHeight = 1.5f;
                        }
                        setDecorator(WTDecorateTypeEnum.CUSTOM);
                    }
                    break;
                default:
                    break;
            }
        }

    }

    private boolean glFlag = false;

    private void getCustomPicture() {
        new AlertDialog.Builder(this).setTitle("从哪里获取图片").setItems(
                new CharSequence[]{"相册", "相机"}, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            photoPickerIntent.setType("image/*");
                            photoPickerIntent.putExtra("outputFormat", CompressFormat.JPEG.toString());
                            photoPickerIntent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());
                            startActivityForResult(photoPickerIntent, IMAGE_REQUEST_CODE);
                        } else {
                            Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intentFromCapture.putExtra("outputFormat", CompressFormat.JPEG.toString());
                            intentFromCapture.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
                            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                            File tempFile = new File(path, TEMP_PHOTO_FILE);
                            // 判断存储卡是否可以用，可用进行存储
                            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                            startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
                        }
                        dialog.dismiss();
                    }
                }).show();
    }

    private SparseArray<Long> ids = new SparseArray<>();

    private void initDecorateId() {
        Long currentId = 0L;
        for (Integer id : decorateResId) {
            ids.put(id, ++currentId);
        }
    }

    private static Integer[] decorateResId = new Integer[]{
            R.drawable.nd1
            , R.drawable.nd2
            , R.drawable.nd3
            , R.drawable.nd5
            , R.drawable.nd7
            , R.drawable.nd8
            , R.drawable.nd9
            , R.drawable.nd10
            , R.drawable.nd11
            , R.drawable.nd12
            , R.drawable.nd13
            , R.drawable.nd14
            , R.drawable.nd15
            , R.drawable.nd16
            , R.drawable.nd17
            , R.drawable.nd18
            , R.drawable.nd19
            , R.drawable.nd20
            , R.drawable.nd21
            , R.drawable.nd22
            , R.drawable.nd23
            , R.drawable.nd24
            , R.drawable.nd25
            , R.drawable.nd26
            , R.drawable.nd27
            , R.drawable.nd28
            , R.drawable.nd29
            , R.drawable.nd30
            , R.drawable.nd31
            , R.drawable.nd32
            , R.drawable.nd33
            , R.drawable.nd34
            , R.drawable.nd35
            , R.drawable.nd36
            , R.drawable.nd37
            , R.drawable.nd38
            , R.drawable.nd39
            , R.drawable.nd40
            , R.drawable.nd41
            , R.drawable.nd42
            , R.drawable.nd43
            , R.drawable.nd44
            , R.drawable.nd45
            , R.drawable.nd46
            , R.drawable.nd47
            , R.drawable.nd48
            , R.drawable.nd49
            , R.drawable.nd50
            , R.drawable.nd51
            , R.drawable.nd52
            , R.drawable.nd53
            , R.drawable.nd54
            , R.drawable.nd55
            , R.drawable.nd56
            , R.drawable.nd57
            , R.drawable.nd58
            , R.drawable.nd59
            , R.drawable.nd60
            , R.drawable.nd61
            , R.drawable.nd62
            , R.drawable.nd63
            , R.drawable.nd64
            , R.drawable.nd65
            , R.drawable.nd66
            , R.drawable.nd67
            , R.drawable.nd68
            , R.drawable.nd69
            , R.drawable.nd70
            , R.drawable.nd71
            , R.drawable.nd72
            , R.drawable.nd73
            , R.drawable.nd74
            , R.drawable.nd75
            , R.drawable.nd76
            , R.drawable.nd77
            , R.drawable.nd78
            , R.drawable.nd79
            , R.drawable.nd80
            , R.drawable.nd81
    };

    private static List<String> customerId = new ArrayList<>();
    private static Map<String, WTDecorator> customerDecorates = new HashMap<>();


    private List<ImageView> decoratorViews = new ArrayList<>();
    private ImageView currentView;
    private boolean glFlag2 = false;
    private boolean isFirstEntry = true;

    private void setDecorator(WTDecorateTypeEnum bw) {

        if (bw == WTDecorateTypeEnum.CUSTOM) {
            changeSize.setVisibility(View.VISIBLE);
            PotteryTextureManager.needP = true;
            //make sure the imageView is enough;
            int needImageView = customerId.size() + 1;
            int avialableImageView = decoratorViews.size();
            adapterImageViewNumber(needImageView, avialableImageView);

            //set decorate to the image view
            int i = 0;
            for (; i < customerId.size(); ++i) {
                String id = customerId.get(i);

                WTDecorator decorator = customerDecorates.get(id);
                decorator.resourceId = id;
                decorator.setWidth(customerHeight);
                changeImageView(decoratorViews.get(i), decorator);
            }

            if (customerId.size() > 0) {
                if (!isFirstEntry) {
                    switchDecorate(decoratorViews.get(0));
                }
            }

            ImageView view = decoratorViews.get(i++);
            view.setImageResource(R.drawable.plus);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getCustomPicture();
                }
            });
            view.setVisibility(View.VISIBLE);
            view.setBackgroundResource(R.drawable.decorate_block);

            for (; i < decoratorViews.size(); ++i) {
                decoratorViews.get(i).setVisibility(View.GONE);
            }
            return;
        }

        if (changeSize == null) {
            changeSize = (SeekBar) findViewById(R.id.changeSize);
        }
        changeSize.setVisibility(View.GONE);
        PotteryTextureManager.needP = false;

        Integer[] decorateResource;
        decorateResource = decorateResId;

        int needImageView = decorateResource.length;
        int availableImageView = decoratorViews.size();
        adapterImageViewNumber(needImageView, availableImageView);
        int i;
        for (i = 0; i < decorateResource.length; ++i) {
            WTDecorator decorator = new WTDecorator();
            decorator.setWidth(getWidthOfDecorator(decorateResource[i]));
            decorator.id = ids.get(decorateResource[i]);
            decorator.resourceId = decorateResource[i].toString();
            changeImageView(decoratorViews.get(i), decorator);
        }

        for (; i < decoratorViews.size(); ++i) {
            decoratorViews.get(i).setVisibility(View.GONE);
        }

        if (!isFirstEntry) {
            switchDecorate(decoratorViews.get(0));
        }
        isFirstEntry = false;
    }

    private float getWidthOfDecorator(Integer id) {
        Options opt = new Options();
        opt.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), id, opt);
        return opt.outHeight / 50f / 8f * 0.7f;
    }


    @Override
    public void onPause() {
        if (glFlag2) {
            glFlag2 = false;
        } else {
            glView.onPause();
        }
        super.onPause();
    }

    private void addToCustomerImage(Bitmap bitmap) {
        int needImageView = customerId.size() + 2;
        int availableImageView = decoratorViews.size();
        adapterImageViewNumber(needImageView, availableImageView);
        try {
            Bitmap bitmap2 = Bitmap.createBitmap(bitmap.getHeight() * 3, bitmap.getHeight(), Config.ARGB_8888);
            bitmap2.eraseColor(Color.TRANSPARENT);
            Canvas c = new Canvas(bitmap2);
            int left = (bitmap2.getWidth() - bitmap.getWidth()) / 2;
            if (left < 0) {
                left = 0;
            }
            c.drawBitmap(bitmap, left, 0, null);
            c.save(Canvas.ALL_SAVE_FLAG);
            c.restore();

            String id = "up" + Long.valueOf(System.currentTimeMillis()).toString();
            OutputStream os = openFileOutput(id, Context.MODE_PRIVATE);
            bitmap2.compress(CompressFormat.PNG, 90, os);
            bitmap2.recycle();
            customerId.add(id);

            WTDecorator decorator = new WTDecorator();
            decorator.resourceId = id;
            decorator.setWidth(customerHeight);
            decorator.setOriginal(bitmap);
            changeImageView(decoratorViews.get(customerId.size() - 1), decorator);
            customerDecorates.put(id, decorator);
            switchDecorate(decoratorViews.get(0));

            PotteryTextureManager.currentDecorator = decorator;

            ImageView view = decoratorViews.get(customerId.size());
            view.setImageResource(R.drawable.plus);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    getCustomPicture();
                }
            });
            decoratorViews.get(customerId.size()).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void adapterImageViewNumber(int needImageView,
                                        int avialableImageView) {
        if (needImageView > avialableImageView) {
            for (int i = 0; i < needImageView - avialableImageView; ++i) {
                ImageView view = (ImageView) getLayoutInflater().inflate(R.layout.decorate_image, decoratorDisplayView, false);
                decoratorDisplayView.addView(view);
                decoratorViews.add(view);
            }
        }
    }

    private void changeImageView(ImageView view, WTDecorator decorator) {
        view.setVisibility(View.VISIBLE);
        view.setImageBitmap(PotteryTextureManager.getPatternTexture(decorator.resourceId, false));
        view.setTag(decorator);
        view.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                switchDecorate(v);
            }

        });
    }

    private void switchDecorate(View view) {
        WTDecorator d = (WTDecorator) view.getTag();

        if (d.getWidth() * 1.05 > Wowtao.getGlManager().getPottery().getHeight()) {
            new AlertDialog.Builder(this).setMessage("当前瓷器高度不足，无法贴上所选图案。")
                    .setNeutralButton("确定", null).show();
            return;
        }

        unChoose();
        if (eraseButton != null) {
            eraseButton.setImageResource(R.drawable.erase);
        }
        PotteryTextureManager.currentDecorator = d;

        if (currentView != null) {
            WTDecorator decorator = (WTDecorator) currentView.getTag();
            decorator.setScale(changeSize.getProgress());
        }
        currentView = (ImageView) view;
        currentView.setBackgroundResource(R.drawable.decorate_block_choosed);
        PotteryTextureManager.isEraseMode = false;
        changeSize.setProgress(((WTDecorator) currentView.getTag()).getScale());
    }

    private void unChoose() {
        if (currentView != null) {
            currentView.setBackgroundResource(R.drawable.decorate_block);
            if (PotteryTextureManager.isEraseMode) {
                PotteryTextureManager.isEraseMode = false;
            }
        }
    }

    private void setMenuVisibility(boolean isShow) {
        if (isShow) {
            showMenu();
            decoratorDisplayScrollView.setVisibility(View.VISIBLE);
            decoratorTypes.setVisibility(View.VISIBLE);
        } else {
            hideMenu();
            decoratorDisplayScrollView.setVisibility(View.GONE);
            decoratorTypes.setVisibility(View.GONE);
        }
    }

    private void hideMenu() {
        nextButton.setVisibility(View.GONE);
        eraseButton.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
    }


    private void showMenu() {
        nextButton.setVisibility(View.VISIBLE);
        eraseButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);
    }


    public GLView getGLView() {
        return glView;
    }


    /**
     * 裁剪图片方法实现
     *
     * @param uri the uri of the photo
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("outputFormat", CompressFormat.PNG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        startActivityForResult(intent, ZOOM_REQUEST_CODE);
    }

    /**
     * 保存裁剪之后的图片数据
     */
    private void getImageToView() {
        Bitmap photo = decodeUriAsBitmap(imageUri);// decode bitmap
        addToCustomerImage(photo);
    }

    private Uri getTempUri() {
        return Uri.fromFile(getTempFile());
    }

    private static final String TEMP_PHOTO_FILE = "temporary_holder.jpg";

    private File getTempFile() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    TEMP_PHOTO_FILE);
            if (!file.exists()) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return file;
        } else {
            return null;
        }
    }

    private Uri imageUri = getTempUri(); // The Uri to store the big bitmap

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap;
        try {
            Options opt = new Options();
            opt.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri), null, opt);
            int wr = opt.outWidth / 400;
            int hr = opt.outHeight / 400;
            opt.inSampleSize = wr < hr ? wr : hr;
            opt.inJustDecodeBounds = false;
            opt.inMutable = true;
            opt.inPreferredConfig = Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(getContentResolver()
                    .openInputStream(uri), null, opt);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        bitmap.setHasAlpha(true);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int alphaWidth = (int) ((width < height ? width : height) * 0.1f);
        float alphaStep = 255f / alphaWidth;

        for (int i = 0; i < alphaWidth; ++i) {
            int a = (int) (alphaStep * i);
            for (int j = i; j < width - i; ++j) {
                int[] x = {j, j};
                int[] y = {i, height - 1 - i};
                for (int k = 0; k < 2; ++k) {
                    addAlpha(x[k], y[k], a, bitmap);
                }
            }

            for (int j = i; j < height - i; ++j) {
                int[] x = {i, width - 1 - i};
                int[] y = {j, j};
                for (int k = 0; k < 2; ++k) {
                    addAlpha(x[k], y[k], a, bitmap);
                }
            }
        }
        return bitmap;
    }

    private void addAlpha(int x, int y, int a, Bitmap bitmap) {
        int color = bitmap.getPixel(x, y);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        bitmap.setPixel(x, y, Color.argb(a, r, g, b));
    }

}
