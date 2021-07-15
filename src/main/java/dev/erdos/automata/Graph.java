package dev.erdos.automata;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static java.util.Collections.singleton;

public final class Graph {


	static <T> void breadthFirstTraversal(T root, Function<T, Collection<T>> step) {
		Queue<T> q = new ArrayDeque<>(singleton(root));
		Set<T> visited = new HashSet<>();
		while (!q.isEmpty()) {
			T item = q.remove();
			if (visited.contains(item)) continue;
			q.addAll(step.apply(item));
			visited.add(item);
		}
	}

	// TODO: add support for not visiting already visited items.

	/**
	 * Root is the first item of the traversal.
	 * The method is called with the root array and the first elem is the current node to visit.
	 * The method returns the number of child nodes filled in the parameter array.
	 */
	static <T> void breadthFirstTraversal(T[] root, ToIntFunction<T[]> step) {
		Queue<T> q = new ArrayDeque<>();
		q.add(root[0]);
		Set<T> visited = new HashSet<>();
		while (!q.isEmpty()) {
			T item = q.remove();
			if (visited.contains(item)) continue;
			root[0] = item;
			root[1] = null;
			int result = step.applyAsInt(root);
			//noinspection ManualArrayToCollectionCopy
			for (int i = 0; i < result; i++) {
				q.add(root[i]);
			}
			visited.add(item);
		}
	}
}
