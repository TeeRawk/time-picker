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
import android.support.annotation.IntDef;
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
    private final static double INTERPOLATED_TIME_LIMIT = 0.7;
    private final static int STROKE_WIDTH = 7;
    private final static int TEXT_SELECTION_SIZE_DIFF = 5;

    private int mCirclePositionY;
    private Paint mCirclePaint;
    private int mCircleRadius;
    private int mCirclePositionX;
    private volatile float mRotateAngle;
    private int mTouchActionDownY;
    private VelocityTracker mVelocityTracker;
    private int mSlop;
    private Paint mTextPaint;
    private int mNumbersCount = 12;
    private float mYVelocity;
    private boolean isDrag;
    private boolean isRotationAnimating;
    private Paint mHighlightPaint;
    private int mAngleBetweenNumbers;

    @IntDef({TWELVE_HOURS, TWENTY_FOUR_HOURS, SIXTY_MINUTES})
    public @interface DivisionNumber {
    }

    public static final int TWELVE_HOURS = 12;
    public static final int TWENTY_FOUR_HOURS = 24;
    public static final int SIXTY_MINUTES = 60;

    @IntDef({GRAVITY_LEFT, GRAVITY_RIGHT})
    public @interface Gravity {
    }

    public static final int GRAVITY_LEFT = 1;
    public static final int GRAVITY_RIGHT = -1;

    private int mGravity = GRAVITY_LEFT;

    public static class TimePickerBuilder {
        private final Context mContext;
        private int mCircleRadius;
        private int mTextColor;
        private int mHighLightColor;
        private int mCircleColor;
        private int mTextSize;
        private int mSelectedNumber;
        @Gravity
        private int mGravity = GRAVITY_LEFT;
        @DivisionNumber
        private int mNumbersCount = TWELVE_HOURS;

        public TimePickerBuilder(Context context) {
            this.mContext = context;
        }

        public TimePickerBuilder setCircleRadius(int circleRadius) {
            this.mCircleRadius = circleRadius;
            return this;
        }

        protected TimePickerBuilder setTextColor(@ColorInt int color) {
            mTextColor = color;
            return this;
        }

        protected TimePickerBuilder setTextColorRes(@ColorRes int color) {
            mTextColor = ContextCompat.getColor(mContext, color);
            return this;
        }

        public TimePickerBuilder setHighlightColor(@ColorInt int color) {
            mHighLightColor = color;
            return this;
        }

        public TimePickerBuilder setHighlightColorRes(@ColorRes int color) {
            mHighLightColor = ContextCompat.getColor(mContext, color);
            return this;
        }

        public TimePickerBuilder setCircleColor(@ColorInt int color) {
            mCircleColor = color;
            return this;
        }

        public TimePickerBuilder setCircleColorRes(@ColorRes int color) {
            mCircleColor = ContextCompat.getColor(mContext, color);
            return this;
        }

        public TimePickerBuilder setTextSize(int size) {
            mTextSize = size;
            return this;
        }

        public TimePickerBuilder setSelectedNumber(int number) {
            mSelectedNumber = number;
            return this;
        }

        public TimePickerBuilder setGravity(@Gravity int gravity) {
            this.mGravity = gravity;
            return this;
        }

        public TimePickerBuilder setNumbersCount(@DivisionNumber int count) {
            mNumbersCount = count;
            return this;
        }

        public TimePicker build() {
            return new TimePicker(mContext, mNumbersCount, mTextColor, mHighLightColor, mCircleColor, mSelectedNumber, mTextSize, mGravity, mCircleRadius);
        }
    }

    private TimePicker(Context context, int numbersCount, int textColor, int highlightColor, int circleColor, int selectedNumber, int textSize, int gravity, int circleRadius) {
        this(context, null);
        mRotateAngle = selectedNumber * mAngleBetweenNumbers;
        mGravity = gravity;
        mNumbersCount = numbersCount;
        mCircleRadius = circleRadius;
        mAngleBetweenNumbers = MAX_ANGLE / mNumbersCount;
        initPaints(circleColor, textColor, highlightColor, textSize);
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
        int highlightColor = Color.RED;
        int textSize = DimenUtils.convertDpToPixel(getContext(), TEXT_SIZE_DP);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mCirclePaint = new Paint();
        mHighlightPaint = new Paint();
        mTextPaint = new Paint();

        if (attrs != null) {
            TypedArray a = mContext.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.TimePicker,
                    0, 0);

            try {
                mNumbersCount = a.getInteger(R.styleable.TimePicker_numbersCount, mNumbersCount);
                circleColor = a.getColor(R.styleable.TimePicker_clockColor, circleColor);
                textColor = a.getColor(R.styleable.TimePicker_textColor, textColor);
                highlightColor = a.getColor(R.styleable.TimePicker_highlightColor, highlightColor);
                textSize = a.getDimensionPixelSize(R.styleable.TimePicker_textSize, textSize);
                mGravity = a.getInteger(R.styleable.TimePicker_gravity, mGravity);
            } finally {
                a.recycle();
            }
            initPaints(circleColor, textColor, highlightColor, textSize);
            mCircleRadius = DimenUtils.convertDpToPixel(mContext, CIRCLE_RADIUS_DP);
            mAngleBetweenNumbers = MAX_ANGLE / mNumbersCount;
        }

        mSlop = getTouchSlop();
    }

    private int getTouchSlop() {
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        return configuration.getScaledTouchSlop();
    }

    private void initPaints(int circleColor, int textColor, int highlightColor, int textSize) {
        mHighlightPaint.setTextSize(DimenUtils.convertSpToPixel(getContext(), TEXT_SIZE_DP + TEXT_SELECTION_SIZE_DIFF));
        mTextPaint.setAntiAlias(true);
        mCirclePaint.setColor(circleColor);
        mTextPaint.setColor(textColor);
        mHighlightPaint.setColor(highlightColor);
        mTextPaint.setTextSize(textSize);
    }

    private void setSelectedNumber(float number) {
        mRotateAngle = number * mAngleBetweenNumbers;
        invalidate();
    }

    public float getSelectedNumber() {
        float selectedNumber = (float) Math.round(-mRotateAngle) / (MAX_ANGLE / mNumbersCount);
        if (selectedNumber < 0) {
            selectedNumber = mNumbersCount + selectedNumber;
        }

        return selectedNumber;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //checking the gravity and mirroring the time picker
        if (mGravity == GRAVITY_LEFT) {
            mCirclePositionX = -mCircleRadius / 3;
        } else {
            mCirclePositionX = getMeasuredWidth() + mCircleRadius / 3;
        }
        mCirclePositionY = getMeasuredHeight() / 2;
    }

    public int getNumbersCount() {
        return mNumbersCount;
    }

    public void setNumbersCount(int numbersCount) {
        mNumbersCount = numbersCount;
        invalidate();
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
        if (mGravity == GRAVITY_LEFT) {
            canvas.rotate(mRotateAngle, mCirclePositionX, mCirclePositionY);
        } else {
            canvas.rotate(-mRotateAngle, mCirclePositionX, mCirclePositionY);
        }
        canvas.drawCircle(mCirclePositionX, mCirclePositionY, mCircleRadius + STROKE_WIDTH, mHighlightPaint);
        canvas.drawCircle(mCirclePositionX, mCirclePositionY, mCircleRadius, mCirclePaint);

        drawNumbersOnWatchFace(canvas, Math.round(selectedNumber));
    }

    private void drawNumbersOnWatchFace(Canvas canvas, int selectedNumber) {
        for (int i = 1; i <= mNumbersCount; i++) {
            final String text = String.valueOf(i);
            rotateCircleOneNotch(canvas);
            drawNumber(canvas, text, selectedNumber);
        }
    }

    private float getTextX(String text) {
        final Rect textBounds = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), textBounds);
        if (mGravity == GRAVITY_LEFT) {
            return mCirclePositionX + (mCircleRadius) - textBounds.right - DimenUtils.convertDpToPixel(mContext, TEXT_OFFSETX_DP);
        } else {
            return mCirclePositionX - mCircleRadius + textBounds.left + DimenUtils.convertDpToPixel(mContext, TEXT_OFFSETX_DP);
        }
    }

    private void drawNumber(Canvas canvas, String text, int selectedNumber) {
        int number = Integer.valueOf(text) == mNumbersCount ? 0 : Integer.valueOf(text);
        text = canonizeNumber(number);
        float textX = getTextX(text);
        if (number == selectedNumber) {
            canvas.drawText(text, textX, mCirclePositionY, mHighlightPaint);
        } else {
            canvas.drawText(text, textX, mCirclePositionY, mTextPaint);
        }
    }

    private String canonizeNumber(Integer number) {
        String text = String.valueOf(number);
        if (number < 10) {
            text = "0" + text;
        } else if (number == mNumbersCount) {
            text = "00";
        }
        return text;
    }

    private void rotateCircleOneNotch(Canvas canvas) {
        if (mGravity == GRAVITY_LEFT) {
            canvas.rotate(MAX_ANGLE / mNumbersCount, mCirclePositionX, mCirclePositionY);

        } else {
            canvas.rotate(-MAX_ANGLE / mNumbersCount, mCirclePositionX, mCirclePositionY);
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
        mTouchActionDownY = (int) event.getY();
        mVelocityTracker = VelocityUtils.resetVelocityTracker(event, mVelocityTracker);
    }

    private void handleSwipe(MotionEvent event, int pointerId) {
        final float deltaX = event.getRawX() - mTouchActionDownY;
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
        if (mRotateAngle <= -MAX_ANGLE) {
            mRotateAngle = MAX_ANGLE - Math.abs(mRotateAngle);
        } else if (mRotateAngle >= MAX_ANGLE) {
            mRotateAngle = mRotateAngle - MAX_ANGLE;
        }
    }

}