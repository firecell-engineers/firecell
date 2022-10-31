package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemperaturePropagator temperaturePropagator;
    private final FirePropagator firePropagator;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 0.2;
    public static final int MAX_BURNING_TIME = 50;


    public BasicAlgorithm(double deltaTime) {
        this.temperaturePropagator = new TemperaturePropagator(deltaTime);
        this.firePropagator = new FirePropagator();
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);
        // NOTE: Following method calls are dependent on each other, the order matters.
        double newTemperature = temperaturePropagator.computeNewTemperature(oldState, cellIndex, oldCell);
        int newBurningTime = firePropagator.computeBurningTime(oldState, oldCell, cellIndex, newTemperature);
        int newRemainingHeightOfFirePillar = firePropagator.computeFirePillar(oldState, oldCell, cellIndex, oldCell.remainingFirePillar());
        boolean newFlammable = firePropagator.computeNewFlammable(oldCell, newBurningTime);
        newTemperature = temperaturePropagator.updateTemperatureBasedOnFire(oldState, oldCell, cellIndex, newTemperature, newBurningTime);
        // boolean isBurned = false; || maybe in future

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                oldCell.material(),
                newRemainingHeightOfFirePillar
        );
    }

    public static boolean isUpNeighbourAir(State state, Vector3i cellIndex){
        Vector3i upIndex = NeighbourUtils.up(cellIndex);
        return state.hasCell(upIndex) && state.getCell(upIndex).material().equals(Material.AIR);
    }

}
