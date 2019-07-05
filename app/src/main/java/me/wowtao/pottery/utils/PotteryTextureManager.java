package me.wowtao.pottery.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.activity.DecorateActivity;
import me.wowtao.pottery.gl.Pottery;
import me.wowtao.pottery.type.WTDecorator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.opengl.GLES32.*;
import static java.lang.Math.abs;

public class PotteryTextureManager {

    public static void setTexture(Bitmap texture) {
        PotteryTextureManager.texture = texture;
    }

    private static Bitmap texture;
    private static Bitmap textureTempBackup;
    private static Bitmap originalTexture;

    private static int textureWidth_PIX;
    private static int textureHeight_PIX;

    public final static MyBoolean isTextureInvalid = new MyBoolean(false);
    private final static int[] GLTextureNames = new int[1];

    private static String[] occupied = new String[Pottery.VERTICAL_PRECISION * 10];
    private static boolean needLoadForce = false;

    private static Map<String, Pattern> patterns = new HashMap<>();

    public static Map<String, Pattern> getPatterns() {
        return patterns;
    }

    public static boolean isEraseMode = false;

    public static boolean needP = false;

    public static void changeBaseTexture(Resources resource, int id) {
        if (textureTempBackup != null) {
            textureTempBackup.recycle();
            textureTempBackup = null;
        }
        if (originalTexture != null) {
            originalTexture.recycle();
            originalTexture = null;
        }
        System.gc();

        Options opts = new Options();
        opts.inMutable = true;
        texture = BitmapFactory.decodeStream(resource.openRawResource(id), null, opts);
        originalTexture = texture.copy(Config.ARGB_8888, true);
        textureWidth_PIX = texture.getWidth();
        textureHeight_PIX = texture.getHeight() / 2;
        reloadPattern();
        isTextureInvalid.value = true;
    }

    public static void setBaseTexture(Resources resource, int id) {
        if (textureTempBackup != null) {
            textureTempBackup.recycle();
            textureTempBackup = null;
        }
        if (originalTexture != null) {
            originalTexture.recycle();
            originalTexture = null;
        }
        System.gc();
        Options opts = new Options();
        opts.inMutable = true;
        texture = BitmapFactory.decodeStream(resource.openRawResource(id), null, opts);
        originalTexture = texture.copy(Config.ARGB_8888, true);
        textureWidth_PIX = texture.getWidth();
        textureHeight_PIX = texture.getHeight() / 2;
        for (int i = 0; i < Pottery.VERTICAL_PRECISION * 10; ++i) {
            occupied[i] = null;
        }
        patterns.clear();
        isTextureInvalid.value = true;
    }

    public static void setBaseTextureForCollect(Bitmap bitmap) {
        if (texture != null) {
            texture.recycle();
            texture = null;
        }
        if (textureTempBackup != null) {
            textureTempBackup.recycle();
            textureTempBackup = null;
        }
        if (originalTexture != null) {
            originalTexture.recycle();
            originalTexture = null;
        }
        System.gc();
        texture = bitmap.copy(Config.ARGB_8888, true);
        bitmap.recycle();
        System.gc();
        originalTexture = texture.copy(Config.ARGB_8888, true);
        textureWidth_PIX = texture.getWidth();
        textureHeight_PIX = texture.getHeight() / 2;
        isTextureInvalid.value = true;
    }

    public static Bitmap getTexture() {
        return texture;
    }


    public static void reloadPattern() {
        texture = originalTexture.copy(Config.ARGB_8888, true);
        Canvas canvas = new Canvas(texture);
        for (Pattern pattern : patterns.values()) {
            occupy(pattern.bottom, pattern.top, pattern);
            Bitmap tempTexture = getPatternTexture(pattern.idResource, true);

            if (!pattern.needP) {
                addTexture(canvas, pattern, tempTexture);
            }
        }

        for (Pattern pattern : patterns.values()) {
            occupy(pattern.bottom, pattern.top, pattern);
            Bitmap tempTexture = getPatternTexture(pattern.idResource, true);

            if (pattern.needP) {
                addTexture(canvas, pattern, tempTexture);
            }
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
    }

    private static void addTexture(Canvas canvas, Pattern pattern, Bitmap decorator) {
        float dx = pattern.dx;
        float y = pattern.heightF + pattern.topF;

        Matrix myMatrix = new Matrix();
        float[] src = getDrawRegion(0, 0, decorator.getWidth(), decorator.getHeight());
        final float dstWidth = pattern.widthF;
        float offset = textureWidth_PIX * (1 - dx) - dstWidth / 2;
        float right = offset + dstWidth;
        float[] dst = getDrawRegion(offset, pattern.topF, right, y);
        myMatrix.setPolyToPoly(src, 0, dst, 0, 4);
        canvas.drawBitmap(decorator, myMatrix, null);
        if (offset < 0) {
            offset += textureWidth_PIX;
            right = offset + dstWidth;
            dst = getDrawRegion(offset, pattern.topF, right, y);
            myMatrix.setPolyToPoly(src, 0, dst, 0, 4);
            canvas.drawBitmap(decorator, myMatrix, null);
        } else if (right > textureWidth_PIX) {
            offset -= textureWidth_PIX;
            right = offset + dstWidth;
            dst = getDrawRegion(offset, pattern.topF, right, y);
            myMatrix.setPolyToPoly(src, 0, dst, 0, 4);
            canvas.drawBitmap(decorator, myMatrix, null);
        }
        DecorateActivity.isModify = true;
    }


    private static void addTexture(int bottom, int top, Object decorator_, float horizontal_factor) {
        synchronized (isTextureInvalid) {
            int middleNewPattern = (bottom + top) / 2;
            Set<String> checkOccupied = checkOccupied(bottom, top);

            float topF = (float) top / (float) Pottery.VERTICAL_PRECISION / 10;
            float bottomF = (float) bottom / (float) Pottery.VERTICAL_PRECISION / 10;
            Canvas canvas = new Canvas(texture);
            int dstHeight = (int) ((topF - bottomF) * textureHeight_PIX);
            float drawTopF = textureHeight_PIX * (2 - topF);
            if (isEraseMode) {
                if (checkOccupied.size() != 0) {
                    if (eraseMode == 0) {
                        String erasedPatternId = getErasedPatternId(checkOccupied, middleNewPattern);
                        Pattern tempP = patterns.get(erasedPatternId);
                        if (decorator_ == null) {
                            Paint paint = new Paint();
                            paint.setColor(Color.parseColor("#00FFFFFF"));
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawRect(0, tempP.topF, textureWidth_PIX, tempP.topF + tempP.heightF, paint);
                        } else {
                            deletePattern(erasedPatternId);
                            reloadPattern();
                        }
                    } else {
                        if (decorator_ != null) {
                            deletePattern(checkOccupied);
                            reloadPattern();
                        }
                    }
                    canvas.save(Canvas.ALL_SAVE_FLAG);
                    canvas.restore();
                    isTextureInvalid.value = true;
                }
                return;
            }
            Bitmap decorator = (Bitmap) decorator_;
            if (decorator != null) {
                float dx = ((Wowtao.getGlManager().horizontalAngle + 450 - 150 * horizontal_factor) % 360) / 360f;

                final float dstWidth = (float) (textureWidth_PIX
                        * (decorator.getWidth() / 50f / 8f)
                        / Wowtao.getGlManager().getPottery().getPerimeter((top + bottom) / 2)
                        * 0.7);
                Pattern pattern = new Pattern(top, bottom, drawTopF, dstHeight, dstWidth, currentDecorator.resourceId, needP, currentDecorator.id, dx);
                patterns.put(pattern.idResource + pattern.top, pattern);
                occupy(bottom, top, pattern);

                Matrix myMatrix = new Matrix();
                float[] src = getDrawRegion(0, 0, decorator.getWidth(), decorator.getHeight());
                float y = dstHeight + drawTopF;
                float offset = textureWidth_PIX * (1 - dx) - dstWidth / 2;
                float right = offset + dstWidth;
                float[] dst = getDrawRegion(offset, drawTopF, right, y);
                myMatrix.setPolyToPoly(src, 0, dst, 0, 4);
                canvas.drawBitmap(decorator, myMatrix, null);
                if (offset < 0) {
                    offset += textureWidth_PIX;
                    right = offset + dstWidth;
                    dst = getDrawRegion(offset, drawTopF, right, y);
                    myMatrix.setPolyToPoly(src, 0, dst, 0, 4);
                    canvas.drawBitmap(decorator, myMatrix, null);
                } else if (right > textureWidth_PIX) {
                    offset -= textureWidth_PIX;
                    right = offset + dstWidth;
                    dst = getDrawRegion(offset, drawTopF, right, y);
                    myMatrix.setPolyToPoly(src, 0, dst, 0, 4);
                    canvas.drawBitmap(decorator, myMatrix, null);
                }

                DecorateActivity.isModify = true;
            } else {
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#00ffffff"));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawRect(0, drawTopF, textureWidth_PIX, drawTopF + dstHeight, paint);
            }
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
            isTextureInvalid.value = true;
        }
    }

    private static float[] getDrawRegion(float left, float top, float right, float bottom) {
        return new float[]{left, top,
                right, top,
                right, bottom,
                left, bottom};
    }

    private static String getErasedPatternId(Set<String> checkOccupied,
                                             int middleNewPattern) {
        int gapP = 100000;
        int gap = 100000;
        String keyNormal = null;
        String keyP = null;
        for (String key : checkOccupied) {
            Pattern p = patterns.get(key);
            int temp = abs(middleNewPattern - (p.bottom + p.top) / 2);
            if (p.needP) {
                if (gapP > temp) {
                    gapP = temp;
                    keyP = key;
                }
            } else {
                if (gap > temp) {
                    gap = temp;
                    keyNormal = key;
                }
            }
        }
        if (keyP != null) {
            return keyP;
        } else {
            return keyNormal;
        }
    }

    private static void deletePattern(Set<String> checkOccupied) {
        if (needP) {
            for (String id : checkOccupied) {
                Pattern pattern = patterns.get(id);
                if (pattern.needP) {
                    patterns.remove(id);
                    for (int i = pattern.bottom; i < pattern.top; ++i) {
                        occupied[i] = null;
                    }
                }
            }
        } else {
            for (String id : checkOccupied) {
                Pattern pattern = patterns.get(id);
                patterns.remove(id);
                for (int i = pattern.bottom; i < pattern.top && i < 500; ++i) {
                    occupied[i] = null;
                }
            }
        }
    }

    private static void deletePattern(String id) {
        Pattern pattern = patterns.get(id);
        patterns.remove(id);
        for (int i = pattern.bottom; i < pattern.top; ++i) {
            occupied[i] = null;
        }
    }

    private static Set<String> checkOccupied(int bottom, int top) {
        Set<String> result = new HashSet<>();
        if (top >= 500) {
            top = 499;
        }
        if (bottom < 0) {
            bottom = 0;
        }
        for (int i = bottom; i < top; ++i) {
            if (occupied[i] != null) {
                result.add(occupied[i]);
            }
        }
        return result;
    }

    private static void occupy(int bottom, int top, Pattern pattern) {
        if (bottom < 0) {
            bottom = 0;
        }
        if (top > 499) {
            top = 499;
        }
        for (int i = bottom; i < top; ++i) {
            occupied[i] = pattern.idResource + pattern.top;
        }
    }

    public static void loadTexture() {
        if (GLTextureNames[0] == 0) {
            synchronized (GLTextureNames) {
                if (GLTextureNames[0] == 0) {
                    glGenTextures(1, GLTextureNames, 0);
                }
            }
        }

        glBindTexture(GL_TEXTURE_2D, GLTextureNames[0]);

        if (isTextureInvalid.value) {
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, texture, 0);
            isTextureInvalid.value = false;
        } else if (needLoadForce) {
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, texture, 0);
            needLoadForce = false;
        }

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    private static void preDecorate_(int bottom, int top) {
        textureTempBackup = texture.copy(Config.ARGB_8888, true);
        addTexture(bottom, top, null, 0);
    }

    private static void tempDecorate_(int bottom, int top) {
        if (textureTempBackup == null) {
            return;
        }
        texture = textureTempBackup.copy(Config.ARGB_8888, true);
        addTexture(bottom, top, null, 0);
    }

    private static void finalDecorate_(int bottom, int top, String currentId, float horizontal_factor) {
        if (textureTempBackup == null) {
            return;
        }
        texture = textureTempBackup;
        textureTempBackup = null;
        if (isEraseMode) {
            addTexture(bottom, top, new Object(), horizontal_factor);
        } else {
            Bitmap patternTexture = getPatternTexture(currentId, true);
            addTexture(bottom, top, patternTexture, horizontal_factor);
        }

        needLoadForce = true;
    }

    private static Map<String, SoftReference<Bitmap>> texturePool = new HashMap<>();

    public static void setPatternTexture(String id, Bitmap bitmap) {
        texturePool.put(id, new SoftReference<>(bitmap));
    }

    public static Bitmap getPatternTexture(String id, boolean needSave) {
        SoftReference<Bitmap> softReference = texturePool.get(id);
        Bitmap res;
        if (softReference == null || (res = softReference.get()) == null) {
            res = getBitmapFromPatternId(id);
            if (needSave) {
                texturePool.put(id, new SoftReference<>(res));
            }
        }
        return res;
    }

    @NonNull
    private static Bitmap getBitmapFromPatternId(String id) {
        try {
            int i = Integer.parseInt(id);
            return BitmapFactory.decodeStream(Wowtao.getGlManager().context.getResources().openRawResource(i));
        } catch (Exception e) {
            try {
                Context context = Wowtao.getGlManager().context;
                FileInputStream is = context.openFileInput(id);
                return BitmapFactory.decodeStream(is);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    public static void setIsInvalidGL() {
        GLTextureNames[0] = 0;
    }

    public static WTDecorator currentDecorator;

    public static class Pattern implements Serializable {

        private final float dx;

        private static final long serialVersionUID = -7459629728473683679L;
        final float widthF;

        public int top;
        public int bottom;
        float topF;
        int heightF;
        public String idResource;
        public boolean needP;
        public Long id;
        float topFBackup;
        int heightFBackup;

        Pattern(int top, int bottom, float topF, int heightF, float dstWidth, String idResource, boolean needP, Long id, float dx) {
            this.top = top;
            this.bottom = bottom;
            this.topF = topF;
            this.topFBackup = topF;
            this.heightF = heightF;
            this.heightFBackup = heightF;
            this.idResource = idResource;
            this.needP = needP;
            this.id = id;
            this.dx = dx;
            this.widthF = dstWidth;
        }

    }

    private static float beginY;

    public static void preDecorate(float y, float height) {
        float yInPottery = computerYInPottery(y, height);
        beginY = yInPottery;
        if (yInPottery < -0.15 || yInPottery - Wowtao.getGlManager().getPottery().getHeight() >= 0.3) {
            return;
        }
        if (currentDecorator == null) {
            return;
        }

        int[] border = computerBorder(yInPottery, currentDecorator.getWidth(), Wowtao.getGlManager().getPottery().getHeight());
        preDecorate_(border[0], border[1]);
    }

    public static void tempDecorate(float y, float height) {
        float yInPottery = computerYInPottery(y, height);
        if (yInPottery < -0.15 || yInPottery - Wowtao.getGlManager().getPottery().getHeight() >= 0.3) {
            return;
        }
        if (currentDecorator == null) {
            return;
        }

        int[] border = computerBorder(yInPottery, currentDecorator.getWidth(), Wowtao.getGlManager().getPottery().getHeight());
        tempDecorate_(border[0], border[1]);
    }

    private static int eraseMode;

    public static void finalDecorate(float x, float y, float height, float width) {
        float yInPottery = computerYInPottery(y, height);
        if (yInPottery < -0.15 || yInPottery - Wowtao.getGlManager().getPottery().getHeight() >= 0.3) {
            PotteryTextureManager.reloadPattern();
            isTextureInvalid.value = true;
            return;
        }

        if (currentDecorator == null) {
            return;
        }

        int[] border;
        if (isEraseMode) {
            float mid = (yInPottery + beginY) / 2;
            float w = abs(yInPottery - beginY);
            if (w > 0.5) {
                eraseMode = 1;
                border = computerBorder(mid, w, Wowtao.getGlManager().getPottery().getHeight());
            } else {
                eraseMode = 0;
                border = computerBorder(yInPottery, currentDecorator.getWidth(), Wowtao.getGlManager().getPottery().getHeight());
            }
        } else {
            border = computerBorder(yInPottery, currentDecorator.getWidth(), Wowtao.getGlManager().getPottery().getHeight());
        }

        String currentId = currentDecorator.resourceId;

        finalDecorate_(border[0], border[1], currentId, (x - width / 2) / (width / 2));
    }

    static private int[] computerBorder(float yInPottery, float width, float height) {
        int[] border = new int[2];
        if (yInPottery > height) {
            yInPottery = height;
        } else if (yInPottery < 0) {
            yInPottery = 0;
        }
        float bottom = yInPottery - width / 2;
        float top = yInPottery + width / 2;
        if (top > height) {
            bottom -= (top - height);
            top = height;
        }
        if (bottom < 0) {
            top += -bottom;
            bottom = 0;
        }
        border[0] = (int) (bottom / height * Pottery.VERTICAL_PRECISION * 10);
        border[1] = (int) (top / height * Pottery.VERTICAL_PRECISION * 10);
        return border;
    }

    private final static float TAN_22_5 = 0.40402622583516f;

    private static float computerYInPottery(float y, float height) {
        float verticalAngle = Wowtao.getGlManager().getVerticalAngle();
        float aux = (y / height - 0.35f) / 0.65f / 3f;
        if (aux < 0) {
            aux = 0;
        }
        float temp = 1 + verticalAngle / 100;
        return ((0.5f * height - y) / height * 2 * TAN_22_5 * 6f + 1.9f) * temp + aux * (verticalAngle / 25);
    }

    public static void changePatternWidth(String id, float rate) {
        for (Pattern pattern : patterns.values()) {
            if (pattern.idResource.equals(id)) {
                pattern.topF = pattern.topFBackup - pattern.heightFBackup * (rate - 1) / 2;

                pattern.heightF = (int) (pattern.heightFBackup * rate);
            }
        }
    }

}

