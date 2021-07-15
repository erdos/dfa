package dev.erdos.automata;

import java.util.function.Predicate;

public interface SimpleIntDfa extends Predicate<CharSequence> {

	int START = 0;
	char ANY = (char) 0;

	/**
	 * Number of states in this automaton.
	 */
	int size();

	/**
	 * Returns the next state after reading chracter or -1 when not found.
	 *
	 * Does not handle * transitions.
	 */
	int step(int state, char c);

	/**
	 * Returns label of nth edge from state. First entry is '*', rest are in alphabetical order.
	 */
	char label(int state, int n);

	/**
	 * Number of outgoing edges from a state.
	 */
	int labels(int state);

	/**
	 * Returns true iff parameter identifies an accepting state.
	 */
	boolean accepts(int state);

	// nth target state from source
	default int target(int source, int n) {
		return step(source, label(source, n));
	}

	// return state after parsing word.
	default int parse(CharSequence word) {
		int state = 0;
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			int nt = step(state, c);
			if (nt == -1) nt = step(state, ANY);
			if (nt == -1) return -1;
			else state = nt;
		}
		return state;
	}

	@Override
	default boolean test(CharSequence word) {
		return accepts(parse(word));
	}
}
