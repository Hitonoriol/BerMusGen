package hmusgen;

import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;

public class MarkovChain {
	private double[][] tranMatrix;
	private int states;

	public MarkovChain(int states) {
		tranMatrix = new double[states][states + 1];
		this.states = states;
	}

	public void addTransition(int stateA, int stateB) {
		if (stateA >= states || stateB >= states)
			return;

		++tranMatrix[stateA][stateB];
		++tranMatrix[stateA][states];
	}

	public double getTransitionSum(int state) {
		return tranMatrix[state][states];
	}

	public double getTransitionProbability(int stateA, int stateB) {
		double sum = getTransitionSum(stateA);

		if (sum == 0)
			return 0;

		return tranMatrix[stateA][stateB] / sum;
	}

	public void changeNStates(int currentState, int statesToChange, Consumer<Integer> stateConsumer) {
		for (; statesToChange >= 0; --statesToChange) {
			double r = GenMain.random().nextDouble();
			double sum = 0d;

			for (int j = 0; j < this.states; ++j) {
				sum += getTransitionProbability(currentState, j);
				if (r <= sum) {
					stateConsumer.accept(currentState = j);
					break;
				}
			}

		}
	}

	public MutableInt changeState(MutableInt currentState) {
		changeNStates(currentState.intValue(), 1, newState -> currentState.setValue(newState));
		return currentState;
	}

	public int getStates() {
		return states;
	}

	public void dump() {
		final int width = 7;
		GenMain.out("", width);
		for (int i = 0; i < states; ++i)
			GenMain.out("S" + i, width);
		GenMain.out("T", width);
		GenMain.out("\n");

		for (int i = 0; i < states; ++i) {
			GenMain.out("S" + i, width);
			for (int j = 0; j < states + 1; ++j) {
				if (j < states)
					GenMain.out(GenMain.round(getTransitionProbability(i, j)), width);
				else
					GenMain.out(GenMain.round(getTransitionSum(i), 0), width);
			}
			GenMain.out("\n");
		}
	}
}
