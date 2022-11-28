package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.exception.ConductionCoefficientException;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.material.MatterState;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemperaturePropagator temperaturePropagator;
    private final FirePropagator firePropagator;
    private final SmokePropagator smokePropagator;
    private final DiffusionGenerator diffusionGenerator;
    // private final OxygenPropagator oxygenPropagator;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 0.2;
    public static final int MAX_BURNING_TIME = 50;


    public BasicAlgorithm(double deltaTime) throws ConductionCoefficientException {
        this.temperaturePropagator = new TemperaturePropagator(deltaTime);
        this.firePropagator = new FirePropagator();
        this.diffusionGenerator = new DiffusionGenerator(deltaTime);
        this.smokePropagator = new SmokePropagator(deltaTime);
        // this.oxygenPropagator = new OxygenPropagator(deltaTime);
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);
        double newTemperature = oldCell.temperature();
        int newBurningTime;
        int newRemainingFirePillar;
        boolean newFlammable;
        double newOxygenLevel;
        // NOTE: Following method calls are dependent on each other, the order matters.
        // Conduction
        newTemperature = temperaturePropagator.computeConduction(oldState, cellIndex, newTemperature);
        // Convection
        if(oldCell.material().getMatterState().equals(MatterState.FLUID))
            newTemperature = temperaturePropagator.computeConvection(oldState, cellIndex, newTemperature);
        // Smoke update
        double newSmokeIndicator = smokePropagator.computeNewSmokeIndicator(oldState, cellIndex, oldCell);
        // Fire status update
        newRemainingFirePillar = firePropagator.computeFirePillar(oldState, oldCell, cellIndex, oldCell.remainingFirePillar());
        newBurningTime = firePropagator.computeBurningTime(oldState, oldCell, cellIndex, newTemperature);
        newFlammable = firePropagator.computeNewFlammable(oldCell, newBurningTime);
        newTemperature = temperaturePropagator.updateTemperatureBasedOnFire(oldCell, newTemperature);

        // Diffusion update
        if(oldCell.isFluid())
            newSmokeIndicator = diffusionGenerator.smokeUpdate(oldState, cellIndex, newSmokeIndicator);
            newTemperature = diffusionGenerator.temperatureUpdate(oldState, cellIndex, newTemperature);
            // newOxygenLevel = oxygenPropagator.makeUseOfOxygen(oldState, cellIndex, oldCell.oxygenLevel());
            newOxygenLevel = diffusionGenerator.oxygenUpdate(oldState, cellIndex, oldCell.oxygenLevel());

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

    public static boolean isUpNeighbourAir(State state, Vector3i cellIndex) {
        Vector3i upIndex = NeighbourUtils.up(cellIndex);
        return state.hasCell(upIndex) && state.getCell(upIndex).material().equals(Material.AIR);
    }

    public static boolean isCellBurning(Cell cell) {
        return cell.flammable() && cell.burningTime() > 0;
    }
}
