package com.page.acuratek.magic;

import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by Ryan on 2016/6/27.
 */
public class PageSetting extends PageNext {
    int ibtnUpID = -1;
    int ibtnDownID = -1;
    int tvTimerID = -1;
    ImageButton ibtnUp;
    ImageButton ibtnDown;
    TextView tvTimer;
    PageSetting(String fileName, MyPage nx) {
        super(fileName, nx, R.id.ibtnHomeInSetting, R.id.tvNameInSetting, R.id.ivTagInSetting, R.id.ibtnNextInSetting);
        super.layout_id = R.layout.layout_setting;
        super.tmpl_id = R.id.tmpl_setting;
        ibtnUpID = R.id.ibtnUpInSetting;
        ibtnDownID = R.id.ibtnDownInSetting;
        tvTimerID = R.id.tvTimerInSetting;
    }

    public void iniUIComponent() {
        super.iniUIComponent();
        if(tvTimerID > 0) {
            tvTimer = (TextView) PageManager.getUIComponent(tvTimerID);
            tvTimerUpdate();
        }
        ibtnUp = super.setImageButton(ibtnUpID, null, btnUpAction);
        ibtnDown = super.setImageButton(ibtnDownID, null, btnDownAction);

    }

    public void tvTimerUpdate() {
        int sec = MagicUserManager.getUserTargetSeconds();
        tvTimer.setText(String.format("%02d:%02d", sec / 60, sec % 60));
    }

    Runnable btnUpAction = new Runnable() {
        public void run() {
            MagicUserManager.setUserTargetSeconds(MagicUser.secondStep);
            tvTimerUpdate();
        }
    };

    Runnable btnDownAction = new Runnable() {
        public void run() {
            MagicUserManager.setUserTargetSeconds(-MagicUser.secondStep);
            tvTimerUpdate();
        }
    };
}
