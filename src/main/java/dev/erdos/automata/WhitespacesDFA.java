package dev.erdos.automata;

/**
 * DFA that recognizes (\w*)
 */
public final class WhitespacesDFA implements SimpleIntDfa {

	public static final SimpleIntDfa INSTANCE = new WhitespacesDFA();

	private WhitespacesDFA() {}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public int step(int state, char c) {
		return (c == ' ') ? 0 : -1;
	}

	@Override
	public char label(int state, int n) {
		return ' ';
	}

	@Override
	public int labels(int state) {
		return 1;
	}

	@Override
	public boolean accepts(int state) {
		return true;
	}

	@Override
	public int target(int source, int n) {
		return 0;
	}
}
