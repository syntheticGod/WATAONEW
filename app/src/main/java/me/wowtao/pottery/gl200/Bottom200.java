package me.wowtao.pottery.gl200;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import me.wowtao.pottery.gl.Table;
import me.wowtao.pottery.shader.BottomShader;
import me.wowtao.pottery.utils.ShaderUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static android.opengl.GLES31.*;

public class Bottom200 extends Table {

    private BottomShader shader;

    private boolean hasNewTexture = true;
    private int cubeTextureId;
    private Bitmap texture;

    public Bottom200(Context context, String fileName) {
        super(context, fileName);
    }


    public void createShader(float offset) {
        shader = new BottomShader("vertex_pottery.glsl", "frag_pottery_ci.glsl", resources, offset);
        cubeTextureId = Pottery200.generateCubeMap(context.getResources());
    }


    public void setTexture(Bitmap bitmap) {
        texture = bitmap;
        hasNewTexture = true;
    }

    @Override
    public void draw() {
        drawWithShader(shader);
        super.draw();
    }


    private void drawWithShader(BottomShader shader) {
        shader.useProgram();
        shader.setVertexAttributeOffset(vertexBufferOffset, normalBufferOffset, texCoordinateBufferOffset);

        if (textureName == 0 || hasNewTexture) {
            if (textureName == 0) {
                int[] temp = new int[1];
                glGenTextures(1, temp, 0);
                textureName = temp[0];
            }
            glBindTexture(GL_TEXTURE_2D, textureName);
            if (texture == null || texture.isRecycled()) {
                texture = BitmapFactory.decodeResource(context.getResources(), resId);
            }
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, texture, 0);
            hasNewTexture = false;
            texture.recycle();
            System.gc();
        }

        ShaderUtil.setTexParameter(textureName);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubeTextureId);
        glActiveTexture(GL_TEXTURE0);

        shader.setTextureCube();
        shader.setTexture(0);
        shader.reloadModelMatrix();
        shader.translate(0f, 0.0f, -6.4f);
        shader.rotate(angleForSensor, 1.0f, 0.0f, 0.0f);
        shader.rotate(angleForRotate, 0f, 0f, 1f);
        shader.scale(0.7f, 0.4f, 0.7f);
        shader.drawVBO(indicesBuffer.capacity(), indicesBufferOffset);
    }

    public void setOffset(float d) {
        if (shader != null) {
            shader.setLookAt(d);
        }
    }

    @Override
    public void LoadObj(Context context, String fileName) {
        InputStream input;
        BufferedReader reader;
        try {
            ArrayList<String> verticesLines = new ArrayList<>();
            ArrayList<String> textureLines = new ArrayList<>();
            ArrayList<String> normalLines = new ArrayList<>();
            ArrayList<String> verticesIndicesLines = new ArrayList<>();
            ArrayList<String> textureIndicesLines = new ArrayList<>();
            ArrayList<String> normalIndicesLines = new ArrayList<>();
            input = context.getResources().getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(input));
            loadOBJ(reader, verticesLines, textureLines, normalLines, verticesIndicesLines, textureIndicesLines, normalIndicesLines);
            int indicesSize = verticesIndicesLines.size();
            indices = new short[indicesSize];
            int verticesSize = indicesSize * 3;
            vertices = new float[verticesSize];
            int textureSize = indicesSize * 2;
            texCoords = new float[textureSize];
            int normalSize = indicesSize * 3;
            normals = new float[normalSize];

            for (int i = 0; i < verticesIndicesLines.size(); i++) {
                indices[i] = (short) i;

                int indices = Integer.valueOf(verticesIndicesLines.get(i)) - 1;
                vertices[i * 3] = Float.valueOf(verticesLines.get(indices * 3));
                vertices[i * 3 + 1] = Float.valueOf(verticesLines.get(indices * 3 + 1));
                vertices[i * 3 + 2] = Float.valueOf(verticesLines.get(indices * 3 + 2));

                int textureIndices = Integer.valueOf(textureIndicesLines.get(i)) - 1;
                texCoords[i * 2] = 1 - Float.valueOf(textureLines.get(textureIndices * 2)) % 1f;
                texCoords[i * 2 + 1] = 1 - Float.valueOf(textureLines.get(textureIndices * 2 + 1)) % 1f;

                if (texCoords[i * 2] < 0) {
                    texCoords[i * 2] += 1f;
                }

                if (texCoords[i * 2 + 1] < 0) {
                    texCoords[i * 2 + 1] += 1f;
                }

                int normalIndices = Integer.valueOf(normalIndicesLines.get(i)) - 1;
                normals[i * 3] = Float.valueOf(normalLines.get(normalIndices * 3));
                normals[i * 3 + 1] = Float.valueOf(normalLines.get(normalIndices * 3 + 1));
                normals[i * 3 + 2] = Float.valueOf(normalLines.get(normalIndices * 3 + 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}