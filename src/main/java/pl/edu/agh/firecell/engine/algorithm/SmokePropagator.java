package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pl.edu.agh.firecell.engine.algorithm.BasicAlgorithm.isCellBurning;

public class SmokePropagator {

    private final double deltaTime;

    public SmokePropagator(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public double computeNewSmokeIndicator(State oldState, Vector3i cellIndex, Cell oldCell) {
        int smokeFromFire = 0;
        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).isSolid()) {
            smokeFromFire = oldState.getCell(NeighbourUtils.down(cellIndex)).material().smokeCoe();
        }
        Optional<Cell> northCell = Optional.empty();
        Optional<Cell> southCell = Optional.empty();
        Optional<Cell> westCell = Optional.empty();
        Optional<Cell> eastCell = Optional.empty();
        if (oldState.hasCell(NeighbourUtils.west(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.west(cellIndex))) &&
                oldState.getCell(NeighbourUtils.west(cellIndex)).isSolid()) {
            westCell = Optional.of(oldState.getCell(NeighbourUtils.west(cellIndex)));
        }
        if (oldState.hasCell(NeighbourUtils.east(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.east(cellIndex))) &&
                oldState.getCell(NeighbourUtils.east(cellIndex)).isSolid()) {
            eastCell = Optional.of(oldState.getCell(NeighbourUtils.east(cellIndex)));
        }
        if (oldState.hasCell(NeighbourUtils.north(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.north(cellIndex))) &&
                oldState.getCell(NeighbourUtils.north(cellIndex)).isSolid()) {
            northCell = Optional.of(oldState.getCell(NeighbourUtils.north(cellIndex)));
        }
        if (oldState.hasCell(NeighbourUtils.south(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.south(cellIndex))) &&
                oldState.getCell(NeighbourUtils.south(cellIndex)).isSolid()) {
            southCell = Optional.of(oldState.getCell(NeighbourUtils.south(cellIndex)));
        }
        if (southCell.isPresent()) smokeFromFire += southCell.get().material().smokeCoe() / 4;
        if (northCell.isPresent()) smokeFromFire += northCell.get().material().smokeCoe() / 4;
        if (westCell.isPresent()) smokeFromFire += westCell.get().material().smokeCoe() / 4;
        if (eastCell.isPresent()) smokeFromFire += eastCell.get().material().smokeCoe() / 4;

        double smokeDifference = getSmokeIndicatorDifference(oldCell, oldState, cellIndex);
        double deviation = 0.0005;
        if (Math.min(oldCell.smokeIndicator() + smokeDifference + smokeFromFire, 100) < deviation)
            return 0.0;
        return Math.min(oldCell.smokeIndicator() + smokeDifference + smokeFromFire, 100);
    }

    private double getSmokeIndicatorDifference(Cell oldCell, State oldState, Vector3i cellIndex) {
        _Double valueOfSmokeDuringComputing = new _Double(oldCell.smokeIndicator());
        return NeighbourUtils.neighboursStream(cellIndex)
                .filter(oldState::hasCell)
                .filter(neighbourIndex -> oldState.getCell(neighbourIndex).isFluid())
                .map(neighbourIndex -> {
                    if (NeighbourUtils.up(cellIndex).equals(neighbourIndex)) {
                        double diff = -Math.min(oldCell.smokeIndicator(), 100 - oldState.getCell(neighbourIndex).smokeIndicator());
                        valueOfSmokeDuringComputing.v += diff;
                        return diff;
                    }
                    if (NeighbourUtils.down(cellIndex).equals(neighbourIndex)) {
                        double diff = Math.min(oldState.getCell(neighbourIndex).smokeIndicator(),
                                100 - oldCell.smokeIndicator());
                        valueOfSmokeDuringComputing.v += diff;
                        return diff;
                    }
                    int numberOfNeighboursMine = numberOfHorizontalNeighboursWithSmokeCapacity(oldState, cellIndex) + 1;
                    int numberOfNeighboursHim = numberOfHorizontalNeighboursWithSmokeCapacity(oldState, neighbourIndex) + 1;
                    double diff = 0.0;
                    if (cellAboveCanNotTakeSmoke(oldState, cellIndex)) {
                        diff -= Math.min((100 - oldState.getCell(neighbourIndex).smokeIndicator()) / numberOfNeighboursHim,
                                (valueOfSmokeDuringComputing.v) / numberOfNeighboursMine);
                    }
                    if (cellAboveCanNotTakeSmoke(oldState, neighbourIndex)) {
                        diff += Math.min((100 - valueOfSmokeDuringComputing.v) / numberOfNeighboursMine,
                                (oldState.getCell(neighbourIndex).smokeIndicator()) / numberOfNeighboursHim);
                    }
                    return diff;
                }).mapToDouble(Double::doubleValue).sum();
    }

    private boolean cellAboveCanNotTakeSmoke(State state, Vector3i index) {
        Vector3i cellAbove = NeighbourUtils.up(index);
        int smokeLimitValue = 50;
        return (state.hasCell(cellAbove) && state.getCell(cellAbove).smokeIndicator() >= smokeLimitValue) ||
                (state.hasCell(cellAbove) && !state.getCell(cellAbove).isFluid()) ||
                !state.hasCell(cellAbove);
    }

    private int numberOfHorizontalNeighboursWithSmokeCapacity(State state, Vector3i cellIndex) {
        List<Cell> res = new ArrayList<>();
        NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X)
                .filter(state::hasCell)
                .filter(index -> state.getCell(index).smokeIndicator() < 100)
                .forEach(index -> res.add(state.getCell(index)));
        NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z)
                .filter(state::hasCell)
                .filter(index -> state.getCell(index).smokeIndicator() < 100)
                .forEach(index -> res.add(state.getCell(index)));
        return res.size();
    }

}

class _Double {
    public double v;

    public _Double(double v) {
        this.v = v;
    }
}