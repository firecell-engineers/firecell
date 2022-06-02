package pl.edu.agh.firecell.engine;

import pl.edu.agh.firecell.model.SimulationConfig;

public interface Engine {
    void run(SimulationConfig config);
    void stop();
}
