package model;

public class State {
    public int heuristicValue;
    public Cell[][] state;

    public State(int hValue, Cell[][] state) {
        this.heuristicValue = hValue;
        this.state = state;
    }
}
