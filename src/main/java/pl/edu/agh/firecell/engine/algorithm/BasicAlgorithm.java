package pl.edu.agh.firecell.engine.algorithm;

import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Index;
import pl.edu.agh.firecell.model.State;

public class BasicAlgorithm implements Algorithm {

    @Override
    public Cell compute(State oldState, Index cellIndex) {
        return new Cell(0);
    }
}
