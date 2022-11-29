package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.exception.ConductionCoefficientException;
import pl.edu.agh.firecell.model.material.MatterState;
import pl.edu.agh.firecell.model.State;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemperaturePropagator temperaturePropagator;
    private final FirePropagator firePropagator;
    private final SmokePropagator smokePropagator;
    private final DiffusionGenerator diffusionGenerator;

    public BasicAlgorithm(double deltaTime) throws ConductionCoefficientException {
        this.temperaturePropagator = new TemperaturePropagator(deltaTime);
        this.firePropagator = new FirePropagator();
        this.diffusionGenerator = new DiffusionGenerator(deltaTime);
        this.smokePropagator = new SmokePropagator(deltaTime);
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);

        double newTemperature = temperaturePropagator.computeConduction(oldState, cellIndex, oldCell.temperature());
        if (oldCell.material().getMatterState().equals(MatterState.FLUID))
            newTemperature = temperaturePropagator.computeConvection(oldState, cellIndex, newTemperature);

        double newSmokeIndicator = smokePropagator.computeNewSmokeIndicator(oldState, cellIndex, oldCell);
        diffusionGenerator.smokeUpdate(oldState, cellIndex);

        int newRemainingFirePillar = firePropagator.computeFirePillar(oldState, oldCell, cellIndex, oldCell.remainingFirePillar());
        int newBurningTime = firePropagator.computeBurningTime(oldState, oldCell, cellIndex, newTemperature);
        boolean newFlammable = firePropagator.computeNewFlammable(oldCell, newBurningTime);
        newTemperature = temperaturePropagator.updateTemperatureBasedOnFire(oldCell, newTemperature);

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                oldCell.material(),
                newRemainingFirePillar,
                newSmokeIndicator
        );
    }

}
