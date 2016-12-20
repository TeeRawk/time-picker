package com.yalantis.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.yalantis.library.utils.DimenUtils;

/**
 * Created by Alexey on 08.08.2016.
 */
public class ClockFace extends RelativeLayout {
    private final static int TEXT_SIZE_DP = 16;
    private final static int DEFAULT_HOURS_COUNT = 24;
    private final static int MILLIS_IN_SECOND = 1000;
    private final static int DEFAULT_MINUTES_COUNT = 60;
    private final static int SECONDS_IN_MINUTE = 60;
    private TimePicker mHoursPicker;
    private TimePicker mMinutePicker;
    private TimePicker mBottomBgWheel;
    private LayoutParams mHoursParams;
    private LayoutParams mBottomWheelParams;
    private LayoutParams mMinutesParams;
    private TimePicker mTopBgWheel;
    private LayoutParams mTopWheelParams;
    private LayoutParams mCenterWheelParams;
    private TimePicker mCenterBgWheel;
    private TimePicker mRightBgWheel;
    private LayoutParams mRightWheelParams;

    public ClockFace(Context context) {
        this(context, null);
    }

    public ClockFace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockFace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int minutesSelectedTextColor = Color.RED;
        int hoursSelectedTextColor = Color.RED;
        int hoursTextColor = Color.BLACK;
        int minutesTextColor = Color.BLACK;
        int textSize = DimenUtils.convertSpToPixel(context, TEXT_SIZE_DP);
        int hoursCount = DEFAULT_HOURS_COUNT;
        @TimePicker.DivisionNumber int divisionNumber = TimePicker.TWELVE_HOURS;

        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ClockFace,
                0, 0);

        try {
            hoursSelectedTextColor = a.getColor(R.styleable.ClockFace_hoursHighlightColor, hoursSelectedTextColor);
            minutesSelectedTextColor = a.getColor(R.styleable.ClockFace_minutesHighlightColor, minutesSelectedTextColor);
            hoursCount = a.getInteger(R.styleable.ClockFace_hoursCount, hoursCount);
            textSize = a.getInteger(R.styleable.ClockFace_numbersSize, textSize);
            hoursTextColor = a.getInteger(R.styleable.ClockFace_hoursTextColor,hoursTextColor);
            minutesTextColor = a.getInteger(R.styleable.ClockFace_minutesTextColor,hoursTextColor);
        } finally {
            a.recycle();
        }

        if (hoursCount == TimePicker.TWENTY_FOUR_HOURS) {
            divisionNumber = TimePicker.TWENTY_FOUR_HOURS;
        }

        mHoursPicker = new TimePicker.TimePickerBuilder(getContext())
                .setTextColor(hoursTextColor)
                .setCircleBackground(R.drawable.gear_1)
                .setHighlightColor(hoursSelectedTextColor)
                .setTextSize(textSize)
                .setNumbersCount(divisionNumber)
                .build();

        mMinutePicker = new TimePicker.TimePickerBuilder(getContext())
                .setTextSize(textSize)
                .setHighlightColor(minutesSelectedTextColor)
                .setTextColor(minutesTextColor)
                .setCircleBackground(R.drawable.gear_2)
                .setGravity(TimePicker.GRAVITY_RIGHT)
                .setNumbersCount(TimePicker.SIXTY_MINUTES)
                .build();


        mBottomBgWheel = new TimePicker.TimePickerBuilder(getContext())
                .setTextSize(textSize)
                .setHighlightColor(minutesSelectedTextColor)
                .setTextColor(minutesTextColor)
                .setCircleBackground(R.drawable.smal)
                .setGravity(TimePicker.GRAVITY_CENTER)
                .setNumbersCount(TimePicker.ZERO)
                .build();

        mTopBgWheel = getAdditionalWheel(minutesSelectedTextColor, minutesTextColor, textSize);

        mCenterBgWheel = getAdditionalWheel(minutesSelectedTextColor, minutesTextColor, textSize);

        mRightBgWheel = getAdditionalWheel(minutesSelectedTextColor, minutesTextColor, textSize);

        initBgClocksRotation();

        mCenterBgWheel.setId(R.id.additional_wheel);
        mHoursPicker.setId(R.id.hours_piker);
        mMinutePicker.setId(R.id.minutes_picker);

        initLayoutParams();

        mHoursParams.addRule(CENTER_VERTICAL);
        mMinutesParams.addRule(CENTER_VERTICAL);


        addViews();
    }

    private void initLayoutParams() {
        mBottomWheelParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mTopWheelParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mCenterWheelParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mRightWheelParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mMinutesParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mHoursParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void addViews() {

        ImageView overlay = new ImageView(getContext());

        overlay.setImageResource(R.drawable.overlay);
        overlay.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setScaleType(ImageView.ScaleType.CENTER_CROP);

        addPickerToLayout(mCenterBgWheel);
        addPickerToLayout(mRightBgWheel);
        addPickerToLayout(mTopBgWheel);

        addPickerToLayout(mMinutePicker);
        addPickerToLayout(mHoursPicker);
        addPickerToLayout(mBottomBgWheel);
        addView(overlay);
    }

    private void initBgClocksRotation() {
        mMinutePicker.setOnRotationListner(new OnRotationListener() {
            @Override
            public void onRotate(float angle, float velocity) {
                mRightBgWheel.rotate(-angle, velocity);
            }
        });

        mHoursPicker.setOnRotationListner(new OnRotationListener() {
            @Override
            public void onRotate(float angle, float velocity) {
                mBottomBgWheel.rotate(angle, velocity);
                mTopBgWheel.rotate(angle, velocity);
                mCenterBgWheel.rotate(-angle, velocity);

            }
        });
    }

    private TimePicker getAdditionalWheel(int minutesSelectedTextColor, int minutesTextColor, int textSize) {
        return new TimePicker.TimePickerBuilder(getContext())
                .setTextSize(textSize)
                .setHighlightColor(minutesSelectedTextColor)
                .setTextColor(minutesTextColor)
                .setCircleBackground(R.drawable.bg_gear_1)
                .setGravity(TimePicker.GRAVITY_CENTER)
                .setNumbersCount(TimePicker.ZERO)
                .build();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            if (mMinutePicker.getWidth() <= r / 2) {
                mHoursParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                mMinutesParams.addRule(ALIGN_PARENT_RIGHT);
            } else {
                setPickersFitInScreen(r);
            }
            mHoursParams.addRule(CENTER_VERTICAL);
            mHoursPicker.setLayoutParams(mHoursParams);
            mHoursPicker.setLayoutParams(mHoursParams);
            mMinutePicker.setLayoutParams(mMinutesParams);

            additionalWheelSetUp();
        }
    }

    private void additionalWheelSetUp() {

        mTopWheelParams.addRule(CENTER_HORIZONTAL);
        mTopWheelParams.setMargins(0, 0, 0, 0);
        mCenterWheelParams.setMargins(0,
                mTopBgWheel.getHeight() + DimenUtils.convertDpToPixel(getContext(), 0),
                0, 0);
        mRightWheelParams.setMargins(mCenterBgWheel.getWidth() - DimenUtils.convertDpToPixel(getContext(), 16),
                0, 0, 0);
        mRightWheelParams.addRule(ALIGN_BOTTOM, R.id.minutes_picker);
        mBottomWheelParams.setMargins(0,
                mHoursPicker.getHeight() + (int) mHoursPicker.getY() / 2 - DimenUtils.convertDpToPixel(getContext(), 24),
                0, 0);
        mTopBgWheel.setLayoutParams(mTopWheelParams);
        mCenterBgWheel.setLayoutParams(mCenterWheelParams);
        mRightBgWheel.setLayoutParams(mRightWheelParams);
        mBottomBgWheel.setLayoutParams(mBottomWheelParams);
    }

    private void setPickersFitInScreen(int r) {
        int hoursPickerOffsetX = r / 2 - mHoursPicker.getWidth();
        mHoursPicker.setX(hoursPickerOffsetX);
        int minutesPickerOffsetX = (r - mMinutePicker.getWidth()) + (mMinutePicker.getWidth() - r / 2);
        mMinutesParams.setMargins(minutesPickerOffsetX, 0, 0, 0);
    }


    private void addPickerToLayout(TimePicker hoursPicker) {
        addView(hoursPicker);
    }

    public void setHoursCount(int hours) {
        mHoursPicker.setNumbersCount(hours);
    }

    public long getTimeInMillis() {
        return (long) ((((mHoursPicker.getSelectedNumber() * DEFAULT_MINUTES_COUNT) + mMinutePicker.getSelectedNumber()))) * SECONDS_IN_MINUTE * MILLIS_IN_SECOND;
    }

    public int getHours() {
        return mHoursPicker.getSelectedNumber();
    }

    public int getMinutes() {
        return mMinutePicker.getSelectedNumber();
    }

    public void setCurrentHours(int hours) {
        mHoursPicker.setSelectedNumber(hours);
    }

    public void setCurrentMinutes(int minutes) {
        mMinutePicker.setSelectedNumber(minutes);
    }


}
