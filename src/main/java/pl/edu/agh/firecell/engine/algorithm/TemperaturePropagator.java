package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.*;
import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.MAX_BURNING_TIME;
import static pl.edu.agh.firecell.engine.algorithm.FirePropagator.getBurningHorizontalNeighbours;
import static pl.edu.agh.firecell.model.Material.AIR;
import static pl.edu.agh.firecell.model.Material.WOOD;

public class TemperaturePropagator {

    private final double deltaTime;

    public TemperaturePropagator(double deltaTime){
        this.deltaTime = deltaTime;
    }

    public double computeNewTemperature(State oldState, Vector3i cellIndex, Cell oldCell) {
        return oldCell.temperature() + deltaTime *
                switch (oldCell.material().getMatterState()) {
                    case SOLID -> computeConduction(oldState, oldCell, cellIndex);
                    case FLUID -> computeConvection(oldState, oldCell, cellIndex);
        };
    }

    public double updateTemperatureBasedOnFire(State oldState, Cell oldCell, Vector3i cellIndex, double currentTemperature, int burningTime){

        double newTemperature = currentTemperature;

        switch (oldCell.material()) {
            case WOOD -> newTemperature = newWoodTemperature(oldCell, currentTemperature, burningTime, newTemperature);
            case AIR -> newTemperature = newAirTemperature(oldState, oldCell, cellIndex, currentTemperature);
        }
        return newTemperature;
    }

    private double newAirTemperature(State oldState, Cell oldCell, Vector3i cellIndex, double currentTemperature) {
        double newTemperature = currentTemperature;
        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() > 0 &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).flammable() &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).remainingFirePillar() - 1 > 0) {
            newTemperature = Math.max(currentTemperature, AIR.getBurningTemperature());
        }

        if (!oldState.hasCell(NeighbourUtils.down(cellIndex)) ||
                oldState.getCell(NeighbourUtils.down(cellIndex)).burningTime() == 0 ||
                !oldState.getCell(NeighbourUtils.down(cellIndex)).flammable()) {

            if (getBurningHorizontalNeighbours(oldState, cellIndex)
                    .anyMatch(neighbourIndex -> !isUpNeighbourAir(oldState, neighbourIndex) &&
                            oldState.getCell(neighbourIndex).remainingFirePillar() - 1 > 0))
            {
                newTemperature = Math.max(newTemperature, 600);
            }
        }

        // keep air on fire
        if (oldCell.burningTime() > 0) {
            newTemperature = Math.max(currentTemperature, 600);
        }
        return newTemperature;
    }

    private double newWoodTemperature(Cell oldCell, double currentTemperature, int burningTime, double newTemperature) {
        if (currentTemperature > WOOD.autoIgnitionTemperature() &&
                oldCell.burningTime() == 0 &&
                MAX_BURNING_TIME != 0 &&
                oldCell.flammable()) {
            //set wood on fire
            newTemperature = WOOD.getBurningTemperature();
        } else if (burningTime > 0 && oldCell.burningTime() <= MAX_BURNING_TIME) {
            //keep wood on fire
            newTemperature = Math.max(WOOD.getBurningTemperature(), currentTemperature);
        }
        return newTemperature;
    }

    private double computeConduction(State oldState, Cell oldCell, Vector3i cellIndex) {
        return calculateAxisDifference(oldState, NeighbourUtils.north(cellIndex), oldCell, NeighbourUtils.south(cellIndex)) +
                calculateAxisDifference(oldState, NeighbourUtils.up(cellIndex), oldCell, NeighbourUtils.down(cellIndex)) +
                calculateAxisDifference(oldState, NeighbourUtils.east(cellIndex), oldCell, NeighbourUtils.west(cellIndex));
    }

    private double calculateAxisDifference(State oldState, Vector3i formerIndex, Cell middleCell, Vector3i furtherIndex) {

        double result = 0.0;

        if (!oldState.hasCell(formerIndex) && !oldState.hasCell(furtherIndex))
            return result;

        if(oldState.hasCell(formerIndex) && oldState.hasCell(furtherIndex)){
            Cell formerCell = oldState.getCell(formerIndex);
            Cell furtherCell = oldState.getCell(furtherIndex);
            if (formerCell.isSolid() && formerCell.isSolid())
                result = computeConductivity(formerCell, middleCell, furtherCell);
            else if (formerCell.isSolid())
                result = computeConductivity(formerCell, middleCell, new Cell(middleCell));
            else if (furtherCell.isSolid())
                result = computeConductivity(new Cell(middleCell), middleCell, furtherCell);
        } else if(oldState.hasCell(formerIndex) && oldState.getCell(formerIndex).isSolid())
            result = computeConductivity(oldState.getCell(formerIndex), middleCell, new Cell(middleCell));
        else if(oldState.hasCell(furtherIndex) && oldState.getCell(furtherIndex).isSolid())
            result = computeConductivity(new Cell(middleCell), middleCell, oldState.getCell(furtherIndex));

        return result;
    }

    private double computeConductivity(Cell former, Cell middle, Cell latter) {
        return - CONDUCTIVITY_COEFFICIENT * (tempDiff(middle, former) + tempDiff(middle, latter));
    }

    private static double tempDiffAbs(Cell cellOne, Cell cellTwo) {
        return Math.abs(tempDiff(cellOne, cellTwo));
    }

    private static double tempDiff(Cell cellOne, Cell cellTwo) {
        return cellOne.temperature() - cellTwo.temperature();
    }

    private double computeConvection(State oldState, Cell oldCell, Vector3i cellIndex) {
        double temperatureDifference = 0;

        try {
            Cell cellUnder = oldState.getCell(NeighbourUtils.down(cellIndex));
            if (cellUnder.material().equals(Material.AIR) && cellUnder.temperature() > oldCell.temperature())
                temperatureDifference += CONVECTION_COEFFICIENT * tempDiffAbs(oldCell, cellUnder);

        } catch (IndexOutOfBoundsException ignored) {}

        try {
            Cell cellAbove = oldState.getCell(NeighbourUtils.up(cellIndex));
            if (cellAbove.material().equals(Material.AIR) &&
                    cellAbove.temperature() < oldCell.temperature()) {
                temperatureDifference -= CONVECTION_COEFFICIENT * tempDiffAbs(oldCell, cellAbove);
            }
        } catch (IndexOutOfBoundsException ignored) {}

        return getLetterTRuleTemperatureUpdate(oldState, oldCell, cellIndex, temperatureDifference);
    }

    private double getLetterTRuleTemperatureUpdate(State oldState, Cell oldCell, Vector3i cellIndex, double temperatureDifference) {
        if (!oldState.hasCell(NeighbourUtils.up(cellIndex)) || oldState.getCell(NeighbourUtils.up(cellIndex)).temperature() > 100) {
            // cell above me is ceiling
            try {
                Optional<Cell> northCell = oldState.hasCell(NeighbourUtils.north(cellIndex)) ? Optional.of(oldState.getCell(NeighbourUtils.north(cellIndex))) : Optional.empty();
                Optional<Cell> southCell = oldState.hasCell(NeighbourUtils.south(cellIndex)) ? Optional.of(oldState.getCell(NeighbourUtils.south(cellIndex))) : Optional.empty();
                Optional<Cell> eastCell = oldState.hasCell(NeighbourUtils.east(cellIndex)) ? Optional.of(oldState.getCell(NeighbourUtils.east(cellIndex))) : Optional.empty();
                Optional<Cell> westCell = oldState.hasCell(NeighbourUtils.west(cellIndex)) ? Optional.of(oldState.getCell(NeighbourUtils.west(cellIndex))) : Optional.empty();

                List<Optional<Cell>> neighbours = new ArrayList<>(List.of(northCell, southCell, eastCell, westCell));
                final int[] counter = {0, 0};

                neighbours.stream()
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(cell -> {
                            if (cell.temperature() > oldCell.temperature())
                                counter[0]++;
                            else
                                counter[1]++;
                        });

                // give to other
                temperatureDifference -= neighbours.stream()
                        .filter(Optional::isPresent)
                        .filter(cell -> cell.get().temperature() < oldCell.temperature())
                        .mapToDouble(cell -> tempDiffAbs(oldCell, cell.get()) / counter[1])
                        .sum();

                // take from other
                temperatureDifference += neighbours.stream()
                        .filter(Optional::isPresent)
                        .filter(cell -> cell.get().temperature() > oldCell.temperature())
                        .mapToDouble(cell -> tempDiffAbs(oldCell, cell.get()) / counter[0])
                        .sum();

            } catch (IndexOutOfBoundsException ignored){}
        }
        return temperatureDifference;
    }

}
