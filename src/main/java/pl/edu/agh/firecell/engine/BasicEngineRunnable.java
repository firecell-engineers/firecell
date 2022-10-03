package pl.edu.agh.firecell.engine;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.engine.algorithm.Algorithm;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;
import pl.edu.agh.firecell.storage.StateConsumer;

import java.util.List;

public class BasicEngineRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(BasicEngineRunnable.class);

    private State currentState;
    private int currentStateIndex = 0;
    private final StateConsumer stateConsumer;
    private final Algorithm algorithm;

    public BasicEngineRunnable(State initialState, StateConsumer stateConsumer, Algorithm algorithm) {
        this.currentState = initialState;
        this.stateConsumer = stateConsumer;
        this.algorithm = algorithm;
    }

    @Override
    public void run() {
        logger.info("Engine run.");
        while (!Thread.currentThread().isInterrupted()) {
            stateConsumer.putState(currentState, currentStateIndex);
            logger.info("Putted new state in storage at index: " + currentStateIndex);
            currentState = computeNewState(currentState);
            currentStateIndex++;
        }
        logger.info("Engine stopped.");
    }

    private State computeNewState(State oldState) {
        logger.debug("Computing state %s".formatted(currentStateIndex));

        List<Cell> newCells = Observable.range(0, oldState.cells().size())
                .concatMapEager(flatIndex -> processCell(flatIndex, oldState))
                .doOnError(throwable -> logger.error("Error during computing new state.", throwable))
                .toList()
                .blockingGet();

        return new State(newCells, oldState.spaceSize());
    }

    private Observable<Cell> processCell(int flatIndex, State state) {
        Vector3i expandedIndex = IndexUtils.expandIndex(flatIndex, state.spaceSize());
        Cell cell = algorithm.compute(state, expandedIndex);
        return Observable.just(cell).subscribeOn(Schedulers.computation());
    }
}
