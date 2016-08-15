package com.yalantis.library.utils;

import android.support.v4.view.VelocityTrackerCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;

/**
 * Created by Alexey on 09.08.2016.
 */
public class VelocityUtils {
    public final static int MIN_VELOCITY = 1;
    public final static int MAX_VELOCITY = 20;
    private static final int LOW_VELOCITY = 2;

    public static boolean isLowVelocity(float velocity) {
        return velocity > LOW_VELOCITY || velocity < -LOW_VELOCITY;
    }

    public static float computeVelocity(MotionEvent event, int pointerId, VelocityTracker velocityTracker) {
        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(MIN_VELOCITY, MAX_VELOCITY);
        return VelocityTrackerCompat.getYVelocity(velocityTracker,
                pointerId);
    }


    public static VelocityTracker resetVelocityTracker(MotionEvent event,VelocityTracker velocityTracker) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
        velocityTracker.addMovement(event);
        return velocityTracker;
    }
}
