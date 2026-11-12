package org.example;

public class Main {
    static void main() {
        String[] samples = {"abcTESTabc", "abcTES", "TTEST", "TETEST", "TESTEST", "no word here"};

        for (String sample : samples) {
            AutomatonState result = new TestAutomaton().run(sample);
            IO.println("%-14s -> %s".formatted('"' + sample + '"', result));
        }
    }
}
