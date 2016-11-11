package com.page.acuratek.magic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

/**
 * Created by Ryan on 2016/6/7.
 */
abstract public class MyPage {
    static int background404ResId;
    String backgroundImgFileName = "page1";
    TextView tvName = null;
    int tvNameID = -1;
    int layout_id;
    int tmpl_id;
    String[] storagePathEnv = {"EXTERNAL_STORAGE", "SECONDARY_STORAGE"} ;
//    String[] rawSecondaryStoragesPath = {"storage/sdcard",  "storage/external_storage/sdcard"};
    String rawSecondaryStoragesStr = null;
    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }
    public Drawable loadBackground(Context context) {
        if(rawSecondaryStoragesStr == null) {
            HashSet<String> all_path = getExternalMounts();
            Iterator iterator = all_path.iterator();
            while (iterator.hasNext()) {
                String path = iterator.next() + "/acuratek";
                File f = new File(path);
                if (f.isDirectory()) {
                    rawSecondaryStoragesStr = path;
                    break;
                }
            }
//            for (String env : storagePathEnv) {
//                String path = System.getenv(env) + "/acuratek";
//                File f = new File(path);
//                if (f.isDirectory()) {
//                    rawSecondaryStoragesStr = path;
//                    break;
//                }
//            }
            if(rawSecondaryStoragesStr == null) {
                rawSecondaryStoragesStr = Environment.getExternalStorageDirectory().getAbsolutePath() + "/acuratek";
            }
        }
//        if(rawSecondaryStoragesStr == null) {
//            for(String eachPath : rawSecondaryStoragesPath) {
//                for(int i = 0; i <2; i++) {
//                    String path = eachPath + Integer.toString(i) + "/acuratek";
//                    File f = new File(path);
//                    if (f.isDirectory()) {
//                        rawSecondaryStoragesStr = path;
//                        break;
//                    }
//                }
//                if(rawSecondaryStoragesStr != null) { break; }
//            }
//            if(rawSecondaryStoragesStr == null) {
//                rawSecondaryStoragesStr = "storage/sdcard0/acuratek";
//            }
//        }
        InputStream input;
        try {
            File imgFile = new File(rawSecondaryStoragesStr + "/" + backgroundImgFileName + ".jpg");
            input = new FileInputStream(imgFile);
            Log.d("@@@@@@@@", backgroundImgFileName + " LOAD SUCCESS");
        } catch (FileNotFoundException e) {
//            File f = new File(Environment.getExternalStorageDirectory().getPath());
//            File file[] = f.listFiles();
//            Log.d("#############", "Files Size: ");
//            for (int i=0; i < file.length; i++)
//            {
//                Log.d("Files", "FileName:" + file[i].getName());
//            }
            input = context.getResources().openRawResource(MyPage.background404ResId);
        }
        Bitmap x = BitmapFactory.decodeStream(input, null, PageManager.bitmapOptions);
        return new BitmapDrawable(context.getResources(), x);
    }
    TextView setTextView(int tvID, String text) {
        TextView tv = null;
        if(tvID > 0) {
            tv = (TextView) PageManager.getUIComponent(tvID);
            tv.setText(text);
            tv.setVisibility(View.VISIBLE);
        }
        return tv;
    }
    ImageButton setImageButton(int btnID, final MyPage link, final Runnable action) {
        ImageButton ibtn = null;
        if (btnID > 0) {
            ibtn = (ImageButton) PageManager.getUIComponent(btnID);
            ibtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MagicUserManager.playBeep();
                    if (action != null) {  action.run();  }
                    if (link != null) {  PageManager.updateContentView(link);  }
                }
            });
        }
        return ibtn;
    }

    Button setButton(int btnID, final MyPage link, final Runnable action) {
        Button btn = null;
        if (btnID > 0) {
            btn = (Button) PageManager.getUIComponent(btnID);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MagicUserManager.playBeep();
                    if (action != null) {  action.run();  }
                    if (link != null) {  PageManager.updateContentView(link);  }
                }
            });
        }
        return btn;
    }

    abstract public void iniUIComponent();

    public void tvTimerUpdate() {}

    public void tvInfoUpdate() {}

}
