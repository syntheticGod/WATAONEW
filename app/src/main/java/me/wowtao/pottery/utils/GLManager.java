package me.wowtao.pottery.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import me.wowtao.pottery.Wowtao;
import me.wowtao.pottery.gl.Shadow;
import me.wowtao.pottery.gl200.*;
import me.wowtao.pottery.shader.WTShader;
import me.wowtao.pottery.type.WTMode;
import me.wowtao.pottery.utils.PotteryTextureManager.Pattern;

import javax.microedition.khronos.opengles.GL10;
import java.nio.IntBuffer;
import java.util.Collection;

public class GLManager {

    public WTMode mode;
    public Background200 background;
    public Fire fire;
    public Shadow shadow;
    private Pottery200 pottery;
    private int needBigImage;
    public Bottom200 bottom;
    private static boolean isFix;

    public static boolean isFix() {
        return isFix;
    }

    public static void setIsFix(boolean isFix) {
        GLManager.isFix = isFix;
    }

    public Pottery200 getPottery() {
        return pottery;
    }

    public Table200 getTable() {
        return table;
    }

    private Table200 table;

    public Context context;

    private final static float TAN_22_5 = 0.40402622583516f;

    float horizontalAngle;
    public float rotateSpeed;

    float getVerticalAngle() {
        return verticalAngle;
    }

    private float verticalAngle;
    private int direction;
    private float eyeOffset;
    private Thread moveBackgroundThread;
    private Thread rotateThread;
    private Bitmap bigTempImage;

    public void initForGLFFD() {
        background.createShader(context.getResources());
        WTShader.addGLMeshObject(background);

        WTShader.initVBO();
        alreadyInitGL = true;
    }

    public void initForGLBottom() {
        WTShader.initForAllShader();

        background.createShader(context.getResources());
        WTShader.addGLMeshObject(background);

        bottom.createShader(eyeOffset);
        WTShader.addGLMeshObject(bottom);

        WTShader.initVBO();
        alreadyInitGL = true;
    }

    public void initForGL() {
        WTShader.initForAllShader();
        pottery.createShader(eyeOffset, context.getResources());
        WTShader.addGLMeshObject(pottery);

        background.createShader(context.getResources());
        fire.createShader(context.getResources());
        WTShader.addGLMeshObject(background);
        WTShader.addGLMeshObject(fire);

        table.createShader(eyeOffset);
        WTShader.addGLMeshObject(table);

        ((Shadow200) shadow).createShader(eyeOffset, context.getResources());
        WTShader.addGLMeshObject(shadow);

        WTShader.initVBO();
        alreadyInitGL = true;
    }

    public void changeFrustum(int width, int height) {
        float ratio = (float) width / height;
        float left = -ratio * TAN_22_5;
        float right = ratio * TAN_22_5;
        float bottom = -TAN_22_5;
        float top = TAN_22_5;
        float near = 1.0f;
        float far = 100.0f;
        WTShader.frustumM(left, right, bottom, top, near, far);
        background.genPosition(left, right, bottom, top, near, far);
        fire.genPosition(left, right, bottom, top, near, far);
    }


    public void changeGesture(float deltaX, float deltaY) {
        horizontalAngle += deltaX / 6;
        verticalAngle += deltaY / 6;
        if (verticalAngle > 45) {
            verticalAngle = 45;
        } else if (verticalAngle < -10) {
            verticalAngle = -10;
        }
    }

    private float computerYInPottery(float y, float height) {
        return (0.5f * height - y) / height * 2 * TAN_22_5 * 6f + 2.1f;
    }

    public void init(Context context) {
        this.context = context;

        pottery = new Pottery200();
        table = new Table200(context, "table.obj");
        bottom = new Bottom200(context, "bottom.obj");
        shadow = new Shadow200(context, "shadow.obj");
        background = new Background200();
        fire = new Fire();

        if (rotateThread == null) {
            rotateThread = new Thread() {
                @Override
                public void run() {
                    long startTime = System.currentTimeMillis();
                    while (!this.isInterrupted()) {
                        long temp = System.currentTimeMillis();
                        horizontalAngle += (temp - startTime) * rotateSpeed;
                        startTime = temp;
                        horizontalAngle %= 360;
                    }
                }
            };
            rotateThread.start();
        }
        if (moveBackgroundThread == null) {

            moveBackgroundThread = new Thread() {
                @Override
                public void run() {
                    MySensor.openAccelerometer();
                    while (!this.isInterrupted()) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        float[] data = MySensor.getAccelerometerData();
                        if (data != null) {
                            data = data.clone();
                            float current[] = background.getVertices().clone();
                            float alpha = 0.2f;
                            float vertices[] = new float[12];
                            data[0] = data[0] * 0.1f;
                            data[1] = data[1] * 0.1f;
                            vertices[0] = (background.originalVertices[0] - data[0] - current[0]) * alpha + current[0];
                            vertices[1] = (background.originalVertices[1] - data[1] - current[1]) * alpha + current[1];
                            vertices[2] = background.originalVertices[2];
                            vertices[3] = (background.originalVertices[3] - data[0] - current[3]) * alpha + current[3];
                            vertices[4] = (background.originalVertices[4] - data[1] - current[4]) * alpha + current[4];
                            vertices[5] = background.originalVertices[5];
                            vertices[6] = (background.originalVertices[6] - data[0] - current[6]) * alpha + current[6];
                            vertices[7] = (background.originalVertices[7] - data[1] - current[7]) * alpha + current[7];
                            vertices[8] = background.originalVertices[8];
                            vertices[9] = (background.originalVertices[9] - data[0] - current[9]) * alpha + current[9];
                            vertices[10] = (background.originalVertices[10] - data[1] - current[10]) * alpha + current[10];
                            vertices[11] = background.originalVertices[11];
                            background.setVertices(vertices);
                        }
                    }
                }
            };

            moveBackgroundThread.start();

        }
        alreadyInit = true;
    }

    public void setEyeOffset(float eyeOffset) {
        if (alreadyInit) {
            table.setOffset(eyeOffset);
            pottery.setOffset(eyeOffset);
            ((Shadow200) shadow).setOffset(eyeOffset);
            background.setEyeLookAt(eyeOffset);
            this.eyeOffset = eyeOffset;
        }
    }


    public boolean reshape(float lastX, float lastY, float currentX,
                           float currentY, float width, float height) {
        setIsFix(false);
        if (pottery != null) {
            float deltaX = currentX - lastX;
            float deltaY = currentY - lastY;
            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                float y = computerYInPottery(currentY, height);
                if (currentX > width * 0.5f && deltaX > 0
                        || currentX < width * 0.5f && deltaX < 0) {
                    pottery.fatter(y);
                }
                if (currentX < width * 0.5f && deltaX > 0
                        || currentX > width * 0.5f && deltaX < 0) {
                    pottery.thinner(y);
                }

            } else {
                if (deltaY < -3) {
                    pottery.taller();
                } else if (deltaY > 3) {
                    pottery.shorter();
                }
            }
        }
        return true;
    }

    public boolean isDrew = false;


    public void drawBottom() {
        if (!alreadyInitGL || !alreadyInit) {
            return;
        }

        if (horizontalAngle > 10) {
            horizontalAngle = 10;
        } else if (horizontalAngle < -10) {
            horizontalAngle = -10;
        }

        bottom.setAngleForRotate(180 - horizontalAngle);

        if (verticalAngle > 10) {
            verticalAngle = 10;
        }

        bottom.setAngleRotateY(verticalAngle + 90);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        bottom.draw();

        background.draw();


        isDrew = true;
    }


    public void draw(GL10 gl) {
        horizontalAngle %= 360;
        if (!alreadyInitGL || !alreadyInit) {
            return;
        }
        if (mode != WTMode.INTERACT_VIEW) {
            sensorRotate();
        }
        float tempHorizontalAngle;
        if (needBigImage == 2) {
            tempHorizontalAngle = 90;
            horizontalAngle = 90;
            verticalAngle = -10;
        } else {
            tempHorizontalAngle = horizontalAngle;
        }

        pottery.setAngleForRotate(tempHorizontalAngle);
        pottery.setAngleRotateY(verticalAngle);
        table.setAngleForRotate(tempHorizontalAngle);
        table.setAngleRotateY(verticalAngle);
        shadow.setAngleRotateY(verticalAngle);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        pottery.draw();
        table.draw();

        background.draw();

        GLES20.glEnable(GLES20.GL_BLEND);
        if (mode == WTMode.FIRE) {
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            fire.draw();
        } else {
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            shadow.draw();
        }
        GLES20.glDisable(GLES20.GL_BLEND);

        isDrew = true;

        if (needImage) {
            outImage = SavePixels(gl);
            needImage = false;
        }

        if (needBigImage == 1) {
            bigTempImage = SavePixels(gl);

            Bitmap temp = Bitmap.createScaledBitmap(bigTempImage, 450, 800, false);
            bigTempImage.recycle();
            bigTempImage = null;

            int wid = temp.getWidth();
            int hei = temp.getHeight();
            bigTempImage = Bitmap.createBitmap(temp, 0, (int) (hei * 0.25f), wid, (int) (hei * 0.75f));
            temp.recycle();
            verticalAngle = 0;
        }

        --needBigImage;
    }

    private boolean sensorRotate() {
        float[] sensorData = MySensor.getAngle();
        if (sensorData == null) {
            return false;
        }

        float dataForY = sensorData[0];
        if (dataForY < 0) {
            dataForY = 0;
        }
        float dataForX = sensorData[1];
        if (dataForX < 0) {
            dataForX = -dataForX;
        }
        float newAngle = (dataForX > dataForY ? dataForX
                : dataForY);
        float temp = 30 - newAngle / 3;
        if (direction == 1) {
            if (temp > verticalAngle) {
                if (temp - verticalAngle > 3) {
                    verticalAngle += 0.08 * (temp - verticalAngle);
                }
            } else {
                direction = -1;
            }
        } else {
            if (temp < verticalAngle) {
                if (verticalAngle - temp > 3) {
                    verticalAngle -= 0.08 * (verticalAngle - temp);
                }
            } else {
                direction = 1;
            }
        }

        return true;
    }

    private Bitmap SavePixels(GL10 gl) {
        int x = 0, y = 0, w = Wowtao.screenWidthPixel, h = Wowtao.screenHeightPixel;
        int b[] = new int[w * h];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);
        gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

	    /*  remember, that OpenGL bitmap is incompatible with 
            Android bitmap and so, some correction need.
	     */
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0x00ff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - i - 1) * w + j] = pix1;
            }
        }
        return Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
    }

    private boolean needImage = false;
    private Bitmap outImage;
    private float[] potteryBackup;
    public boolean alreadyInit = false;
    public boolean alreadyInitGL = false;
    private Bitmap tempImage;
    public Integer price;

    public Bitmap getImage(int width, int height) {
        needImage = true;
        while (outImage == null) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Bitmap temp = Bitmap.createScaledBitmap(outImage, width, height, false);
        outImage.recycle();
        outImage = null;

        int wid = temp.getWidth();
        int hei = temp.getHeight();
        tempImage = Bitmap.createBitmap(temp, 0, (int) (hei * 0.25f), wid, (int) (hei * 0.75f));
        return tempImage;
    }

    public void pushPottery() {
        Pottery200 pottery1 = this.pottery;
        float[] vertices = pottery1.getVertices();
        potteryBackup = vertices.clone();
    }

    public void popPottery() {
        if (potteryBackup != null) {
            pottery.setVertices(potteryBackup);
            potteryBackup = null;
        }
    }

    public int getPrice() {
        float result = 0;
        float tijiPrice;
        if (isFix) {
            tijiPrice = 0;
        } else {
            float tiji = (float) ((pottery.getHeight() * 8) * (pottery.getMidRadius() * 8) * (pottery.getMidRadius() * 8) * Math.PI);
            tijiPrice = tiji / 50;
        }

        result += tijiPrice;
        Collection<Pattern> values = PotteryTextureManager.getPatterns().values();
        int customerCount = 0;
        for (Pattern p : values) {
            if (p.needP) {
                result += 20;
                customerCount += 1;
            } else {
                result += 8;
            }
        }

        result += 50;

        int count = values.size() - customerCount;
        switch (count) {
            case 0:
            case 1:
                break;
            case 2:
                result += 10;
                break;
            case 3:
                result += 15;
                break;
            case 4:
                result += 20;
                break;
            case 5:
            case 6:
                result += 25;
                break;
            default:
                result += 25 + 10 * (count - 6);
                break;
        }

        return (int) result;
    }

    public Bitmap getTempImage() {
        return tempImage;
    }

    public Bitmap getBigTempImage() {
        return bigTempImage;
    }

    public void needBigImage() {
        needBigImage = 2;
    }

    public void drawBackground() {
        if (!alreadyInitGL || !alreadyInit) {
            return;
        }

        background.draw();
    }
}
