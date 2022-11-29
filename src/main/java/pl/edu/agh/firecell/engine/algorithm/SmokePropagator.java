package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SmokePropagator {

    private final double deltaTime;
    // Maximum level of smoke in cell
    private static final int MAX_SMOKE_LEVEL = 100;
    // Value of smoke with which we acknowledge cell is dense covered by smoke
    private static final int SMOKE_LIMIT_VALUE = 50;
    // Due to inaccuracies and zenon's paradox, there is need to set the limit to interpret result as zero
    private static final double DEVIATION = 0.0005;
    // Internal smoke coefficient to improve smoke simulation
    private static final double SMOKE_COEFFICIENT = 2;

    public SmokePropagator(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public double computeNewSmokeIndicator(State oldState, Vector3i cellIndex, Cell oldCell) {
        double smokeFromFire = generateSmoke(oldState, cellIndex);
        double smokeDifference = getSmokeIndicatorDifference(oldState, cellIndex);

        double result = Math.min(oldCell.smokeIndicator() + (smokeDifference + smokeFromFire) * deltaTime * SMOKE_COEFFICIENT, MAX_SMOKE_LEVEL);
        return result < DEVIATION ? 0.0 : result;
    }

    private double generateSmoke(State oldState, Vector3i cellIndex) {
        double smokeFromFire = 0;
        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                AlgorithmUtils.isCellBurning(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).isSolid()) {
            smokeFromFire = oldState.getCell(NeighbourUtils.down(cellIndex)).material().smokeCoe();
        }

        smokeFromFire += Stream.concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X),
                NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z))
                        .filter(index -> oldState.hasCell(index) && (
                                !oldState.hasCell(NeighbourUtils.up(index))
                                        || oldState.getCell(NeighbourUtils.up(index)).isSolid()
                            )
                        )
                        .map(oldState::getCell)
                        .filter(cell -> AlgorithmUtils.isCellBurning(cell)&&cell.isSolid())
                        .mapToDouble(cell -> cell.material().smokeCoe() / 4.0).sum();
        return smokeFromFire;
    }

    private double getSmokeIndicatorDifference(State oldState, Vector3i cellIndex) {
        Cell oldCell = oldState.getCell(cellIndex);
        double valueOfSmokeDuringComputing = oldCell.smokeIndicator();

        // up
        double diffFromAbove = 0;
        if (oldState.hasCell(NeighbourUtils.up(cellIndex)) && oldState.getCell(NeighbourUtils.up(cellIndex)).isFluid()) {
            Cell cellAbove = oldState.getCell(NeighbourUtils.up(cellIndex));
            diffFromAbove = -Math.min(oldCell.smokeIndicator(), MAX_SMOKE_LEVEL - cellAbove.smokeIndicator());
            valueOfSmokeDuringComputing += diffFromAbove;
        }
        // down
        double diffFromDown = 0;
        if (oldState.hasCell(NeighbourUtils.down(cellIndex)) && oldState.getCell(NeighbourUtils.down(cellIndex)).isFluid()) {
            Cell cellUnder = oldState.getCell(NeighbourUtils.down(cellIndex));
            diffFromDown = Math.min(cellUnder.smokeIndicator(), MAX_SMOKE_LEVEL - oldCell.smokeIndicator());
            valueOfSmokeDuringComputing += diffFromDown;
        }

        final double valueOfSmokeDuringComputingFinal = valueOfSmokeDuringComputing;
        // around
        double diffFromAround = Stream.concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X),
                NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z))
                .filter(oldState::hasCell)
                .filter(index -> oldState.getCell(index).isFluid())
                .map(neighbourIndex -> {
                    int numberOfNeighboursMine = numberOfHorizontalNeighboursWithSmokeCapacity(oldState, cellIndex) + 1;
                    int numberOfNeighboursHim = numberOfHorizontalNeighboursWithSmokeCapacity(oldState, neighbourIndex) + 1;
                    double diff = 0.0;
                    if (cellAboveCanNotTakeSmoke(oldState, cellIndex)) {
                        diff -= Math.min((MAX_SMOKE_LEVEL - oldState.getCell(neighbourIndex).smokeIndicator()) / numberOfNeighboursHim,
                                valueOfSmokeDuringComputingFinal / numberOfNeighboursMine);
                    }
                    if (cellAboveCanNotTakeSmoke(oldState, neighbourIndex)) {
                        diff += Math.min((MAX_SMOKE_LEVEL - valueOfSmokeDuringComputingFinal) / numberOfNeighboursMine,
                                (oldState.getCell(neighbourIndex).smokeIndicator()) / numberOfNeighboursHim);
                    }
                    return diff;
                }).mapToDouble(Double::doubleValue).sum();

        return diffFromAbove + diffFromDown + diffFromAround;
    }

    private boolean cellAboveCanNotTakeSmoke(State state, Vector3i index) {
        Vector3i cellAbove = NeighbourUtils.up(index);
        return (state.hasCell(cellAbove) && state.getCell(cellAbove).smokeIndicator() >= SMOKE_LIMIT_VALUE) ||
                (state.hasCell(cellAbove) && !state.getCell(cellAbove).isFluid()) ||
                !state.hasCell(cellAbove);
    }

    private int numberOfHorizontalNeighboursWithSmokeCapacity(State state, Vector3i cellIndex) {
        List<Cell> res = new ArrayList<>();
        NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X)
                .filter(state::hasCell)
                .filter(index -> state.getCell(index).smokeIndicator() < MAX_SMOKE_LEVEL)
                .forEach(index -> res.add(state.getCell(index)));
        NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z)
                .filter(state::hasCell)
                .filter(index -> state.getCell(index).smokeIndicator() < MAX_SMOKE_LEVEL)
                .forEach(index -> res.add(state.getCell(index)));
        return res.size();
    }

}
