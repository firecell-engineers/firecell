package pl.edu.agh.firecell.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.engine.algorithm.Algorithm;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Index;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.storage.Storage;

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
            currentStateIndex ++;
        }
    }

    private State computeNewState(State oldState) {

        logger.debug("Computing new state");

        int xLen = oldState.cells().length;
        if(xLen==0){
            logger.info("Compute new state::xLen==0");
            return oldState;
        }
        int yLen = oldState.cells()[0].length;
        if(yLen==0){
            logger.info("Compute new state::yLen==0");
            return oldState;
        }
        int zLen = oldState.cells()[0][0].length;
        if(zLen==0){
            logger.info("Compute new state::zLen==0");
            return oldState;
        }

        State newState = new State(new Cell[xLen][yLen][zLen]);

        for(int x=0;x<xLen;x++){
            for(int y=0;y<yLen;y++){
                for(int z=0;z<zLen;z++){
                    newState.cells()[x][y][z] = algorithm.compute(oldState, new Index(x, y, z));
                }
            }
        }

        logger.debug("Computed new state");

        return newState;
    }
}
