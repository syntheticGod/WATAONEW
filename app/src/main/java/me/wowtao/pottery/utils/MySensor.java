package me.wowtao.pottery.utils;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author lzq
 */
public class MySensor {

    static private SensorManager sensorManager = null;
    static private SensorEventListener sensorEventListener = null;
    static private Sensor sensor = null;
    static private float[] accelerometerData = null;

    static public void init(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    private static boolean readyFlag = false;

    static void openAccelerometer() {
        sensorEventListener = new SensorEventListener() {

            public void onSensorChanged(SensorEvent event) {
                accelerometerData = event.values;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_UI);
        readyFlag = true;
    }

    private static void closeAccelerometer() {
        if (sensorEventListener != null) {
            sensorManager.unregisterListener(sensorEventListener, sensor);
            sensorEventListener = null;
            sensor = null;
            readyFlag = false;
            accelerometerData = null;
        }
    }

    /**
     * 在调用该方法之前必须先调用{@link MySensor#openAccelerometer()}
     * 在不需要继续获取AccelerometerData时建议调用{@link MySensor#closeAccelerometer()},可以省电
     *
     * @return 返回参数为x y z的数组,表示三个方向的加速度,x,y,z的正方向分别为手机的左边,下边和背面
     * @throws InterruptedException
     */
    static float[] getAccelerometerData() {
        return accelerometerData;
    }

    /**
     * 在调用该方法之前必须先调用{@link MySensor#openAccelerometer()}
     * 在不需要继续获取AccelerometerData时建议调用{@link MySensor#closeAccelerometer()},可以省电
     *
     * @return 返回参数为屏幕下方和屏幕右方相对于手机下平放时转动的角度,
     */
    static float[] getAngle() {
        if (!readyFlag) {
            openAccelerometer();
        }
        float[] result = new float[2];
        float[] acc = getAccelerometerData();
        if (acc == null) {
            return null;
        }
        double g = Math.sqrt(Math.pow(acc[0], 2) + Math.pow(acc[1], 2) + Math.pow(acc[2], 2));
        result[0] = (float) (Math.asin(acc[1] / g) / Math.PI * 180);
        result[1] = (float) (Math.asin(Math.abs(acc[0]) / g) / Math.PI * 180);
        return result;
    }

    static public float[] getAngleAsy() {
        float[] result;
        while ((result = getAngle()) == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return result;
    }

    static public boolean isReady() {
        return readyFlag;
    }

}
