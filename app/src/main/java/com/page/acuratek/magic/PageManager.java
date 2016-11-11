package com.page.acuratek.magic;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Ryan on 2016/6/20.
 */
public class PageManager{
    static Activity main;
    static MyPage currentPage;
    static MyPage sessionEndPage;
    static MyPage homePage;
    static MyPage inProgressPage;
    static  BitmapFactory.Options bitmapOptions;


    static class MainHandler extends Handler {
        private WeakReference<Activity> mActivity;
        MainHandler(Activity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MagicUserManager.RESET_COUNTER:
                        MagicUserManager.COUNTER = 0;
                        if(PageManager.currentPage.backgroundImgFileName.equals("slide3")) {
                            PageBonus page = (PageBonus) PageManager.currentPage;
                            page.tvCounter.setText(String.format("%05d", MagicUserManager.COUNTER));
                        }
                        break;
                    case MainActivity.TOUCHPAD_EVENT:
                        MagicUserManager.counterIdle = 0;
//                        MainActivity.updateToast(PageManager.main, ":)", Toast.LENGTH_SHORT);
//                        Window window = main.getWindow();
//                        try{
//                            MotionEvent em = (MotionEvent)msg.obj;
//                            window.superDispatchTouchEvent(em);
////                            String text = Integer.toString((int)em.getX()) + ", " + Integer.toString((int) em.getY());
////                            currentPage.tvName.setText(text);
//                        } catch (Exception e) {
//                            String text = "[" + e.getClass().toString() + "]" + e.getMessage() + "<" + e.getCause().toString() + ">";
//                            currentPage.tvName.setText(text);
//                        }
                        break;
                    case MagicUserManager.SESSION_END:
                        PageManager.updateContentView(PageManager.sessionEndPage);
                        break;
                    case MagicUserManager.UPDATE_SLIDE7:
                        PageManager.currentPage.tvInfoUpdate();
                        break;
                    case MagicUserManager.UPDATE_SLIDE8:
                        PageManager.currentPage.tvInfoUpdate();
                        PageManager.currentPage.tvTimerUpdate();
                        break;
                    case MagicUserManager.SHOW_TOAST:
                        MainActivity.updateToast(PageManager.main, (String)msg.obj, Toast.LENGTH_SHORT);
                        break;
                    case MainActivity.TOUCHPAD_DEBUG:
                        currentPage.tvName.setText((String)msg.obj);
                        currentPage.tvName.setTextColor(Color.DKGRAY);
                        currentPage.tvName.setTypeface(Typeface.DEFAULT_BOLD);
                        break;
                    case MainActivity.TOUCHPAD_DEBUG2:
                        currentPage.tvName.append((String)msg.obj);
                        break;
                    case MagicUserManager.IDLE_TIMEOUT:
                        int to = (int)msg.obj;
                        if(to == MagicUserManager.GO_PROGRESS) {
                            PageManager.updateContentView(PageManager.inProgressPage);
                        } else if(to == MagicUserManager.GO_HOME) {
                            PageManager.updateContentView(PageManager.homePage);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
    static MainHandler mainHandler;

    static public void setBitmapOptions(int scale){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = scale;
        bitmapOptions = options;
    }
    static void updateContentView(MyPage toPage) {
        currentPage = toPage;
        // TODO
        if(currentPage.backgroundImgFileName.equals("slide7")) { MagicUserManager.COUNTER += 1; }
        main.setContentView(currentPage.layout_id);

        main.findViewById(currentPage.tmpl_id).setBackground(currentPage.loadBackground(main));
        currentPage.iniUIComponent();
        MagicUserManager.counterIdle = 0;

//        currentPage.tvName.setText("");
//        recursiveListFiles("/mnt");

        currentPage.tvInfoUpdate();

    }

    static void iniManager(Activity activity) {
        main = activity;
        mainHandler = new MainHandler(main);
    }

    static View getUIComponent(int id) {
        return main.findViewById(id);
    }
//    @Override
//    public boolean onTouch(View v, MotionEvent e)
//    {
//        Message msg = new Message();
//        msg.what = MainActivity.TOUCHPAD_EVENT;
//        msg.obj = e;
//        mainHandler.sendMessage(msg);
//        return false;
//    }
    static private void recursiveListFiles(String path) {
        File f = new File(path);
        File[] files = f.listFiles();
        for (File inFile : files) {
            try {
                currentPage.tvName.append(inFile.getName() + "/");
                if (inFile.isDirectory()) {
                    recursiveListFiles(inFile.getAbsolutePath());
                }

            } catch (Exception e) {
                if (e.getCause() instanceof NullPointerException)
                    currentPage.tvName.append("null" + "/");
                else currentPage.tvName.append(e.toString() + "/");
            }
        }
    }
}
