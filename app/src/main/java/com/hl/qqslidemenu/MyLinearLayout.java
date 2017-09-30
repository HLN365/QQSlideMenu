package com.hl.qqslidemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * 当slideMenu打开的时候，拦截并消费掉触摸事件
 * Created by HL on 2017/9/30/0030.
 */

public class MyLinearLayout extends LinearLayout {

    private SlideMenu mSlideMenu;

    public MyLinearLayout(Context context) {
        super(context);
    }

    public MyLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSlideMenu(SlideMenu slideMenu) {
        this.mSlideMenu = slideMenu;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //当SlideMenu打开时，拦截并消费事件,在onTouchEvent()方法中进行事件处理
        if (mSlideMenu != null && mSlideMenu.getCurrentState() == SlideMenu.DragStatus.OPEN) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSlideMenu != null && mSlideMenu.getCurrentState() == SlideMenu.DragStatus.OPEN) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //点击一下，则将SlideMenu关闭
                mSlideMenu.close();
            }
            //如果slideMenu打开则应该拦截并消费掉事件
            return true;
        }
        return super.onTouchEvent(event);
    }
}
