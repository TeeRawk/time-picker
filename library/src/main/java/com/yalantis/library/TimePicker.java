package com.yalantis.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

import com.yalantis.library.utils.DimenUtils;
import com.yalantis.library.utils.MathUtils;
import com.yalantis.library.utils.VelocityUtils;

import static com.yalantis.library.Constants.*;

/**
 * Created by Alexey on 04.08.2016.
 */
class TimePicker extends View {
    private int TEXT_OFFSETX_DP = 0;
    private final static int TEXT_SIZE_DP = 0;
    private final Context mContext = getContext();
    private final int mPivotOffsetX = DimenUtils.convertDpToPixel(mContext, 3);

    private int mCirclePositionY;
    private Paint mCirclePaint;
    private float mCirclePositionX;
    private volatile float mRotateAngle;
    private int mTouchActionDownY;
    private VelocityTracker mVelocityTracker;
    private int mSlop;
    private Paint mTextPaint;
    private int mNumbersCount = 12;
    private float mYVelocity;
    private boolean isDrag;
    public boolean isRotationAnimating;
    private Paint mHighlightPaint;
    private int mAngleBetweenNumbers;
    private Bitmap mCircleDrawable;

    private OnRotationListener mOnRotationListener;
    private Bitmap mOverlayDrawable;
    private Bitmap mSelectionBackgroundDrawable;
    private float mPreviousAngle;
    private int mPivotOffsetY = DimenUtils.convertDpToPixel(mContext, 8);

    public OnRotationListener getOnRotationListner() {
        return mOnRotationListener;
    }

    public void setOnRotationListner(OnRotationListener onRotationListener) {
        mOnRotationListener = onRotationListener;
    }

    @IntDef({TWELVE_HOURS, TWENTY_FOUR_HOURS, SIXTY_MINUTES, ZERO})
    public @interface DivisionNumber {
    }

    public static final int TWELVE_HOURS = 12;
    public static final int TWENTY_FOUR_HOURS = 24;
    public static final int SIXTY_MINUTES = 60;
    public static final int ZERO = 0;

    @IntDef({GRAVITY_LEFT, GRAVITY_RIGHT, GRAVITY_CENTER})
    public @interface Gravity {
    }

    public static final int GRAVITY_LEFT = 1;
    public static final int GRAVITY_RIGHT = -1;
    public static final int GRAVITY_CENTER = 2;

    private int mGravity = GRAVITY_LEFT;

    static class TimePickerBuilder {
        private final Context mContext;
        private int mTextColor;
        private int mHighLightColor;
        private int mCircleDrawable;
        private int mTextSize;
        @Gravity
        private int mGravity = GRAVITY_LEFT;
        @DivisionNumber
        private int mNumbersCount = TWELVE_HOURS;

        TimePickerBuilder(Context context) {
            this.mContext = context;
        }

        TimePickerBuilder setTextColor(@ColorInt int color) {
            mTextColor = color;
            return this;
        }

        TimePickerBuilder setTextColorRes(@ColorRes int color) {
            mTextColor = ContextCompat.getColor(mContext, color);
            return this;
        }

        TimePickerBuilder setHighlightColor(@ColorInt int color) {
            mHighLightColor = color;
            return this;
        }

        TimePickerBuilder setHighlightColorRes(@ColorRes int color) {
            mHighLightColor = ContextCompat.getColor(mContext, color);
            return this;
        }

        TimePickerBuilder setCircleBackground(@DrawableRes int drawable) {
            mCircleDrawable = drawable;
            return this;
        }


        TimePickerBuilder setTextSize(int size) {
            mTextSize = size;
            return this;
        }


        TimePickerBuilder setGravity(@Gravity int gravity) {
            this.mGravity = gravity;
            return this;
        }

        TimePickerBuilder setNumbersCount(@DivisionNumber int count) {
            mNumbersCount = count;
            return this;
        }

        TimePicker build() {
            return new TimePicker(mContext, mNumbersCount, mTextColor, mHighLightColor, mTextSize, mGravity, mCircleDrawable);
        }
    }

    private TimePicker(Context context, int numbersCount, int textColor, int highlightColor, int textSize, int gravity, int drawableRes) {
        this(context, null);
        mGravity = gravity;
        mNumbersCount = numbersCount;
        if (mNumbersCount != 0) {
            mAngleBetweenNumbers = MAX_ANGLE / mNumbersCount;
        }
        mCircleDrawable = BitmapFactory.decodeResource(context.getResources(), drawableRes);
        if (mGravity == GRAVITY_LEFT) {
            TEXT_OFFSETX_DP = 116;
            mOverlayDrawable = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_1);
            mSelectionBackgroundDrawable = BitmapFactory.decodeResource(context.getResources(), R.drawable.numbers_placeholder_1);
        } else if (mGravity == GRAVITY_RIGHT) {
            TEXT_OFFSETX_DP = 96;
            mOverlayDrawable = BitmapFactory.decodeResource(context.getResources(), R.drawable.overlay_2);
            mSelectionBackgroundDrawable = BitmapFactory.decodeResource(context.getResources(), R.drawable.numbers_placeholder_2);
        }
        initPaints(textColor, highlightColor, textSize);
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
        int textSize = DimenUtils.convertSpToPixel(getContext(), TEXT_SIZE_DP);

        mCirclePaint = new Paint();
        mHighlightPaint = new Paint();
        mTextPaint = new Paint();
        setLayerType(LAYER_TYPE_HARDWARE, mCirclePaint);
        if (attrs != null) {
            TypedArray a = mContext.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.TimePicker,
                    0, 0);

            try {
                mNumbersCount = a.getInteger(R.styleable.TimePicker_divisionCount, mNumbersCount);
                textColor = a.getColor(R.styleable.TimePicker_textColor, textColor);
                highlightColor = a.getColor(R.styleable.TimePicker_highlightColor, highlightColor);
                textSize = a.getDimensionPixelSize(R.styleable.TimePicker_textSize, textSize);
                mGravity = a.getInteger(R.styleable.TimePicker_gravity, mGravity);
            } finally {
                a.recycle();
            }
            initPaints(textColor, highlightColor, textSize);
            mAngleBetweenNumbers = MAX_ANGLE / mNumbersCount;
        }

        mSlop = getTouchSlop();
    }

    private int getTouchSlop() {
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        return configuration.getScaledTouchSlop();
    }

    private void initPaints(int textColor, int highlightColor, int textSize) {
        mHighlightPaint.setTextSize(DimenUtils.convertSpToPixel(getContext(), TEXT_SIZE_DP + TEXT_SELECTION_SIZE_DIFF));
        mTextPaint.setAntiAlias(true);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(200);
        mTextPaint.setColor(textColor);
        mHighlightPaint.setColor(highlightColor);
        mTextPaint.setTextSize(textSize);
    }

    public void setSelectedNumber(final int number) {
        final float distanceToNumber = (number * mAngleBetweenNumbers) - mRotateAngle;
        final float tmpAngle = mRotateAngle;
        startRotationToSelected(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mRotateAngle = -(tmpAngle + distanceToNumber * interpolatedTime);
                getCanonicalAngle();
                invalidate();
            }
        }, ROTATION_ANIMATION_DURATION);
    }

    public int getSelectedNumber() {
        int selectedNumber = 0;
        if (mNumbersCount != 0) {
            selectedNumber = Math.round((float) Math.round(-mRotateAngle) / (MAX_ANGLE / mNumbersCount));
            if (selectedNumber < 0) {
                selectedNumber = mNumbersCount + selectedNumber;
            }
        }

        return selectedNumber >= mNumbersCount ? 0 : selectedNumber;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //checking the gravity and mirroring the time picker
        if (mGravity == GRAVITY_LEFT) {
            mCirclePositionX = -mCircleDrawable.getWidth() / 2;
            setMeasuredDimension(mCircleDrawable.getWidth() / 2, mCircleDrawable.getHeight());
        } else if (mGravity == GRAVITY_RIGHT) {
            mCirclePositionX = getMeasuredWidth() - mCircleDrawable.getWidth() / 2;
            setMeasuredDimension(mCircleDrawable.getWidth() / 2, mCircleDrawable.getHeight());
        } else if (mGravity == GRAVITY_CENTER) {
            mCirclePositionX = 0;
            setMeasuredDimension(mCircleDrawable.getWidth(), mCircleDrawable.getHeight());
        }
        mCirclePositionY = mCircleDrawable.getHeight() / 2;

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
        int drawableTop = mCirclePositionY - mCircleDrawable.getHeight() / 2;
        rotateCanvas(canvas);

        if (mOnRotationListener != null && mPreviousAngle != mRotateAngle)
            mOnRotationListener.onRotate(mRotateAngle, mYVelocity);

        canvas.drawBitmap(mCircleDrawable, mCirclePositionX, drawableTop, new Paint());

        if (mGravity != GRAVITY_CENTER) {
            drawOverlay(canvas, mSelectionBackgroundDrawable, -2);
            drawNumbersOnWatchFace(canvas, Math.round(selectedNumber));
            drawOverlay(canvas, mOverlayDrawable, 0);
        }
        mPreviousAngle = mRotateAngle;
    }

    private void rotateCanvas(Canvas canvas) {
        if (mGravity == GRAVITY_LEFT) {
            canvas.rotate(mRotateAngle, 0, mCirclePositionY);
        } else if (mGravity == GRAVITY_RIGHT) {
            canvas.rotate(-mRotateAngle, canvas.getWidth() + mPivotOffsetX, mCirclePositionY - mPivotOffsetY);
        } else if (mGravity == GRAVITY_CENTER) {
            canvas.rotate(-mRotateAngle, canvas.getWidth() / 2, canvas.getHeight() / 2 - DimenUtils.convertDpToPixel(mContext, 2));
        }
        isRotationAnimating = true;
    }

    private void drawOverlay(Canvas canvas, Bitmap bitmap, int offsetx) {
        Matrix matrix = new Matrix();
        int overlayTop = mCirclePositionY - mOverlayDrawable.getHeight() / 2;
        int overlayOffsetX = 0;
        int overlayOffsetY = 0;
        int i = 0;
        if (mGravity == GRAVITY_RIGHT) {
            i = DimenUtils.convertDpToPixel(mContext, 5);
            overlayOffsetX = DimenUtils.convertDpToPixel(mContext, 64) - DimenUtils.convertDpToPixel(mContext, offsetx);
            overlayOffsetY = DimenUtils.convertDpToPixel(mContext, 5);
            matrix.postRotate(mRotateAngle, canvas.getWidth() + mPivotOffsetX, mCirclePositionY - mPivotOffsetY);
        } else if (mGravity == GRAVITY_LEFT) {
            overlayOffsetX = DimenUtils.convertDpToPixel(mContext, 45) + DimenUtils.convertDpToPixel(mContext, offsetx);
            overlayOffsetY = 0;
            matrix.postRotate(-mRotateAngle, 0, mCirclePositionY);
        }

        matrix.preTranslate(mCirclePositionX + overlayOffsetX + i, overlayTop - overlayOffsetY);
        canvas.drawBitmap(bitmap, matrix, new Paint());
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
            return mCirclePositionX + mCircleDrawable.getWidth() - textBounds.right - DimenUtils.convertDpToPixel(mContext, TEXT_OFFSETX_DP);
        } else {
            return mCirclePositionX + textBounds.centerX() + DimenUtils.convertDpToPixel(mContext, TEXT_OFFSETX_DP);
        }
    }

    private void drawNumber(Canvas canvas, String text, int selectedNumber) {
        int number = Integer.valueOf(text) == mNumbersCount ? 0 : Integer.valueOf(text);
        text = canonizeNumber(number);
        float textX = getTextX(text);
        if (number == selectedNumber) {
            int offsetY = mGravity == GRAVITY_LEFT ? DimenUtils.convertDpToPixel(mContext, 8) : DimenUtils.convertDpToPixel(mContext, 2);
            canvas.drawText(text, textX, mCirclePositionY + offsetY, mHighlightPaint);
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
            canvas.rotate(MAX_ANGLE / mNumbersCount, 0, mCirclePositionY);
        } else {
            canvas.rotate(-MAX_ANGLE / mNumbersCount, canvas.getWidth() + mPivotOffsetX, mCirclePositionY - mPivotOffsetY);
            ;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int index = event.getActionIndex();
        final int pointerId = event.getPointerId(index);
        if (mGravity != GRAVITY_CENTER) {
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
        startRotateToClosestAnimation(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mRotateAngle = tmpAngle - (distanceToClosestNumber * interpolatedTime);
                invalidate();
            }
        });
    }

    public boolean isRotationAnimating() {
        return isRotationAnimating;
    }

    private void startRotateToClosestAnimation(Animation animation) {
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(SLOW_DOWN_ANIMATION_DURATION);
        startAnimation(animation);
    }

    private void rotateOnDrag() {
        mRotateAngle = mRotateAngle + SLOW_ROTATION_STEP * mYVelocity;
        getCanonicalAngle();
        invalidate();
    }

    public void rotate(float angle, float velocity) {
        mRotateAngle = angle + 1f * velocity;
        invalidate();
    }

    private void startSlowDownAnimation() {
        final float tmpAngle = mRotateAngle;
        startRotation(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime < Constants.SLOW_DOWN_INTERPOLATION_LIMIT) {
                    mRotateAngle = tmpAngle + 2 * mAngleBetweenNumbers * mYVelocity / Math.abs(mYVelocity) * interpolatedTime;
                } else {
                    rotateToClosestNumber();
                }
                getCanonicalAngle();
                invalidate();
            }
        }, SLOW_DOWN_ROTATION_DURATION);
    }

    private void rotateAnimation() {
        final float tmpAngle = mRotateAngle;
        startRotation(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime < ROTATION_INTERPOLATION_LIMIT) {
                    mRotateAngle = tmpAngle + mYVelocity * ROTATION_STEP * interpolatedTime;
                } else {
                    if (VelocityUtils.isLowVelocity(mYVelocity)) {
                        rotateToClosestNumber();
                    } else {
                        startSlowDownAnimation();
                    }
                }
                getCanonicalAngle();
                invalidate();
            }
        }, ROTATION_ANIMATION_DURATION);
    }

    private void startRotation(Animation rotation, int duration) {
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
        rotation.setDuration(duration);
        if (VelocityUtils.isLowVelocity(mYVelocity)) {
            startAnimation(rotation);
        } else {
            rotateToClosestNumber();
        }
    }


    private void startRotationToSelected(Animation rotation, int duration) {
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
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        rotation.setDuration(duration);
        startAnimation(rotation);
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