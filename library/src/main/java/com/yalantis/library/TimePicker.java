package com.yalantis.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.yalantis.library.utils.DimenUtils;
import com.yalantis.library.utils.MathUtils;
import com.yalantis.library.utils.VelocityUtils;

/**
 * Created by Alexey on 04.08.2016.
 */
public class TimePicker extends View {
    private final Context mContext = getContext();
    private final static int CIRCLE_RADIUS_DP = 300;
    private final static int MAX_ANGLE = 360;
    private final static int TEXT_OFFSETX_DP = 12;
    private final static int TEXT_SIZE_DP = 16;
    private final static int DEFAULT_ANIMATION_DELAY = 120;
    private final static int SLOW_DOWN_ANIMATION_DURATION = 300;
    private final static int ROTATION_ANIMATION_DURATION = 2000;
    private final int ROTATION_STEP = 20;
    private final static int ANIMATIONS_CLEAR_DELAY = 30;
    private final static float SLOW_ROTATION_STEP = 2f;
    private int mCirclePositionY;
    private Paint mCirclePaint;
    private int mCircleRadius;
    private int mCirclePositionX;
    private float mRotateAngle;
    private int touchActionDownY;
    private VelocityTracker mVelocityTracker;
    private int mSlop;
    private Paint mTextPaint;
    private int mNumbersCount;
    private float mYVelocity;
    private int mGravity;
    private int mCircleColor;
    private int mTextColor;
    private boolean isDrag;
    private boolean isRotationAnimating;
    private Paint mCircleStrokePaint;
    private int mStrokeColor;

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = mContext.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TimePicker,
                0, 0);

        mCircleRadius = DimenUtils.convertDpToPixel(mContext, CIRCLE_RADIUS_DP);
        mCirclePaint = new Paint();
        mCircleStrokePaint = new Paint();
        mCircleStrokePaint.setColor(ContextCompat.getColor(mContext, R.color.hoursSelectedColor));
        mCircleStrokePaint.setTextSize(DimenUtils.convertDpToPixel(getContext(), TEXT_SIZE_DP + 3));
        mTextPaint = new Paint();
        mTextPaint.setTextSize(DimenUtils.convertDpToPixel(getContext(), TEXT_SIZE_DP));
        mTextPaint.setAntiAlias(true);

        try {
            mNumbersCount = a.getInteger(R.styleable.TimePicker_numbersCount, 12);
            mCircleColor = a.getColor(R.styleable.TimePicker_clockColor, Color.WHITE);
            mTextColor = a.getColor(R.styleable.TimePicker_textColor, Color.WHITE);
            mStrokeColor = a.getColor(R.styleable.TimePicker_strokeColor, Color.WHITE);
            mGravity = a.getInteger(R.styleable.TimePicker_gravity, 0);
        } finally {
            a.recycle();
        }
        mCirclePaint.setColor(mCircleColor);
        mTextPaint.setColor(mTextColor);
        mCircleStrokePaint.setColor(mStrokeColor);

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mSlop = configuration.getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mGravity == 0) {
            mCirclePositionX = -mCircleRadius / 2;
        } else {
            mCirclePositionX = getMeasuredWidth() + mCircleRadius / 2;
        }
        mCirclePositionY = getMeasuredHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        drawWatchFace(canvas, mCirclePositionX, mCirclePositionY);
        canvas.restore();
    }

    private void drawWatchFace(final Canvas canvas, float circleX, float circleY) {
        if (mGravity == 0) {
            canvas.rotate(mRotateAngle, circleX, circleY);
        } else {
            canvas.rotate(-mRotateAngle, circleX, circleY);
        }
        canvas.drawCircle(circleX, circleY, mCircleRadius + 5, mCircleStrokePaint);
        canvas.drawCircle(circleX, circleY, mCircleRadius, mCirclePaint);

        fillWatchFaceWithNumbers(canvas, circleX, circleY);
    }

    private void fillWatchFaceWithNumbers(Canvas canvas, float circleX, float circleY) {
        for (int i = 1; i <= mNumbersCount; i++) {
            String text = String.valueOf(i);
            drawNumberOnWatchFace(canvas, circleX, circleY, text, getTextX(circleX, text));
        }
    }

    private float getTextX(float circleX, String text) {
        Rect textBounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), textBounds);
        if (mGravity == 0) {
            return circleX + (mCircleRadius) - textBounds.right - DimenUtils.convertDpToPixel(mContext, TEXT_OFFSETX_DP);
        } else {
            return circleX - mCircleRadius + textBounds.left + DimenUtils.convertDpToPixel(mContext, TEXT_OFFSETX_DP);
        }
    }

    private void drawNumberOnWatchFace(Canvas canvas, float circleX, float circleY, String text, float textX) {
        canvas.rotate(MAX_ANGLE / mNumbersCount, circleX, circleY);
        canvas.drawText(text, textX, circleY, mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int index = event.getActionIndex();
        int pointerId = event.getPointerId(index);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                handleTouch(event);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (!MathUtils.isAngleAtNumber(mRotateAngle, MAX_ANGLE, mNumbersCount)) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            rotateToClosestNumber();
                        }
                    }, DEFAULT_ANIMATION_DELAY);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                handleSwipe(event, pointerId);
                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    private void handleTouch(MotionEvent event) {
        touchActionDownY = (int) event.getY();
        mVelocityTracker = VelocityUtils.resetVelocityTracker(event, mVelocityTracker);
    }

    private void handleSwipe(MotionEvent event, int pointerId) {
        float deltaX = event.getRawX() - touchActionDownY;
        float previousVelocity = mYVelocity;
        mYVelocity = VelocityUtils.computeVelocity(event, pointerId, mVelocityTracker);
        if ((previousVelocity * mYVelocity) < 0) {
            clearAnimations();
        }
        if (Math.abs(deltaX) > mSlop) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    rotateOnDrag();
                }
            }, DEFAULT_ANIMATION_DELAY);
        }
    }


    private void rotateToClosestNumber() {
        final float distanceToClosestNumber = MathUtils.getDistanceToClosestNumber(mYVelocity, mRotateAngle, MAX_ANGLE, mNumbersCount);
        final float tmpAngle = mRotateAngle;
        startSlowdownAnimation(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mRotateAngle = tmpAngle - (distanceToClosestNumber * interpolatedTime);
                invalidate();
            }
        });
    }

    private void startSlowdownAnimation(Animation animation) {
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(SLOW_DOWN_ANIMATION_DURATION);
        startAnimation(animation);
    }

    private void rotateOnDrag() {
        mRotateAngle = mRotateAngle + (SLOW_ROTATION_STEP * (mYVelocity));
        getCanonicalAngle();
        invalidate();
    }

    private void clearAnimations() {
        clearAnimation();
        getCanonicalAngle();
    }

    private void getCanonicalAngle() {
        if (mRotateAngle >= MAX_ANGLE || mRotateAngle <= -MAX_ANGLE) {
            mRotateAngle = MAX_ANGLE - Math.abs(mRotateAngle);
        }
    }

}