package me.wowtao.pottery.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.*;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import me.wowtao.pottery.R;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.gl.GLViewBottom;
import me.wowtao.pottery.type.WTMode;
import me.wowtao.pottery.utils.PotteryTextureManager;
import org.lucasr.twowayview.TwoWayView;

/**
 * Created by ac on 6/8/15.
 *
 */
public class BottomActivity extends BaseActivity implements View.OnClickListener {

    private static final int DEFAULT = R.drawable.songti;
    private static final int KAI = R.drawable.kaiti;
    private static final int Font = 0;
    private static final int STYLE_BLUE = 1;
    private static final int STYLE_BLACK = 2;
    private static int[] fontName = new int[]{R.drawable.songti, R.drawable.kaiti};
    private static int[] markIdBlue = new int[]{R.drawable.i2_cover, R.drawable.i3_cover, R.drawable.i4_cover, R.drawable.i5_cover, R.drawable.i1_cover};
    private static int[] markIdBlueBack = new int[]{R.drawable.i2_back, R.drawable.i3_back, R.drawable.i4_back, R.drawable.i5_back, R.drawable.i1_back};

    private static int[] markIdBlack = new int[]{R.drawable.b2_cover, R.drawable.b3_cover, R.drawable.b4_cover, R.drawable.b5_cover, R.drawable.b1_cover};
    private static int[] markIdBlackBack = new int[]{R.drawable.b2_back, R.drawable.b3_back, R.drawable.b4_back, R.drawable.b5_back, R.drawable.b1_back};

    private static final int BLUE = Color.BLUE;
    private GLViewBottom glView;
    private String m_Text;
    private MarkStyleAdapter adapter;
    private int currentBackground = markIdBlueBack[0];
    private int currentColor = BLUE;
    private int currentFont = DEFAULT;
    private int fontOrStyle = Font;
    private static final int BLACK = Color.BLACK;
    private View lastStyleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom);


        View backButton = findViewById(R.id.back_to_home_page_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        glView = (GLViewBottom) findViewById(R.id.pottery);

        findViewById(R.id.addMark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(BottomActivity.this);
                builder.setTitle("请输入四字落款");
                // Set up the input
                final EditText input = new EditText(BottomActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("完成", null);

                final AlertDialog myDialog = builder.create();

                myDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button b = myDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                m_Text = input.getText().toString();
                                if (m_Text.length() > 4) {
                                    new AlertDialog.Builder(BottomActivity.this)
                                            .setMessage("最多只能输入四个字的落款")
                                            .setNegativeButton("好的", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            }).show();
                                    m_Text = null;
                                } else if (m_Text.length() == 0) {
                                    Wowtao.getGlManager().bottom.setTexture(BitmapFactory.decodeResource(getResources(), R.drawable.bottom_texture));
                                    myDialog.dismiss();
                                } else {
                                    Wowtao.getGlManager().bottom.setTexture(drawTextToBitmap(
                                            BottomActivity.this, R.drawable.bottom_texture, m_Text, getColor(currentBackground, currentColor), getFont(currentFont, getAssets()), currentBackground));
                                    myDialog.dismiss();
                                }
                            }
                        });
                    }
                });

                myDialog.show();
            }
        });

        findViewById(R.id.finishMark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent()
                        .putExtra("name", m_Text)
                        .putExtra("color", currentColor)
                        .putExtra("font", currentFont)
                        .putExtra("back", currentBackground));
                finish();
            }
        });

        TwoWayView twoWayView = (TwoWayView) findViewById(R.id.mark_styles);
        adapter = new MarkStyleAdapter(fontName, fontName);
        twoWayView.setAdapter(adapter);

        twoWayView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (lastStyleView != null) {
                    lastStyleView.setBackgroundResource(R.drawable.decorate_block);
                }
                View imageView = view.findViewById(R.id.styleImage);
                imageView.setBackgroundResource(R.drawable.decorate_block_choosed);
                lastStyleView = imageView;
                if (fontOrStyle == Font) {
                    currentFont = (int) view.getTag();
                } else {
                    currentBackground = (int) view.getTag();
                }
            }
        });

        findViewById(R.id.decorate_type_1).setOnClickListener(this);
        findViewById(R.id.decorate_type_2).setOnClickListener(this);
        findViewById(R.id.decorate_type_3).setOnClickListener(this);

        Intent data = getIntent();
        String text = data.getStringExtra("name");
        int color = data.getIntExtra("color", 0);
        int font = data.getIntExtra("font", 0);
        int background = data.getIntExtra("back", 0);

        if (!TextUtils.isEmpty(text)) {
            Wowtao.getGlManager().bottom.setTexture(drawTextToBitmap(
                    BottomActivity.this, R.drawable.bottom_texture, text,
                    getColor(background, color), getFont(font, getAssets()), background));
        }
    }

    static int getColor(int backgroundId, int color) {
        if (backgroundId == markIdBlueBack[1] ||
                backgroundId == markIdBlueBack[3] ||
                backgroundId == markIdBlackBack[1] ||
                backgroundId == markIdBlackBack[3]) {
            return Color.WHITE;
        } else {
            return color;
        }
    }

    static Typeface getFont(int font, AssetManager assetManager) {
        if (font == KAI) {
            return Typeface.createFromAsset(assetManager, "hwkt.ttf");
        } else {
            return Typeface.DEFAULT;
        }
    }


    static Bitmap drawTextToBitmap(Context gContext, int textureResId, String gText,
                                   int color, Typeface font, int backgroundResId) {
        Resources resources = gContext.getResources();
        float scale = resources.getDisplayMetrics().density;
        Bitmap bitmap =
                BitmapFactory.decodeResource(resources, textureResId);

        Bitmap.Config bitmapConfig =
                bitmap.getConfig();
        // set default bitmap config if none
        if (bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are immutable,
        // so we need to convert it to mutable one
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        // new antiAliased Paint
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // text color - #3D3D3D

        paint.setColor(color);
        // text size in pixels
        paint.setTextSize((int) (14 * scale));
        // text shadow
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

        //typeFace
        paint.setTypeface(font);

        // draw text to the Canvas center
        Rect bounds = new Rect();
        paint.getTextBounds("你好", 0, 2, bounds);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (canvas.getHeight() - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()

        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() - bounds.height() * 2) / 2;
        paint.setTextAlign(Paint.Align.CENTER);

        if (backgroundResId == markIdBlueBack[1] ||
                backgroundResId == markIdBlueBack[3]) {
            paint.setColor(Color.WHITE);
        }

        Bitmap background = BitmapFactory.decodeResource(resources, backgroundResId, null);
        Rect rectSource = new Rect(0, 0, background.getWidth(), background.getHeight());
        Rect rectDest;
        if (backgroundResId == markIdBlueBack[4] || backgroundResId == markIdBlackBack[4]) {
            rectDest = new Rect(x - 125, y - 125, x + bounds.width() + 125, y + bounds.height() * 2 + 125);
        } else {
            rectDest = new Rect(x - 25, y - 25, x + bounds.width() + 25, y + bounds.height() * 2 + 25);
        }
        canvas.drawBitmap(background, rectSource, rectDest, paint);

        switch (gText.length()) {
            case 1:
                canvas.drawText(gText, canvas.getHeight() / 2, baseline, paint);
                break;
            case 2:
                canvas.drawText(gText.substring(0, 2), canvas.getHeight() / 2, baseline, paint);
                break;
            case 3:
                canvas.drawText(gText.substring(0, 2), canvas.getHeight() / 2, baseline - bounds.height() / 2, paint);
                canvas.drawText(gText.substring(2) + "　", canvas.getHeight() / 2, baseline + bounds.height() / 2, paint);
                break;
            case 4:
                canvas.drawText(gText.substring(0, 2), canvas.getHeight() / 2, baseline - bounds.height() / 2, paint);
                canvas.drawText(gText.substring(2), canvas.getHeight() / 2, baseline + bounds.height() / 2, paint);
                break;
        }

        return bitmap;
    }


    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Wowtao.getGlManager().setEyeOffset(0.0f);
        Wowtao.getGlManager().mode = WTMode.VIEW;
        Wowtao.getGlManager().background.setTexture(this, R.drawable.decorate_background);
        Wowtao.getGlManager().bottom.setTexture(this, R.drawable.bottom_texture);
        Wowtao.getGlManager().background.setLum(1f);
        PotteryTextureManager.isTextureInvalid.value = true;

        Wowtao.getGlManager().background.position = "center";
        Wowtao.getGlManager().fire.position = "center";

        glView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glView.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.decorate_type_1:
                adapter.setData(fontName, fontName);
                fontOrStyle = Font;
                break;

            case R.id.decorate_type_2:
                currentColor = BLUE;
                adapter.setData(markIdBlue, markIdBlueBack);
                fontOrStyle = STYLE_BLUE;
                break;

            case R.id.decorate_type_3:
                currentColor = BLACK;
                adapter.setData(markIdBlack, markIdBlackBack);
                fontOrStyle = STYLE_BLACK;
                break;
        }
    }

    private class MarkStyleAdapter extends BaseAdapter {

        private int[] backgrounds;
        int[] data = new int[0];

        MarkStyleAdapter(int[] markId, int[] backgrounds) {
            this.data = markId;
            this.backgrounds = backgrounds;
        }

        void setData(int[] markId, int[] backgrounds) {
            this.data = markId;
            this.backgrounds = backgrounds;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        View[] blueViews = new View[markIdBlue.length];
        View[] blackViews = new View[markIdBlack.length];
        View[] fontViews = new View[2];
        View[][] views = new View[3][];

        {
            views[Font] = fontViews;
            views[STYLE_BLUE] = blueViews;
            views[STYLE_BLACK] = blackViews;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (views[fontOrStyle][i] == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(BottomActivity.this);
                view = layoutInflater.inflate(R.layout.bottom_mark_style, viewGroup, false);
                views[fontOrStyle][i] = view;
                if (i == 0 && (fontOrStyle == Font || fontOrStyle == STYLE_BLUE)) {
                    View imageView = view.findViewById(R.id.styleImage);
                    imageView.setBackgroundResource(R.drawable.decorate_block_choosed);
                    lastStyleView = imageView;
                }
            } else {
                view = views[fontOrStyle][i];
            }
            ImageView imageView = (ImageView) view.findViewById(R.id.styleImage);
            imageView.setImageResource(data[i]);
            view.setTag(backgrounds[i]);

            return view;
        }
    }
}
