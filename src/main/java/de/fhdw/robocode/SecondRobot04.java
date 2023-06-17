package de.fhdw.robocode;

import robocode.HitByBulletEvent;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class SecondRobot04 extends AdvancedRobot {
    private boolean dodging = false;

    @Override
    public void run() {

        double radius = 100.0;
        double angle = 90.0;

        while (true) {
            if (!dodging) {
                moveAround();
            }
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        fire(10);

        if (!dodging) {
            dodge(e);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        turnLeft(90 - e.getBearing());

        if (!dodging) {
            startDodging(e.getBearing());
        }
    }

    private void moveAround() {
        setAhead(100); // Move forward
        setTurnRight(45); // Turn right
        setBack(100); // Move back
        setTurnLeft(45); // Turn left
    }

    private void dodge(ScannedRobotEvent event) {
        double bulletBearing = event.getBearingRadians();
        double bulletHeading = getHeadingRadians() + bulletBearing;

        double bulletX = getX() + Math.sin(bulletHeading) * event.getDistance();
        double bulletY = getY() + Math.cos(bulletHeading) * event.getDistance();

        double dodgeAngle = Math.toDegrees(Math.atan2(getX() - bulletX, getY() - bulletY));

        setTurnRight(normalizeBearing(dodgeAngle - getHeading()));
        setAhead(100); // Move forward to dodge
    }

    private void startDodging(double bulletBearing) {
        dodging = true;
        setTurnRight(normalizeBearing(getHeading() - 90 - bulletBearing));
        setAhead(150); // Move forward to dodge
    }

    private double normalizeBearing(double angle) {
        while (angle > 180) {
            angle -= 360;
        }
        while (angle < -180) {
            angle += 360;
        }
        return angle;
    }

}
