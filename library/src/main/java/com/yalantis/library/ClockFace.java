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
    private final static int CIRCLE_RADIUS_DP = 200;
    private final static int TEXT_SIZE_DP = 16;
    private final static int DEFAULT_HOURS_COUNT = 24;
    private final static int MILLIS_IN_SECOND = 1000;
    private final static int DEFAULT_MINUTES_COUNT = 60;
    private final static int SECONDS_IN_MINUTE = 60;
    private TimePicker mHoursPicker;
    private TimePicker mMinutePicker;
    private TimePicker mAdditionalPicker;
    private LayoutParams mHoursParams;
    private LayoutParams mAdditionalParams;
    private LayoutParams mMinutesParams;
    private TimePicker mAdditionalPicker1;
    private LayoutParams mAdditionalParams1;
    private LayoutParams mAdditionalParams2;
    private TimePicker mAdditionalPicker2;
    private TimePicker mAdditionalPicker3;
    private LayoutParams mAdditionalParams3;

    public ClockFace(Context context) {
        this(context, null);
    }

    public ClockFace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockFace(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int hoursClockColor = Color.WHITE;
        int minutesClockColor = Color.WHITE;
        int minutesSelectedTextColor = Color.RED;
        int hoursSelectedTextColor = Color.RED;
        int hoursTextColor = Color.BLACK;
        int minutesTextColor = Color.BLACK;
        int textSize = DimenUtils.convertSpToPixel(context, TEXT_SIZE_DP);
        int hoursCount = DEFAULT_HOURS_COUNT;
        int circleRadius = DimenUtils.convertDpToPixel(context, CIRCLE_RADIUS_DP);
        @TimePicker.DivisionNumber int divisionNumber = TimePicker.TWELVE_HOURS;

        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ClockFace,
                0, 0);

        try {
            hoursClockColor = a.getColor(R.styleable.ClockFace_hoursClockColor, hoursClockColor);
            hoursSelectedTextColor = a.getColor(R.styleable.ClockFace_hoursHighlightColor, hoursSelectedTextColor);
            minutesClockColor = a.getColor(R.styleable.ClockFace_minutesClockColor, minutesClockColor);
            minutesSelectedTextColor = a.getColor(R.styleable.ClockFace_minutesHighlightColor, minutesSelectedTextColor);
            hoursTextColor = a.getColor(R.styleable.ClockFace_hoursTextColor, hoursTextColor);
            minutesTextColor = a.getColor(R.styleable.ClockFace_minutesTextColor, minutesTextColor);
            textSize = a.getDimensionPixelSize(R.styleable.ClockFace_numbersSize, textSize);
            hoursCount = a.getInteger(R.styleable.ClockFace_hoursCount, hoursCount);
            circleRadius = a.getInteger(R.styleable.ClockFace_circleRadius, circleRadius);
        } finally {
            a.recycle();
        }

        if (hoursCount == TimePicker.TWENTY_FOUR_HOURS) {
            divisionNumber = TimePicker.TWENTY_FOUR_HOURS;
        }

        mHoursPicker = new TimePicker.TimePickerBuilder(getContext())
                .setCircleRadius(circleRadius)
                .setTextColor(hoursTextColor)
                .setCircleColor(hoursClockColor)
                .setCircleBackground(R.drawable.gear_1)
                .setHighlightColor(hoursSelectedTextColor)
                .setTextSize(textSize)
                .setNumbersCount(divisionNumber)
                .build();

        mMinutePicker = new TimePicker.TimePickerBuilder(getContext())
                .setCircleRadius(circleRadius)
                .setTextSize(textSize)
                .setHighlightColor(minutesSelectedTextColor)
                .setTextColor(minutesTextColor)
                .setCircleBackground(R.drawable.gear_2)
                .setCircleColor(minutesClockColor)
                .setGravity(TimePicker.GRAVITY_RIGHT)
                .setNumbersCount(TimePicker.SIXTY_MINUTES)
                .build();


        mAdditionalPicker = getAdditionalWheel(minutesClockColor, minutesSelectedTextColor, minutesTextColor, textSize, circleRadius);

        mAdditionalPicker1 = getAdditionalWheel(minutesClockColor, minutesSelectedTextColor, minutesTextColor, textSize, circleRadius);

        mAdditionalPicker2 = getAdditionalWheel(minutesClockColor, minutesSelectedTextColor, minutesTextColor, textSize, circleRadius);

        mAdditionalPicker3 = getAdditionalWheel(minutesClockColor, minutesSelectedTextColor, minutesTextColor, textSize, circleRadius);

        ImageView additionalGear = new ImageView(getContext());
        additionalGear.setImageDrawable(getContext().getResources().getDrawable(R.drawable.gear_1));

        mMinutePicker.setOnRotationListner(new OnRotationListner() {
            @Override
            public void onRotate(float angle, float velocity) {
                mAdditionalPicker.rotate(angle, velocity);
                mAdditionalPicker1.rotate(angle, velocity);
                mAdditionalPicker2.rotate(angle, velocity);
                mAdditionalPicker3.rotate(angle, velocity);
            }
        });

        mHoursPicker.setOnRotationListner(new OnRotationListner() {
            @Override
            public void onRotate(float angle, float velocity) {
                mAdditionalPicker.rotate(angle, velocity);
                mAdditionalPicker1.rotate(angle, velocity);
                mAdditionalPicker2.rotate(angle, velocity);
                mAdditionalPicker3.rotate(angle, velocity);
            }
        });

        mAdditionalPicker1.setId(R.id.additional_wheel);
        mAdditionalPicker2.setId(R.id.additional_wheel1);
        mHoursPicker.setId(R.id.hours_piker);
        mMinutePicker.setId(R.id.minutes_picker);
        mHoursParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mHoursParams.addRule(CENTER_VERTICAL);
        mHoursPicker.setLayoutParams(mHoursParams);

        mAdditionalParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mAdditionalParams1 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mAdditionalParams2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mAdditionalParams3 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mMinutesParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mMinutesParams.addRule(CENTER_VERTICAL);


        addPickerToLayout(mAdditionalPicker);
        addPickerToLayout(mAdditionalPicker2);
        addPickerToLayout(mAdditionalPicker3);
        addPickerToLayout(mAdditionalPicker1);

        addPickerToLayout(mMinutePicker);
        addPickerToLayout(mHoursPicker);
    }

    private TimePicker getAdditionalWheel(int minutesClockColor, int minutesSelectedTextColor, int minutesTextColor, int textSize, int circleRadius) {
        return new TimePicker.TimePickerBuilder(getContext())
                .setCircleRadius(circleRadius)
                .setTextSize(textSize)
                .setHighlightColor(minutesSelectedTextColor)
                .setTextColor(minutesTextColor)
                .setCircleBackground(R.drawable.shape_copy_5)
                .setCircleColor(minutesClockColor)
                .setGravity(TimePicker.GRAVITY_CENTER)
                .setNumbersCount(TimePicker.ZERO)
                .build();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (mMinutePicker.getWidth() <= r / 2) {
            mHoursParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            mMinutesParams.addRule(ALIGN_PARENT_RIGHT);
        } else {
            setPickersFitInScreen(r);
        }
        mHoursParams.addRule(CENTER_VERTICAL);
        mHoursPicker.setLayoutParams(mHoursParams);
        mMinutePicker.setLayoutParams(mMinutesParams);

        additionalWheelSetUp();
    }

    private void additionalWheelSetUp() {
        mAdditionalParams.addRule(RelativeLayout.BELOW, R.id.hours_piker);
        mAdditionalParams1.addRule(CENTER_HORIZONTAL);
        mAdditionalParams1.addRule(ALIGN_TOP, R.id.minutes_picker);
        mAdditionalParams2.setMargins(0, (int) (mHoursPicker.getY() / 2) + mHoursPicker.getHeight() / 2, 0, 0);
        mAdditionalParams3.setMargins(mAdditionalPicker2.getWidth() - DimenUtils.convertDpToPixel(getContext(), 16), 0, 0, 0);
        mAdditionalParams3.addRule(ALIGN_BOTTOM, R.id.minutes_picker);
        mAdditionalPicker.setLayoutParams(mAdditionalParams);
        mAdditionalPicker1.setLayoutParams(mAdditionalParams1);
        mAdditionalPicker2.setLayoutParams(mAdditionalParams2);
        mAdditionalPicker3.setLayoutParams(mAdditionalParams3);
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
