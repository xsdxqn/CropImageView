package com.example.mycropimageview.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.epoint.app.widget.crop.util.BitmapUtil;
import com.example.mycropimageview.widget.util.PaintUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：kk
 * 创建时间： 2022/3/4 9:53
 * 版本： [1.0, 2022/3/4]
 * 版权：
 * 描述： <画圈框选view>
 */
public class PaintCropImageView extends View {

    //画笔
    public static Paint mPaint;

    public static Path path;

    public static List<Point> points = new ArrayList<>();

    private boolean isLine = false;

    private Context context;

    public static Bitmap cachebBitmap;

    public static Canvas cacheCanvas;

    public static int mWidth = 0;

    public static int mHeight = 0;

    public PaintCropImageView(Context context) {
        super(context);
        init(context);
    }

    public PaintCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PaintCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(8);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = this.getMeasuredWidth();
        mHeight = this.getMeasuredHeight();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        //将路径分3段平滑连接
        path = new Path();
        if (points.size() > 1) {
            for (int i = points.size() - 2; i < points.size(); i++) {
                if (i >= 0) {
                    Point point = points.get(i);
                    if (i == 0) {
                        Point next = points.get(i + 1);
                        point.dx = ((next.x - point.x) / 3);
                        point.dy = ((next.y - point.y) / 3);
                    } else if (i == points.size() - 1) {
                        Point prev = points.get(i - 1);
                        point.dx = ((point.x - prev.x) / 3);
                        point.dy = ((point.y - prev.y) / 3);
                    } else {
                        Point next = points.get(i + 1);
                        Point prev = points.get(i - 1);
                        point.dx = ((next.x - prev.x) / 3);
                        point.dy = ((next.y - prev.y) / 3);
                    }
                }
            }
        }
        boolean first = true;
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else {
                Point prev = points.get(i - 1);
                path.cubicTo(prev.x + prev.dx, prev.y + prev.dy, point.x - point.dx, point.y - point.dy,
                        point.x, point.y);
            }
        }
        canvas.drawPath(path, mPaint);
        if (points.size() > 1 && isLine) {
            //连线
            canvas.drawLine(points.get(0).x, points.get(0).y, points.get(points.size() - 1).x,
                    points.get(points.size() - 1).y, mPaint);
            //其他区域绘制黑色背景
            canvas.clipOutPath(path);
            canvas.drawColor(Color.parseColor(PaintUtil.DEFAULT_BACKGROUND_COLOR_ID));
        }
    }

    /**
     * saveBitmap
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void saveBitmap(Bitmap sourceBitmap, String filePath, String fileName) {
        cachebBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, mWidth, mHeight);
        cacheCanvas = new Canvas(cachebBitmap);
        cacheCanvas.drawPath(path, mPaint);
        cacheCanvas.drawLine(points.get(0).x, points.get(0).y, points.get(points.size() - 1).x,
                points.get(points.size() - 1).y, mPaint);
        cacheCanvas.clipOutPath(path);
        cacheCanvas.drawColor(Color.BLACK);
        BitmapUtil.INSTANCE.saveBitmap(cachebBitmap, filePath, fileName);
    }

    private static class Point {
        float x;
        float y;
        float dx;
        float dy;
    }

    /**
     * 触摸事件处理
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 是否向下传递事件标志 true为消耗
        boolean ret = super.onTouchEvent(event);
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        Point point = new Point();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                //手指按下
                isLine = false;
                ret = true;
                points.clear();
                point.x = x;
                point.y = y;
                points.add(point);
                break;
            case MotionEvent.ACTION_MOVE:
                //手指移动
                point.x = x;
                point.y = y;
                points.add(point);
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //手指抬起
                isLine = true;
                point.x = x;
                point.y = y;
                points.add(point);
                invalidate();
                break;
        }
        return ret;
    }
}
