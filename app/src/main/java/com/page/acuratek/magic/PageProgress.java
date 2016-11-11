package com.page.acuratek.magic;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Ryan on 2016/6/28.
 */
public class PageProgress extends PageEnd {
    int tvTimerID = -1;
    int ibtnPauseID = -1;
    int ibtnContID = -1;
    int ibtnGotoID = -1;
    ImageButton ibtnGoto = null;
    ImageButton ibtnPause;
    ImageButton ibtnCont;
    TextView tvTimer;
    MyPage pageSetting;
    MyPage pageProgress;

    PageProgress(String fileName) {
        super(fileName, R.id.ibtnHomeInProgress, R.id.tvNameInProgress, R.id.ivTagInProgress);
        super.layout_id = R.layout.layout_progress;
        super.tmpl_id = R.id.tmpl_progress;
        super.tvInfoID = R.id.tvInfoInProgress;
        tvTimerID = R.id.tvTimerInProgress;
        ibtnPauseID = R.id.ibtnPauseInProgress;
        ibtnContID = R.id.ibtnContInProgress;
        ibtnGotoID = R.id.ivGOTOinProgress;
    }

    public void setGotoPage(MyPage ps, MyPage pp) {
        pageSetting = ps;
        pageProgress = pp;
    }

    public void iniUIComponent() {
        super.iniUIComponent();
        if (tvTimerID > 0) {
            tvTimer = (TextView) PageManager.getUIComponent(tvTimerID);
            tvTimerUpdate();
        }
        ibtnPause = super.setImageButton(ibtnPauseID, null, btnPauseAction);
        ibtnCont = super.setImageButton(ibtnContID, null, btnContAction);

        if (MagicUserManager.userNum == 2) {
            int gotoIdx = 1 - MagicUserManager.userIndex;
            MyPage gotoPage;
            if(MagicUserManager.getUserProgressStatus(gotoIdx)) { gotoPage = pageProgress; }
            else { gotoPage = pageSetting; }
            ibtnGoto = super.setImageButton(ibtnGotoID, gotoPage, btnGotoAction);
            switch (MagicUserManager.userIndex) {
                case MagicUserManager.USER_A_IDX:
                    ibtnGoto.setImageResource(R.drawable.mode_b_200);
                    break;
                case MagicUserManager.USER_B_IDX:
                    ibtnGoto.setImageResource(R.drawable.mode_a_200);
                    break;
                default:
                    break;
            }

//                ibtnGoto.set
        }
    }

    @Override
    public void tvTimerUpdate() {
        int sec = MagicUserManager.getUserRemainSeconds();
        tvTimer.setText(String.format("%02d:%02d", sec / 60, sec % 60));
    }

    Runnable btnPauseAction = new Runnable() {
        public void run() {
            MagicUserManager.setUserBypassProgress(true);
        }
    };

    Runnable btnContAction = new Runnable() {
        public void run() {
            MagicUserManager.setUserBypassProgress(false);
        }
    };
    Runnable btnGotoAction = new Runnable() {
        public void run() {
            MagicUserManager.userIndex = 1 - MagicUserManager.userIndex;
        }
    };
}
