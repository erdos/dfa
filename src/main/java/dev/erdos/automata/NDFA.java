package dev.erdos.automata;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static dev.erdos.automata.SimpleIntDfa.ANY;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

/**
 * Nondeterministic Finite Automata
 */
public final class NDFA<T> implements Predicate<CharSequence> {
	public static final int START_STATE = 0;
	public static final char EPSILON = 'ε';

	//private final NavigableSet<Transition> transitions = new TreeSet<>(TRANSITION_COMPARATOR);
	private final List<List<Transition>> transitionsList = new ArrayList<>();
	private final Set<Character> alphabet = new HashSet<>();
	final StateLabelMap<T> stateLabels = new StateLabelMap<>();

	{
		newState();
	}

	// increases nr of states, returns new state index.
	int newState() {
		transitionsList.add(new ArrayList<>(1));
		return transitionsList.size() - 1;
	}

	void addTransition(char c, int source, int destination) {
		if (c != EPSILON && c != ANY) alphabet.add(c);
		transitionsList.get(source).add(new Transition(source, c, destination));
		//		transitions.add(new Transition(source, c, destination));
	}

	void addDefaultTransition(int source, int destination) {
		addTransition(ANY, source, destination);
	}

	void addEpsilonTransition(int source, int destination) {
		addTransition(EPSILON, source, destination);
	}

	public static NDFA<Boolean> levenshtein(CharSequence word, int maxDistance) {
		return levenshtein(word, maxDistance, true);
	}

	// build levenshtein automata for a word with a distance.
	public static <T> NDFA<T> levenshtein(CharSequence word, int maxDistance, T value) {
		assert maxDistance >= 0;
		NDFA<T> automaton = new NDFA<>();

		// first column
		int[] col1 = new int[maxDistance + 1];
		col1[0] = NDFA.START_STATE;
		for (int i = 1; i <= maxDistance; i++) {
			col1[i] = automaton.newState();
			automaton.addDefaultTransition(col1[i - 1], col1[i]);
		}

		int[] col2 = new int[maxDistance + 1];
		for (int j = 1; j <= word.length(); j++) {
			char c = word.charAt(j - 1);

			col2[0] = automaton.newState();
			automaton.addTransition(c, col1[0], col2[0]);
			for (int i = 1; i <= maxDistance; i++) {
				int state = automaton.newState();
				col2[i] = state;
				automaton.addDefaultTransition(col2[i - 1], state);
				automaton.addDefaultTransition(col1[i - 1], state);
				automaton.addEpsilonTransition(col1[i - 1], state);
				automaton.addTransition(c, col1[i], state);
			}

			// we only need two arrays
			int[] tmp = col1;
			col1 = col2;
			col2 = tmp;
		}

		for (int i = 0; i <= maxDistance; i++) {
			automaton.stateLabels.put(col1[i], value);
		}

		return automaton;
	}

	@Override
	public boolean test(CharSequence word) {
		Set<Integer> states = startStateSet();
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);

			states = states.stream().flatMap(state -> transitions(state, c).stream()).collect(toSet());
			if (states.isEmpty()) return false;
		}
		return states.stream().flatMap(s -> transitiveClosure(s).stream()).anyMatch(stateLabels::hasKey);
	}

	// TODO: speed it up!!!!
	private Stream<Transition> transitionsFromState(int state) {
		return transitionsList.get(state).stream();
	}

	// TODO: speed it up
	private Set<Integer> transitiveClosure(int state) {
		Set<Integer> closure = new HashSet<>();
		Set<Integer> buffer = new HashSet<>();
		breadthFirstTraversal(state, t -> {
			if (closure.add(t)) {
				buffer.clear();
				// TODO: make this faster?
				transitionsFromState(t).filter(x -> x.c == EPSILON).map(x -> x.target).forEach(buffer::add);
				return buffer;
			} else {
				return emptySet();
			}
		});

		return closure;
	}

	// start set and its transitive closure
	Set<Integer> startStateSet() {
		return transitiveClosure(START_STATE);
	}

	public Set<Character> alphabet() {
		return alphabet;
	}

	public int size() {
		return transitionsList.size();
	}

	private static final class Transition {
		final int source;
		final char c;
		final int target;

		private Transition(int source, char c, int target) {
			this.source = source;
			this.c = c;
			this.target = target;
		}

		@Override
		public String toString() {
			return "[" + source + " -" + c + "-> " + target + "]";
		}
	}

	private static <T> void breadthFirstTraversal(T root, Function<T, Collection<T>> step) {
		Queue<T> q = new ArrayDeque<>(singleton(root));
		while (!q.isEmpty()) {
			q.addAll(step.apply(q.remove()));
		}
	}

	public WritableIntDfa dfa() {
		Set<Character> characters = this.alphabet();

		Set<Integer> start = this.startStateSet();

		WritableIntDfa result = new MutableArrayDFA<>();

		Map<Set<Integer>, Integer> statesToIndex = new HashMap<>();
		statesToIndex.put(start, SimpleIntDfa.START);

		List<Set<Integer>> traversalOutputBuffer = new ArrayList<>();

		Graph.breadthFirstTraversal(start, states -> {
			assert !states.isEmpty();
			int sourceIndex = requireNonNull(statesToIndex.get(states));
			traversalOutputBuffer.clear();

			// TODO: copy state labels
			for (int state : states) {
				if (this.stateLabels.hasKey(state)) {
					result.accept(sourceIndex);
				}
			}

			// if there is arrow for *
			Set<Integer> antTargetStates = this.step(ANY, states);
			if (!antTargetStates.isEmpty()) {
				int anyTarget = statesToIndex.computeIfAbsent(antTargetStates, __ -> result.newState());
				result.connect(sourceIndex, ANY, anyTarget);
				traversalOutputBuffer.add(antTargetStates);
			}

			for (char c : characters) {
				Set<Integer> target = this.step(c, states);
				if (target.equals(antTargetStates)) continue; // if same as for * then we can skip

				assert !target.isEmpty();

				int targetIndex = statesToIndex.getOrDefault(target, -1);
				if (targetIndex == -1) {
					targetIndex = result.newState();
					statesToIndex.put(target, targetIndex);
				}
				result.connect(sourceIndex, c, targetIndex);
				traversalOutputBuffer.add(target);
			}
			return traversalOutputBuffer;
		});

		return result;
	}


	// set of all states accessible when reading character. follows transitive closure.
	private Set<Integer> transitions(int sourceState, char c) {
		/*
		// same but slower:
		return transitionsFromState(sourceState)
				.filter(t -> t.c == c || t.c == ANY)
				.map(t -> t.target)
				.flatMap(t -> transitiveClosure(t).stream())
				.collect(toSet());
		*/
		Set<Integer> result = new HashSet<>();
		for (Transition transition : transitionsList.get(sourceState)) {
			if (transition.c == c || transition.c == ANY) {
				result.addAll(transitiveClosure(transition.target));
			}
		}
		return result;
	}

	Set<Integer> step(char c, Set<Integer> fromStates) {
		// return fromStates.stream().flatMap(t -> transitions(t, c).stream()).collect(Collectors.toSet());

		Set<Integer> result = new HashSet<>();
		Set<Integer> buffer = new HashSet<>();
		for (Integer t : fromStates) {
			// TODO figure out transitions
			for (Transition transition : transitionsList.get(t)) {
				if (transition.c == c || transition.c == ANY) {

					breadthFirstTraversal(transition.target, tt -> {
						if (result.add(tt)) {
							buffer.clear();

							for (Transition x : transitionsList.get(tt)) {
								if (x.c == EPSILON) {
									buffer.add(x.target);
								}
							}

							return buffer;
						} else {
							return emptySet();
						}
					});

					result.addAll(transitiveClosure(transition.target));
				}
			}
		}

		return result;
	}
}
