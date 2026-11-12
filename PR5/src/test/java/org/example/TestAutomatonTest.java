package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TestAutomatonTest {

    @ParameterizedTest(name = "[{index}] \"{0}\" -> {1}")
    @CsvSource({
            "'abcTESTabc', F",
            "'abcTES', 3",

            "'TEST', F",
            "'TESTTEST', F",
            "'abcTEST', F",
            "'TESTabc', F",
            "'XTESTX', F",
            "'T TEST T', F",
            "'TESTS', F",

            "'', S",
            "'T', 1",
            "'TE', 2",
            "'TES', 3",
            "'TESX', S",
            "'TEXX', S",
            "'TXX', S",
            "'abc', S",
            "'TSET', 1",
            "'TESTTESS', F",

            "'test', S",
            "'TesT', 1",
            "'TEsT', 1",
            "'ABCtestABC', S"
    })
    @DisplayName("final state after reading the whole string")
    void reportsFinalState(String input, String expected) {
        assertEquals(expected, new TestAutomaton().run(input).toString());
    }

    @ParameterizedTest(name = "[{index}] state {0}, character ''{1}'' -> {2}")
    @CsvSource({
            "S,     T, ONE",
            "S,     A, S",
            "ONE,   E, TWO",
            "ONE,   T, ONE",
            "ONE,   S, S",
            "ONE,   A, S",
            "TWO,   S, THREE",
            "TWO,   T, ONE",
            "TWO,   E, S",
            "TWO,   A, S",
            "THREE, T, F",
            "THREE, E, S",
            "THREE, S, S",
            "THREE, A, S",
            "F,     T, F",
            "F,     A, F"
    })
    @DisplayName("transition table: every state and every relevant character")
    void transitionTable(AutomatonState from, String character, AutomatonState expected) {
        assertEquals(expected, TestAutomaton.transition(from, character.charAt(0)));
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" contains TEST")
    @ValueSource(strings = {
            "TEST",
            "abcTESTabc",
            "TTEST",
            "TETEST",
            "TTTEST",
            "TESTEST",
            "abc TEST abc",
            "TETESTEST"
    })
    void matchesStringsThatContainTheWord(String input) {
        assertTrue(new TestAutomaton().matches(input));
        assertTrue(new TestAutomaton().run(input).isAccepting());
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" does not contain TEST")
    @ValueSource(strings = {
            "",
            "T",
            "TE",
            "TES",
            "TESS",
            "TST",
            "TET",
            "TESA",
            "test"
    })
    void rejectsStringsWithoutTheWord(String input) {
        assertFalse(new TestAutomaton().matches(input));
    }

    /**
     * Task 3 (*). The state diagram sends every unexpected character from states 1 and 2 back
     * to the start state. That loses a match when the character is the 'T' that starts the next
     * attempt, so these strings contain "TEST" while the diagram automaton ends in state 1.
     */
    @ParameterizedTest(name = "[{index}] overlap: \"{0}\" -> F")
    @ValueSource(strings = {
            "TTEST",      // T then TEST: state 1 must stay in state 1 on 'T'
            "TETEST",     // TE then TEST: state 2 must go to state 1 on 'T'
            "TTTEST",     // several repeated 'T'
            "TETETEST",   // repeated partial prefixes
            "TES TTEST",  // overlap after a rejected prefix
            "abcTTEST",   // overlap inside surrounding text
            "TETESTabc"   // overlap followed by other text
    })
    void findsWordHiddenBehindOverlappingPrefix(String input) {
        assertEquals(AutomatonState.F, new TestAutomaton().run(input),
                "overlapping prefix must restart the match instead of resetting it");
    }

    @Test
    @DisplayName("state changes step by step while characters arrive")
    void reportsStateAfterEachCharacter() {
        TestAutomaton automaton = new TestAutomaton();

        assertEquals(AutomatonState.S, automaton.state());
        assertEquals(AutomatonState.S, automaton.accept('a'));
        assertEquals(AutomatonState.ONE, automaton.accept('T'));
        assertEquals(AutomatonState.TWO, automaton.accept('E'));
        assertEquals(AutomatonState.THREE, automaton.accept('S'));
        assertEquals(AutomatonState.F, automaton.accept('T'));
        assertEquals(AutomatonState.F, automaton.state());
        assertTrue(automaton.state().isAccepting());
    }

    @Test
    @DisplayName("accepting state is absorbing")
    void keepsAcceptingStateForTheRestOfTheInput() {
        TestAutomaton automaton = new TestAutomaton();
        automaton.run("TEST");

        for (char c : "anything else".toCharArray()) {
            assertEquals(AutomatonState.F, automaton.accept(c));
        }
    }

    @Test
    @DisplayName("run() starts from the start state again")
    void runResetsPreviousState() {
        TestAutomaton automaton = new TestAutomaton();
        assertEquals(AutomatonState.F, automaton.run("TEST"));
        assertEquals(AutomatonState.THREE, automaton.run("abcTES"));
        assertEquals(AutomatonState.F, automaton.run("TTEST"));
    }

    @Test
    @DisplayName("the word constant matches the automaton")
    void wordConstantIsTest() {
        assertEquals("TEST", TestAutomaton.WORD);
    }
}
