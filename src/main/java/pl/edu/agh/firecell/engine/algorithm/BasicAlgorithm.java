package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final double deltaTime;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 1;
    public static final int MAX_BURNING_TIME = 5;

    public BasicAlgorithm(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);

        double newTemperature = computeNewTemperature(oldState, cellIndex, oldCell);
        boolean newFlammable = oldCell.flammable();
        int newBurningTime = oldCell.burningTime();

        // computeFirePropagation();
        // computeSmokePropagation();

        if (newFlammable && newBurningTime >= 0 && newTemperature > 100) {
            newBurningTime++;
        }

        if (newBurningTime > MAX_BURNING_TIME) {
            newFlammable = false;
        }

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                oldCell.material()
        );
    }

    private double computeNewTemperature(State oldState, Vector3i cellIndex, Cell oldCell) {
        return oldCell.temperature() + deltaTime *
                switch (oldCell.material().getMatterState()) {
                    case SOLID -> computeConductivity(oldState, oldCell, cellIndex);
                    case FLUID -> computeConvection(oldState, oldCell, cellIndex);
                };
    }

    private double computeConductivity(State oldState, Cell oldCell, Vector3i cellIndex) {
        return NeighbourUtils.neighboursStream(cellIndex)
                .filter(oldState::hasCell)
                .map(oldState::getCell)
                .mapToDouble(neighbour -> computeConductivityWithNeighbour(oldCell, neighbour))
                .sum();
    }

    private double computeConvection(State oldState, Cell oldCell, Vector3i cellIndex) {
        return NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Y)
                .filter(oldState::hasCell)
                .map(oldState::getCell)
                .filter(Cell::isFluid)
                .mapToDouble(neighbour -> computeConvectionWithNeighbour(oldCell, neighbour))
                .sum();
    }

    private static double computeConductivityWithNeighbour(Cell oldCell, Cell neighbour) {
        return CONDUCTIVITY_COEFFICIENT * (oldCell.temperature() - neighbour.temperature());
    }

    private static double computeConvectionWithNeighbour(Cell oldCell, Cell neighbour) {
        return CONVECTION_COEFFICIENT * (oldCell.temperature() - neighbour.temperature());
    }
}
