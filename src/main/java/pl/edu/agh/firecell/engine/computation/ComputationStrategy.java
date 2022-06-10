package pl.edu.agh.firecell.engine.computation;

import pl.edu.agh.firecell.engine.algorithm.Algorithm;
import pl.edu.agh.firecell.model.State;

public interface ComputationStrategy {
    State compute(State oldState, Algorithm algorithm);
}
