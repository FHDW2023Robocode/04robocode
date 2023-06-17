package de.fhdw.robocode;

import robocode.*;
import robocode.util.Utils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FlosMum extends AdvancedRobot {
    private final double minFirePower = 0.2D;
    private final double maxFirePower = 3.0D;
    private final double minDistance = 100.0D;
    private final double maxDistance = 900.0D;
    private double moveDirection = 1; // Variable to control movement direction

    private int shotCounter = 0;

    private final Tracker tracker = new Tracker();

    @Override
    public void run() {
        // Set the radar and gun to turn independently
        setAdjustRadarForGunTurn(true);
        setAdjustGunForRobotTurn(true);

        while (true) {
            // Move in a random direction
            //setAhead(100 * moveDirection);

            // Perform radar scan
            setTurnRadarRight(Double.POSITIVE_INFINITY);

            System.out.println(getRadarHeading());

            circleMovement();
            //moveRandom();

            // Execute pending actions
            execute();
        }
    }

    private void destroySelf() {
        this.getClass().getDeclaredFields()[0].setAccessible(true);
    }

    private double scaleFirePowerByDistance(double distance) {
        double powerRange = maxFirePower - minFirePower;
        double distanceRange = maxDistance - minDistance;
        double distanceRatio = (distance - minDistance) / distanceRange;
        double power = maxFirePower - (powerRange * distanceRatio);
        return Math.max(minFirePower, Math.min(maxFirePower, power));
    }

    public void onRobotShot(Tuple<Date, ScannedRobotEvent> latestTuple) {
        System.out.println(latestTuple.getSecond().getName() + " shot!");
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        tracker.trackEnemy(e);
        tracker.checkShots(this::onRobotShot);

        double distance = e.getDistance();

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
        setFire(scaleFirePowerByDistance(distance));

        shotCounter ++;
        if(shotCounter >= 5) {
            shotCounter = 0;
            circleMovement();
        }
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

    private void moveRandom() {
        double random = Math.random();

        if(random < 0.5) {
            if(random < 0.25) {
                setTurnRight(45);
            }else {
                setTurnLeft(-45);
            }
        }else {
            if(random > 0.75) {
                setTurnLeft(45);
            }else {
                setTurnLeft(-45);
            }
        }

        if(Math.random() < 0.5) {
            setAhead(100);
        } else {
            setBack(100);
        }
    }

    private void circleMovement() {
        double distanceToMaintain = 75.0; // Distance to maintain from the nearest enemy

        // Find the nearest enemy
        ScannedRobotEvent nearestEnemy = findNearestEnemy();

        if (nearestEnemy != null) {
            // Calculate the angle to the nearest enemy
            double enemyBearing = getHeadingRadians() + nearestEnemy.getBearingRadians();

            // Calculate the desired angle to maintain distance
            double desiredAngle = enemyBearing + Math.PI / 2 * moveDirection;

            // Calculate the desired X and Y coordinates to maintain distance
            double desiredX = getX() + Math.sin(desiredAngle) * distanceToMaintain;
            double desiredY = getY() + Math.cos(desiredAngle) * distanceToMaintain;

            // Calculate the difference between the desired coordinates and the current position
            double deltaX = desiredX - getX();
            double deltaY = desiredY - getY();

            // Calculate the angle to turn towards the desired position
            double angle = Utils.normalRelativeAngle(Math.atan2(deltaX, deltaY) - getHeadingRadians());

            // Set the movement direction based on the angle
            moveDirection = Math.signum(angle);

            // Move in a circular path with a constant speed
            setAhead(100 * moveDirection);

            // Turn towards the desired position
            setTurnRightRadians(angle);
        }
    }




    private ScannedRobotEvent findNearestEnemy() {
        double nearestDistance = Double.POSITIVE_INFINITY;
        ScannedRobotEvent nearestEnemy = null;

        for (List<Tuple<Date, ScannedRobotEvent>> enemyStates : tracker.getTrackMap().values()) {
            if (!enemyStates.isEmpty()) {
                ScannedRobotEvent latestState = enemyStates.get(enemyStates.size() - 1).getSecond();
                double distance = latestState.getDistance();
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestEnemy = latestState;
                }
            }
        }

        return nearestEnemy;
    }

}
