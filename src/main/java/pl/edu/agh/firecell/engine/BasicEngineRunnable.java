package pl.edu.agh.firecell.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.engine.algorithm.Algorithm;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;
import pl.edu.agh.firecell.storage.Storage;

import java.util.List;
import java.util.stream.IntStream;

public class BasicEngineRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(BasicEngineRunnable.class);

    private State currentState;
    private int currentStateIndex;
    private final Storage storage;
    private final Algorithm algorithm;

    public BasicEngineRunnable(State initialState, Storage storage, Algorithm algorithm) {
        this.currentState = initialState;
        this.storage = storage;
        this.algorithm = algorithm;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            storage.putState(currentState, currentStateIndex);
            currentState = computeNewState(currentState);
            currentStateIndex++;
        }
    }

    private State computeNewState(State oldState) {
        logger.debug("Computing state %s".formatted(currentStateIndex));

        List<Cell> newCells = IntStream.range(0, oldState.cells().size())
                .mapToObj(flatIndex -> IndexUtils.expandIndex(flatIndex, oldState.spaceSize()))
                .map(expandedIndex -> algorithm.compute(oldState, expandedIndex))
                .toList();

        return new State(newCells, oldState.spaceSize());
    }
}
