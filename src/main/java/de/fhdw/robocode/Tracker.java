package de.fhdw.robocode;

import robocode.ScannedRobotEvent;

import java.util.*;
import java.util.stream.Collectors;

public class Tracker {
    private final Map<String, List<Tuple<Date, ScannedRobotEvent>>> trackedEnemies;
    private Map<String, Date> lastShot;

    public Tracker() {
        trackedEnemies = Collections.synchronizedMap(new HashMap<>());
        lastShot = Collections.synchronizedMap(new HashMap<>());
    }

    public void trackEnemy(ScannedRobotEvent scannedRobotEvent) {
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

    public void trackShot(String name) {
        lastShot.put(name, Calendar.getInstance().getTime());
    }

    public Map<String, List<Tuple<Date, ScannedRobotEvent>>> getTrackMap() {
        return trackedEnemies;
    }

    public Set<String> getNames() {
        return trackedEnemies.keySet();
    }

    public List<Tuple<Date, ScannedRobotEvent>> getData(String name) {
        if(!trackedEnemies.containsKey(name)) {
            return new ArrayList<>();
        }

        return trackedEnemies.get(name);
    }

    public Tuple<Date, ScannedRobotEvent> getLastScan(String name) {
        if(!trackedEnemies.containsKey(name)) {
            return null;
        }

        return getData(name).stream().sorted((tuple1, tuple2) -> {
            if(tuple1.getFirst().getTime() < tuple2.getFirst().getTime()) {
                return 1;
            } else if(tuple1.getFirst().getTime() > tuple2.getFirst().getTime()) {
                return -1;
            } else {
                return 0;
            }
        }).collect(Collectors.toList()).get(0);
    }

    public Date getLastShot(String name) {
        if(!lastShot.containsKey(name)) {
            return new Date(0);
        }

        return lastShot.get(name);
    }

    public String findNearestEnemyAt(double x, double y) {
        return null;
    }
}
