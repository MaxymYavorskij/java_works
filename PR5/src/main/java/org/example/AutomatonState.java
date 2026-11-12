package org.example;

/**
 * States of the automaton that searches for "TEST".
 * {@link #F} is the accepting state.
 */
public enum AutomatonState {
    S('S'),
    ONE('1'),
    TWO('2'),
    THREE('3'),
    F('F');

    private final char label;

    AutomatonState(char label) {
        this.label = label;
    }

    public char label() {
        return label;
    }

    public boolean isAccepting() {
        return this == F;
    }

    @Override
    public String toString() {
        return String.valueOf(label);
    }
}
