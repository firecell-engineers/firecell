package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;



public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemperaturePropagator temperatureCalculation;
    private final FirePropagation firePropagation;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 0.2;
    public static final int MAX_BURNING_TIME = 50;


    public BasicAlgorithm(double deltaTime) {
        this.temperatureCalculation = new TemperaturePropagator(deltaTime);
        this.firePropagation = new FirePropagation();
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);
        // follow the order
        double newTemperature = temperatureCalculation.computeNewTemperature(oldState, cellIndex, oldCell);
        int newBurningTime = firePropagation.computeBurningTime(oldState, oldCell, cellIndex, newTemperature);
        int newRemainingHeightOfFirePillar = firePropagation.computeFirePillar(oldState, oldCell, cellIndex, oldCell.remainingFirePillar());
        boolean newFlammable = firePropagation.computeNewFlammable(oldCell, newBurningTime);
        newTemperature = firePropagation.updateTemperature(oldState, oldCell, cellIndex, newTemperature, newBurningTime);
        // boolean isBurned = false; || maybe in future

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                oldCell.material(),
                newRemainingHeightOfFirePillar,
                oldCell.possibleToGoUp()
        );
    }
}
