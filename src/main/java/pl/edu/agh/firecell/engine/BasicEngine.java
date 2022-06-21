package pl.edu.agh.firecell.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.engine.algorithm.Algorithm;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.storage.StateConsumer;

public class BasicEngine implements Engine {

    private final Logger logger = LoggerFactory.getLogger(BasicEngine.class);

    private final Thread engineThread;

    public BasicEngine(SimulationConfig config, StateConsumer stateConsumer, Algorithm algorithm) {
        var engineRunnable = new BasicEngineRunnable(config.initialState(), stateConsumer, algorithm);
        this.engineThread = new Thread(engineRunnable, "engine-thread");
    }

    @Override
    public void run() {
        engineThread.start();
    }

    @Override
    public void stop() {
        engineThread.interrupt();
        try {
            engineThread.join();
        } catch (InterruptedException e) {
            logger.warn(String.format("Interrupted while joining %s.", engineThread.getName()), e);
        }
    }
}
