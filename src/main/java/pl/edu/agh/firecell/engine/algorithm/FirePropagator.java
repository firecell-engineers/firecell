package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.Optional;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.MAX_BURNING_TIME;
import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.possibleToGoUp;
import static pl.edu.agh.firecell.model.Material.AIR;
import static pl.edu.agh.firecell.model.Material.WOOD;

public class FirePropagator {

    public boolean computeNewFlammable(Cell oldCell, int newBurningTime){
        return switch (oldCell.material()) {
            case WOOD -> newBurningTime <= MAX_BURNING_TIME;
            case AIR -> true;
        };
    }

    public int computeBurningTime(State oldState, Cell oldCell, Vector3i cellIndex, double newTemperature){
        int newBurningTime = oldCell.burningTime();
        switch (oldCell.material()) {
            case WOOD -> newBurningTime = newWoodBurningTime(oldCell, newTemperature, newBurningTime);
            case AIR -> newBurningTime = newAirBurningTime(oldState, cellIndex);
        }
        return newBurningTime;
    }

    public static Optional<Vector3i> doesNeighbourOnFire(State oldState, Vector3i cellIndex){



        if (oldState.hasCell(NeighbourUtils.north(cellIndex)) &&
                oldState.getCell(NeighbourUtils.north(cellIndex)).burningTime() > 0 &&
                oldState.getCell(NeighbourUtils.north(cellIndex)).remainingFirePillar() > 0 &&
                !possibleToGoUp(oldState, NeighbourUtils.north(cellIndex))) {
            return Optional.of(NeighbourUtils.north(cellIndex));
        }

        if (oldState.hasCell(NeighbourUtils.south(cellIndex)) &&
                oldState.getCell(NeighbourUtils.south(cellIndex)).burningTime() > 0 &&
                oldState.getCell(NeighbourUtils.south(cellIndex)).remainingFirePillar() > 0 &&
                !possibleToGoUp(oldState, NeighbourUtils.south(cellIndex))) {
            return Optional.of(NeighbourUtils.south(cellIndex));
        }

        if (oldState.hasCell(NeighbourUtils.east(cellIndex)) &&
                oldState.getCell(NeighbourUtils.east(cellIndex)).burningTime() > 0 &&
                oldState.getCell(NeighbourUtils.east(cellIndex)).remainingFirePillar() > 0 &&
                !possibleToGoUp(oldState, NeighbourUtils.east(cellIndex))) {
            return Optional.of(NeighbourUtils.east(cellIndex));
        }

        if (oldState.hasCell(NeighbourUtils.west(cellIndex)) &&
                oldState.getCell(NeighbourUtils.west(cellIndex)).burningTime() > 0 &&
                oldState.getCell(NeighbourUtils.west(cellIndex)).remainingFirePillar() > 0 &&
                !possibleToGoUp(oldState, NeighbourUtils.west(cellIndex))) {
            return Optional.of(NeighbourUtils.west(cellIndex));
        }


        return Optional.empty();
    }

    public int computeFirePillar(State oldState, Cell oldCell, Vector3i cellIndex, int currentFirePillar){
        int newRemainingHeightOfFirePillar = currentFirePillar;

        if (!oldCell.material().equals(AIR))
            return newRemainingHeightOfFirePillar;

        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() > 0 &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).flammable() &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1 > 0) {
            newRemainingHeightOfFirePillar = oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1;
        }
        if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() == 0 ||
                !oldState.getCell(NeighbourUtils.down(cellIndex)).flammable()) {
            Optional<Vector3i> neighbourOnFire = doesNeighbourOnFire(oldState, cellIndex);
            if (neighbourOnFire.isPresent() &&
                    !possibleToGoUp(oldState, neighbourOnFire.get()) &&
                    oldState.getCell(neighbourOnFire.get()).remainingFirePillar() - 1 > 0)
            {
                newRemainingHeightOfFirePillar = oldState.getCell(neighbourOnFire.get()).remainingFirePillar() - 1;
            }
        }

        return newRemainingHeightOfFirePillar;
    }

    private int newAirBurningTime(State oldState, Vector3i cellIndex) {
        int newBurningTime;
        newBurningTime = 0;

        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() > 0 &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).flammable() &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1 > 0) {
            newBurningTime++;
        }

        if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() == 0 ||
                !oldState.getCell(NeighbourUtils.down(cellIndex)).flammable()) {

            Optional<Vector3i> neighbourOnFire = doesNeighbourOnFire(oldState, cellIndex);
            if (neighbourOnFire.isPresent() &&
                    !possibleToGoUp(oldState, neighbourOnFire.get()) &&
                    oldState.getCell(neighbourOnFire.get()).remainingFirePillar() - 1 > 0)
            {
                newBurningTime++;
            }
        }
        return newBurningTime;
    }

    private int newWoodBurningTime(Cell oldCell, double newTemperature, int newBurningTime) {
        if (newTemperature > WOOD.autoIgnitionTemperature() &&
                oldCell.burningTime() == 0 &&
                MAX_BURNING_TIME != 0 &&
                oldCell.flammable()) {
            newBurningTime++;
        }
        if (oldCell.burningTime() > 0 && oldCell.burningTime() <= MAX_BURNING_TIME) {
            newBurningTime++;
        }
        if (oldCell.burningTime() > MAX_BURNING_TIME) {
            newBurningTime = 0;
        }
        return newBurningTime;
    }
}
