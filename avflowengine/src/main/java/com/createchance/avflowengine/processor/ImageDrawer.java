package com.createchance.avflowengine.processor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.createchance.avflowengine.processor.gpuimage.OpenGlUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES10.glViewport;
import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDisable;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * Image drawer.
 *
 * @author createchance
 * @date 2018-10-12
 */
public class ImageDrawer {
    private static final String TAG = "ImageDrawer";

    private final String VERTEX_SHADER =
            "uniform mat4 u_Matrix;\n" +
                    "\n" +
                    "attribute vec4 a_Position;\n" +
                    "attribute vec2 a_TextureCoordinates;\n" +
                    "\n" +
                    "varying vec2 v_TextureCoordinates;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    v_TextureCoordinates = a_TextureCoordinates;\n" +
                    "    gl_Position = u_Matrix * a_Position;\n" +
                    "}\n";

    private final String FRAGMENT_SHADER =
            "precision mediump float;\n" +
                    "\n" +
                    "uniform sampler2D u_TextureUnit;\n" +
                    "uniform float u_AlphaFactor;\n" +
                    "varying vec2 v_TextureCoordinates;\n" +
                    "\n" +
                    "void main() {\n" +
                    "    mediump vec4 sampledColor = texture2D(u_TextureUnit, v_TextureCoordinates);\n" +
                    "    gl_FragColor = vec4(sampledColor.rgb, sampledColor.a * u_AlphaFactor);\n" +
                    "}\n";

    private List<Integer> mTextureIds;
    private int mCurrentIndex;
    private int width;
    private int height;
    private int mPosX, mPosY;

    // Uniform
    private final String U_MATRIX = "u_Matrix";
    private final String U_TEXTURE_UNIT = "u_TextureUnit";
    private final String U_ALPHA_FACTOR = "u_AlphaFactor";

    // Attribute
    private final String A_POSITION = "a_Position";
    private final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    private FloatBuffer mVertexPositionBuffer;
    private FloatBuffer mTextureCoordinateBuffer;

    private int mMartixLocation, mTextureUnitLocation, mPositionLocaiton, mTextureCoorLocation, mAlphaFactorLocation;

    private float mAlphaFactor = 1.0f;

    private int mProgramId;

    public ImageDrawer() {
        mTextureIds = new ArrayList<>();
        mProgramId = OpenGlUtils.loadProgram(
                VERTEX_SHADER,
                FRAGMENT_SHADER
        );

        // init location
        mMartixLocation = glGetUniformLocation(mProgramId, U_MATRIX);
        mTextureUnitLocation = glGetUniformLocation(mProgramId, U_TEXTURE_UNIT);
        mAlphaFactorLocation = glGetUniformLocation(mProgramId, U_ALPHA_FACTOR);
        mPositionLocaiton = glGetAttribLocation(mProgramId, A_POSITION);
        mTextureCoorLocation = glGetAttribLocation(mProgramId, A_TEXTURE_COORDINATES);

        glUseProgram(mProgramId);

        mTextureCoordinateBuffer = OpenGlUtils.createFloatBuffer(
                new float[]{
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,
                }
        );

        // set matrix
        // set uniform vars
        float[] projectionMatrix = new float[16];
        float[] modelMatrix = new float[16];
//        perspectiveM(projectionMatrix,45, (float) surfaceWidth
//                / (float) surfaceHeight, 1f, 10f);
        Matrix.orthoM(
                projectionMatrix,
                0,
                -1f,
                1f,
                1f,
                -1f,
                0.1f,
                100f
        );

        setIdentityM(modelMatrix, 0);
        translateM(modelMatrix, 0, 0f, 0f, -2.5f);
//        rotateM(modelMatrix, 0, -60f, 1f, 0f, 0f);

        final float[] temp = new float[16];
        multiplyMM(temp, 0, projectionMatrix, 0, modelMatrix, 0);
        System.arraycopy(temp, 0, projectionMatrix, 0, temp.length);
        OpenGlUtils.flip(projectionMatrix, false, true);

        glUniformMatrix4fv(
                mMartixLocation,
                1,
                false,
                projectionMatrix,
                0);
    }

    public void setImage(List<String> imageList, int posX, int posY, float scaleFactor) {
        for (String image : imageList) {
            Bitmap bitmap = BitmapFactory.decodeFile(image);
            if (width == 0) {
                width = (int) (bitmap.getWidth() * scaleFactor);
            }
            if (height == 0) {
                height = (int) (bitmap.getHeight() * scaleFactor);
            }
            int textureId = OpenGlUtils.loadTexture(bitmap, OpenGlUtils.NO_TEXTURE, true);
            mTextureIds.add(textureId);
        }

        mPosX = posX;
        mPosY = posY;

        mVertexPositionBuffer = OpenGlUtils.createFloatBuffer(
                new float[]{
                        -1.0f * scaleFactor, 1.0f * scaleFactor,
                        -1.0f * scaleFactor, -1.0f * scaleFactor,
                        1.0f * scaleFactor, 1.0f * scaleFactor,
                        1.0f * scaleFactor, -1.0f * scaleFactor,
                }
        );
    }

    public void draw() {
        if (mAlphaFactor <= 0f) {
            return;
        }

        glUseProgram(mProgramId);
        glViewport(mPosX, mPosY, width, height);

        GLES20.glClearColor(0, 0, 0, 0);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        // bind texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, mTextureIds.get(mCurrentIndex));
        glUniform1i(mTextureUnitLocation, 0);

        GLES20.glUniform1f(mAlphaFactorLocation, mAlphaFactor);

        glEnableVertexAttribArray(mPositionLocaiton);
        mVertexPositionBuffer.position(0);
        glVertexAttribPointer(
                mPositionLocaiton,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                mVertexPositionBuffer);
        glEnableVertexAttribArray(mTextureCoorLocation);
        mTextureCoordinateBuffer.position(0);
        glVertexAttribPointer(
                mTextureCoorLocation,
                2,
                GLES20.GL_FLOAT,
                false,
                0,
                mTextureCoordinateBuffer);
        glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        glDisableVertexAttribArray(mPositionLocaiton);
        glDisableVertexAttribArray(mTextureCoorLocation);
        glDisable(GL_BLEND);

        if (mCurrentIndex < mTextureIds.size() - 1) {
            mCurrentIndex++;
        } else {
            mAlphaFactor -= 0.1f;
        }
    }

    public void release() {
        glDeleteProgram(mProgramId);
    }
}
