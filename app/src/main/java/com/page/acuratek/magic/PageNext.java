package com.page.acuratek.magic;

import android.widget.ImageButton;

/**
 * Created by Ryan on 2016/6/7.
 */
public class PageNext extends PageEnd {
    MyPage next = null;
    ImageButton ibtnNext = null;
    int ibtnNextID = -1;

    PageNext(String fileName, MyPage nx) {
        super(fileName, R.id.ibtnHomeInNext, R.id.tvNameInNext, R.id.ivTagInNext);
        super.layout_id = R.layout.layout_next;
        super.tmpl_id = R.id.tmpl_next;
        next = nx;
        ibtnNextID = R.id.ibtnNextInNext;
    }
    PageNext(String fileName, MyPage nx, int ibtnHID, int tvID, int ivID, int ibtnNID) {
        super(fileName, ibtnHID, tvID, ivID);
        next = nx;
        ibtnNextID = ibtnNID;
    }

    public void iniUIComponent() {
        super.iniUIComponent();
        ibtnNext = super.setImageButton(ibtnNextID, next, btnNextAction);
    }

    Runnable btnNextAction = new Runnable() {
        public void run() {}
    };
}
