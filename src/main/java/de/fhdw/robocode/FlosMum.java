package de.fhdw.robocode;

import jdk.internal.reflect.Reflection;
import robocode.*;
import robocode.util.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FlosMum extends AdvancedRobot {

    private double moveDirection = 1; // Variable to control movement direction
    private double previousEnergy = 100; // Variable to track previous energy level

    @Override
    public void run() {
        // Set the radar and gun to turn independently
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        Field[] fields = this.getClass().getDeclaredFields();
        for(Field field : fields) {
            System.out.println("Declared field:" + field.getName());
        }

        // Get reflections of superclass
        Field[] superClassFields = this.getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getDeclaredFields();
        for(Field field : superClassFields) {
            //field.setAccessible(true);
            //

        }

        while (true) {
            // Move in a random direction
            setAhead(100 * moveDirection);

            // Perform radar scan
            turnRadarRight(Double.POSITIVE_INFINITY);

            // Fire at enemy if within range
            if (getGunHeat() == 0) {
                fire(2);
            }

            // Execute pending actions
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Calculate the angle to the scanned robot
        double angle = Math.toRadians((getHeading() + e.getBearing()) % 360);

        // Turn gun and radar towards the enemy
        turnGunRightRadians(Utils.normalRelativeAngle(angle - getGunHeadingRadians()));
        turnRadarRightRadians(Utils.normalRelativeAngle(angle - getRadarHeadingRadians()));

        // Predict enemy position
        double enemyX = getX() + e.getDistance() * Math.sin(angle);
        double enemyY = getY() + e.getDistance() * Math.cos(angle);

        // Calculate the angle to the predicted enemy position
        double gunAngle = Math.toDegrees(Math.atan2(enemyX - getX(), enemyY - getY()));

        // Turn the gun towards the predicted enemy position
        turnGunRight(Utils.normalRelativeAngle(gunAngle - getGunHeading()));

        // Fire at the enemy
        fire(3);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bullet's bearing relative to the tank
        double bulletBearing = e.getBearing();

        // Calculate the absolute angle to the bullet impact point
        double impactAngle = getHeading() + bulletBearing;

        // Calculate the bullet's speed
        double bulletSpeed = Rules.getBulletSpeed(e.getBullet().getPower());

        // Calculate the distance to the bullet impact point
        double impactDistance = bulletSpeed * e.getTime();

        // Calculate the coordinates of the bullet impact point
        double impactX = getX() + Math.sin(Math.toRadians(impactAngle)) * impactDistance;
        double impactY = getY() + Math.cos(Math.toRadians(impactAngle)) * impactDistance;

        // Calculate the perpendicular angle to the bullet trajectory for dodging
        double dodgeAngle = bulletBearing + 90 * Math.signum(impactDistance);

        // Dodge the bullet by moving perpendicularly
        setTurnRight(dodgeAngle);
        setAhead(100 * Math.signum(impactDistance));
    }
    @Override
    public void onHitWall(HitWallEvent e) {
        // Reverse the movement direction when hitting a wall
        moveDirection *= -1;

        // Move away from the wall
        setBack(100);
    }

    private void dodgeBullet() {
        // Perform a random movement to dodge the bullet
        double angle = Math.random() * 45 + 45; // Random angle between 45 and 90 degrees
        setTurnLeft(angle);
        setAhead(100 * moveDirection);
    }
}
