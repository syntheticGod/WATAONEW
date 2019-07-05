package me.wowtao.pottery.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static android.opengl.GLES31.*;

/**
 * 加载顶点Shader与片元Shader的工具类
 *
 * @author albuscrow
 */
public class ShaderUtil {

    /**
     * 加载制定shader的方法
     *
     * @param shaderType shader的类型  GL_VERTEX_SHADER(顶点)   GL_FRAGMENT_SHADER(片元)
     * @param source     shader的脚本字符串
     * @return shader id
     */
    private static int loadShader(int shaderType, String source) {
        //创建一个新shader
        int shader = glCreateShader(shaderType);
        //若创建成功则加载shader
        if (shader != 0) {
            //加载shader的源代码
            glShaderSource(shader, source);
            //编译shader
            glCompileShader(shader);
            //存放编译成功shader数量的数组
            int[] compiled = new int[1];
            //获取Shader的编译情况
            glGetShaderiv(shader, GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {//若编译失败则显示错误日志并删除此shader
                Log.e("ES20_ERROR", "Could not compile shader " + shaderType + ":");
                Log.e("ES20_ERROR", glGetShaderInfoLog(shader));
                glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    /**
     * 创建shader程序的方法
     */
    public static int createProgram(String vertexSource, String fragmentSource) {
        //加载顶点着色器
        int vertexShader = loadShader(GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }

        //加载片元着色器
        int pixelShader = loadShader(GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        //创建程序
        int program = glCreateProgram();
        //若程序创建成功则向程序中加入顶点着色器与片元着色器
        if (program != 0) {
            //向程序中加入顶点着色器
            glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            //向程序中加入片元着色器
            glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            //链接程序
            glLinkProgram(program);
            //存放链接成功program数量的数组
            int[] linkStatus = new int[1];
            //获取program的链接情况
            glGetProgramiv(program, GL_LINK_STATUS, linkStatus, 0);
            //若链接失败则报错并删除程序
            if (linkStatus[0] != GL_TRUE) {
                Log.e("ES20_ERROR", "Could not link program: ");
                Log.e("ES20_ERROR", glGetProgramInfoLog(program));
                glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }

    //检查每一步操作是否有错误的方法
    private static void checkGlError(String op) {
        int error;
        if ((error = glGetError()) != GL_NO_ERROR) {
            Log.e("ES20_ERROR", op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

    //从sh脚本中加载shader内容的方法
    public static String loadFromAssetsFile(String fileName, Resources r) {
        String result = null;
        try {
            InputStream in = r.getAssets().open(fileName);
            int ch;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((ch = in.read()) != -1) {
                baos.write(ch);
            }
            byte[] buff = baos.toByteArray();
            baos.close();
            in.close();
            result = new String(buff, "UTF-8");
            result = result.replaceAll("\\r\\n", "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    static private Set<Integer> mem = new HashSet<>();

    public static void clearMem() {
        mem.clear();
    }

    public static void setTexParameter(int textureId) {
        glBindTexture(GL_TEXTURE_2D, textureId);
        if (!mem.contains(textureId)) {
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
            mem.add(textureId);
        }
    }

    static public int loadTexture(Context c, int textureName, int resId) {

        if (textureName == 0) {
            int[] temp = new int[1];
            glGenTextures(1, temp, 0);
            textureName = temp[0];
            glBindTexture(GL_TEXTURE_2D, textureName);
            InputStream is = c.getResources().openRawResource(resId);
            Bitmap texture = BitmapFactory.decodeStream(is);
            GLUtils.texImage2D(GL_TEXTURE_2D, 0, texture, 0);
            texture.recycle();
            System.gc();
        }
        return textureName;
    }
}
