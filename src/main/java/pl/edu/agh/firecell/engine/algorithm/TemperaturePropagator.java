package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
        if (oldState.hasCell(formerIndex) && oldState.hasCell(furtherIndex)) {
            return computeConductivity(oldState.getCell(formerIndex), middleCell,
                    oldState.getCell(furtherIndex),
                    getCoe(oldState.getCell(formerIndex), middleCell),
                    getCoe(oldState.getCell(furtherIndex), middleCell));
        }
        if (!oldState.hasCell(formerIndex) && oldState.hasCell(furtherIndex)) {
            Cell tmp = new Cell(middleCell);
            return computeConductivity(tmp, middleCell, oldState.getCell(furtherIndex),
                    getCoe(tmp, middleCell),
                    getCoe(oldState.getCell(furtherIndex), middleCell));
        }
        if (oldState.hasCell(formerIndex) && !oldState.hasCell(furtherIndex)) {
            Cell tmp = new Cell(middleCell);
            return computeConductivity(oldState.getCell(formerIndex), middleCell, tmp,
                    getCoe(oldState.getCell(formerIndex), middleCell),
                    getCoe(tmp, middleCell));
        }
        return 0.0;
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
        if (oldState.hasCell(NeighbourUtils.up(cellIndex))) {
            Cell cellAbove = oldState.getCell(NeighbourUtils.up(cellIndex));
            if (cellAbove.isFluid() && cellAbove.temperature() < currentTemperature) {
                // I want to give some temperature to up
                double difference = tempDiffAbs(oldCell, cellAbove);
                double coe = (Math.atan((difference/5)-5)/Math.PI)+(Math.PI/2)-1;
                coe = coe<0.05?0.0:coe;
                temperatureDifference -= coe*0.9*oldCell.temperature();
            }
        }
        if (oldState.hasCell(NeighbourUtils.down(cellIndex))) {
            Cell cellUnder = oldState.getCell(NeighbourUtils.down(cellIndex));
            if (cellUnder.isFluid() && cellUnder.temperature() > currentTemperature) {
                // I want to get some temperature from down
                double difference = tempDiffAbs(oldCell, cellUnder);
                double coe = (Math.atan((difference/5)-5)/Math.PI)+(Math.PI/2)-1;
                coe = coe<0.05?0.0:coe;
                temperatureDifference += coe*0.9*cellUnder.temperature();
            }
        }

        final double temperatureDuringComputingFinal = oldCell.temperature() + temperatureDifference;

        double neighbourValue = Stream.concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z),
                NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X))
                .filter(oldState::hasCell)
                .filter(index -> oldState.getCell(index).isFluid())
                .mapToDouble(neighbourIndex -> {
                    int numberOfNeighboursMine = numberOfHorizontalNeighboursWithTemperatureCapacity(oldState, cellIndex) + 1;
                    int numberOfNeighboursHim = numberOfHorizontalNeighboursWithTemperatureCapacity(oldState, neighbourIndex) + 1;
                    double diff = 0.0;
                    if (cellAboveCanNotTakeMoreTemperature(oldState, cellIndex)) {
                        diff -= (temperatureDuringComputingFinal / numberOfNeighboursMine);
                    }
                    if (cellAboveCanNotTakeMoreTemperature(oldState, neighbourIndex)) {
                        diff += (oldState.getCell(neighbourIndex).temperature()) / numberOfNeighboursHim;
                    }
                    return diff;
                }).sum();

        return currentTemperature + deltaTime * (temperatureDifference+neighbourValue);
    }

    private boolean cellAboveCanNotTakeMoreTemperature(State state, Vector3i index) {
        Vector3i cellAbove = NeighbourUtils.up(index);
        return (state.hasCell(cellAbove) && state.hasCell(index) && state.getCell(cellAbove).temperature() >= state.getCell(index).temperature()) ||
                (state.hasCell(cellAbove) && !state.getCell(cellAbove).isFluid()) ||
                !state.hasCell(cellAbove);
    }

    private int numberOfHorizontalNeighboursWithTemperatureCapacity(State state, Vector3i cellIndex) {
        List<Cell> res = new ArrayList<>();
        NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X)
                .filter(state::hasCell)
                .forEach(index -> res.add(state.getCell(index)));
        NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z)
                .filter(state::hasCell)
                .forEach(index -> res.add(state.getCell(index)));
        return res.size();
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
