package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.Optional;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.MAX_BURNING_TIME;
import static pl.edu.agh.firecell.model.Material.AIR;
import static pl.edu.agh.firecell.model.Material.WOOD;

public class FirePropagation {

    public boolean computeNewFlammable(Cell oldCell, int newBurningTime){
        return switch (oldCell.material()) {
            case WOOD -> !(newBurningTime > MAX_BURNING_TIME);
            case AIR -> true;
        };
    }

    public int computeBurningTime(State oldState, Cell oldCell, Vector3i cellIndex, double newTemperature){
        int newBurningTime = oldCell.burningTime();
        switch (oldCell.material()) {
            case WOOD -> {
                if (newTemperature > WOOD.autoIgnitionTemperature() &&
                        oldCell.burningTime() == 0 &&
                        MAX_BURNING_TIME != 0 &&
                        oldCell.flammable()) {
                    newBurningTime ++;
                }
                if (oldCell.burningTime() > 0 && oldCell.burningTime() <= MAX_BURNING_TIME) {
                    newBurningTime++;
                }
                if (oldCell.burningTime() > MAX_BURNING_TIME) {
                    newBurningTime = 0;
                }
            }
            case AIR -> {
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

                    Optional<Cell> neighbourOnFire = doesNeighbourOnFire(oldState, cellIndex);
                    if (neighbourOnFire.isPresent() &&
                            !neighbourOnFire.get().possibleToGoUp() &&
                            neighbourOnFire.get().remainingFirePillar() - 1 > 0)
                    {
                        newBurningTime++;
                    }
                }
            }
        }
        return newBurningTime;
    }

    private static Optional<Cell> doesNeighbourOnFire(State oldState, Vector3i cellIndex){
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

    private static boolean doesHorizontalFire(Cell cell){
        return cell.burningTime() > 0 && cell.remainingFirePillar() > 0 && !cell.possibleToGoUp();
    }

    public int computeFirePillar(State oldState, Cell oldCell, Vector3i cellIndex, int currentFirePillar){
        int newRemainingHeightOfFirePillar = currentFirePillar;
        if(oldCell.material().equals(AIR)){
            if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                    oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() > 0 &&
                    oldState.getCell(NeighbourUtils.down(cellIndex)).flammable() &&
                    oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1 > 0) {
                newRemainingHeightOfFirePillar = oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1;
            }
            if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                    oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() == 0 ||
                    !oldState.getCell(NeighbourUtils.down(cellIndex)).flammable()) {
                Optional<Cell> neighbourOnFire = doesNeighbourOnFire(oldState, cellIndex);
                if (neighbourOnFire.isPresent() &&
                        !neighbourOnFire.get().possibleToGoUp() &&
                        neighbourOnFire.get().remainingFirePillar() - 1 > 0)
                {
                    newRemainingHeightOfFirePillar = neighbourOnFire.get().remainingFirePillar() - 1;
                }
            }
        }
        return newRemainingHeightOfFirePillar;
    }

    public double updateTemperature(State oldState, Cell oldCell, Vector3i cellIndex, double currentTemperature, int burningTime){

        double newTemperature = currentTemperature;

        switch (oldCell.material()) {
            case WOOD -> {
                //set wood on fire
                if (currentTemperature > WOOD.autoIgnitionTemperature() &&
                        oldCell.burningTime() == 0 &&
                        MAX_BURNING_TIME != 0 &&
                        oldCell.flammable()) {
                    newTemperature = WOOD.getBurningTemperature();
                }
                //keep wood on fire
                if (burningTime > 0 && oldCell.burningTime() <= MAX_BURNING_TIME) {
                    newTemperature = Math.max(WOOD.getBurningTemperature(), currentTemperature);
                }
            }

            case AIR -> {

                if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                        oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() > 0 &&
                        oldState.getCell(NeighbourUtils.down(cellIndex)).flammable() &&
                        oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1 > 0) {
                    newTemperature = Math.max(currentTemperature, AIR.getBurningTemperature());
                }

                if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                        oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() == 0 ||
                        !oldState.getCell(NeighbourUtils.down(cellIndex)).flammable()) {

                    Optional<Cell> neighbourOnFire = doesNeighbourOnFire(oldState, cellIndex);
                    if (neighbourOnFire.isPresent() &&
                            !neighbourOnFire.get().possibleToGoUp() &&
                            neighbourOnFire.get().remainingFirePillar() - 1 > 0)
                    {
                        newTemperature = Math.max(newTemperature, 600);
                    }
                }

                // keep air on fire
                if (oldCell.burningTime() > 0) {
                    newTemperature = Math.max(currentTemperature, 600);
                }
            }
        }
        return newTemperature;
    }

}
