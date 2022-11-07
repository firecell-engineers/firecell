package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.ConductionCoefficientProvider;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.Optional;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.*;
import static pl.edu.agh.firecell.model.material.Material.AIR;
import static pl.edu.agh.firecell.model.material.Material.WOOD;

public class TemperaturePropagator {

    private final double deltaTime;
    private final ConductionCoefficientProvider conductionCoefficientProvider;

    public TemperaturePropagator(double deltaTime) {
        this.deltaTime = deltaTime;
        this.conductionCoefficientProvider = new ConductionCoefficientProvider();
    }

    public double computeConduction(State oldState, Vector3i cellIndex, double currentTemperature) {
        Cell oldCell = oldState.getCell(cellIndex);
        return currentTemperature + deltaTime * calculateAxisDifference(oldState, NeighbourUtils.north(cellIndex), oldCell, NeighbourUtils.south(cellIndex)) +
                calculateAxisDifference(oldState, NeighbourUtils.up(cellIndex), oldCell, NeighbourUtils.down(cellIndex)) +
                calculateAxisDifference(oldState, NeighbourUtils.east(cellIndex), oldCell, NeighbourUtils.west(cellIndex));
    }

    private double calculateAxisDifference(State oldState, Vector3i formerIndex, Cell middleCell, Vector3i furtherIndex) {
        if(oldState.hasCell(formerIndex) && oldState.hasCell(furtherIndex)){
            return computeConductivity(oldState.getCell(formerIndex), middleCell,
                    oldState.getCell(furtherIndex),
                    getCoe(oldState.getCell(formerIndex), middleCell),
                    getCoe(oldState.getCell(furtherIndex), middleCell));
        }
        if(!oldState.hasCell(formerIndex) && oldState.hasCell(furtherIndex)){
            Cell tmp = new Cell(middleCell);
            return computeConductivity(tmp, middleCell, oldState.getCell(furtherIndex),
                    getCoe(tmp, middleCell),
                    getCoe(oldState.getCell(furtherIndex), middleCell));
        }
        if(oldState.hasCell(formerIndex) && !oldState.hasCell(furtherIndex)){
            Cell tmp = new Cell(middleCell);
            return computeConductivity(oldState.getCell(formerIndex), middleCell, tmp,
                    getCoe(oldState.getCell(formerIndex), middleCell),
                    getCoe(tmp, middleCell));
        }
        return 0.0;
    }

    private double getCoe(Cell neighbour, Cell middleCell) {
        Optional<Double> coe = conductionCoefficientProvider.getConductionCoe(neighbour.material(), middleCell.material());
        return coe.orElse(0.0);
    }

    private double computeConductivity(Cell former, Cell middle, Cell latter, double conductivityCoeFormer, double conductivityCoeFurther) {
        return -(conductivityCoeFormer * tempDiff(middle, former) + conductivityCoeFurther * tempDiff(middle, latter));
    }

    public double computeConvection(State oldState, Vector3i cellIndex, double currentTemperature) {
        Cell oldCell = oldState.getCell(cellIndex);
        double temperatureDifference = 0;

        try {
            Cell cellUnder = oldState.getCell(NeighbourUtils.down(cellIndex));
            if (cellUnder.isFluid() && cellUnder.temperature() > oldCell.temperature())
                temperatureDifference += CONVECTION_COEFFICIENT * tempDiffAbs(oldCell, cellUnder);
        } catch (IndexOutOfBoundsException ignored) {
        }

        try {
            Cell cellAbove = oldState.getCell(NeighbourUtils.up(cellIndex));
            if (cellAbove.isFluid() && cellAbove.temperature() < oldCell.temperature())
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
