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
    private final OxygenPropagator oxygenPropagator;

    public BasicAlgorithm(double deltaTime) throws ConductionCoefficientException {
        this.temperaturePropagator = new TemperaturePropagator(deltaTime);
        this.firePropagator = new FirePropagator();
        this.diffusionGenerator = new DiffusionGenerator(deltaTime);
        this.smokePropagator = new SmokePropagator(deltaTime);
        this.oxygenPropagator = new OxygenPropagator(deltaTime);
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);

        // Temperature propagation
        double newTemperature = temperaturePropagator.computeConduction(oldState, cellIndex, oldCell.temperature());
        if (oldCell.material().getMatterState().equals(MatterState.FLUID))
            newTemperature = temperaturePropagator.computeConvection(oldState, cellIndex, newTemperature);

        // Fire propagation
        int newRemainingFirePillar = firePropagator.computeFirePillar(oldState, oldCell, cellIndex, oldCell.remainingFirePillar());
        int newBurningTime = firePropagator.computeBurningTime(oldState, oldCell, cellIndex, newTemperature);
        boolean newFlammable = firePropagator.computeNewFlammable(oldState, cellIndex, newBurningTime);
        newTemperature = temperaturePropagator.updateTemperatureBasedOnFire(oldCell, newTemperature);

        // Smoke propagation
        double newSmokeIndicator = smokePropagator.computeNewSmokeIndicator(oldState, cellIndex, oldCell);

        // Diffusion
        if (oldCell.isFluid())
            newSmokeIndicator = diffusionGenerator.smokeUpdate(oldState, cellIndex, newSmokeIndicator);
        newTemperature = diffusionGenerator.temperatureUpdate(oldState, cellIndex, newTemperature);
        double newOxygenLevel = oxygenPropagator.makeUseOfOxygen(oldState, cellIndex, oldCell.oxygenLevel());
        newOxygenLevel = diffusionGenerator.oxygenUpdate(oldState, cellIndex, newOxygenLevel);

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                oldCell.material(),
                newRemainingFirePillar,
                newSmokeIndicator,
                newOxygenLevel
        );
    }

}
