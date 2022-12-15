package pl.edu.agh.firecell.core;

import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.engine.BasicEngine;
import pl.edu.agh.firecell.engine.Engine;
import pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.model.exception.ConductionCoefficientException;
import pl.edu.agh.firecell.storage.FileSystemStorage;
import pl.edu.agh.firecell.storage.StateProvider;
import pl.edu.agh.firecell.storage.serialization.BinaryStateSerializer;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class SimulationScene extends AbstractSimulationScene {

    private final Engine engine;
    private final FileSystemStorage storage;

    public SimulationScene(SimulationConfig config, Runnable finishSimulationHandler,
                           IOListener ioListener, float aspectRatio, Path stateStoragePath)
            throws IOException, InvalidPathException, IllegalStateException, ConductionCoefficientException {
        super(config, finishSimulationHandler, ioListener, aspectRatio);
        this.storage = new FileSystemStorage(new BinaryStateSerializer(), stateStoragePath);
        this.storage.clearDirectory();
        this.engine = new BasicEngine(config.initialState(), this.storage, new BasicAlgorithm(getStepTime()));
        this.engine.run();
    }

    @Override
    public void dispose() {
        super.dispose();
        engine.stop();
    }

    @Override
    protected StateProvider getStateProvider() {
        return storage;
    }
}
