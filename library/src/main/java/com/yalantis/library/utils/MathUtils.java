package com.yalantis.library.utils;

import com.yalantis.library.TimePicker;

/**
 * Created by Alexey on 11.08.2016.
 */
public class MathUtils {

    public static float getDistanceToClosestNumber(float velocity, float rotateAngle, int numberCount) {
        float distanceToClosestNumber;//Velocity < 0 when swipe down , Velocity> 0 when swipe up
        if (VelocityUtils.isLowVelocity(velocity)) {
            if (velocity > 0) {
                distanceToClosestNumber = rotateAngle - (TimePicker.MAX_ANGLE / numberCount) * (float) Math.ceil((rotateAngle) / (TimePicker.MAX_ANGLE / numberCount));
            } else {
                distanceToClosestNumber = rotateAngle - (TimePicker.MAX_ANGLE / numberCount) * (float) Math.floor((rotateAngle) / (TimePicker.MAX_ANGLE / numberCount));
            }
        } else {
            distanceToClosestNumber = rotateAngle - (TimePicker.MAX_ANGLE / numberCount) * (float) Math.round((rotateAngle) / (TimePicker.MAX_ANGLE / numberCount));
        }
        return distanceToClosestNumber;
    }

    public static boolean isAngleAtNumber(float rotateAngle, int numbersCount) {
        return Math.abs(rotateAngle) % (TimePicker.MAX_ANGLE / numbersCount) == 0;
    }

}
