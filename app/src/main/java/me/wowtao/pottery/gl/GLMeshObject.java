package me.wowtao.pottery.gl;

import android.content.Context;

import me.wowtao.pottery.utils.MyBoolean;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class GLMeshObject {
    protected float[] normals;
    protected float[] texCoords;
    protected float[] vertices;

    public float[] getVertices() {
        return vertices;
    }

    protected short[] indices;

    public FloatBuffer vertexBuffer;
    public int vertexBufferOffset;
    final public MyBoolean isVertexBufferDirty = new MyBoolean(true);

    public FloatBuffer normalBuffer;
    public int normalBufferOffset;
    final public MyBoolean isNormalBufferDirty = new MyBoolean(true);

    public FloatBuffer texCoordinateBuffer;
    public int texCoordinateBufferOffset;
    public final MyBoolean isTexCoordinateBufferDirty = new MyBoolean(true);

    public ShortBuffer indicesBuffer;
    public int indicesBufferOffset;
    public final MyBoolean isIndicesBufferDirty = new MyBoolean(true);

    protected void updateVertexBuffer() {
        if (vertices != null) {
            if (vertexBuffer == null) {
                vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
            }
            updateBuffer(isVertexBufferDirty, vertexBuffer, vertices);
        }
    }

    private static void updateBuffer(MyBoolean isVertexBufferDirty, FloatBuffer vertexBuffer, float[] vertices) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (isVertexBufferDirty) {
            vertexBuffer.position(0);
            vertexBuffer.put(vertices);
            isVertexBufferDirty.value = true;
            vertexBuffer.position(0);
        }
    }

    void updateNormalBuffer() {
        if (normalBuffer == null) {
            normalBuffer = ByteBuffer.allocateDirect(normals.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        updateBuffer(isNormalBufferDirty, normalBuffer, normals);
    }

    private void updateTextureBuffer() {
        if (texCoordinateBuffer == null) {
            texCoordinateBuffer = ByteBuffer.allocateDirect(texCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        updateBuffer(isTexCoordinateBufferDirty, texCoordinateBuffer, texCoords);
    }


    private void updateIndiceBuffer() {
        if (indicesBuffer == null) {
            indicesBuffer = ByteBuffer.allocateDirect(indices.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
        }
        synchronized (isIndicesBufferDirty) {
            indicesBuffer.position(0);
            indicesBuffer.put(indices);
            indicesBuffer.position(0);
            isIndicesBufferDirty.value = true;
        }
    }

    void updateBuffers() {
        updateVertexBuffer();
        updateNormalBuffer();
        updateTextureBuffer();
        updateIndiceBuffer();
    }


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

                int textureIndice = Integer.valueOf(textureIndicesLines.get(i)) - 1;
                texCoords[i * 2] = 1 - Float.valueOf(textureLines.get(textureIndice * 2)) % 1f;
                texCoords[i * 2 + 1] = Float.valueOf(textureLines.get(textureIndice * 2 + 1)) % 1f;

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

    protected static void loadOBJ(BufferedReader reader, ArrayList<String> verticesLines, ArrayList<String> textureLines, ArrayList<String> normalLines, ArrayList<String> verticesIndicesLines, ArrayList<String> textureIndicesLines, ArrayList<String> normalIndicesLines) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("//") || line.startsWith("#") || line.trim().equals(" ") || line.equals("")) {
                continue;
            }
            String SPACE = " ";
            String SLASH = "/";
            StringTokenizer st = new StringTokenizer(line, SPACE);
            String lineType = st.nextToken();
            switch (lineType) {
                case "v":
                    verticesLines.add(st.nextToken());
                    verticesLines.add(st.nextToken());
                    verticesLines.add(st.nextToken());
                    break;
                case "vt":
                    textureLines.add(st.nextToken());
                    textureLines.add(st.nextToken());
                    break;
                case "vn":

                    normalLines.add(st.nextToken());
                    normalLines.add(st.nextToken());
                    normalLines.add(st.nextToken());
                    break;
                case "f":
                    String v1 = st.nextToken();
                    String v2 = st.nextToken();
                    String v3 = st.nextToken();

                    StringTokenizer st1 = new StringTokenizer(v1, SLASH);
                    StringTokenizer st2 = new StringTokenizer(v2, SLASH);
                    StringTokenizer st3 = new StringTokenizer(v3, SLASH);

                    verticesIndicesLines.add(st1.nextToken());
                    verticesIndicesLines.add(st2.nextToken());
                    verticesIndicesLines.add(st3.nextToken());

                    textureIndicesLines.add(st1.nextToken());
                    textureIndicesLines.add(st2.nextToken());
                    textureIndicesLines.add(st3.nextToken());

                    normalIndicesLines.add(st1.nextToken());
                    normalIndicesLines.add(st2.nextToken());
                    normalIndicesLines.add(st3.nextToken());

                    if (st.hasMoreTokens()) {

                        String v4 = st.nextToken();
                        st1 = new StringTokenizer(v1, SLASH);
                        st2 = new StringTokenizer(v3, SLASH);
                        st3 = new StringTokenizer(v4, SLASH);

                        verticesIndicesLines.add(st1.nextToken());
                        verticesIndicesLines.add(st2.nextToken());
                        verticesIndicesLines.add(st3.nextToken());

                        textureIndicesLines.add(st1.nextToken());
                        textureIndicesLines.add(st2.nextToken());
                        textureIndicesLines.add(st3.nextToken());

                        normalIndicesLines.add(st1.nextToken());
                        normalIndicesLines.add(st2.nextToken());
                        normalIndicesLines.add(st3.nextToken());
                    }
                    break;
                default:
                    System.out.println("format error");
                    break;
            }

        }
    }


    public int getAttributeBufferLength() {
        return vertexBuffer.capacity() + normalBuffer.capacity() + texCoordinateBuffer.capacity();
    }

    public int getIndicesBufferLength() {
        return indicesBuffer.capacity();
    }

    public void toOBJFile(File file, boolean isSingleLayer) {
        Writer writer = null;
        try {
            writer = new FileWriter(file);
            for (int i = 0; i < vertices.length; i += 3) {
                writer.write("v " + vertices[i] + " " + vertices[i + 1] + " " + vertices[i + 2] + "\n");
            }

            for (int i = 0; i < normals.length; i += 3) {
                writer.write("vn " + normals[i] + " " + normals[i + 1] + " " + normals[i + 2] + "\n");
            }

            for (int i = 0; i < texCoords.length; i += 2) {
                writer.write("vt " + texCoords[i] + " " + texCoords[i + 1] + "\n");
            }


            if (isSingleLayer) {
                for (int i = 0; i < Pottery.VERTICAL_PRECISION * (Pottery.HORIZONTAL_PRECISION - 2) * 6; i += 3) {
                    writeHelper(writer, i);
                }

                for (int i = (2 * Pottery.VERTICAL_PRECISION - 3) * Pottery.HORIZONTAL_PRECISION * 6 + Pottery.HORIZONTAL_PRECISION * 3; i < indices.length; i += 3) {
                    writeHelper(writer, i);
                }
            } else {
                for (int i = 0; i < indices.length; i += 3) {
                    int index = indices[i] + 1;
                    writer.write("f " + index + "/" + index + "/" + index + " ");
                    index = indices[i + 1] + 1;
                    writer.write("" + index + "/" + index + "/" + index + " ");
                    index = indices[i + 2] + 1;
                    writer.write("" + index + "/" + index + "/" + index + "\n");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeHelper(Writer writer, int i) throws IOException {
        int index = helper(indices[i] + 1);
        writer.write("f " + index + "/" + index + "/" + index + " ");
        index = helper(indices[i + 1] + 1);
        writer.write("" + index + "/" + index + "/" + index + " ");
        index = helper(indices[i + 2] + 1);
        writer.write("" + index + "/" + index + "/" + index + "\n");
    }

    private int helper(int i) {
        if (i % (Pottery.HORIZONTAL_PRECISION + 1) == 0 && i != 0) {
            return i - Pottery.HORIZONTAL_PRECISION;
        } else {
            return i;
        }
    }

}

