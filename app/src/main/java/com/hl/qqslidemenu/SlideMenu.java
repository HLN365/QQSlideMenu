package com.hl.qqslidemenu;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

/**
 * 自定义侧滑菜单View
 * Created by HL on 2017/9/29/0029.
 */

public class SlideMenu extends FrameLayout {


    private View mMenuView;
    private View mMainView;
    private float mDragRange;//拖拽范围
    private ViewDragHelper mDragHelper;
    private DragStatus currentStatus = DragStatus.CLOSE;
    private ArgbEvaluator colorEvaluator;
    private FloatEvaluator floatEvaluator;

    public SlideMenu(Context context) {
        super(context);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * SlideMenu的状态：打开或者关闭
     */
    enum DragStatus {
        OPEN, CLOSE;
    }

    /**
     * 获取当前SlideMenu的状态
     *
     * @return
     */
    public DragStatus getCurrentState() {
        return currentStatus;
    }

    private void init() {
        mDragHelper = ViewDragHelper.create(this, callback);
        colorEvaluator = new ArgbEvaluator();
        floatEvaluator = new FloatEvaluator();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //拦截事件交由ViewDragHelper处理
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //事件交由ViewDragHelper处理
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //在读取xml结束标签后调用，可以获取子View
        mMenuView = getChildAt(0);
        mMainView = getChildAt(1);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //在onMeasure()方法执行后调用，可以获取控件的确切宽高
        mDragRange = getMeasuredWidth() * 0.6f;
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //尝试捕获触摸的控件
            return child == mMainView || child == mMenuView;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            //获取View在水平方向的拖拽方位，但并不限制边界，主要用在计算动画时间上
            return (int) mDragRange;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            //修正View在水平方向的位置，可以在此方法里做边界限制操作
            if (child == mMainView) {
                if (left < 0) left = 0;
                if (left > mDragRange) left = (int) mDragRange;
            }
            return left;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            //当触摸的View位置变化时调用，可以做View的伴随移动操作
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if (changedView == mMenuView) {
                //对mMenuView进行固定
                mMenuView.layout(0, 0, mMenuView.getMeasuredWidth(), mMenuView.getMeasuredHeight());
                //让mainView作伴随移动    对mainView做边界限制
                int newLeft = mMainView.getLeft() + dx;
                if (newLeft < 0) newLeft = 0;
                if (newLeft > mDragRange) newLeft = (int) mDragRange;
                mMainView.layout(newLeft, mMainView.getTop() + dy,
                        mMainView.getRight() + dx, mMainView.getBottom() + dy);
            }
            //计算滑动的百分比
            float fraction = mMainView.getLeft() / mDragRange;
            //执行动画
            executeAnim(fraction);
            //接口回调
            if (fraction == 1 && currentStatus != DragStatus.OPEN) {
                currentStatus = DragStatus.OPEN;
                if (mListener != null) {
                    mListener.onOpen();
                }
            }
            if (fraction == 0 && currentStatus != DragStatus.CLOSE) {
                currentStatus = DragStatus.CLOSE;
                if (mListener != null) {
                    mListener.onClose();
                }
            }

            if (mListener != null) {
                mListener.onDragging(fraction);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //触摸的View抬手释放，可以做View的平滑滚动操作
            super.onViewReleased(releasedChild, xvel, yvel);
            if (mMainView.getLeft() < mDragRange / 2) {
                //关闭
                close();
            } else {
                //打开
                open();
            }
            //很小的拖动就可以打开或者关闭
            if (xvel > 300 && currentStatus != DragStatus.OPEN) {
                //打开
                open();
                currentStatus = DragStatus.OPEN;
                if (mListener != null) {
                    mListener.onOpen();
                }

            } else if (xvel < -300 && currentStatus != DragStatus.CLOSE) {
                //关闭
                close();
                currentStatus = DragStatus.CLOSE;
                if (mListener != null) {
                    mListener.onClose();
                }
            }
        }
    };

    //执行动画
    private void executeAnim(float fraction) {
        //mMainView缩放变化 从1~0.8进行过度
        ViewHelper.setScaleX(mMainView, floatEvaluator.evaluate(fraction, 1, 0.8f));
        ViewHelper.setScaleY(mMainView, floatEvaluator.evaluate(fraction, 1, 0.8f));

        //mMenuView透明度变化
        ViewHelper.setAlpha(mMenuView, floatEvaluator.evaluate(fraction, 0.3, 1));
        //mMenuView移动
        ViewHelper.setTranslationX(mMenuView, floatEvaluator.evaluate(fraction, -mMenuView.getMeasuredWidth() / 2, 0));
        //mMenuView缩放变化
        ViewHelper.setScaleX(mMenuView, floatEvaluator.evaluate(fraction, 0.5f, 1));
        ViewHelper.setScaleY(mMenuView, floatEvaluator.evaluate(fraction, 0.5f, 1));

        //SlideMenu背景变化 给SlideMenu的背景添加黑色的遮罩效果
        getBackground().setColorFilter(
                (Integer) colorEvaluator.evaluate(fraction, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
    }

    /**
     * 打开SlideMenu
     */
    public void open() {
        mDragHelper.smoothSlideViewTo(mMainView, (int) mDragRange, mMainView.getTop());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /**
     * 打开SlideMenu
     */
    public void close() {
        mDragHelper.smoothSlideViewTo(mMainView, 0, mMainView.getTop());
        //调用此方法是，必须调用下面的方法
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private OnDragStateChangeListener mListener;

    public void setOnDragStateChangeListener(OnDragStateChangeListener listener) {
        mListener = listener;
    }

    //通过接口回调将SlideMenu的状态和变化的百分比传递出去
    public interface OnDragStateChangeListener {
        /**
         * 打开的回调
         */
        void onOpen();

        /**
         * 关闭的回调
         */
        void onClose();

        /**
         * 正在拖拽中的回调
         */
        void onDragging(float fraction);
    }
}
