package me.wowtao.pottery.gl;

import android.app.Activity;
import android.graphics.Bitmap;

public class Background extends GLMeshObject {
    protected int textureName = 0;
    protected float top;
    protected float bottom;
    protected float left;
    protected float right;
    public float[] originalVertices;
    public Bitmap texture;
    protected int resId;

    transient protected Activity context;

    public Background() {
        vertices = new float[12];
        originalVertices = new float[12];
        normals = new float[]{
                0, 0, 1,
                0, 0, 1,
                0, 0, 1,
                0, 0, 1
        };
        texCoords = new float[]{
                0, 1,
                1, 1,
                1, 0,
                0, 0
        };
        indices = new short[]{
                0, 1, 3,
                1, 2, 3
        };
        updateBuffers();
    }

    public void draw() {

    }

    private static final float Z = 90.0f;

    public String position = "center";

    public void genPosition(float left, float right, float bottom,
                            float top, float near, float far) {
        if (position.equals("center")) {
            this.top = top * Z / near * 0.6f;
            this.bottom = bottom * Z / near * 0.5f;
            this.left = left * Z / near;
            this.right = right * Z / near;
        } else {
            this.top = top * Z / near * 0.7f;
            this.bottom = bottom * Z / near * 0.6f;
            this.left = left * Z / near * 1.2f;
            this.right = right * Z / near * 0.8f;
        }
        int i = 45;
        vertices = new float[]{
                this.left, this.bottom, -Z + i,
                this.right, this.bottom, -Z + i,
                this.right, this.top, -Z + i,
                this.left, this.top, -Z + i
        };
        updateVertexBuffer();
        this.originalVertices = vertices.clone();
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
        updateBuffers();
    }

    public float[] getVertices() {
        return this.vertices;
    }

    public void setTexture(Activity activity, int mainActivityBackground) {
        resId = mainActivityBackground;
        context = activity;
        textureName = 0;
    }

}
