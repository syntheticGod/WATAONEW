package me.wowtao.pottery.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;
import com.dfire.retail.app.manage.network.AsyncHttpPostForUploadOrder;
import com.dfire.retail.app.manage.network.RequestParameter;
import com.dfire.retail.app.manage.network.RequestResultCallback;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import me.wowtao.pottery.Constants;
import me.wowtao.pottery.R;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.gl.GLView;
import me.wowtao.pottery.gl200.Pottery200;
import me.wowtao.pottery.gl200.Table200;
import me.wowtao.pottery.listener.StartActivityListener;
import me.wowtao.pottery.type.WTMode;
import me.wowtao.pottery.utils.FileUtils;
import me.wowtao.pottery.utils.FileUtils.PotterySaved;
import me.wowtao.pottery.utils.GLManager;
import me.wowtao.pottery.utils.NumberUntil;
import me.wowtao.pottery.utils.PotteryTextureManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PotteryFinishedActivity extends BaseActivity {

    private GLView glView;
    private String from;
    private String fileName = null;
    private int currentColor;
    private int currentFont;
    private int currentBackground;
    private String text;
    private ImageView markImage;

    static final public String appId = "wx15592023243a88b0";
    //    private String appSecret = "e296bc1a2811d10e0c46a62e1ebb8ec9";
    private IWXAPI api;

    private void regToWx() {
        api = WXAPIFactory.createWXAPI(this, appId, true);
        api.registerApp(appId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        regToWx();
        super.onCreate(savedInstanceState);
        from = getIntent().getStringExtra("fromcolloct");

        if (from != null) {
            fileName = getIntent().getStringExtra("fileName");
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(openFileInput(fileName + "1.png"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            PotteryTextureManager.setBaseTextureForCollect(bitmap);
            float[] vertices = (float[]) FileUtils.getSerializable(this, fileName);
            Wowtao.getGlManager().price = (Integer) FileUtils.getSerializable(this, fileName + "p");
            if (vertices != null) {
                Wowtao.getGlManager().getPottery().setVertices(vertices);
            }
            Wowtao.getGlManager().getPottery().fastEstimateNormals();
        } else {
            PotteryTextureManager.changeBaseTexture(this.getResources(), R.drawable.w);
        }


        initUI();
        if (from != null) {
            Wowtao.getGlManager().rotateSpeed = 0.00f;
        } else {
            Wowtao.getGlManager().rotateSpeed = 0.006f;
        }


        text = getIntent().getStringExtra("name");
        currentColor = getIntent().getIntExtra("color", 0);
        currentFont = getIntent().getIntExtra("font", 0);
        currentBackground = getIntent().getIntExtra("back", 0);

    }

    private Bitmap image;

    private ImageButton collect;

    private float maxHeight;

    private View menuBar;

    private View inf;

    private View complete;

    private SeekBar seekBar;

    private void initUI() {
        setContentView(R.layout.activity_pottery_finished);

        glView = (GLView) findViewById(R.id.pottery);

        findViewById(R.id.back_to_home_page_button).setOnClickListener(v -> {
            if (!(Boolean) collect.getTag() && from == null) {
                new AlertDialog.Builder(PotteryFinishedActivity.this).setMessage("作品尚未收藏，是否继续？").setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(PotteryFinishedActivity.this, ShapeActivity.class).putExtra("backToMain", true).putExtra("fromFinish", 1));
                    }
                }).setNegativeButton("否", null).show();
            } else {
                startActivity(new Intent(PotteryFinishedActivity.this, ShapeActivity.class).putExtra("backToMain", true).putExtra("fromFinish", 1));
            }
        });

        findViewById(R.id.buy_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Wowtao.getGlManager().getImage(240, 430);
                Intent intent = new Intent(PotteryFinishedActivity.this, BuyActivity.class);
                if (from != null) {
                    intent.putExtra("from", "collect");
                }
                PotteryFinishedActivity.this.startActivity(intent);
            }
        });
        findViewById(R.id.share_button).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                image = Wowtao.getGlManager().getImage(270, 480);

                File tempFile;
                try {
                    tempFile = File.createTempFile("temp", ".jpg", getCacheDir());
                    image.compress(Bitmap.CompressFormat.JPEG, 90, new FileOutputStream(tempFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }


                RequestParameter rp = new RequestParameter(false);
                rp.setUrl(Constants.UPLOAD_IMAGE_URL);
                rp.setParam("image", tempFile);
                new AsyncHttpPostForUploadOrder(rp, new RequestResultCallback() {
                    @Override
                    public void onSuccess(String str) {
                        JsonElement je = new JsonParser().parse(str.substring(1, str.length() - 1));
                        JsonObject data;
                        try {
                            data = je.getAsJsonObject().get("data").getAsJsonObject();
                        } catch (Exception e) {
                            Toast.makeText(PotteryFinishedActivity.this, "分享到朋友圈失败", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String url = data.get("file_url").getAsString();
                        if (url.indexOf(0) == '.') {
                            url = url.substring(1);
                        }
                        url = url.replace("\\", "");

                        WXWebpageObject webpage = new WXWebpageObject();
                        webpage.webpageUrl = Constants.GET_IMAGE_URL + url;

                        WXMediaMessage msg = new WXMediaMessage(webpage);
                        msg.title = "哇陶分享";
                        msg.description = "这是我用哇陶新作的瓷器，超级好玩的哦!";
                        msg.setThumbImage(image);

                        SendMessageToWX.Req req = new SendMessageToWX.Req();
                        req.transaction = "sp" + System.currentTimeMillis() % 10086;
                        req.message = msg;
                        req.scene = SendMessageToWX.Req.WXSceneTimeline;
                        api.sendReq(req);
                    }

                    @Override
                    public void onFail(Exception e) {
                        e.printStackTrace();
                    }
                }).execute();

            }
        });
        collect = (ImageButton) findViewById(R.id.take_photo);

        if (fileName != null) {
            collect.setTag(true);
        } else {
            collect.setTag(false);
        }

        collect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isChecked = (Boolean) v.getTag();
                if (!isChecked) {
                    toast = Toast.makeText(PotteryFinishedActivity.this, "正在收藏...", Toast.LENGTH_LONG);
                    toast.show();
                    saveCollect();
                } else {
                    deleteCollect();
                    Toast.makeText(PotteryFinishedActivity.this, "已移除收藏", Toast.LENGTH_SHORT).show();
                }
                v.setTag(!isChecked);
            }
        });

        final ImageView width = (ImageView) findViewById(R.id.width);
        Pottery200 pottery = Wowtao.getGlManager().getPottery();
        int widthF = (int) pottery.getMaxWidth();
        widthInt = widthF;
        Bitmap bitmapWidth = NumberUntil.getNumberBitmapI(this, widthF);
        width.setImageBitmap(bitmapWidth);

        final ImageView height = (ImageView) findViewById(R.id.height);
        int heightF = (int) Wowtao.getGlManager().getPottery().getHeightReal();
        heightInt = heightF;
        Bitmap bitmapHeight = NumberUntil.getNumberBitmapI(this, heightF);
        height.setImageBitmap(bitmapHeight);

        seekBar = (SeekBar) findViewById(R.id.seekBar1);
        if (from != null) {
            seekBar.setVisibility(View.GONE);
            int widthT = (Integer) FileUtils.getSerializable(this, fileName + "w");
            int heightT = (Integer) FileUtils.getSerializable(this, fileName + "h");

            Bitmap bitmapHeightt = NumberUntil.getNumberBitmapI(this, heightT);
            height.setImageBitmap(bitmapHeightt);

            Bitmap bitmapWidtht = NumberUntil.getNumberBitmapI(this, widthT);
            width.setImageBitmap(bitmapWidtht);
        } else if (!GLManager.isFix()) {
            //init seekBar
            float maxR = Wowtao.getGlManager().getPottery().getMaxWidth();
            float maxH = 17f / maxR * heightF;
            float maxH2 = heightF * 1.3f;
            maxHeight = Math.min(maxH, maxH2);
            maxHeight = Math.min(maxHeight, 24);

            float minR = Wowtao.getGlManager().getPottery().getMinWidth();
            float minH = 3f / minR * heightF;
            float minH2 = heightF * 0.3f;
            minHeight = Math.max(minH, minH2);
            int oldProgress = (int) ((heightF - minHeight) / (maxHeight - minHeight) * 100);
            seekBar.setProgress(oldProgress);

            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    float heightF = progress / 100f * (maxHeight - minHeight) + minHeight;

                    heightInt = (int) heightF;
                    Bitmap bitmapHeight = NumberUntil.getNumberBitmapI(PotteryFinishedActivity.this, heightInt);
                    height.setImageBitmap(bitmapHeight);

                    float widthF = Wowtao.getGlManager().getPottery().getWidth(heightF);
                    widthInt = (int) (widthF * 8 * 2);
                    Bitmap bitmapWidth = NumberUntil.getNumberBitmapI(PotteryFinishedActivity.this, widthInt);
                    width.setImageBitmap(bitmapWidth);

                }
            });
        } else {
            seekBar.setVisibility(View.GONE);
        }

        menuBar = findViewById(R.id.menu_bar);
        inf = findViewById(R.id.potteryinf);
        markImage = (ImageView) findViewById(R.id.markB);

        complete = findViewById(R.id.next);
        complete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                complete(seekBar, menuBar, inf, complete);
            }

        });
        if (from != null) {
            complete(seekBar, menuBar, inf, complete);
        }

        findViewById(R.id.ffd).setOnClickListener(new StartActivityListener(this, AFFDActivity.class) {
            @Override
            public void onClick(View v) {
                final File file = new File(getExternalCacheDir(), "test.obj");
                Wowtao.getGlManager().getPottery().toOBJFile(file, false);
                try {
                    PotteryTextureManager.getTexture().compress(Bitmap.CompressFormat.JPEG, 100, openFileOutput("textureForFFD.jpg", Activity.MODE_PRIVATE));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                super.onClick(v);
            }
        });

    }

    private int heightInt;
    private int widthInt;

    private void complete(final SeekBar seekBar, final View menuBar,
                          final View inf, final View complete) {
        menuBar.setVisibility(View.VISIBLE);
        inf.setVisibility(View.VISIBLE);
        if (from == null && !GLManager.isFix()) {
            seekBar.setVisibility(View.GONE);
        }
        markImage.setVisibility(View.VISIBLE);
        complete.setVisibility(View.GONE);

        menuBar.animate().alpha(1).setDuration(500).start();
        inf.animate().alpha(1).setDuration(500).start();
        seekBar.animate().alpha(1).setDuration(500).start();
        complete.animate().alpha(0).setDuration(500).start();
        markImage.animate().alpha(1).setDuration(500).start();

        Wowtao.getGlManager().rotateSpeed = 0.0f;

        if (text != null && text.length() != 0) {
            markImage.setImageBitmap(BottomActivity.drawTextToBitmap(
                    this, R.drawable.bottom_texture, text,
                    BottomActivity.getColor(currentBackground, currentColor), BottomActivity.getFont(currentFont, getAssets()), currentBackground));
        }
    }

    private float minHeight;

    private Toast toast;

    private void saveCollect() {
        new Thread(new Runnable() {


            @Override
            public void run() {
                image = Wowtao.getGlManager().getImage(1200, 2100);
                PotterySaved ps = new PotterySaved();
                ps.texture = PotteryTextureManager.getTexture();
                ps.vertices = Wowtao.getGlManager().getPottery().getVertices();
                ps.image = image;
                ps.price = Wowtao.getGlManager().getPrice();
                ps.height = heightInt;
                ps.width = widthInt;
                fileName = FileUtils.savePottery(PotteryFinishedActivity.this, ps);
                PotteryFinishedActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        toast.cancel();
                        Toast.makeText(PotteryFinishedActivity.this, "收藏成功,可在首页我的收藏查看", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void deleteCollect() {
        while (fileName == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        FileUtils.deletePottery(this, fileName);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glView == null) {
            glView = (GLView) findViewById(R.id.pottery);
        }
        glView.setMode(WTMode.INTERACT_VIEW);

        Wowtao.getGlManager().setEyeOffset(0.0f);
        Wowtao.getGlManager().mode = WTMode.INTERACT_VIEW;
        Wowtao.getGlManager().getPottery().switchShader(Pottery200.CI);
        Table200 table = Wowtao.getGlManager().getTable();
        table.switchShader(Table200.COMMON);
        Wowtao.getGlManager().background.setTexture(this, R.drawable.pottery_finished_activity_background);
        Wowtao.getGlManager().shadow.setTexture(this, R.drawable.shadow);
        Wowtao.getGlManager().getTable().setTexture(this, R.drawable.finish_table);
        Wowtao.getGlManager().getPottery().setLum(1f);
        Wowtao.getGlManager().background.setLum(1f);
        Wowtao.getGlManager().getTable().setLum(1f);
        PotteryTextureManager.isTextureInvalid.value = true;

        Wowtao.getGlManager().background.position = "center";
        Wowtao.getGlManager().fire.position = "center";
        Wowtao.getGlManager().needBigImage();

        glView.onResume();
    }

    @Override
    protected void onPause() {
        glView.onPause();
        super.onPause();
    }

}
