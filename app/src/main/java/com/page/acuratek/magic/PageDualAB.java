package com.page.acuratek.magic;

import android.widget.ImageButton;

/**
 * Created by Ryan on 2016/7/16.
 */
public class PageDualAB extends PageEnd {
    ImageButton ibtnA;
    ImageButton ibtnB;
    MyPage next = null;
    MyPage pageProgress = null;
    int ibtnAID = -1;
    int ibtnBID = -1;
    PageDualAB(String fileName, MyPage nx) {
        super(fileName, R.id.ibtnHomeInDualAB, R.id.tvNameInDualAB, -1);
        super.layout_id = R.layout.layout_dual_ab;
        super.tmpl_id = R.id.tmpl_dual_ab;
        ibtnAID = R.id.ibtnAInDualAB;
        ibtnBID = R.id.ibtnBInDualAB;
        next = nx;
    }
    public void setProgressPage(MyPage pp) {
        pageProgress = pp;
    }
    public void iniUIComponent() {
        super.iniUIComponent();
        MyPage gotoPage;
        if(MagicUserManager.getUserProgressStatus(0)) { gotoPage = pageProgress; }
        else { gotoPage = next; }
        ibtnA = super.setImageButton(ibtnAID, gotoPage, btnA_Action);
        gotoPage = null;
        if(MagicUserManager.getUserProgressStatus(1)) { gotoPage = pageProgress; }
        else { gotoPage = next; }
            ibtnB = super.setImageButton(ibtnBID, gotoPage, btnB_Action);


    }

    Runnable btnA_Action = new Runnable() {
        public void run() { MagicUserManager.userIndex = MagicUserManager.USER_A_IDX; }
    };

    Runnable btnB_Action = new Runnable() {
        public void run() { MagicUserManager.userIndex = MagicUserManager.USER_B_IDX;}
    };
}
