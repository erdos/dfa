package dev.erdos.automata;

import java.util.Arrays;

/**
 * Idea: all automata with the same state count are isomorphic when all of their letters are unique.
 * So we can quickly recreate common automata layouts just by caching them and renaming the edges using a lookup table.
 */
public final class LevenshteinDFA {

	private final int distance;

	public LevenshteinDFA(int distance) {
		this.distance = distance;
		assert distance >= 0;
	}

	public static boolean uniqueLetters(CharSequence cs) {
		long mask = 0;
		for (int i = 0, len = cs.length(); i < len; i++) {
			char  c = cs.charAt(i);
			long off = 1L << (c % 64);
			if ((off & mask) != 0) {
				return false;
			} else {
				mask |= off;
			}
		}
		return true;
	}

	private static SimpleIntDfa factor(CharSequence cs, int distance) {
		return NDFA.levenshtein(cs, distance).dfa();
	}

	private final SimpleIntDfa[] cache = new SimpleIntDfa[64];
	private final char[] targetMappingCache = new char[64];
	{
		targetMappingCache[0] = SimpleIntDfa.ANY;
	}

	int cacheHitCount = 0;
	int uniqueCount = 0;

	public SimpleIntDfa cached(CharSequence cs) {
		if (!uniqueLetters(cs)) {
			return factor(cs, distance);
		} else {
			uniqueCount++;
			int length = cs.length();
			SimpleIntDfa template = template(length);

			for (int i = 0; i < length; i++) {
				targetMappingCache[i + 1] = cs.charAt(i);
			}
			return new IsomorphicIntDfa(template, sourceMapping, targetMappingCache);
		}
	}

	private static long combineInts(char a, int b) {
		return (long) a << 32 | b & 0xFFFFFFFFL;
	}

	private static char decombineInt1(long c) {
		return (char) (c >> 32);
	}

	private static int decombineInt2(long c) {
		return (int) c;
	}

	private SimpleIntDfa template(int length) {
		SimpleIntDfa template = cache[length];
		if (template == null) {
			CharSequence seq = new CharSequence() {
				@Override
				public int length() {
					return length;
				}

				@Override
				public char charAt(int index) {
					return (char) ('a' + index);
				}

				@Override
				public CharSequence subSequence(int start, int end) {
					throw new RuntimeException("Not implemented!");
				}
			};
			template = cache[length] = factor(seq, distance);
		} else {
			cacheHitCount++;
		}
		return template;
	}

	private static final char[] sourceMapping = new char[128];
	static {
		sourceMapping[0] = SimpleIntDfa.ANY;
		for(int i = 0; i < sourceMapping.length - 1; i++) {
			sourceMapping[i + 1] = (char) ('a' + i);
		}
	}

	private static class IsomorphicIntDfa implements SimpleIntDfa {
		private final long[][] transitions;
		private final int[] acceptors;

		IsomorphicIntDfa(SimpleIntDfa template, char[] oldLabels, char[] newLabels) {
			transitions = new long[template.size()][];

			int acceptCount = 0;

			for(int state = 0, maxState = template.size(); state < maxState; state++) {
				int labels = template.labels(state);
				long[] node = new long[labels];

				if (template.accepts(state)) {
					acceptCount++;
				}

				for (int k = 0; k < labels; k++) {
					char oldChar = template.label(state, k);
					int newCharIdx = Arrays.binarySearch(oldLabels, oldChar);
					char newChar = newLabels[newCharIdx];
					node[k] = combineInts(newChar, k);
				}
				Arrays.sort(node);
				// then re-rename contents of array.
				for (int k = 0; k < labels; k++) {
					char newChar = decombineInt1(node[k]);
					int index = decombineInt2(node[k]);
					node[k] = combineInts(newChar, template.target(state, index));
				}

				transitions[state] = node;
			}

			this.acceptors = new int[acceptCount];
			for(int i = 0, j = 0; i < transitions.length; i++) {
				if (template.accepts(i)) {
					acceptors[j++] = i;
				}
			}
		}

		@Override
		public int size() {
			return transitions.length;
		}

		@Override
		public char label(int state, int n) {
			assert state > -1;
			assert n > -1;
			return decombineInt1(transitions[state][n]);
		}

		@Override
		public int target(int state, int n) {
			assert state > -1;
			assert n > -1;
			return decombineInt2(transitions[state][n]);
		}

		@Override
		public int step(int source, char c) {
			long[] array = transitions[source];
			// TODO: better scan!
			for (long value : array) {
				if (decombineInt1(value) == c) {
					return decombineInt2(value);
				}
			}
			return -1;
		}

		@Override
		public int labels(int state) {
			return transitions[state].length;
		}

		@Override
		public boolean accepts(int state) {
			return Arrays.binarySearch(acceptors, state) >= 0;
		}
	}
}
