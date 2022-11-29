package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.stream.Stream;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.*;

public class FirePropagator {

    // Required time period to set on fire neighbour cell with temperature
    // higher than ignition temperature
    private static final int REQUIRED_TIME = 10;
    // Value how much percentage of oxygen one cell should use
    // it also define how much must be accessible
    public static final double OXYGEN_USAGE_IN_FIRE = 5;

    public boolean computeNewFlammable(State oldState, Vector3i cellIndex, int newBurningTime) {
        Cell oldCell = oldState.getCell(cellIndex);
        return switch (oldCell.material()) {
            case WOOD -> newBurningTime < MAX_BURNING_TIME && haveAccessToOxygen(oldState, cellIndex);
            case AIR -> oldCell.oxygenLevel() > 0;
            case CELLULAR_CONCRETE -> false;
        };
    }

    public int computeBurningTime(State oldState, Cell oldCell, Vector3i cellIndex, double newTemperature) {
        return switch (oldCell.material()) {
            case WOOD -> computeBurningTimeWood(oldState, cellIndex, newTemperature, oldCell.burningTime());
            case AIR -> oldState.getCell(cellIndex).remainingFirePillar() > 0 ? 1 : 0;
            case CELLULAR_CONCRETE -> 0;
        };
    }

    public int computeFirePillar(State oldState, Cell oldCell, Vector3i cellIndex, int currentFirePillar) {
        if (!oldCell.material().equals(Material.AIR))
            return currentFirePillar;

        // from under
        int downFirePillar = 0;
        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() > 1) {
            downFirePillar = oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1;
        }

        // from neighbour
        int neighbourFirePillar = 0;
        int horizontalNeighbourFirePillar = getBurningHorizontalNeighbours(oldState, cellIndex)
                .filter(neighbourIndex -> !isUpNeighbourAir(oldState, neighbourIndex) &&
                        oldState.getCell(neighbourIndex).remainingFirePillar() - 1 > 0)
                .map(neighbourIndex -> oldState.getCell(neighbourIndex).remainingFirePillar())
                .max(Integer::compareTo)
                .orElse(0);
        if (horizontalNeighbourFirePillar > currentFirePillar) {
            neighbourFirePillar = horizontalNeighbourFirePillar - 1;
        }

        return Math.max(downFirePillar, neighbourFirePillar);
    }

    public static Stream<Vector3i> getBurningHorizontalNeighbours(State oldState, Vector3i cellIndex) {
        return Stream
                .concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X), NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z))
                .filter(neighbourIndex -> oldState.hasCell(neighbourIndex) &&
                        isCellBurning(oldState.getCell(neighbourIndex)) &&
                        oldState.getCell(neighbourIndex).remainingFirePillar() > 0 &&
                        !isUpNeighbourAir(oldState, neighbourIndex));
    }

    private int computeBurningTimeWood(State oldState, Vector3i cellIndex, double newTemperature, int currenBurningTime) {
        Cell oldCell = oldState.getCell(cellIndex);
        int newBurningTime = currenBurningTime;
        if (oldCell.burningTime() == 0 &&
                MAX_BURNING_TIME != 0 &&
                oldCell.flammable()) {
            if (newTemperature > Material.WOOD.autoIgnitionTemperature()) {
                newBurningTime++;
            } else if (shouldIgniteFromNeighbour(oldState, cellIndex) &&
                    newTemperature > Material.WOOD.ignitionTemperature()) {
                newBurningTime++;
            }
        }
        if (oldCell.burningTime() > 0 && oldCell.burningTime() <= MAX_BURNING_TIME) {
            newBurningTime++;
        }
        return newBurningTime;
    }

    private boolean shouldIgniteFromNeighbour(State oldState, Vector3i cellIndex) {
        return Stream.concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X),
                        NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z))
                .filter(oldState::hasCell)
                .map(oldState::getCell)
                .anyMatch(cell -> cell.burningTime() > REQUIRED_TIME);
    }

    private boolean haveAccessToOxygen(State oldState, Vector3i cellIndex) {
        double sumOfOxygen = NeighbourUtils.neighboursStream(cellIndex)
                .filter(oldState::hasCell)
                .map(oldState::getCell)
                .filter(cell -> cell.material().equals(Material.AIR))
                .mapToDouble(Cell::oxygenLevel)
                .sum();
        return sumOfOxygen >= OXYGEN_USAGE_IN_FIRE;
    }

    private static boolean isCellBurning(Cell cell) {
        return cell.flammable() && cell.burningTime() > 0;
    }
}
