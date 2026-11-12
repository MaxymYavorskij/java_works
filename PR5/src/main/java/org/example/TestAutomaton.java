package org.example;

/**
 * Finite state machine that detects whether the exact word "TEST" occurs in an input string.
 * It reads one character at a time; the final state {@link AutomatonState#F} means the word
 * was found.
 * <p>
 * The state diagram in the assignment is incomplete: it sends every non-matching character
 * back to the start state. That loses matches whose first letter is also the last letter of a
 * partially matched prefix, for example "TTEST" or "TETEST". Here a 'T' restarts the match in
 * states 1 and 2, the same way a string search keeps the overlapping prefix.
 */
public final class TestAutomaton {

    public static final String WORD = "TEST";

    private AutomatonState state = AutomatonState.S;

    public AutomatonState state() {
        return state;
    }

    public AutomatonState accept(char c) {
        state = transition(state, c);
        return state;
    }

    public AutomatonState run(String input) {
        state = AutomatonState.S;
        for (int i = 0; i < input.length(); i++) {
            accept(input.charAt(i));
        }
        return state;
    }

    public boolean matches(String input) {
        return run(input).isAccepting();
    }

    public static AutomatonState transition(AutomatonState state, char c) {
        return switch (state) {
            // The accepting state is absorbing: one found word stays found.
            case F -> AutomatonState.F;
            case S -> c == 'T' ? AutomatonState.ONE : AutomatonState.S;
            // "T" matched. 'E' continues, 'T' restarts the match ("TTEST").
            case ONE -> switch (c) {
                case 'E' -> AutomatonState.TWO;
                case 'T' -> AutomatonState.ONE;
                default -> AutomatonState.S;
            };
            // "TE" matched. 'S' continues, 'T' restarts the match ("TETEST").
            case TWO -> switch (c) {
                case 'S' -> AutomatonState.THREE;
                case 'T' -> AutomatonState.ONE;
                default -> AutomatonState.S;
            };
            // "TES" matched. Only 'T' completes the word; anything else starts over.
            case THREE -> c == 'T' ? AutomatonState.F : AutomatonState.S;
        };
    }
}
