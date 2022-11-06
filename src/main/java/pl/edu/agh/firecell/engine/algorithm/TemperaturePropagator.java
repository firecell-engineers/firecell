package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.*;
import static pl.edu.agh.firecell.model.Material.AIR;
import static pl.edu.agh.firecell.model.Material.WOOD;

public class TemperaturePropagator {

    private final double deltaTime;

    public TemperaturePropagator(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public double computeConduction(State oldState, Vector3i cellIndex, double currentTemperature) {
        Cell oldCell = oldState.getCell(cellIndex);
        return currentTemperature + deltaTime * calculateAxisDifference(oldState, NeighbourUtils.north(cellIndex), oldCell, NeighbourUtils.south(cellIndex)) +
                calculateAxisDifference(oldState, NeighbourUtils.up(cellIndex), oldCell, NeighbourUtils.down(cellIndex)) +
                calculateAxisDifference(oldState, NeighbourUtils.east(cellIndex), oldCell, NeighbourUtils.west(cellIndex));
    }

    private double calculateAxisDifference(State oldState, Vector3i formerIndex, Cell middleCell, Vector3i furtherIndex) {
        return oldState.hasCell(formerIndex) && oldState.hasCell(furtherIndex) ?
                computeConductivity(oldState.getCell(formerIndex), middleCell,
                        oldState.getCell(furtherIndex),
                        getCoe(oldState.getCell(formerIndex), middleCell),
                        getCoe(oldState.getCell(furtherIndex), middleCell)) :
                0.0;
        // repair, bug at the border cause one of neighbour is not present
    }

    private double getCoe(Cell neighbour, Cell middleCell) {
        double coe = 0.0;
        if (neighbour.material().equals(WOOD) && middleCell.material().equals(WOOD))
            coe = CONDUCTIVITY_COEFFICIENT_WOOD;

        if ((neighbour.material().equals(WOOD) && middleCell.material().equals(AIR)) ||
                (neighbour.material().equals(AIR) && middleCell.material().equals(WOOD)))
            coe = CONDUCTIVITY_COEFFICIENT_WOOD_AIR;

        if ((neighbour.material().equals(AIR) && middleCell.material().equals(AIR)))
            coe = CONDUCTIVITY_COEFFICIENT_AIR;
        return coe;
    }

    private double computeConductivity(Cell former, Cell middle, Cell latter, double conductivityCoeFormer, double conductivityCoeFurther) {
        return -(conductivityCoeFormer * tempDiff(middle, former) + conductivityCoeFurther * tempDiff(middle, latter));
    }

    public double computeConvection(State oldState, Vector3i cellIndex, double currentTemperature) {
        Cell oldCell = oldState.getCell(cellIndex);
        double temperatureDifference = 0;

        try {
            Cell cellUnder = oldState.getCell(NeighbourUtils.down(cellIndex));
            if (cellUnder.material().equals(Material.AIR) && cellUnder.temperature() > oldCell.temperature())
                temperatureDifference += CONVECTION_COEFFICIENT * tempDiffAbs(oldCell, cellUnder);
        } catch (IndexOutOfBoundsException ignored) {
        }

        try {
            Cell cellAbove = oldState.getCell(NeighbourUtils.up(cellIndex));
            if (cellAbove.material().equals(Material.AIR) && cellAbove.temperature() < oldCell.temperature())
                temperatureDifference -= CONVECTION_COEFFICIENT * tempDiffAbs(oldCell, cellAbove);
        } catch (IndexOutOfBoundsException ignored) {
        }
        return currentTemperature + deltaTime * temperatureDifference;
    }

    public double updateTemperatureBasedOnFire(Cell oldCell, double currentTemperature) {
        return oldCell.burningTime() > 0 && oldCell.flammable() ? oldCell.material().getBurningTemperature() : currentTemperature;
    }

    private static double tempDiffAbs(Cell cellOne, Cell cellTwo) {
        return Math.abs(tempDiff(cellOne, cellTwo));
    }

    private static double tempDiff(Cell cellOne, Cell cellTwo) {
        return cellOne.temperature() - cellTwo.temperature();
    }

}
