package com.yalantis.library.utils;

/**
 * Created by Alexey on 11.08.2016.
 */
public class MathUtils {

    public static float getDistanceToClosestNumber(float velocity, float rotateAngle, int angleBetweenNumbers) {
        float distanceToClosestNumber;//Velocity < 0 when swipe down , Velocity> 0 when swipe up
        if (VelocityUtils.isLowVelocity(velocity)) {
            if (velocity > 0) {
                distanceToClosestNumber = rotateAngle - angleBetweenNumbers * (float) Math.ceil((rotateAngle) / angleBetweenNumbers);
            } else {
                distanceToClosestNumber = rotateAngle - angleBetweenNumbers * (float) Math.floor((rotateAngle) / angleBetweenNumbers);
            }
        } else {
            distanceToClosestNumber = rotateAngle - angleBetweenNumbers * (float) Math.round(rotateAngle / angleBetweenNumbers);
        }
        return distanceToClosestNumber;
    }

    public static boolean isAngleAtNumber(float rotateAngle, int numbersCount, int maxAngle) {
        return Math.abs(rotateAngle) % (maxAngle / numbersCount) == 0;
    }

}
