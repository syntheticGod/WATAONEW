package me.wowtao.pottery.shader;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.Matrix;

import me.wowtao.pottery.gl.GLMeshObject;
import me.wowtao.pottery.utils.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class WTShader {

    //matrix
    float[] modelMatrix = new float[16];
    float[] viewMatrix = new float[16];
    static float[] perspectiveMatrix = new float[16];


    protected int program;

    //vertex parameter handle
    int uMVPMatrixHandle;
    int aPositionHandle;
    int aNormalHandle = -1;
    int aTexCoordinateHandle;

    //frag parameter handle
    int uTextureHandle;

    public static void initForAllShader() {
        // Set up any OpenGL options we need
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
    }

    WTShader(String vertexShader, String fragShader, Resources resources) {
        //加载顶点着色器的脚本内容
        String mVertexShader = ShaderUtil.loadFromAssetsFile(vertexShader, resources);
        if (mVertexShader == null) {
            System.err.println("create shade failed!");
        }
        //加载片元着色器的脚本内容
        String mFragmentShader = ShaderUtil.loadFromAssetsFile(fragShader, resources);
        if (mFragmentShader == null) {
            System.err.println("create shade failed!");
        }
        //基于顶点着色器与片元着色器创建程序
        program = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        if (program == 0) {
            System.err.println("create shade failed!");
        }

        Matrix.setIdentityM(modelMatrix, 0);
    }

    protected void checkParameterHandle(int handle, String tag) {
        if (handle < 0) {
            System.err.println("no such variable: " + tag);
        }
    }

    public void setTexture(int i) {
        GLES20.glUniform1i(uTextureHandle, i);
    }

    public static void frustumM(float left, float right, float bottom, float top,
                                float near, float far) {
        WTShader.frustum(perspectiveMatrix, left, right, bottom, top, near, far);
    }

    private static void frustum(float[] m, float left, float right, float bottom, float top,
                                float near, float far) {
        if (left == right) {
            throw new IllegalArgumentException("left == right");
        }
        if (top == bottom) {
            throw new IllegalArgumentException("top == bottom");
        }
        if (near == far) {
            throw new IllegalArgumentException("near == far");
        }
        if (near <= 0.0f) {
            throw new IllegalArgumentException("near <= 0.0f");
        }
        if (far <= 0.0f) {
            throw new IllegalArgumentException("far <= 0.0f");
        }
        final float r_width = 1.0f / (right - left);
        final float r_height = 1.0f / (top - bottom);
        final float r_depth = 1.0f / (near - far);
        final float x = 2.0f * (near * r_width);
        final float y = 2.0f * (near * r_height);
        final float A = (right + left) * r_width;
        final float B = (top + bottom) * r_height;
        final float C = (far + near) * r_depth;
        final float D = 2.0f * (far * near * r_depth);
        m[0] = x;
        m[5] = y;
        m[8] = A;
        m[9] = B;
        m[10] = C;
        m[14] = D;
        m[11] = -1.0f;
        m[1] = 0.0f;
        m[2] = 0.0f;
        m[3] = 0.0f;
        m[4] = 0.0f;
        m[6] = 0.0f;
        m[7] = 0.0f;
        m[12] = 0.0f;
        m[13] = 0.0f;
        m[15] = 0.0f;
    }

    private static List<GLMeshObject> glMeshObjects = new ArrayList<>();
    private static int[] VBOId = new int[2];

    public static void addGLMeshObject(GLMeshObject meshObject) {
        glMeshObjects.add(meshObject);
    }

    static public void initVBO() {
        int attributeBufferLength = 0;
        int indicesBufferLength = 0;
        for (GLMeshObject object : glMeshObjects) {
            attributeBufferLength += object.getAttributeBufferLength();
            indicesBufferLength += object.getIndicesBufferLength();
        }
        FloatBuffer attributeBuffer = ByteBuffer.allocateDirect(attributeBufferLength * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (GLMeshObject object : glMeshObjects) {
            synchronized (object.isVertexBufferDirty) {
                object.vertexBufferOffset = attributeBuffer.position() * 4;
                object.vertexBuffer.position(0);
                attributeBuffer.put(object.vertexBuffer);
                object.vertexBuffer.position(0);
            }

            synchronized (object.isNormalBufferDirty) {
                object.normalBufferOffset = attributeBuffer.position() * 4;
                object.normalBuffer.position(0);
                attributeBuffer.put(object.normalBuffer);
                object.normalBuffer.position(0);
            }

            synchronized (object.isTexCoordinateBufferDirty) {
                object.texCoordinateBufferOffset = attributeBuffer.position() * 4;
                object.texCoordinateBuffer.position(0);
                attributeBuffer.put(object.texCoordinateBuffer);
                object.texCoordinateBuffer.position(0);
            }
        }
        attributeBuffer.position(0);
        ShortBuffer indicesBuffer = ByteBuffer.allocateDirect(indicesBufferLength * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        for (GLMeshObject object : glMeshObjects) {
            synchronized (object.isIndicesBufferDirty) {
                object.indicesBufferOffset = indicesBuffer.position() * 2;
                object.indicesBuffer.position(0);
                indicesBuffer.put(object.indicesBuffer);
                object.indicesBuffer.position(0);
            }
        }
        indicesBuffer.position(0);
        GLES20.glGenBuffers(2, VBOId, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, VBOId[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, attributeBufferLength * 4, attributeBuffer, GLES20.GL_DYNAMIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, VBOId[1]);
        GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indicesBufferLength * 2, indicesBuffer, GLES20.GL_STATIC_DRAW);
    }

    public void setVertexAttributeOffset(int vertexBufferOffset, int normalBufferOffset, int texCoordinateBufferOffset) {
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBufferOffset);
        if (aNormalHandle >= 0) {
            GLES20.glVertexAttribPointer(aNormalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBufferOffset);
        }
        GLES20.glVertexAttribPointer(aTexCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordinateBufferOffset);
    }

    static void updateVBO() {
        for (GLMeshObject object : glMeshObjects) {
            if (object.isVertexBufferDirty.value) {
                synchronized (object.isVertexBufferDirty) {
                    GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, object.vertexBufferOffset, object.vertexBuffer.capacity() * 4, object.vertexBuffer);
                    object.isVertexBufferDirty.value = false;
                }
            }

            if (object.isNormalBufferDirty.value) {
                synchronized (object.isNormalBufferDirty) {
                    GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, object.normalBufferOffset, object.normalBuffer.capacity() * 4, object.normalBuffer);
                    object.isNormalBufferDirty.value = false;
                }
            }

            if (object.isTexCoordinateBufferDirty.value) {
                synchronized (object.isTexCoordinateBufferDirty) {
                    GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, object.texCoordinateBufferOffset, object.texCoordinateBuffer.capacity() * 4, object.texCoordinateBuffer);
                    object.isTexCoordinateBufferDirty.value = false;
                }
            }
        }
    }

    public void drawVBO(int indicesNum, int offset) {
        updateVBO();
        float[] multiplyMMResult = new float[16];
        Matrix.multiplyMM(multiplyMMResult, 0, viewMatrix, 0, modelMatrix, 0);
        Matrix.multiplyMM(multiplyMMResult, 0, perspectiveMatrix, 0, multiplyMMResult, 0);
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, multiplyMMResult, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indicesNum, GLES20.GL_UNSIGNED_SHORT, offset);
    }

    public void useProgram() {
        GLES20.glUseProgram(program);
    }

    public void setLookAt(float d) {
        //Position the eye behind the origin.
        final float eyeY = 0.0f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -1.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        Matrix.setLookAtM(viewMatrix, 0, genOffsetX(d), eyeY, genOffsetZ(d), lookX, lookY, lookZ, upX, upY, upZ);
    }

    static float genOffsetX(float input) {
        return input * 0.5f;
    }

    static float genOffsetZ(float input) {
        return input * -1.1f;
    }

}
