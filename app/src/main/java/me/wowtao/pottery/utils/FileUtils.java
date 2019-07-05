package me.wowtao.pottery.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileUtils {
    /**
     * used for collect pottery;
     *
     * @author albuscrow
     */
    public static class PotterySaved {
        public String fileName;
        public float[] vertices;
        public Bitmap texture;
        public Bitmap image;
        public int price;
        public int height;
        public int width;
    }

    public static String savePottery(Context context, PotterySaved pottery) {
        String filename = new SimpleDateFormat("yyyyMMddhhmmss", Locale.CHINA).format(new Date(System.currentTimeMillis()));
        saveSerializable(context, filename, pottery.vertices);
        saveSerializable(context, filename + "p", pottery.price);
        saveSerializable(context, filename + "h", pottery.height);
        saveSerializable(context, filename + "w", pottery.width);
        saveImage(context, filename + "1", pottery.texture);
        saveImage(context, filename + "2", pottery.image);
        return filename;
    }

    public static void deletePottery(Context context, String fileName) {
        deleteSerializable(context, fileName);
        deleteImage(context, fileName + "1");
        deleteImage(context, fileName + "2");
    }

    public static List<PotterySaved> getSavedPottery(Context context) {
        File parent = context.getFilesDir();
        String[] lists = parent.list();
        Set<String> fileNames = new HashSet<>();
        for (String string : lists) {
            if (string.length() == 18) {
                fileNames.add(string.substring(0, string.length() - 4));
            }
        }
        List<PotterySaved> result = new ArrayList<>();

        for (String fileName : fileNames) {
            System.out.println(fileName);
            Options op = new Options();
            op.inSampleSize = 3;
            op.inJustDecodeBounds = false;
            Bitmap image = null;
            try {
                image = BitmapFactory.decodeStream(context.openFileInput(fileName + "2.png"), null, op);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            PotterySaved pottery = new PotterySaved();
            pottery.image = image;
            pottery.fileName = fileName;
            result.add(pottery);
        }
        return result;
    }

    private static void saveSerializable(Context context, String fileName, Serializable object) {
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(context.openFileOutput(fileName + ".obj", Context.MODE_PRIVATE));
            os.writeObject(object);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void deleteSerializable(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName + ".obj");
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    public static Object getSerializable(Context context, String fileName) {
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(context.openFileInput(fileName + ".obj"));
            return is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private static void saveImage(Context context, String fileName, Bitmap bitmap) {
        FileOutputStream os = null;
        try {
            os = context.openFileOutput(fileName + ".png", Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, os);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void deleteImage(Context context, String fileName) {
        File file = new File(context.getFilesDir(), fileName + ".png");
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
