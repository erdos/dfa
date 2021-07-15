package dev.erdos.automata;

/**
 * Deterministic Finite Automaton which is mutable and states are represented as integers.
 */
public interface WritableIntDfa extends SimpleIntDfa {

	/**
	 * Creates a new state and returns its id.
	 */
	int newState();


	/**
	 * Connect two states with a transition on letter c.
	 */
	WritableIntDfa connect(int source, char c, int target);


	/**
	 * Create a new state with outgoing transitions copied from state in parameter.
	 */
	default int copyWithOutgoingEdges(int state) {
		int newState = newState();
		int max = labels(state);
		for (int i = 0; i < max; i++) {
			char c = label(state, i);
			int target = step(state, c);
			connect(newState, c, target);
		}
		return newState;
	}

	/**
	 * Mark state as accepting state.
	 */
	void accept(int state);

	void reject(int state);

	default WritableIntDfa union(SimpleIntDfa other) {
		return new IntDfaOperations().union(this, other);
	}

	default WritableIntDfa concat(SimpleIntDfa other) {
		return new IntDfaOperations().concat(this, other);
	}
}
