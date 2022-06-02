package pl.edu.agh.firecell.engine;

import pl.edu.agh.firecell.model.SimulationConfig;
import pl.edu.agh.firecell.storage.Storage;

public class BasicEngine implements Engine{

    private final SimulationConfig config;
    private final Storage storage;

    public BasicEngine(SimulationConfig config, Storage storage) {
        this.config = config;
        this.storage = storage;
    }

    @Override
    public void run() {

    }

    @Override
    public void stop() {

    }
}
