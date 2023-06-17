package de.fhdw.robocode;

import robocode.ScannedRobotEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Tracker {
    private final Map<String, List<Tuple<Date, ScannedRobotEvent>>> trackedEnemies;
    private final Map<String, Date> lastShot;

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

        states.add(new Tuple<>(Calendar.getInstance().getTime(), scannedRobotEvent));
    }

    public void trackShot(String name) {
        lastShot.put(name, Calendar.getInstance().getTime());
    }

    public void checkShots(Consumer<Tuple<Date, ScannedRobotEvent>> callback) {
        for(String name : getNames()) {
            List<Tuple<Date, ScannedRobotEvent>> states = getTrackMap().get(name);
            List<Tuple<Date, ScannedRobotEvent>> relevantStates = states.stream()
                    .filter(tuple -> tuple.getFirst().getTime() > getLastShot(name).getTime())
                    .sorted((tuple1, tuple2) -> Long.compare(tuple2.getFirst().getTime(), tuple1.getFirst().getTime()))
                    .collect(Collectors.toList());

            List<Double> energyLevels = relevantStates.stream()
                    .map(tuple -> tuple.getSecond().getEnergy())
                    .distinct()
                    .collect(Collectors.toList());

            if(energyLevels.size() >= 2) {
                trackShot(name);
                callback.accept(relevantStates.get(0));
            }
        }
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

        return getData(name).stream()
                .sorted((tuple1, tuple2) -> Long.compare(tuple2.getFirst().getTime(), tuple1.getFirst().getTime()))
                .collect(Collectors.toList())
                .get(0);
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
