package pl.edu.agh.firecell.core;

import pl.edu.agh.firecell.model.SimulationConfig;

public interface StartSimulationHandler {
    void runNewSimulation(SimulationConfig config, String simulationName);

    void runSavedSimulation(String simulationName);
}
