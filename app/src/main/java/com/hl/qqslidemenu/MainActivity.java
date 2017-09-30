package com.hl.qqslidemenu;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.CycleInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class MainActivity extends AppCompatActivity {

    private ListView mMenuListView;
    private ListView mMainListView;
    private SlideMenu mSlideMenu;
    private ImageView mHeadImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initData() {
        mMenuListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.sCheeseStrings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                return textView;
            }
        });
        mMainListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Constant.NAMES) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView == null ? super.getView(position, convertView, parent) : convertView;
                //先缩小View
                ViewHelper.setScaleX(view, 0.5f);
                ViewHelper.setScaleY(view, 0.5f);

                //在以属性动画放大
                ViewPropertyAnimator.animate(view).scaleX(1).setDuration(350).start();
                ViewPropertyAnimator.animate(view).scaleY(1).setDuration(350).start();

                return view;
            }
        });

        mSlideMenu.setOnDragStateChangeListener(new SlideMenu.OnDragStateChangeListener() {
            @Override
            public void onOpen() {
                Log.d("MainActivity", "open");
            }

            @Override
            public void onClose() {
                Log.d("MainActivity", "close");
                ViewPropertyAnimator.animate(mHeadImageView).translationX(15)
                        .setInterpolator(new CycleInterpolator(5))
                        .setDuration(500)
                        .start();
            }

            @Override
            public void onDragging(float fraction) {
                //隐藏头像
                ViewHelper.setAlpha(mHeadImageView, 1 - fraction);
            }
        });
    }

    private void initView() {
        mMenuListView = (ListView) findViewById(R.id.menu_listview);
        mMainListView = (ListView) findViewById(R.id.main_listview);
        mSlideMenu = (SlideMenu) findViewById(R.id.slideMenu);
        mHeadImageView = (ImageView) findViewById(R.id.iv_head);
        MyLinearLayout myLinearLayout = (MyLinearLayout) findViewById(R.id.my_layout);
        myLinearLayout.setSlideMenu(mSlideMenu);
    }
}
