package garg.drawingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Shivam Garg on 09-03-2017.
 */

public class DrawingView extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private PointF startPoint = new PointF(), endPoint = new PointF();
    private Paint mBitmapPaint;
    private Paint circlePaint;
    private Path circlePath;
    private Paint mPaint;

    private MainActivity.DrawingType dType;
    private boolean erase;
    private float brushSize;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpDrawing();
    }

    private void setUpDrawing() {
        mPath = new Path();

        brushSize = 20;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        circlePaint = new Paint();
        circlePath = new Path();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLACK);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeJoin(Paint.Join.MITER);
        circlePaint.setStrokeWidth(4f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(circlePath, circlePaint);

        switch (getDrawingType()) {
            case Line:
                canvas.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y, mPaint);
                break;
            case Brush:
                canvas.drawPath(mPath, mPaint);
                break;
            case Rectangle:
                canvas.drawRect(startPoint.x, startPoint.y, endPoint.x, endPoint.y, mPaint);
                break;
            case RoundRectangle:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    canvas.drawRoundRect(startPoint.x, startPoint.y, endPoint.x, endPoint.y, 20, 20, mPaint);
                }
                break;
            case Eclipse:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    canvas.drawOval(startPoint.x, startPoint.y, endPoint.x, endPoint.y, mPaint);
                }
                break;
        }
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        startPoint = new PointF(x, y);
        endPoint = new PointF(x, y);
        mX = x;
        mY = y;
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            switch (getDrawingType()) {

                case Line:
                case RoundRectangle:
                case Eclipse:
                case Rectangle:
                    endPoint.x = x;
                    endPoint.y = y;
                    break;
                case Brush:
                    mPath.lineTo(x, y);
                    break;

            }
            mX = x;
            mY = y;

            circlePath.reset();
            circlePath.addCircle(mX, mY, 10, Path.Direction.CW);
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        circlePath.reset();
        switch (getDrawingType()) {
            case Line:
            case Brush:
                mCanvas.drawPath(mPath, mPaint);
                break;
            case Rectangle:
                mCanvas.drawRect(startPoint.x, startPoint.y, endPoint.x, endPoint.y, mPaint);
                break;
            case RoundRectangle:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mCanvas.drawRoundRect(startPoint.x, startPoint.y, endPoint.x, endPoint.y, 20, 20, mPaint);
                }
                break;
            case Eclipse:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mCanvas.drawOval(startPoint.x, startPoint.y, endPoint.x, endPoint.y, mPaint);
                }
                break;

        }
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                return false;
        }
        return true;
    }


    public void setDrawingType(MainActivity.DrawingType type) {
        this.dType = type;
    }

    public MainActivity.DrawingType getDrawingType() {
        return dType;
    }

    public void setBrushSize(float newSize) {
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        mPaint.setStrokeWidth(brushSize);
    }



    public Bitmap saveDrawing() {

        Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        this.draw(canvas);



        return bitmap;
    }


    public void startNew() {
        mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        startPoint = new PointF();
        endPoint = new PointF();
        invalidate();
    }

    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase) mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else mPaint.setXfermode(null);
    }

    public void setColor(int color) {
        mPaint.setColor(color);
    }
}