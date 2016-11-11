package com.page.acuratek.magic;

import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by Ryan on 2016/6/24.
 */
public class PageBonus extends PageNext {
    MyPage Bonus;
    ImageButton ibtnBonus;
    TextView tvCounter;
    int tvCounterID = -1;
    int ibtnBonusID = -1;
    PageBonus(String fileName, MyPage nx, MyPage bns) {
        super(fileName, nx, R.id.ibtnHomeInBonus, R.id.tvNameInBonus, -1, R.id.ibtnNextInBonus);
        super.layout_id = R.layout.layout_bonus;
        super.tmpl_id = R.id.tmpl_bonus;
        Bonus = bns;
        ibtnBonusID = R.id.ibtnBonusInBonus;
        tvCounterID = R.id.tvCounterInBonus;
    }
    public void iniUIComponent() {
        super.iniUIComponent();
        tvCounter = (TextView) PageManager.getUIComponent(tvCounterID);
        tvCounter.setText(String.format("%05d", MagicUserManager.COUNTER));
        ibtnBonus = super.setImageButton(ibtnBonusID, Bonus, btnBonusAction);
    }

    Runnable btnBonusAction = new Runnable() {
        public void run() {}
    };

}
