package com.example.mycropimageview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * 作者：kk
 * 创建时间： 2022/3/4 9:53
 * 版本： [1.0, 2022/3/4]
 * 版权：
 * 描述： <加载裁剪图片源的ImageView>
 */
@SuppressLint("AppCompatCustomView")
public class SourceImageView extends ImageView {

    public static final String LOG_TAG = "SourceImageView";
    private Matrix mSuppMatrix = new Matrix();
    private RectF mBitmapRect = new RectF();
    private final Matrix mDisplayMatrix = new Matrix();
    private Matrix mBaseMatrix = new Matrix();
    private int mWidth = 0;
    private int mHeight = 0;

    public SourceImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setScaleType(ScaleType.CENTER_CROP);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = this.getMeasuredWidth();
        mHeight = this.getMeasuredHeight();
    }

    public RectF getBitmapRect() {
//        Drawable drawable = getDrawable();
//        if (drawable == null) {
//            return null;
//        }
        Matrix m = getImageViewMatrix(mSuppMatrix);
        mBitmapRect.set(0, 0, mWidth, mHeight);
//        mBitmapRect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        m.mapRect(mBitmapRect);
        return mBitmapRect;
    }

    /**
     * Returns the current view matrix
     *
     * @return
     */
    public Matrix getImageViewMatrix() {
        return getImageViewMatrix(mSuppMatrix);
    }

    public Matrix getImageViewMatrix(Matrix supportMatrix) {
        mDisplayMatrix.set(mBaseMatrix);
        mDisplayMatrix.postConcat(supportMatrix);
        return mDisplayMatrix;
    }
}
