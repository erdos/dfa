package dev.erdos.automata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Deterministic Finite Automaton
 */
public final class MutableArrayDFA<T> implements WritableIntDfa {

	private int size;

	final List<long[]> transitions = new ArrayList<>();
	private final StateLabelMap<T> stateLabels = new StateLabelMap<>();

	public MutableArrayDFA(SimpleIntDfa parent) {
		size = parent.size();
		for (int i = 0; i < size; i++) {
			long[] ary = new long[parent.labels(i)];
			for (int j = 0; j < ary.length; j++) {
				ary[j] = combineInts(parent.label(i, j), parent.target(i, j));
			}
			transitions.add(ary);
			if (parent.accepts(i)) {
				stateLabels.put(i, null);
			}
		}
	}

	public MutableArrayDFA() {
		newState();
	}

	@Override
	public int newState() {
		transitions.add(new long[0]);
		return size++;
	}

	public static <T> MutableArrayDFA<T> empty() {
		return new MutableArrayDFA<>();
	}

	@Override
	public MutableArrayDFA<T> connect(int sourceIndex, char c, int targetIndex) {
		assert sourceIndex < size;
		assert targetIndex < size;

		long[] array = transitions.get(sourceIndex);
		long[] newArray = new long[array.length + 1];

		newArray[0] = combineInts(c, targetIndex);
		System.arraycopy(array, 0, newArray, 1, array.length);
		Arrays.sort(newArray);
		transitions.set(sourceIndex, newArray);

		return this;
	}

	@Override
	public int step(int source, char c) {

		long[] array = transitions.get(source);

		// TODO: better scan!
		for (long value : array) {
			if (decombineInt1(value) == c) {
				return decombineInt2(value);
			}
		}

		return -1;
	}

	// creates a new state with outgoing edges copied from given
	@Override
	public int copyWithOutgoingEdges(int state) {
		assert 0 <= state;
		transitions.add(transitions.get(state).clone());
		return size++;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public char label(int state, int n) {
		assert state > -1;
		assert n > -1;
		return decombineInt1(transitions.get(state)[n]);
	}

	@Override
	public int target(int state, int n) {
		assert state > -1;
		assert n > -1;
		return decombineInt2(transitions.get(state)[n]);
	}

	@Override
	public int labels(int state) {
		return transitions.get(state).length;
	}

	@Override
	public boolean accepts(int state) {
		return stateLabels.hasKey(state);
	}

	@Override
	public void accept(int state) {
		stateLabels.put(state, null);
	}

	@Override
	public void reject(int state) {
		stateLabels.remove(state);
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

	public String debug() {
		Object ary = transitions.stream()
				.map(array ->
						new TreeMap<>(Arrays.stream(array)
								.boxed()
								.collect(Collectors.toMap(MutableArrayDFA::decombineInt1, MutableArrayDFA::decombineInt2))))
				.collect(Collectors.toList());
		System.out.println("!> " + size() + " :"+ ary);

		return Objects.toString(ary);
	}
}
