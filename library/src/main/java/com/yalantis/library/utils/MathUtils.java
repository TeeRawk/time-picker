package com.yalantis.library.utils;

/**
 * Created by Alexey on 11.08.2016.
 */
public class MathUtils {

    public static float getDistanceToClosestNumber(float velocity, float rotateAngle, int maxAngle, int numberCount) {
        float distanceToClosestNumber;//Velocity < 0 when swipe down , Velocity> 0 when swipe up
        if (VelocityUtils.isLowVelocity(velocity)) {
            if (velocity > 0) {
                distanceToClosestNumber = rotateAngle - (maxAngle / numberCount) * (float) Math.ceil((rotateAngle) / (maxAngle / numberCount));
            } else {
                distanceToClosestNumber = rotateAngle - (maxAngle / numberCount) * (float) Math.floor((rotateAngle) / (maxAngle / numberCount));
            }
        } else {
            distanceToClosestNumber = rotateAngle - (maxAngle / numberCount) * (float) Math.round((rotateAngle) / (maxAngle / numberCount));
        }
        return distanceToClosestNumber;
    }

    public static boolean isAngleAtNumber(float rotateAngle, int maxAngle, int numbersCount) {
        return Math.abs(rotateAngle) % (maxAngle / numbersCount) == 0;
    }

}
