package com.yalantis.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.yalantis.library.utils.DimenUtils;

/**
 * Created by Alexey on 08.08.2016.
 */
public class ClockFace extends LinearLayout {
    private final static int CIRCLE_RADIUS_DP = 200;
    private final static int TEXT_SIZE_DP = 16;
    private final static int DEFAULT_HOURS_COUNT = 24;
    private final static int MILLIS_IN_SECOND = 100;
    private final int DEFAULT_MINUTES_COUNT = 60;
    private final int SECONDS_IN_MINUTE = 60;
    private TimePicker mHoursPicker;
    private TimePicker mMinutePicker;

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

        TypedArray a = context.getTheme().obtainStyledAttributes(
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

        mHoursPicker = new TimePicker.TimePickerBuilder(getContext())
                .setCircleRadius(circleRadius)
                .setTextColor(hoursTextColor)
                .setCircleColor(hoursClockColor)
                .setHighlightColor(hoursSelectedTextColor)
                .setTextSize(textSize)
                .setNumbersCount(hoursCount)
                .build();

        mMinutePicker = new TimePicker.TimePickerBuilder(getContext())
                .setCircleRadius(circleRadius)
                .setTextSize(textSize)
                .setHighlightColor(minutesSelectedTextColor)
                .setTextColor(minutesTextColor)
                .setCircleColor(minutesClockColor)
                .setGravity(-1)
                .setNumbersCount(DEFAULT_MINUTES_COUNT)
                .build();


        addPickerToLayout(mHoursPicker);
        addPickerToLayout(mMinutePicker);
    }

    private void addPickerToLayout(TimePicker hoursPicker) {
        hoursPicker.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        addView(hoursPicker);
    }

    public void setHoursCount(int hours) {
        mHoursPicker.setNumbersCount(hours);
    }

    public long getTimeInMillis() {
        return (long) ((((mHoursPicker.getSelectedNumber() * DEFAULT_MINUTES_COUNT) + mMinutePicker.getSelectedNumber())))* SECONDS_IN_MINUTE *MILLIS_IN_SECOND;
    }
}
