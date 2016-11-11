package com.page.acuratek.magic;

import android.content.Context;
import android.content.res.Resources;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.CompoundButton;
import android.widget.Toast;

/**
 * Created by Ryan on 2016/6/24.
 */
public class PageMode extends PageNext {
    static final int NUM_RBTN = 5;
//    RadioGroup rbtngrpMode;
    ImageButton[] rbtnMode = new ImageButton[NUM_RBTN];
    int[] rbtnModeID = new int[NUM_RBTN];
//    int rbtngrpModeID = -1;
    PageMode(String fileName, MyPage nx) {
        super(fileName, nx, R.id.ibtnHomeInMode, R.id.tvNameInMode, R.id.ivTagInMode, -1);
        super.layout_id = R.layout.layout_mode;
        super.tmpl_id = R.id.tmpl_mode;
//        rbtngrpModeID = R.id.rbtngrpMode;
        Resources r = PageManager.main.getResources();
        String pkg_name = PageManager.main.getPackageName();
        for(int i = 0; i < NUM_RBTN; i++) {
            rbtnModeID[i] = r.getIdentifier("rbtnMode" + Integer.toString(i + 1), "id", pkg_name);
        }
    }

    public void iniUIComponent(){
        super.iniUIComponent();

        for(int i = 0; i < NUM_RBTN; i++) {
            if(rbtnModeID[i] > 0) {
                rbtnMode[i] = (ImageButton) PageManager.getUIComponent(rbtnModeID[i]);
                rbtnMode[i].setTag(i);
                rbtnMode[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int idx = (int)v.getTag();
                        MainActivity.updateToast(PageManager.main, "Select Mode " + Integer.toString(idx + 1), Toast.LENGTH_SHORT);
                        MagicUserManager.updateUserMode(idx + 1);
                        PageManager.updateContentView(next);
                    }
                });
            }
        }

        if (MagicUserManager.userNum == 1) if(rbtnModeID[0] > 0) {
            RelativeLayout rl = (RelativeLayout)PageManager.getUIComponent(R.id.rlRbtnOffset);
            LayoutParams params = new LayoutParams(0, LayoutParams.MATCH_PARENT);
            params.weight = 0.07f;
            rl.setLayoutParams(params);
        }
    }

}
