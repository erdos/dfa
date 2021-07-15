package dev.erdos.automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StateLabelMap<T> {

	private final Map<Integer, List<T>> values = new HashMap<>(4);

	public void put(int state, T value) {
		values.computeIfAbsent(state, __ -> new ArrayList<>()).add(value);
	}

	public List<T> values(int state) {
		return values.getOrDefault(state, Collections.emptyList());
	}

	public boolean hasKey(int state) {
		return values.containsKey(state);
	}

	public String debug() {
		return values.toString();
	}

	public void clear() {
		values.clear();
	}

	public void remove(int state) {
		values.remove(state);
	}
}
