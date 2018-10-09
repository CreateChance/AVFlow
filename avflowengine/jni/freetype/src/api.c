/* example.c                                                      */
/*                                                                 */
/* This small program shows how to print a rotated string with the */
/* FreeType 2 library.                                             */


#include <stdio.h>
#include <string.h>
#include <math.h>
#include <jni.h>
#include <GLES2/gl2.h>
#include <android/log.h>

#include <ft2build.h>
#include FT_FREETYPE_H


#define WIDTH   640
#define HEIGHT  480


/* origin is the upper left corner */
unsigned char image[HEIGHT][WIDTH];


/* Replace this function with something useful. */

void
draw_bitmap(FT_Bitmap *bitmap,
            FT_Int x,
            FT_Int y) {
    FT_Int i, j, p, q;
    FT_Int x_max = x + bitmap->width;
    FT_Int y_max = y + bitmap->rows;


    /* for simplicity, we assume that `bitmap->pixel_mode' */
    /* is `FT_PIXEL_MODE_GRAY' (i.e., not a bitmap font)   */

    for (i = x, p = 0; i < x_max; i++, p++) {
        for (j = y, q = 0; j < y_max; j++, q++) {
            if (i < 0 || j < 0 ||
                i >= WIDTH || j >= HEIGHT)
                continue;

            image[j][i] |= bitmap->buffer[q * bitmap->width + p];
        }
    }
}


void
show_image(void) {
    int i, j;

    for (i = 0; i < HEIGHT; i++) {
        for (j = 0; j < WIDTH; j++) {
            putchar(image[i][j] == 0 ? ' '
                                     : image[i][j] < 128 ? '+'
                                                         : '*');
//            __android_log_print(ANDROID_LOG_DEBUG, "FreeTypeNative", "%c", image[i][j]);
        }
        putchar('\n');
//        __android_log_print(ANDROID_LOG_DEBUG, "FreeTypeNative", "\n");
    }
}

int main(int argc, char **argv) {
    FT_Library library;
    FT_Face face;

    FT_GlyphSlot slot;
    FT_Matrix matrix;                 /* transformation matrix */
    FT_Vector pen;                    /* untransformed origin  */
    FT_Error error;

    char *filename;
    char *text;

    double angle;
    int target_height;
    int n, num_chars;


    if (argc != 3) {
        fprintf(stderr, "usage: %s font sample-text\n", argv[0]);
        exit(1);
    }

    filename = argv[1];                           /* first argument     */
    text = argv[2];                           /* second argument    */
    num_chars = strlen(text);
    angle = (0.0 / 360) * 3.14159 * 2;      /* use 25 degrees     */
    target_height = HEIGHT;

    printf("Text len: %d \n", num_chars);

    error = FT_Init_FreeType(&library);              /* initialize library */
    /* error handling omitted */

    error = FT_New_Face(library, filename, 0, &face);/* create face object */
    /* error handling omitted */

    /* use 50pt at 100dpi */
    error = FT_Set_Char_Size(face, 50 * 64, 0,
                             100, 0);                /* set character size */
    /* error handling omitted */

    /* cmap selection omitted;                                        */
    /* for simplicity we assume that the font contains a Unicode cmap */

    slot = face->glyph;

    /* set up matrix */
    matrix.xx = (FT_Fixed) (cos(angle) * 0x10000L);
    matrix.xy = (FT_Fixed) (-sin(angle) * 0x10000L);
    matrix.yx = (FT_Fixed) (sin(angle) * 0x10000L);
    matrix.yy = (FT_Fixed) (cos(angle) * 0x10000L);

    /* the pen position in 26.6 cartesian space coordinates; */
    /* start at (300,200) relative to the upper left corner  */
    pen.x = 300 * 64;
    pen.y = (target_height - 200) * 64;

    for (n = 0; n < num_chars; n++) {
        /* set transformation */
        FT_Set_Transform(face, &matrix, &pen);

        /* load glyph image into the slot (erase previous one) */
        error = FT_Load_Char(face, 0x597D, FT_LOAD_RENDER);
        if (error)
            continue;                 /* ignore errors */

        /* now, draw to our target surface (convert position) */
        draw_bitmap(&slot->bitmap,
                    slot->bitmap_left,
                    target_height - slot->bitmap_top);

        /* increment pen position */
        pen.x += slot->advance.x;
        pen.y += slot->advance.y;
    }

    show_image();

    FT_Done_Face(face);
    FT_Done_FreeType(library);

    return 0;
}

JNIEXPORT jintArray JNICALL
Java_com_createchance_avflowengine_processor_TextWriter_loadText(JNIEnv *env, jobject obj,
                                                                 jstring jFontPath,
                                                                 jintArray textArray) {
    FT_Library library;
    FT_Face face;

    FT_Error error;

    int n, num_chars;

    const char *fontPath = (*env)->GetStringUTFChars(env, jFontPath, NULL);
    const int *text = (*env)->GetIntArrayElements(env, textArray, 0);

    num_chars = (*env)->GetArrayLength(env, textArray);

    error = FT_Init_FreeType(&library);              /* initialize library */
    /* error handling omitted */

    error = FT_New_Face(library, fontPath, 0, &face);/* create face object */
    /* error handling omitted */

    FT_Set_Pixel_Sizes(face, 0, 48);

    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
    jint resultArray[num_chars * 7];
    int resultIndex = 0;
    for (n = 0; n < num_chars; n++) {
        /* load glyph image into the slot (erase previous one) */
        error = FT_Load_Char(face, text[n], FT_LOAD_RENDER);
        if (error) {
            continue;                 /* ignore errors */
        }

        __android_log_print(ANDROID_LOG_DEBUG, "FreeTypeNative",
                            "bitmap width: %d, bitmap height: %d, bitmap left: %d, bitmap top: %d, glyph advance x: %ld, glyph advance y: %ld.",
                            face->glyph->bitmap.width,
                            face->glyph->bitmap.rows, face->glyph->bitmap_left,
                            face->glyph->bitmap_top, face->glyph->advance.x,
                            face->glyph->advance.x);

        // 生成纹理
        GLuint texture;
        glGenTextures(1, &texture);
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_ALPHA,
                face->glyph->bitmap.width,
                face->glyph->bitmap.rows,
                0,
                GL_ALPHA,
                GL_UNSIGNED_BYTE,
                face->glyph->bitmap.buffer
        );
        // 设置纹理选项
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        // 保存纹理id
        resultArray[resultIndex++] = texture;
        resultArray[resultIndex++] = face->glyph->bitmap.width;
        resultArray[resultIndex++] = face->glyph->bitmap.rows;
        resultArray[resultIndex++] = face->glyph->bitmap_left;
        resultArray[resultIndex++] = face->glyph->bitmap_top;
        resultArray[resultIndex++] = face->glyph->advance.x;
        resultArray[resultIndex++] = face->glyph->advance.y;
    }

    FT_Done_Face(face);
    FT_Done_FreeType(library);

    // result array
    jintArray result = (*env)->NewIntArray(env, num_chars * 7);
    (*env)->SetIntArrayRegion(env, result, 0, num_chars * 7, resultArray);
    return result;
}

/* EOF */