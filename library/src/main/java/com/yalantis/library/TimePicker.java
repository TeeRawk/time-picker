package com.yalantis.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
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
    private final static int CIRCLE_RADIUS_DP = 200;
    public final static int MAX_ANGLE = 360;
    private final static int TEXT_OFFSETX_DP = 12;
    private final static int TEXT_SIZE_DP = 16;
    private final static int SLOW_DOWN_ANIMATION_DURATION = 300;
    private final static int ROTATION_ANIMATION_DURATION = 2000;
    private final int ROTATION_STEP = 20;
    private final static int ANIMATIONS_CLEAR_DELAY = 30;
    private final static float SLOW_ROTATION_STEP = 2f;
    private final static double INTERPOLATED_TIME_LIMIT = 0.8;

    private int mCirclePositionY;
    private Paint mCirclePaint;
    private int mCircleRadius;
    private int mCirclePositionX;
    private volatile float mRotateAngle;
    private int touchActionDownY;
    private VelocityTracker mVelocityTracker;
    private int mSlop;
    private Paint mTextPaint;
    private int mNumbersCount = 12;
    private float mYVelocity;
    private int mGravity = 0;
    private boolean isDrag;
    private boolean isRotationAnimating;
    private Paint mSelectedTextPaint;
    private Paint mCircleStrokePaint;
    private int mAngleBetweenNumbers;

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
        int circleColor = Color.WHITE;
        int textColor = Color.WHITE;
        int selectedText = Color.RED;
        int strokeColor = Color.BLACK;
        int textSize = DimenUtils.convertDpToPixel(getContext(), TEXT_SIZE_DP);

        TypedArray a = mContext.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TimePicker,
                0, 0);

        mCircleRadius = DimenUtils.convertDpToPixel(mContext, CIRCLE_RADIUS_DP);
        mCirclePaint = new Paint();
        mSelectedTextPaint = new Paint();
        mSelectedTextPaint.setTextSize(DimenUtils.convertDpToPixel(getContext(), TEXT_SIZE_DP + 3));

        mCircleStrokePaint = new Paint();

        mTextPaint = new Paint();

        mTextPaint.setAntiAlias(true);


        try {
            mNumbersCount = a.getInteger(R.styleable.TimePicker_numbersCount, 12);
            circleColor = a.getColor(R.styleable.TimePicker_clockColor, Color.WHITE);
            textColor = a.getColor(R.styleable.TimePicker_textColor, Color.WHITE);
            selectedText = a.getColor(R.styleable.TimePicker_selectedTextColor, Color.RED);
            strokeColor = a.getColor(R.styleable.TimePicker_strokeColor, Color.BLACK);
            textSize = a.getDimensionPixelSize(R.styleable.TimePicker_textSize, textSize);
            mGravity = a.getInteger(R.styleable.TimePicker_gravity, 0);
        } finally {
            a.recycle();
        }
        mCirclePaint.setColor(circleColor);
        mTextPaint.setColor(textColor);
        mSelectedTextPaint.setColor(selectedText);
        mCircleStrokePaint.setColor(strokeColor);
        mTextPaint.setTextSize(textSize);

        mAngleBetweenNumbers = MAX_ANGLE / mNumbersCount;

        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mSlop = configuration.getScaledTouchSlop();
    }


    public int getCircleRadius() {
        return mCircleRadius;
    }

    public void setCircleRadius(int circleRadius) {
        mCircleRadius = circleRadius;
        invalidate();
        requestLayout();
    }

    @ColorInt
    public int getTextColor() {
        return mTextPaint.getColor();
    }

    public void setTextColor(@ColorInt int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    public void setTextColorRes(@ColorRes int color) {
        mTextPaint.setColor(ContextCompat.getColor(mContext, color));
        invalidate();
    }

    @ColorInt
    public int getSelectedTextColor() {
        return mSelectedTextPaint.getColor();
    }

    public void setSelectedTextColor(@ColorInt int color) {
        mSelectedTextPaint.setColor(color);
        invalidate();
    }

    public void setSelectedTextColorRes(@ColorRes int color) {
        mSelectedTextPaint.setColor(ContextCompat.getColor(mContext, color));
        invalidate();
    }

    @ColorInt
    public int getCircleColor() {
        return mCirclePaint.getColor();
    }

    public void setCircleColor(@ColorInt int color) {
        mCirclePaint.setColor(color);
        invalidate();
    }

    public void setCircleColorRes(@ColorRes int color) {
        mCirclePaint.setColor(ContextCompat.getColor(mContext, color));
        invalidate();
    }

    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
        invalidate();
        requestLayout();
    }

    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    public void setSelectedNumber(int number) {
        mRotateAngle = number * mAngleBetweenNumbers;
        invalidate();
    }

    public float getSelectedNumber() {
        float selectedNumber = (float) Math.floor(mRotateAngle) / (MAX_ANGLE / mNumbersCount);
        if (selectedNumber <= 0) {
            selectedNumber = mNumbersCount + selectedNumber;
        } else if (selectedNumber == 0) {
            selectedNumber = mNumbersCount;
        }
        return selectedNumber;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //checking the gravity and mirroring the time picker
        if (mGravity == 0) {
            mCirclePositionX = -mCircleRadius / 4;
        } else {
            mCirclePositionX = getMeasuredWidth() + mCircleRadius / 4;
        }
        mCirclePositionY = getMeasuredHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        drawWatchFace(canvas);
        canvas.restore();
    }

    private void drawWatchFace(final Canvas canvas) {

        float selectedNumber = getSelectedNumber();
        // we should rotate in a different direction when view has right gravity,
        // to ensure all two pickers rotating in the same direction
        if (mGravity == 0) {
            canvas.rotate(mRotateAngle, mCirclePositionX, mCirclePositionY);
        } else {
            canvas.rotate(-mRotateAngle, mCirclePositionX, mCirclePositionY);
        }

        //TODO extract 5 when design is ready
        canvas.drawCircle(mCirclePositionX, mCirclePositionY, mCircleRadius + 5, mCircleStrokePaint);

        canvas.drawCircle(mCirclePositionX, mCirclePositionY, mCircleRadius, mCirclePaint);


        drawNumbersOnWatchFace(canvas, Math.round(selectedNumber));
    }

    private void drawNumbersOnWatchFace(Canvas canvas, int selectedNumber) {
        for (int i = 1; i <= mNumbersCount; i++) {
            final String text = String.valueOf(i);
            drawNumber(canvas, text, selectedNumber);
        }
    }

    private float getTextX(String text) {
        final Rect textBounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), textBounds);
        if (mGravity == 0) {
            return mCirclePositionX + (mCircleRadius) - textBounds.right - DimenUtils.convertDpToPixel(mContext, TEXT_OFFSETX_DP);
        } else {
            return mCirclePositionX - mCircleRadius + textBounds.left + DimenUtils.convertDpToPixel(mContext, TEXT_OFFSETX_DP);
        }
    }

    private void drawNumber(Canvas canvas, String text, int selectedNumber) {
        if (mGravity == 0) {
            canvas.rotate(-MAX_ANGLE / mNumbersCount, mCirclePositionX, mCirclePositionY);

        } else {
            canvas.rotate(MAX_ANGLE / mNumbersCount, mCirclePositionX, mCirclePositionY);
        }
        float textX = getTextX(text);
        if (Integer.valueOf(text) == selectedNumber) {
            canvas.drawText(text, textX, mCirclePositionY, mSelectedTextPaint);
        } else {
            canvas.drawText(text, textX, mCirclePositionY, mTextPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int index = event.getActionIndex();
        final int pointerId = event.getPointerId(index);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                handleTouch(event);
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (!MathUtils.isAngleAtNumber(mRotateAngle, mAngleBetweenNumbers)) {
                    rotateAnimation();
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
        isDrag = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDrag) {
                    clearAnimations();
                }
            }
        }, ANIMATIONS_CLEAR_DELAY);
        touchActionDownY = (int) event.getY();
        mVelocityTracker = VelocityUtils.resetVelocityTracker(event, mVelocityTracker);
    }

    private void handleSwipe(MotionEvent event, int pointerId) {
        final float deltaX = event.getRawX() - touchActionDownY;
        final float previousVelocity = mYVelocity;
        mYVelocity = VelocityUtils.computeVelocity(event, pointerId, mVelocityTracker);
        if ((previousVelocity * mYVelocity) < 0) {
            clearAnimations();
        }
        if (Math.abs(deltaX) > mSlop) {
            isDrag = true;
            if (Math.abs(mYVelocity) != VelocityUtils.MAX_VELOCITY) {
                rotateOnDrag();
            } else if (!isRotationAnimating) {
                rotateAnimation();
            }
        }
    }


    private void rotateToClosestNumber() {
        final float distanceToClosestNumber = MathUtils.getDistanceToClosestNumber(mYVelocity, mRotateAngle, mAngleBetweenNumbers);
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
        mRotateAngle = mRotateAngle + SLOW_ROTATION_STEP * mYVelocity;
        getCanonicalAngle();
        invalidate();
    }

    private void rotateAnimation() {
        final float tmpAngle = mRotateAngle;
        startRotation(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime < INTERPOLATED_TIME_LIMIT) {
                    mRotateAngle = tmpAngle + mYVelocity * ROTATION_STEP * interpolatedTime;
                } else {
                    rotateToClosestNumber();
                }
                getCanonicalAngle();
                invalidate();
            }
        });
    }

    private void startRotation(Animation rotation) {
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isRotationAnimating = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isRotationAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rotation.setInterpolator(new DecelerateInterpolator());
        rotation.setDuration(ROTATION_ANIMATION_DURATION);
        if (VelocityUtils.isLowVelocity(mYVelocity)) {
            startAnimation(rotation);
        } else {
            rotateToClosestNumber();
        }
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