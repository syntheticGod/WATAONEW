package me.wowtao.pottery.gl;

import me.wowtao.pottery.utils.GLManager;

import java.util.Random;

public class Pottery extends GLMeshObject {

    public final static int VERTICAL_PRECISION = 25;
    final static int HORIZONTAL_PRECISION = 25;

    private final float midHeight = 1.75f;
    private final float initialRadius = 0.6f;

    protected float currentHeight = 1.0f;

    public float getCurrentHeight() {
        return currentHeight;
    }

    protected float[] radii = new float[VERTICAL_PRECISION];

    private float[] radiusMin = new float[VERTICAL_PRECISION];

    {
        for (int i = 0; i < VERTICAL_PRECISION; i++) {
            radiusMin[i] = 0.38f - ((float) i) / VERTICAL_PRECISION * 0.19f;
        }
    }

    protected float angleForSensor = 0.0f;
    protected float angleForRotate = 0.0f;
    private float varUsedForEllipseToRegular;

    public float getHeight() {
        return currentHeight;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
        fastEstimateNormals();
        updateVertexBuffer();
        updateNormalBuffer();
    }

    public Pottery() {
        initializeData();
        int pointNum = 2 * VERTICAL_PRECISION * (HORIZONTAL_PRECISION + 1);
        vertices = new float[(pointNum + 2) * 3];
        genVerticesRandom();


        normals = new float[(pointNum + 2) * 3];
        genNormalsXZ();
        fastEstimateNormals();

        texCoords = new float[(pointNum + 2) * 2];
        genTexCoords();

        indices = new short[(2 * VERTICAL_PRECISION - 3) * HORIZONTAL_PRECISION * 6 + HORIZONTAL_PRECISION * 3 + HORIZONTAL_PRECISION * 3];
        genIndices();

        updateBuffers();
    }

    private void initializeData() {
        for (int i = 0; i < VERTICAL_PRECISION; i++) {
            radii[i] = initialRadius * (1.03f - i / (float) VERTICAL_PRECISION / 16.0f);
        }


        //change the shape randomly
        Random random = new Random(System.currentTimeMillis());
        float delta = 0.1f;
        changeBasesFatter(random.nextFloat() * currentHeight, 0, delta);
        changeBasesFatter(random.nextFloat() * currentHeight, 0, delta);
        changeBasesThinner(random.nextFloat() * currentHeight, 0, delta);
        changeBasesThinner(random.nextFloat() * currentHeight, 0, delta);
        changeBasesThinner(random.nextFloat() * currentHeight, 0, delta);
        changeBasesThinner(random.nextFloat() * currentHeight, 0, delta);
    }

    private void genVerticesRandom() {
        varUsedForEllipseToRegular = 0;
        genVertices();

        int length = vertices.length;
        vertices[length - 4] = 0;
        vertices[length - 5] = 2 * currentHeight / (VERTICAL_PRECISION - 1);
        vertices[length - 6] = 0;

        vertices[length - 1] = 0;
        vertices[length - 2] = 0;
        vertices[length - 3] = 0;
    }


    protected void genVerticesFromBases() {
        genVertices();
        if (varUsedForEllipseToRegular < 0.9) {
            varUsedForEllipseToRegular += 0.005f;
        }
    }

    private void genVertices() {
        for (int i = 0; i < 2 * VERTICAL_PRECISION; i++) {
            for (int j = 0; j < HORIZONTAL_PRECISION + 1; j++) {
                int offset = (i * (HORIZONTAL_PRECISION + 1) + j) * 3;
                vertices[offset] = computerVertexX(i, j);
                vertices[offset + 1] = computerVertexY(i);
                vertices[offset + 2] = computerVertexZ(i, j);
            }
        }
    }

    private void genNormalsXZ() {
        for (int i = 0; i < 2 * VERTICAL_PRECISION; i++) {
            for (int j = 0; j < HORIZONTAL_PRECISION + 1; j++) {
                int offset = (i * (HORIZONTAL_PRECISION + 1) + j) * 3;
                normals[offset] = computerNormalX(i, j);
                normals[offset + 2] = computerNormalZ(i, j);
            }
        }
        int length = normals.length;
        normals[length - 4] = z;
        normals[length - 5] = y;
        normals[length - 6] = x;

        normals[length - 1] = z;
        normals[length - 2] = -y;
        normals[length - 3] = x;
        int i = 2 * VERTICAL_PRECISION - 1;
        for (int j = 0; j < HORIZONTAL_PRECISION + 1; j++) {
            int offset = (i * (HORIZONTAL_PRECISION + 1) + j) * 3;
            normals[offset] = x;
            normals[offset + 1] = y;
            normals[offset + 2] = z;
        }
    }


    private void genTexCoords() {
        float verticalStep = 0.5f / VERTICAL_PRECISION;
        float horizontalStep = 1f / HORIZONTAL_PRECISION;
        for (int i = 0; i < 2 * VERTICAL_PRECISION; ++i) {
            float f = (1f - i * verticalStep) * 1.08f - 0.08f;
            if (f < 0.1f) {
                f = 0.1f;
            }
            for (int j = 0; j < HORIZONTAL_PRECISION + 1; ++j) {
                int offset = (i * (HORIZONTAL_PRECISION + 1) + j) * 2;
                texCoords[offset] = 1 - j * horizontalStep;
                texCoords[offset + 1] = f;
            }
        }

        int length = texCoords.length;
        texCoords[length - 4] = 0.5f; //x
        texCoords[length - 3] = 0.11f; //y

        texCoords[length - 2] = 0.5f; //x
        texCoords[length - 1] = 0.11f; //y
    }

    private void genIndices() {
        for (int i = 0; i < 2 * VERTICAL_PRECISION - 3; ++i) {
            for (int j = 0; j < HORIZONTAL_PRECISION; ++j) {
                int offset = (i * HORIZONTAL_PRECISION + j) * 6;
                int baseIndex = (i * (HORIZONTAL_PRECISION + 1) + j);
                indices[offset] = (short) baseIndex;
                indices[offset + 1] = (short) (baseIndex + HORIZONTAL_PRECISION + 1);
                indices[offset + 2] = (short) (baseIndex + 1);
                indices[offset + 3] = (short) (baseIndex + 1);
                indices[offset + 4] = (short) (baseIndex + HORIZONTAL_PRECISION + 1);
                indices[offset + 5] = (short) (baseIndex + HORIZONTAL_PRECISION + 1 + 1);
            }
        }

        int offset = (2 * VERTICAL_PRECISION - 3) * HORIZONTAL_PRECISION * 6;
        int base = (2 * VERTICAL_PRECISION - 3) * (HORIZONTAL_PRECISION + 1);
        for (int j = 0; j < HORIZONTAL_PRECISION; ++j) {
            indices[offset + j * 3] = (short) (j + base);
            indices[offset + j * 3 + 2] = (short) ((j + 1) + base);
            indices[offset + j * 3 + 1] = (short) (vertices.length / 3 - 2);
        }
        offset += HORIZONTAL_PRECISION * 3;
        for (int j = 0; j < HORIZONTAL_PRECISION; ++j) {
            indices[offset + j * 3] = (short) (j);
            indices[offset + j * 3 + 1] = (short) ((j + 1));
            indices[offset + j * 3 + 2] = (short) (vertices.length / 3 - 1);
        }
    }

    private float computeRadius(int i) {
        float radius;
        float thickness = 0.05f;
        if (i > VERTICAL_PRECISION - 1) {
            i = 2 * VERTICAL_PRECISION - 1 - i;
            if (i == VERTICAL_PRECISION - 1) {
                radius = radii[i] - thickness * 4 / 5;
            } else {
                radius = radii[i] - thickness;
            }
        } else {
            if (i == VERTICAL_PRECISION - 1) {
                radius = radii[i] - thickness * 1 / 5;
            } else {
                radius = radii[i];
            }

        }
        return radius;
    }

    private float computerVertexX(int i, int j) {
        float radius = computeRadius(i);
        j %= HORIZONTAL_PRECISION;
        if (i > VERTICAL_PRECISION - 1) {
            i = 2 * VERTICAL_PRECISION - 1 - i;
        }
        radius = radius * (0.95f + 0.05f * varUsedForEllipseToRegular + 0.15f * (1.0f - varUsedForEllipseToRegular) / VERTICAL_PRECISION * i);
        return (float) (Math.cos(j * 2.0f * Math.PI / HORIZONTAL_PRECISION) * radius);
    }

    private float computerVertexY(int i) {
        if (i > VERTICAL_PRECISION - 1) {
            i = 2 * VERTICAL_PRECISION - 1 - i;
        }
        return (float) i * currentHeight / (VERTICAL_PRECISION - 1);
    }

    private float computerVertexZ(int i, int j) {
        float radius = computeRadius(i);
        j %= HORIZONTAL_PRECISION;
        if (i > VERTICAL_PRECISION - 1) {
            i = 2 * VERTICAL_PRECISION - 1 - i;
        }
        radius = radius * (1.1f - 0.15f * (1.0f - varUsedForEllipseToRegular) / VERTICAL_PRECISION * i - 0.1f * varUsedForEllipseToRegular);
        return (float) Math.sin(j * 2 * Math.PI / HORIZONTAL_PRECISION) * radius;
    }

    private float computerNormalX(int i, int j) {
        float result = (float) Math.cos(j * 2 * Math.PI / HORIZONTAL_PRECISION);
        if (i > VERTICAL_PRECISION) {
            result = -result;
        } else if (i == VERTICAL_PRECISION - 1 || i == VERTICAL_PRECISION) {
            result = 0.0f;
        }
        return result;
    }

    private float computerNormalZ(int i, int j) {
        float result = (float) Math.sin(j * 2 * Math.PI / HORIZONTAL_PRECISION);
        if (i > VERTICAL_PRECISION) {
            result = -result;
        } else if (i == VERTICAL_PRECISION - 1 || i == VERTICAL_PRECISION) {
            result = 0.0f;
        }
        return result;
    }

    public void taller() {
        if (currentHeight < midHeight) {
            currentHeight += hSpeed;
        } else {
            currentHeight += computerVerticalDelta();
        }
        genVerticesFromBases();
        fastEstimateNormals();
        updateVertexBuffer();
        updateNormalBuffer();
    }

    public void shorter() {
        if (currentHeight > midHeight) {
            currentHeight -= hSpeed;
        } else {
            currentHeight -= computerVerticalDelta();
        }
        genVerticesFromBases();
        fastEstimateNormals();
        updateVertexBuffer();
        updateNormalBuffer();
    }

    private float computerVerticalDelta() {
        float delta = Math.abs(currentHeight - midHeight);
        float vSpeed = 0.018f;
        float minHeight = 0.5f;
        float maxHeight = 3.0f;
        float f = vSpeed * (1.0f - 2 * delta / (maxHeight - minHeight)) / 1.2f;
        if (f < 0) {
            f = 0;
        }
        return f;
    }


    private static final float CONSTANTS_FOR_GAUSSIAN = (float) (1.0f / Math.sqrt(2 * Math.PI));

    private float gaussian(float delta, float mean, float x) {
        return (float) (CONSTANTS_FOR_GAUSSIAN / delta * Math.exp(-(x - mean) * (x - mean) / 2.0f / delta / delta));
    }

    private float viscosity = 0.28f;

    private float hSpeed = 0.01f;

    public void thinner(float y) {
        changeBasesThinner(y, 0.0f);
        genVerticesFromBases();
        fastEstimateNormals();
        updateVertexBuffer();
        updateNormalBuffer();
    }

    public void fatter(float y) {
        changeBasesFatter(y, 0.0f);
        genVerticesFromBases();
        fastEstimateNormals();
        updateVertexBuffer();
        updateNormalBuffer();
    }

    private void changeBasesFatter(float y, float mean) {
        changeBasesFatter(y, mean, viscosity);
    }

    private void changeBasesFatter(float y, float mean, float delta) {
        for (int i = 0; i < VERTICAL_PRECISION; i++) {
            float radiusMax = 1.0625f;
            radii[i] += (float) Math.atan((radiusMax - radii[i]))
                    * 2.0f / (float) Math.PI * hSpeed
                    * gaussian(delta, mean, (float) i / VERTICAL_PRECISION * currentHeight - y);
        }
    }

    private void changeBasesThinner(float y, float mean) {
        changeBasesThinner(y, mean, viscosity);
    }

    private void changeBasesThinner(float y, float mean, float delta) {
        for (int i = 0; i < VERTICAL_PRECISION; i++) {
            float temp = (float) Math.atan((radii[i] - radiusMin[i])) * 2.0f / (float) Math.PI;
            radii[i] = radii[i] - temp * hSpeed * gaussian(delta, mean, (float) i / VERTICAL_PRECISION * currentHeight - y);
        }
    }


    public float[] getRadii() {
        return radii;
    }

    private class vec3 {
        public float x = 0.0f;
        public float y = 0.0f;
        public float z = 0.0f;

        public vec3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public vec3() {}

        public vec3(vec3 begin, vec3 end) {
            this.x = end.x - begin.x;
            this.y = end.y - begin.y;
            this.z = end.z - begin.z;
        }

        public vec3(vec3 currentPoint) {
            this.x = currentPoint.x;
            this.y = currentPoint.y;
            this.z = currentPoint.z;
        }

        vec3 crossProduct(vec3 v2) {
            vec3 result = new vec3();
            result.x = this.y * v2.z - this.z * v2.y;
            result.y = this.x * v2.z - this.z * v2.x;
            result.z = this.x * v2.y - this.y * v2.x;
            return result;
        }

        public void normalize() {
            float length = (float) Math.sqrt(x * x + y * y + z * z);
            this.x /= length;
            this.y /= length;
            this.z /= length;
        }

    }

    public void fastEstimateNormals() {

        for (int i = 0; i < 2 * VERTICAL_PRECISION; i++) {
            vec3 normal = new vec3();
            if (i == VERTICAL_PRECISION - 1 || i == VERTICAL_PRECISION) {
                normal.y = 1.0f;
            } else {
                //get current point;
                int baseIndex = i * (HORIZONTAL_PRECISION + 1) * 3;
                vec3 currentPoint = new vec3(vertices[baseIndex], vertices[baseIndex + 1], vertices[baseIndex + 2]);

                //get the current point's up point;
                vec3 upPoint;
                int upBaseIndex = (i + 1) * (HORIZONTAL_PRECISION + 1) * 3;
                upPoint = new vec3(vertices[upBaseIndex], vertices[upBaseIndex + 1], vertices[upBaseIndex + 2]);

                //get the current point's down point;
                vec3 downPoint;
                if (i == 0 || i == 2 * VERTICAL_PRECISION - 1) {
                    downPoint = new vec3(currentPoint);
                } else {
                    int downBaseIndex = (i - 1) * (HORIZONTAL_PRECISION + 1) * 3;
                    downPoint = new vec3(vertices[downBaseIndex], vertices[downBaseIndex + 1], vertices[downBaseIndex + 2]);
                }

                //get the current point's left point
                int leftBaseIndex = (i * (HORIZONTAL_PRECISION + 1) + HORIZONTAL_PRECISION - 1) * 3;
                vec3 leftPoint = new vec3(vertices[leftBaseIndex], vertices[leftBaseIndex + 1], vertices[leftBaseIndex + 2]);

                //get the current point's right point
                int rightBaseIndex = (i * (HORIZONTAL_PRECISION + 1) + 1) * 3;
                vec3 rightPoint = new vec3(vertices[rightBaseIndex], vertices[rightBaseIndex + 1], vertices[rightBaseIndex + 2]);

                //computer the four vector: up,down,left,right
                vec3 upVector = new vec3(currentPoint, upPoint);
                vec3 downVector = new vec3(currentPoint, downPoint);
                vec3 leftVector = new vec3(currentPoint, leftPoint);
                vec3 rightVector = new vec3(currentPoint, rightPoint);

                //computer the four sub-normal
                vec3 normal1 = rightVector.crossProduct(upVector);
                vec3 normal2 = upVector.crossProduct(leftVector);
                vec3 normal3 = leftVector.crossProduct(downVector);
                vec3 normal4 = downVector.crossProduct(rightVector);

                //add the four sub-normal
                normal.x = normal1.x + normal2.x + normal3.x + normal4.x;
                normal.y = normal1.y + normal2.y + normal3.y + normal4.y;
                normal.z = normal1.z + normal2.z + normal3.z + normal4.z;

                //normalize the final normal
                normal.normalize();
            }

            //the above code computer the first pointer in this floor,because the pottery's
            //shape is very regular, so we assume that the normal of the points in the same
            //storey have the same normal.y;
            for (int j = 0; j < HORIZONTAL_PRECISION + 1; ++j) {
                int offset = (i * (HORIZONTAL_PRECISION + 1) + j) * 3;
                float x = normals[offset];
                float z = normals[offset + 2];
                if (z != 0.0f) {
                    float xdz = x / z;
                    normals[offset + 1] = normal.y;
                    float newZ = (float) Math.sqrt((1 - normal.y * normal.y) / (1 + xdz * xdz));
                    if (normals[offset + 2] < 0) {
                        normals[offset + 2] = -newZ;
                    } else {
                        normals[offset + 2] = newZ;
                    }
                    normals[offset] = xdz * normals[offset + 2];
                } else {
                    normals[offset + 1] = normal.y;
                    normals[offset + 2] = 0;
                    float newX = (float) Math.sqrt(1 - normal.y * normal.y);
                    if (normals[offset] < 0) {
                        normals[offset] = -newX;
                    } else {
                        normals[offset] = newX;
                    }
                }
            }
        }
        int i = 2 * VERTICAL_PRECISION - 1;
        for (int j = 0; j < HORIZONTAL_PRECISION + 1; j++) {
            int offset = (i * (HORIZONTAL_PRECISION + 1) + j) * 3;
            normals[offset] = x;
            normals[offset + 1] = y;
            normals[offset + 2] = z;
        }
    }

    private static final float x = 0f;
    private static final float y = 1f;
    private static final float z = 0f;


    public void setAngleRotateY(float potteryCurrentAngleRotateX) {
        this.angleForSensor = potteryCurrentAngleRotateX;
    }

    public void setAngleForRotate(float rotateAngle) {
        this.angleForRotate = rotateAngle;
    }

    public void reset() {
        GLManager.setIsFix(false);
        final float heightBegin = this.currentHeight;
        final float[] radiusBegin = radii.clone();

        for (int i = 0; i < VERTICAL_PRECISION; i++) {
            radii[i] = initialRadius * (1.03f - i / (float) VERTICAL_PRECISION / 16.0f);
        }
        //change the shape randomly
        Random random = new Random(System.currentTimeMillis());
        float delta = 0.1f;
        changeBasesFatter(random.nextFloat() * currentHeight, 0, delta);
        changeBasesFatter(random.nextFloat() * currentHeight, 0, delta);
        changeBasesThinner(random.nextFloat() * currentHeight, 0, delta);
        changeBasesThinner(random.nextFloat() * currentHeight, 0, delta);
        changeBasesThinner(random.nextFloat() * currentHeight, 0, delta);
        changeBasesThinner(random.nextFloat() * currentHeight, 0, delta);
        final float[] radiusEnd = radii.clone();

        float initializeHeight = 1.0f;
        startReset(heightBegin, initializeHeight, radiusBegin, radiusEnd, true);


    }

    private void startReset(final float heightBegin, final float heightEnd,
                            final float[] radiusBegin, final float[] radiusEnd, final boolean isRandom) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                long beginTime = System.currentTimeMillis();
                long timeEscape;
                while ((timeEscape = System.currentTimeMillis() - beginTime) < 800) {
                    float rate = timeEscape / 800.0f;
                    currentHeight = heightBegin - (heightBegin - heightEnd) * rate;
                    for (int i = 0; i < VERTICAL_PRECISION; i++) {
                        radii[i] = radiusBegin[i] - (radiusBegin[i] - radiusEnd[i]) * rate;
                    }
                    if (isRandom) {
                        genVerticesRandom();
                    } else {
                        genVerticesFromBases();
                    }
                    fastEstimateNormals();
                    updateVertexBuffer();
                    updateNormalBuffer();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void draw() {

    }

    public void setShape(float[] bases, float height) {
        this.radii = bases;
        this.currentHeight = height;
        varUsedForEllipseToRegular = 1.0f;
        genVerticesFromBases();
        fastEstimateNormals();
        updateNormalBuffer();
        updateVertexBuffer();
    }

    public float getMaxWidth() {
        float result = 0;
        for (float f : radii) {
            if (f > result) {
                result = f;
            }
        }
        return result * 16;
    }

    public float getMinWidth() {
        float result = 100;
        for (float f : radii) {
            if (f < result) {
                result = f;
            }
        }
        return result * 16;
    }

    public float getHeightReal() {
        return currentHeight * 8;
    }

    public float getMidRadius() {
        float total = 0;
        for (float r : radii) {
            total += r;
        }
        return total / radii.length;
    }
}
