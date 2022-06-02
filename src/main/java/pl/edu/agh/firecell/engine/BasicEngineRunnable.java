package pl.edu.agh.firecell.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.engine.algorithm.Algorithm;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.storage.Storage;

public class BasicEngineRunnable implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(BasicEngineRunnable.class);

    private State currentState;
    private int currentStateIndex;
    private Storage storage;
    private Algorithm algorithm;

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
            currentStateIndex ++;
        }
    }

    private State computeNewState(State oldState) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // we have to pass interruption event, since it's cleaned after catching an exception
            Thread.currentThread().interrupt();
        }
        return oldState;
    }
}
