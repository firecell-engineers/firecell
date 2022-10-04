package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.CONDUCTIVITY_COEFFICIENT;
import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.CONVECTION_COEFFICIENT;

public class TemperatureCalculation {

    private final double deltaTime;

    public TemperatureCalculation(double deltaTime){
        this.deltaTime = deltaTime;
    }

    public double computeNewTemperature(State oldState, Vector3i cellIndex, Cell oldCell) {
        return oldCell.temperature() + deltaTime *
                switch (oldCell.material().getMatterState()) {
                    case SOLID -> computeConduction(oldState, oldCell, cellIndex);
                    case FLUID -> computeConvection(oldState, oldCell, cellIndex);
        };
    }

    private double computeConduction(State oldState, Cell oldCell, Vector3i cellIndex) {
        double yTemp = 0;
        double zTemp = 0;
        double xTemp = 0;

        try {
            Cell northCell = oldState.getCell(NeighbourUtils.north(cellIndex));
            MatterState northMatter = northCell.material().getMatterState();
            Cell southCell = oldState.getCell(NeighbourUtils.south(cellIndex));
            MatterState southMatter = southCell.material().getMatterState();

            Cell upCell = oldState.getCell(NeighbourUtils.up(cellIndex));
            MatterState upMatter = upCell.material().getMatterState();
            Cell downCell = oldState.getCell(NeighbourUtils.down(cellIndex));
            MatterState downMatter = downCell.material().getMatterState();

            Cell eastCell = oldState.getCell(NeighbourUtils.east(cellIndex));
            MatterState eastMatter = eastCell.material().getMatterState();
            Cell westCell = oldState.getCell(NeighbourUtils.west(cellIndex));
            MatterState westMatter = westCell.material().getMatterState();

            if (northMatter == MatterState.SOLID && southMatter == MatterState.SOLID)
                yTemp = computeConductivity(northCell, oldCell, southCell);
            else if (northMatter == MatterState.SOLID)
                yTemp = computeConductivity(northCell, oldCell, new Cell(oldCell));
            else if (southMatter == MatterState.SOLID)
                yTemp = computeConductivity(new Cell(oldCell), oldCell, southCell);

            if (upMatter == MatterState.SOLID && downMatter == MatterState.SOLID)
                zTemp = computeConductivity(upCell, oldCell, downCell);
            else if (upMatter == MatterState.SOLID)
                zTemp = computeConductivity(upCell, oldCell, new Cell(oldCell));
            else if (downMatter == MatterState.SOLID)
                zTemp = computeConductivity(new Cell(oldCell), oldCell, downCell);

            if (eastMatter == MatterState.SOLID && westMatter == MatterState.SOLID)
                xTemp = computeConductivity(eastCell, oldCell, westCell);
            else if (eastMatter == MatterState.SOLID)
                xTemp = computeConductivity(eastCell, oldCell, new Cell(oldCell));
            else if (westMatter == MatterState.SOLID)
                xTemp = computeConductivity(new Cell(oldCell), oldCell, westCell);

        } catch (IndexOutOfBoundsException ignored) {
            if (oldState.hasCell(NeighbourUtils.up(cellIndex))){
                zTemp = computeConductivity(oldState.getCell(NeighbourUtils.up(cellIndex)), oldCell, new Cell(oldCell));
            }
        }
        return yTemp + zTemp + xTemp;
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

        final int HOTTER = 0;
        final int COLDER = 1;

        double fromDownToMe = 0;
        double fromMeToUp = 0;

        double fromMeToNeighbour = 0;
        double toMeFromNeighbour = 0;

        try {
            Cell cellUnder = oldState.getCell(NeighbourUtils.down(cellIndex));
            if (cellUnder.material().equals(Material.AIR) && cellUnder.temperature() > oldCell.temperature()) {
                fromDownToMe += CONVECTION_COEFFICIENT * tempDiffAbs(oldCell, cellUnder);
            }
        } catch (IndexOutOfBoundsException ignored) {}

        try {
            Cell cellAbove = oldState.getCell(NeighbourUtils.up(cellIndex));
            if (cellAbove.material().equals(Material.AIR)) {
                if(cellAbove.temperature() < oldCell.temperature()) {
                    fromMeToUp -= CONVECTION_COEFFICIENT * tempDiffAbs(oldCell, cellAbove);
                } else {
                    // cell above me is hotter
                }
            }
        } catch (IndexOutOfBoundsException ignored) {}

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
                                counter[HOTTER]++;
                            else
                                counter[COLDER]++;
                        });

                // give to other
                fromMeToNeighbour -= neighbours.stream()
                        .filter(Optional::isPresent)
                        .filter(cell -> cell.get().temperature() < oldCell.temperature())
                        .mapToDouble(cell -> tempDiffAbs(oldCell, cell.get()) / counter[COLDER])
                        .sum();

                // take from other
                fromMeToNeighbour += neighbours.stream()
                        .filter(Optional::isPresent)
                        .filter(cell -> cell.get().temperature() > oldCell.temperature())
                        .mapToDouble(cell -> tempDiffAbs(oldCell, cell.get()) / counter[HOTTER])
                        .sum();

            } catch (IndexOutOfBoundsException ignored){}
        }

        return fromDownToMe + fromMeToUp + fromMeToNeighbour + toMeFromNeighbour;
    }

}
