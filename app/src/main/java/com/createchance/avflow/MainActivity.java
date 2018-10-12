package com.createchance.avflow;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.createchance.avflowengine.base.WorkRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        findViewById(R.id.btn_start_take).setOnClickListener(this);
        WorkRunner.addTaskToBackground(new Runnable() {
            @Override
            public void run() {
                tryCopyFontFile();
                tryCopyStickers();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_take:
                VideoRecordActivity.start(this);
                break;
            case R.id.btn_test_freetype:
                break;
            default:
                break;
        }
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    private void tryCopyFontFile() {
        File fontDir = new File(getFilesDir(), "fonts");
        if (fontDir.exists()) {
            return;
        }
        fontDir.mkdir();

        InputStream is = null;
        OutputStream os = null;
        List<String> fontList = new ArrayList<>();
        fontList.add("KaBuQiNuo.otf");
        fontList.add("MFYanSong-Regular.ttf");
        fontList.add("SentyWEN2017.ttf");
        fontList.add("YouLangRuanBi.ttf");
        try {
            for (String font : fontList) {
                is = getAssets().open(font);
                os = new FileOutputStream(new File(fontDir, font));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void tryCopyStickers() {
        File stickerDir = new File(getFilesDir(), "stickers");
        if (stickerDir.exists()) {
            return;
        }
        stickerDir.mkdir();

        InputStream is = null;
        OutputStream os = null;
        File targetDir;
        try {
            List<String> beachList = Arrays.asList(getAssets().list("stickers/beach"));
            targetDir = new File(stickerDir, "beach");
            targetDir.mkdir();
            for (String image : beachList) {
                is = getAssets().open("stickers/beach/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> bookList = Arrays.asList(getAssets().list("stickers/book"));
            targetDir = new File(stickerDir, "book");
            targetDir.mkdir();
            for (String image : bookList) {
                is = getAssets().open("stickers/book/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> cityList = Arrays.asList(getAssets().list("stickers/city"));
            targetDir = new File(stickerDir, "city");
            targetDir.mkdir();
            for (String image : cityList) {
                is = getAssets().open("stickers/city/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> drinkList = Arrays.asList(getAssets().list("stickers/drink"));
            targetDir = new File(stickerDir, "drink");
            targetDir.mkdir();
            for (String image : drinkList) {
                is = getAssets().open("stickers/drink/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> imcomeList = Arrays.asList(getAssets().list("stickers/imcome"));
            targetDir = new File(stickerDir, "imcome");
            targetDir.mkdir();
            for (String image : imcomeList) {
                is = getAssets().open("stickers/imcome/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> pingfanList = Arrays.asList(getAssets().list("stickers/pingfan"));
            targetDir = new File(stickerDir, "pingfan");
            targetDir.mkdir();
            for (String image : pingfanList) {
                is = getAssets().open("stickers/pingfan/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> trainList = Arrays.asList(getAssets().list("stickers/train"));
            targetDir = new File(stickerDir, "train");
            targetDir.mkdir();
            for (String image : trainList) {
                is = getAssets().open("stickers/train/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> tubuList = Arrays.asList(getAssets().list("stickers/tubu"));
            targetDir = new File(stickerDir, "tubu");
            targetDir.mkdir();
            for (String image : tubuList) {
                is = getAssets().open("stickers/tubu/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> weizhiList = Arrays.asList(getAssets().list("stickers/weizhi"));
            targetDir = new File(stickerDir, "weizhi");
            targetDir.mkdir();
            for (String image : weizhiList) {
                is = getAssets().open("stickers/weizhi/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }

            List<String> xingchenList = Arrays.asList(getAssets().list("stickers/xingchen"));
            targetDir = new File(stickerDir, "xingchen");
            targetDir.mkdir();
            for (String image : xingchenList) {
                is = getAssets().open("stickers/xingchen/" + image);
                os = new FileOutputStream(new File(targetDir, image));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                is.close();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
