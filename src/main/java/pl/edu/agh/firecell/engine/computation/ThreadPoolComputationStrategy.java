package pl.edu.agh.firecell.engine.computation;

import pl.edu.agh.firecell.engine.algorithm.Algorithm;
import pl.edu.agh.firecell.model.State;

public class ThreadPoolComputationStrategy implements ComputationStrategy {

    public ThreadPoolComputationStrategy() {

    }

    @Override
    public State compute(State oldState, Algorithm algorithm) {
        return oldState;
    }
}
