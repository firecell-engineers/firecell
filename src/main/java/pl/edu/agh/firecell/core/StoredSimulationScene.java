package pl.edu.agh.firecell.core;

import pl.edu.agh.firecell.core.io.IOListener;
import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.storage.FileSystemStorage;
import pl.edu.agh.firecell.storage.StateProvider;
import pl.edu.agh.firecell.storage.serialization.BinaryStateSerializer;

import java.io.IOException;
import java.nio.file.Path;

public class StoredSimulationScene extends AbstractSimulationScene {

    private final StateProvider stateProvider;

    protected StoredSimulationScene(SimulationConfig config, Runnable finishSimulationHandler,
                                    IOListener ioListener, float aspectRatio, Path stateStoragePath) throws IOException {
        super(config, finishSimulationHandler, ioListener, aspectRatio);
        stateProvider = new FileSystemStorage(new BinaryStateSerializer(), stateStoragePath);
    }

    @Override
    protected StateProvider getStateProvider() {
        return stateProvider;
    }
}
