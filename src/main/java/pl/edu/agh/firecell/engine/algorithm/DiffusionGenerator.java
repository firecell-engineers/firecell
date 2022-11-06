package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.State;

public class DiffusionGenerator {
    private final double deltaTime;
    public DiffusionGenerator(double deltaTime){
        this.deltaTime = deltaTime;
    }

    public double oxygenUpdate(State oldState, Vector3i cellIndex) {
        return 0.0;
    }

    public double smokeUpdate(State oldState, Vector3i cellIndex) {
        return 0.0;
    }

}
