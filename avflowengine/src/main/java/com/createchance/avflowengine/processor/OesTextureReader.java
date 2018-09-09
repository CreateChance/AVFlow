package com.createchance.avflowengine.processor;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.createchance.avflowengine.processor.gpuimage.OpenGlUtils;

import java.nio.FloatBuffer;

import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;

/**
 * ${DESC}
 *
 * @author createchance
 * @date 2018/5/19
 */
class OesTextureReader {
    private final String BASE_OES_VERTEX_SHADER =
            "attribute vec4 a_Position;\n" +
                    "attribute vec2 a_TextureCoordinates;\n" +
                    "uniform mat4 u_Matrix;\n" +
                    "varying vec2 v_TextureCoordinates;\n" +
                    "\n" +
                    "void main(){\n" +
                    "    gl_Position = u_Matrix * a_Position;\n" +
                    "    v_TextureCoordinates = a_TextureCoordinates;\n" +
                    "}";

    private final String BASE_OES_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 v_TextureCoordinates;\n" +
                    "uniform samplerExternalOES u_TextureUnit;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D( u_TextureUnit, v_TextureCoordinates );\n" +
                    "}";

    // Uniform
    private final String U_MATRIX = "u_Matrix";
    private final String U_TEXTURE_UNIT = "u_TextureUnit";

    // Attribute
    private final String A_POSITION = "a_Position";
    private final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    private int mMartixLocation, mTextureUnitLocation, mPositionLocaiton, mTextureCoorLocation;

    private FloatBuffer vertexPositionBuffer;
    private FloatBuffer textureCoordinateBuffer;

    private int mProgramId;

    private int mOesTextureId;

    OesTextureReader(int oesTextureId) {
        mOesTextureId = oesTextureId;
        init();
    }

    private void init() {
        mProgramId = OpenGlUtils.loadProgram(
                BASE_OES_VERTEX_SHADER,
                BASE_OES_FRAGMENT_SHADER
        );
//        if (OpenGlUtils.validateProgram(mProgramId)) {
//            throw new IllegalStateException("Program id: " + mProgramId + " invalid!");
//        }

        // init location
        mMartixLocation = glGetUniformLocation(mProgramId, U_MATRIX);
        mTextureUnitLocation = glGetUniformLocation(mProgramId, U_TEXTURE_UNIT);
        mPositionLocaiton = glGetAttribLocation(mProgramId, A_POSITION);
        mTextureCoorLocation = glGetAttribLocation(mProgramId, A_TEXTURE_COORDINATES);

        glUseProgram(mProgramId);

        vertexPositionBuffer = OpenGlUtils.createFloatBuffer(
                new float[]{
                        -1.0f, 1.0f,
                        -1.0f, -1.0f,
                        1.0f, 1.0f,
                        1.0f, -1.0f,
                }
        );
        // default rotation is 0 degree
        textureCoordinateBuffer = OpenGlUtils.createFloatBuffer(
                new float[]{
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,
                }
        );

        // set uniform vars
        glUniformMatrix4fv(
                mMartixLocation,
                1,
                false,
                OpenGlUtils.flip(OpenGlUtils.getIdentityMatrix(), true, true),
                0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mOesTextureId);
        GLES20.glUniform1i(mTextureUnitLocation, 0);
        setRotation(270);
    }

    void read(int x, int y, int width, int height) {
        glUseProgram(mProgramId);
        glViewport(x, y, width, height);
        glEnableVertexAttribArray(mPositionLocaiton);
        glVertexAttribPointer(
                mPositionLocaiton,
                2,
                GLES20.GL_FLOAT,
                false,
                2 * OpenGlUtils.SIZEOF_FLOAT,
                vertexPositionBuffer);
        glEnableVertexAttribArray(mTextureCoorLocation);
        glVertexAttribPointer(
                mTextureCoorLocation,
                2,
                GLES20.GL_FLOAT,
                false,
                2 * OpenGlUtils.SIZEOF_FLOAT,
                textureCoordinateBuffer);
        glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        glDisableVertexAttribArray(mPositionLocaiton);
        glDisableVertexAttribArray(mTextureCoorLocation);
    }

    void setRotation(int rotation) {
        float[] coord;
        switch (rotation) {
            case 0:
                coord = new float[]{
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,
                };
                break;
            case 90:
                coord = new float[]{
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        0.0f, 0.0f,
                        1.0f, 0.0f
                };
                break;
            case 180:
                coord = new float[]{
                        1.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        0.0f, 0.0f,
                };
                break;
            case 270:
                coord = new float[]{
                        1.0f, 0.0f,
                        0.0f, 0.0f,
                        1.0f, 1.0f,
                        0.0f, 1.0f
                };
                break;
            default:
                return;
        }
        textureCoordinateBuffer.clear();
        textureCoordinateBuffer.put(coord);
        textureCoordinateBuffer.position(0);
    }
}
