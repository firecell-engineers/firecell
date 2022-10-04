package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.Optional;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemperatureCalculation temperatureCalculation;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 0.2;
    public static final int MAX_BURNING_TIME = 50;


    public BasicAlgorithm(double deltaTime) {
        this.temperatureCalculation = new TemperatureCalculation(deltaTime);
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);

        double newTemperature = temperatureCalculation.computeNewTemperature(oldState, cellIndex, oldCell);
        boolean newFlammable = oldCell.flammable();
        int newBurningTime = oldCell.burningTime();
        int newRemainingHeightOfFirePillar = oldCell.remainingHeightOfFirePillar();
        boolean newPossibleToGoUp = oldCell.possibleToGoUp();
        boolean isBurned = false;

        // should be calculated only once
        newPossibleToGoUp = isPossibleToGoUp(oldState, cellIndex, newPossibleToGoUp);

        // wood section
        switch (oldCell.material()) {
            case WOOD -> {
                //set wood on fire
                if (newTemperature > Material.WOOD.autoIgnitionTemperature() && newBurningTime == 0 && MAX_BURNING_TIME != 0 && newFlammable) {
                    newBurningTime++;
                    newTemperature = Material.WOOD.getBurningTemperature();
                }
                //keep wood on fire
                if (newBurningTime > 0 && newBurningTime <= MAX_BURNING_TIME) {
                    newBurningTime++;
                    newTemperature = Math.max(Material.WOOD.getBurningTemperature(), newTemperature);
                }
                //wood is carbonated
                if (newBurningTime > MAX_BURNING_TIME) {
                    isBurned = true;
                    newFlammable = false;
                    newBurningTime = 0;
                }
            }
            case AIR -> {
                newBurningTime = 0;
                try {
                    Cell underCell = oldState.getCell(NeighbourUtils.down(cellIndex));
                    // set air into fire
                    if (underCell.burningTime() > 0 && underCell.flammable() && underCell.remainingHeightOfFirePillar() - 1 > 0) {
                        newRemainingHeightOfFirePillar = underCell.remainingHeightOfFirePillar() - 1;
                        // Rethink it
                        newTemperature = Math.max(newTemperature, Material.AIR.getBurningTemperature());
                        newBurningTime++;
                    }
                } catch (IndexOutOfBoundsException ignored) {}
                if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                        oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() == 0 ||
                        !oldState.getCell(NeighbourUtils.down(cellIndex)).flammable()) {

                    Optional<Cell> neighbourOnFire = doesNeighbourOnFire(oldState, cellIndex);
                    if (neighbourOnFire.isPresent() &&
                            !neighbourOnFire.get().possibleToGoUp() &&
                            neighbourOnFire.get().remainingHeightOfFirePillar() - 1 > 0)
                    {
                        newRemainingHeightOfFirePillar = neighbourOnFire.get().remainingHeightOfFirePillar() - 1;
                        newTemperature = Math.max(newTemperature, 600);
                        newBurningTime++;
                    } else { // there is no neighbour on fire
                    }
                }

                // keep air on fire
                if (oldCell.burningTime() > 0) {
                    newTemperature = Math.max(newTemperature, 600);
                }
            }
        }

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                oldCell.material(),
                newRemainingHeightOfFirePillar,
                newPossibleToGoUp
        );
    }

    private boolean isPossibleToGoUp(State oldState, Vector3i cellIndex, boolean newPossibleToGoUp) {
        try {
            if (oldState.getCell(NeighbourUtils.up(cellIndex)).material().getMatterState().equals(MatterState.SOLID)) {
                newPossibleToGoUp = false;
            }
        } catch (IndexOutOfBoundsException ignored){
            newPossibleToGoUp = false;
        }
        return newPossibleToGoUp;
    }

    private Optional<Cell> doesNeighbourOnFire(State oldState, Vector3i cellIndex){
        try {
            Cell northCell = oldState.getCell(NeighbourUtils.north(cellIndex));
            Cell southCell = oldState.getCell(NeighbourUtils.south(cellIndex));
            Cell westCell = oldState.getCell(NeighbourUtils.west(cellIndex));
            Cell eastCell = oldState.getCell(NeighbourUtils.east(cellIndex));
            if (doesHorizontalFire(northCell))
                return Optional.of(northCell);
            if (doesHorizontalFire(southCell))
                return Optional.of(southCell);
            if (doesHorizontalFire(westCell))
                return Optional.of(westCell);
            if (doesHorizontalFire(eastCell))
                return Optional.of(eastCell);
        } catch (IndexOutOfBoundsException ignored) {}
        return Optional.empty();
    }

    private boolean doesHorizontalFire(Cell cell){
        return cell.burningTime() > 0 && cell.remainingHeightOfFirePillar() > 0 && !cell.possibleToGoUp();
    }

}
