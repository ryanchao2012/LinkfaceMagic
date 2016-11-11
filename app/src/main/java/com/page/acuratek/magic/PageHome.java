package com.page.acuratek.magic;

import android.widget.Button;

/**
 * Created by Ryan on 2016/6/21.
 */
public class PageHome extends MyPage {
//    final float
    Button btnFit = null;
    Button btnMassage = null;
    Button btnIncome = null;
    Button btnIonCleanse = null;
    MyPage pageLinkFit;
    MyPage pageLinkMassage;
    MyPage pageLinkIncome;
    MyPage pageLinkIonCleanse;

    PageHome(String fileName) {
        super.backgroundImgFileName = fileName;
        super.layout_id = R.layout.layout_home;
        super.tmpl_id = R.id.tmpl_home;
        super.tvNameID = R.id.tvNameInHome;
    }
    public void iniUIComponent(){
        btnFit = super.setButton(R.id.btnFit, pageLinkFit, null);
        btnMassage = super.setButton(R.id.btnMassage, pageLinkMassage, null);
        btnIncome = super.setButton(R.id.btnIncome, pageLinkIncome, null);
        btnIonCleanse = super.setButton(R.id.btnIonCleanse, pageLinkIonCleanse, null);

        if(MagicUserManager.DEBUG) {
            tvName = super.setTextView(tvNameID, backgroundImgFileName);
        } else {
            tvName = super.setTextView(tvNameID, "");
        }
    }
}
