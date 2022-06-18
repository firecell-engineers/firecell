package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.stream.DoubleStream;

public class BasicAlgorithm implements Algorithm {

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);

        double yTemp = computeConductivity(
                oldState.getCell(IndexUtils.north(cellIndex)),
                oldCell,
                oldState.getCell(IndexUtils.south(cellIndex)));

        double zTemp = computeConductivity(
                oldState.getCell(IndexUtils.up(cellIndex)),
                oldCell,
                oldState.getCell(IndexUtils.down(cellIndex)));

        double xTemp = computeConductivity(
                oldState.getCell(IndexUtils.east(cellIndex)),
                oldCell,
                oldState.getCell(IndexUtils.west(cellIndex)));

        double averageTemp = DoubleStream.of(yTemp, zTemp, xTemp)
                .average()
                .orElse(oldCell.temperature());

        return new Cell(averageTemp, oldCell.conductivityCoefficient(), 0, false, oldCell.material());
    }

    private double computeConductivity(Cell former, Cell middle, Cell latter) {

        // TODO: put them into class constants and give meaningful names
        double dt = 0.05;
        double gammaPrimN = 0;
        double gammaPrimN1 = 0;

        return middle.temperature() +
                dt * (
                        gammaPrimN * (middle.temperature() - former.temperature()) -
                                gammaPrimN1 * (latter.temperature() - middle.temperature())
                );
    }
}
