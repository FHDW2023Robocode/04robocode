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

//            // Move in a random direction
//            setAhead(100 * moveDirection);

            // Perform radar scan
            setTurnRadarRight(Double.POSITIVE_INFINITY);

            circleMovement();
            //moveRandom();

            // Execute pending actions
            execute();
        }
    }

    private double scaleFirePowerByDistance(double distance){
        double powerRange = maxFirePower - minFirePower;
        double distanceRange = maxDistance - minDistance;
        double distanceRatio = (distance - minDistance) / distanceRange;
        double power = maxFirePower - (powerRange * distanceRatio);
        return Math.max(minFirePower, Math.min(maxFirePower, power));
    }

    private void trackEnemy(ScannedRobotEvent scannedRobotEvent) {
        String name = scannedRobotEvent.getName();

        List<Tuple<Date, ScannedRobotEvent>> states;
        if(!trackedEnemies.containsKey(name)) {
            states = Collections.synchronizedList(new ArrayList<>());
            trackedEnemies.put(name, states);
        } else {
            states = trackedEnemies.get(name);
        }

        states.add(new Tuple<Date, ScannedRobotEvent>(Calendar.getInstance().getTime(), scannedRobotEvent));
    }

    private Tuple<Date, ScannedRobotEvent> getLastScan(String name) {
        if(!trackedEnemies.containsKey(name)) {
            return null;
        }

        return trackedEnemies.get(name).stream().sorted((tuple1, tuple2) -> {
            if(tuple1.getFirst().getTime() < tuple2.getFirst().getTime()) {
                return 1;
            } else if(tuple1.getFirst().getTime() > tuple2.getFirst().getTime()) {
                return -1;
            } else {
                return 0;
            }
        }).collect(Collectors.toList()).get(0);
    }

    private Date getLastShot(String name) {
        if(!lastShot.containsKey(name)) {
            return new Date(0);
        }

        return lastShot.get(name);
    }

    private void checkShots() {
        for(String name : trackedEnemies.keySet()) {
            List<Tuple<Date, ScannedRobotEvent>> states = trackedEnemies.get(name);
            List<Tuple<Date, ScannedRobotEvent>> relevantStates = states.stream()
                    .filter(tuple -> tuple.getFirst().getTime() > getLastShot(name).getTime())
                    .sorted((tuple1, tuple2) -> {
                        if(tuple1.getFirst().getTime() < tuple2.getFirst().getTime()) {
                            return 1;
                        } else if(tuple1.getFirst().getTime() > tuple2.getFirst().getTime()) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }).collect(Collectors.toList());

            List<Double> energyLevels = relevantStates.stream()
                    .map(tuple -> tuple.getSecond().getEnergy())
                    .distinct()
                    .collect(Collectors.toList());

            if(energyLevels.size() >= 2) {
                lastShot.put(name, Calendar.getInstance().getTime());
                onRobotShot(relevantStates.get(0), relevantStates, energyLevels);
            }
        }
    }

    public void onRobotShot(Tuple<Date, ScannedRobotEvent> latestTuple, List<Tuple<Date, ScannedRobotEvent>> relevantStates, List<Double> energyLevels) {
        System.out.println(latestTuple.getSecond().getName() + " shot!");
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

        for (List<Tuple<Date, ScannedRobotEvent>> enemyStates : trackedEnemies.values()) {
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