package dev.erdos.automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.erdos.automata.Graph.breadthFirstTraversal;
import static dev.erdos.automata.SimpleIntDfa.ANY;
import static java.lang.Math.min;

@SuppressWarnings({"SameParameterValue", "ForLoopReplaceableByForEach"})
public final class IntDfaOperations {

	private final Map<Integer, Integer> otherStateToNewLocalState = new HashMap<>();
	private final List<Long> result = new ArrayList<>();
	private final List<Integer> bufferForConcat = new ArrayList<>();

	public <T extends WritableIntDfa> T union(T self, SimpleIntDfa other) {
		otherStateToNewLocalState.clear();
		assert self != null;
		assert other != null;

		mergeNodes(self, other, 0, 0);

		return self;
	}

	public <T extends WritableIntDfa> T concat(T self, SimpleIntDfa other) {
		otherStateToNewLocalState.clear();
		bufferForConcat.clear();

		int size = self.size();

		// from self: get all accepting states.
		for (int i = 0; i < size; i++) {
			if (self.accepts(i)) {
				self.reject(i);
				bufferForConcat.add(i);
			}
		}

		for (int i = 0, limit = bufferForConcat.size(); i < limit; i++) {
			int state = bufferForConcat.get(i);
			mergeNodes(self, other, state, 0);
		}

		return self;
	}

	private void mergeNodes(WritableIntDfa self, SimpleIntDfa other, int selfRoot, int otherRoot) {
		long root = combineInts(selfRoot, otherRoot);
		breadthFirstTraversal(root, state -> mergeNodeStep(self, other,  decombineInt1(state), decombineInt2(state)));
	}

	private <T extends WritableIntDfa> List<Long> mergeNodeStep(T self, SimpleIntDfa other, int stateSelf, int stateOther) {
		result.clear();

		final int starSelf = self.step(stateSelf, ANY);
		final int starOther = other.step(stateOther, ANY);
		int selfLabelMax = self.labels(stateSelf); // we mutate self so max nr of edges may change
		final int otherLabelMax = other.labels(stateOther);

		// cimkek atmasolasa itt.
		if (other.accepts(stateOther)) {
			self.accept(stateSelf);
		}

		int selfLabelIdx = 0;
		int otherLabelIdx = 0;

		while (selfLabelIdx < selfLabelMax || otherLabelIdx < otherLabelMax) {
			char selfLabelChar = (selfLabelMax == 0) ? '?' : self.label(stateSelf, min(selfLabelIdx, selfLabelMax - 1));
			char otherLabelChar = (otherLabelMax == 0) ? '?' : other.label(stateOther, min(otherLabelIdx, otherLabelMax - 1));
			if (otherLabelIdx == otherLabelMax || selfLabelIdx < selfLabelMax && selfLabelChar < otherLabelChar) {
				// > csak a self-ben van meg
				if (starOther != -1) {
					int selfTarget = self.target(stateSelf, selfLabelIdx);
					result.add(combineInts(selfTarget, starOther));
				}
				selfLabelIdx++;
			} else if (selfLabelIdx == selfLabelMax || otherLabelIdx < otherLabelMax && selfLabelChar > otherLabelChar) {
				// > csak az other-ben van meg
				int otherTarget = other.target(stateOther, otherLabelIdx);
				assert otherTarget >= 0;
				int newSelfState = (starSelf == -1)
						? otherStateToNewLocalState.computeIfAbsent(otherTarget, __ -> self.newState())
						: self.copyWithOutgoingEdges(starSelf);
				self.connect(stateSelf, otherLabelChar, newSelfState);
				result.add(combineInts(newSelfState, otherTarget));
				otherLabelIdx++;

				selfLabelIdx++; selfLabelMax++; // because we inserted new transition to local
			} else {
				// state is present in both collections
				int selfTarget = self.target(stateSelf, selfLabelIdx);
				int otherTarget = other.target(stateOther, otherLabelIdx);
				result.add(combineInts(selfTarget, otherTarget));
				selfLabelIdx++;
				otherLabelIdx++;
			}
		}

		return result;
	}

	private static long combineInts(int a, int b) {
		return (long) a << 32 | b & 0xFFFFFFFFL;
	}

	private static int decombineInt1(long c) {
		return (int) (c >> 32);
	}

	private static int decombineInt2(long c) {
		return (int) c;
	}
}
