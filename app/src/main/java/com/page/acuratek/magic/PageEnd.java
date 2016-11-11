package com.page.acuratek.magic;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Ryan on 2016/6/7.
 */
public class PageEnd extends MyPage {
    static  MyPage home = null;
    ImageButton ibtnHome = null;
    ImageView ivTag = null;
    boolean showTag = false;
    TextView tvInfo = null;
    int ivTagID = -1;
    int ibtnHomeID = -1;
    int tvInfoID = -1;

    PageEnd(String fileName) {
        super.backgroundImgFileName = fileName;
        super.layout_id = R.layout.layout_end;
        super.tmpl_id = R.id.tmpl_end;
        ibtnHomeID = R.id.ibtnHomeInEnd;
        tvNameID = R.id.tvNameInEnd;
        ivTagID = R.id.ivTagInEnd;
    }

    PageEnd(String fileName, int btnID, int tvID, int ivID) {
        super.backgroundImgFileName = fileName;
        ibtnHomeID = btnID;
        tvNameID = tvID;
        ivTagID = ivID;
    }
    public void iniUIComponent() {
        ibtnHome = super.setImageButton(ibtnHomeID, home, btnHomeAction);
        if(MagicUserManager.DEBUG) {
            tvName = super.setTextView(tvNameID, backgroundImgFileName);
        } else {
            tvName = super.setTextView(tvNameID, "");
        }
        tvInfo = super.setTextView(tvInfoID, "");

        if(MagicUserManager.userNum == 2 && showTag) {
            if(ivTagID > 0) {
                ivTag = (ImageView) PageManager.getUIComponent(ivTagID);
                switch (MagicUserManager.userIndex) {
                    case MagicUserManager.USER_A_IDX:
                        ivTag.setImageResource(R.drawable.mode_a_200);
                        break;
                    case MagicUserManager.USER_B_IDX:
                        ivTag.setImageResource(R.drawable.mode_b_200);
                        break;
                    default:
                        break;
                }
            }
        }

    }

    Runnable btnHomeAction = new Runnable() {
        public void run() {}
    };

}





