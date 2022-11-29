package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.stream.Stream;


public class FirePropagator {

    private static final int MAX_BURNING_TIME = 50;
    // Required time period to set on fire neighbour cell with temperature
    // higher than ignition temperature
    private static final int REQUIRED_TIME = 25;

    public boolean computeNewFlammable(Cell oldCell, int newBurningTime) {
        return switch (oldCell.material()) {
            case WOOD -> newBurningTime < MAX_BURNING_TIME;
            case AIR -> true;
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
                AlgorithmUtils.isCellBurning(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() > 1) {
            downFirePillar = oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1;
        }

        // from neighbour
        int neighbourFirePillar = 0;
        int horizontalNeighbourFirePillar = AlgorithmUtils.getBurningHorizontalNeighbours(oldState, cellIndex)
                .filter(neighbourIndex -> !AlgorithmUtils.isUpNeighbourAir(oldState, neighbourIndex) &&
                        oldState.getCell(neighbourIndex).remainingFirePillar() - 1 > 0)
                .map(neighbourIndex -> oldState.getCell(neighbourIndex).remainingFirePillar())
                .max(Integer::compareTo)
                .orElse(0);
        if (horizontalNeighbourFirePillar > currentFirePillar) {
            neighbourFirePillar = horizontalNeighbourFirePillar - 1;
        }

        return Math.max(downFirePillar, neighbourFirePillar);
    }



    private int computeBurningTimeWood(State oldState, Vector3i cellIndex, double newTemperature, int currenBurningTime) {
        Cell oldCell = oldState.getCell(cellIndex);

        if (oldCell.burningTime() == 0 && MAX_BURNING_TIME != 0 && oldCell.flammable()) {

            if (newTemperature > Material.WOOD.autoIgnitionTemperature()) {
                return 1;

            } else if (shouldIgniteFromNeighbour(oldState, cellIndex)
                    && newTemperature > Material.WOOD.ignitionTemperature()) {
                return 1;
            }
        }

        if (oldCell.burningTime() > 0 && oldCell.burningTime() <= MAX_BURNING_TIME) {
            return currenBurningTime + 1;
        }
        return currenBurningTime;
    }

    private boolean shouldIgniteFromNeighbour(State oldState, Vector3i cellIndex) {
        return Stream.concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X),
                        NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z))
                .filter(oldState::hasCell)
                .map(oldState::getCell)
                .anyMatch(cell -> cell.burningTime() > REQUIRED_TIME);
    }
}
