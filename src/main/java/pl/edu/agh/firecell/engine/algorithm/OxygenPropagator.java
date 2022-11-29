package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;

public class OxygenPropagator {

    private final double deltaTime;

    public OxygenPropagator(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public double makeUseOfOxygen(State oldState, Vector3i cellIndex, double currentOxygenLevel) {
        Cell oldCell = oldState.getCell(cellIndex);
        if (BasicAlgorithm.isCellBurning(oldCell) &&
                oldCell.material().equals(Material.AIR) &&
                currentOxygenLevel > 0) {
            return Math.max(0, currentOxygenLevel - (deltaTime * FirePropagator.OXYGEN_USAGE_IN_FIRE));
        }
        return currentOxygenLevel;
    }

}
