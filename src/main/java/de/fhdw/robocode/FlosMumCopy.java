package de.fhdw.robocode;

import robocode.HitByBulletEvent;
import robocode.Robot;
import robocode.ScannedRobotEvent;

public class FlosMumCopy extends Robot {

    @Override
    public void run() {

        double radius = 100.0;
        double angle = 90.0;

        while (true) {
            ahead(radius);
            turnLeft(angle);
            turnGunLeft(angle);
            fireBullet(getEnergy());
            
            //Print class
            System.out.println();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        fire(10);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        turnLeft(90 - e.getBearing());
    
    }

    

}
