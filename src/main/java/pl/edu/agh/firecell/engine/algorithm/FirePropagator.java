package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.Collections;
import java.util.NoSuchElementException;
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
            case WOOD -> newWoodBurningTime(oldCell, newTemperature, oldCell.burningTime());
            case AIR -> newAirBurningTime(oldState, cellIndex);
        };
    }

    public static Stream<Vector3i> doesNeighbourOnFire(State oldState, Vector3i cellIndex){
        return Stream
                .concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X), NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z))
                .filter(neighbourIndex -> oldState.hasCell(neighbourIndex) &&
                        isOnFire(oldState.getCell(neighbourIndex)) &&
                        oldState.getCell(neighbourIndex).remainingFirePillar() > 0 &&
                        !possibleToGoUp(oldState, neighbourIndex));
    }

    public int computeFirePillar(State oldState, Cell oldCell, Vector3i cellIndex, int currentFirePillar){
        int newRemainingHeightOfFirePillar = currentFirePillar;

        if (!oldCell.material().equals(AIR))
            return newRemainingHeightOfFirePillar;

        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                isOnFire(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1 > 0) {
            newRemainingHeightOfFirePillar = Math.max(oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1, currentFirePillar);
        }
        if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                !isOnFire(oldState.getCell(NeighbourUtils.down(cellIndex)))) {
            try {
                newRemainingHeightOfFirePillar = Collections.max(doesNeighbourOnFire(oldState, cellIndex)
                        .filter(neighbourIndex -> !possibleToGoUp(oldState, neighbourIndex) &&
                                oldState.getCell(neighbourIndex).remainingFirePillar() - 1 > 0)
                        .map(neighbourIndex -> oldState.getCell(neighbourIndex).remainingFirePillar())
                        .toList());
            } catch (NoSuchElementException ignored) {}
        }

        return newRemainingHeightOfFirePillar;
    }

    private int newAirBurningTime(State oldState, Vector3i cellIndex) {
        int newBurningTime;
        newBurningTime = 0;

        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                isOnFire(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1 > 0) {
            newBurningTime++;
        }

        if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                !isOnFire(oldState.getCell(NeighbourUtils.down(cellIndex))))
        {
            // cell under is not on fire or not present
            if(doesNeighbourOnFire(oldState, cellIndex)
                    .anyMatch(neighbourIndex -> !possibleToGoUp(oldState, neighbourIndex) &&
                            oldState.getCell(neighbourIndex).remainingFirePillar() - 1 > 0))
                newBurningTime = 1;
        }
        return newBurningTime;
    }

    private int newWoodBurningTime(Cell oldCell, double newTemperature, int currenBurningTime) {
        if (newTemperature > WOOD.autoIgnitionTemperature() &&
                oldCell.burningTime() == 0 &&
                MAX_BURNING_TIME != 0 &&
                oldCell.flammable()) {
            currenBurningTime++;
        }
        if (oldCell.burningTime() > 0 && oldCell.burningTime() <= MAX_BURNING_TIME) {
            currenBurningTime++;
        }
        return currenBurningTime;
    }
}
