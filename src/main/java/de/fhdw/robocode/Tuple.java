package de.fhdw.robocode;

public class Tuple<T, J> {
    private T first;
    private J second;

    public Tuple(T first, J second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public J getSecond() {
        return second;
    }
}
