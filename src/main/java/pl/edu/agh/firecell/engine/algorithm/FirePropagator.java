package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.stream.Stream;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.*;
import static pl.edu.agh.firecell.model.Material.AIR;
import static pl.edu.agh.firecell.model.Material.WOOD;

public class FirePropagator {

    public boolean computeNewFlammable(Cell oldCell, int newBurningTime){
        return switch (oldCell.material()) {
            case WOOD -> newBurningTime < MAX_BURNING_TIME;
            case AIR -> true;
        };
    }

    public int computeBurningTime(State oldState, Cell oldCell, Vector3i cellIndex, double newTemperature){
        return switch (oldCell.material()) {
            case WOOD -> computeBurningTimeWood(oldCell, newTemperature, oldCell.burningTime());
            case AIR -> computeBurningTimeAir(oldState, cellIndex);
        };
    }

    public int computeFirePillar(State oldState, Cell oldCell, Vector3i cellIndex, int currentFirePillar){
        if (!oldCell.material().equals(AIR))
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
        if(horizontalNeighbourFirePillar>currentFirePillar){
            neighbourFirePillar = horizontalNeighbourFirePillar - 1;
        }

        return Math.max(downFirePillar, neighbourFirePillar);
    }

    public static Stream<Vector3i> getBurningHorizontalNeighbours(State oldState, Vector3i cellIndex){
        return Stream
                .concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X), NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z))
                .filter(neighbourIndex -> oldState.hasCell(neighbourIndex) &&
                        isCellBurning(oldState.getCell(neighbourIndex)) &&
                        oldState.getCell(neighbourIndex).remainingFirePillar() > 0 &&
                        !isUpNeighbourAir(oldState, neighbourIndex));
    }

    private int computeBurningTimeAir(State oldState, Vector3i cellIndex) {
        int newBurningTime = 0;

        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1 > 0) {
            newBurningTime++;
        }

        if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                !isCellBurning(oldState.getCell(NeighbourUtils.down(cellIndex))))
        {
            // cell under is not on fire or not present
            if(getBurningHorizontalNeighbours(oldState, cellIndex)
                    .anyMatch(neighbourIndex -> !isUpNeighbourAir(oldState, neighbourIndex) &&
                            oldState.getCell(neighbourIndex).remainingFirePillar() - 1 > 0))
                newBurningTime = 1;
        }
        return newBurningTime;
    }

    private int computeBurningTimeWood(Cell oldCell, double newTemperature, int currenBurningTime) {
        int newBurningTime = currenBurningTime;
        if (newTemperature > WOOD.autoIgnitionTemperature() &&
                oldCell.burningTime() == 0 &&
                MAX_BURNING_TIME != 0 &&
                oldCell.flammable()) {
            newBurningTime++;
        }
        if (oldCell.burningTime() > 0 && oldCell.burningTime() <= MAX_BURNING_TIME) {
            newBurningTime++;
        }
        return newBurningTime;
    }

    private static boolean isCellBurning(Cell cell) {
        return cell.flammable() && cell.burningTime() > 0;
    }
}
