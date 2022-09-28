package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;


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
            newBurningTime ++;
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
        return switch (oldCell.material().getMatterState()) {
            case SOLID -> oldCell.temperature() + computeConductivityFromAll(oldState, oldCell, cellIndex);
            case FLUID -> oldCell.temperature() + computeConvectionForFluid(oldState, oldCell, cellIndex);
        };
    }

    private double computeConductivity(Cell former, Cell middle, Cell latter) {
        return deltaTime * (
                CONDUCTIVITY_COEFFICIENT * (middle.temperature() - former.temperature()) -
                        CONDUCTIVITY_COEFFICIENT * (latter.temperature() - middle.temperature())
        );
    }

    private double computeConductivityFromAll(State oldState, Cell oldCell, Vector3i cellIndex) {
        double yTemp = 0;
        double zTemp = 0;
        double xTemp = 0;

        try {
            Cell northCell = oldState.getCell(IndexUtils.north(cellIndex));
            MatterState northMatter = northCell.material().getMatterState();
            Cell southCell = oldState.getCell(IndexUtils.south(cellIndex));
            MatterState southMatter = southCell.material().getMatterState();

            Cell upCell = oldState.getCell(IndexUtils.up(cellIndex));
            MatterState upMatter = upCell.material().getMatterState();
            Cell downCell = oldState.getCell(IndexUtils.down(cellIndex));
            MatterState downMatter = downCell.material().getMatterState();

            Cell eastCell = oldState.getCell(IndexUtils.east(cellIndex));
            MatterState eastMatter = eastCell.material().getMatterState();
            Cell westCell = oldState.getCell(IndexUtils.west(cellIndex));
            MatterState westMatter = westCell.material().getMatterState();

            if (northMatter == MatterState.SOLID && southMatter == MatterState.SOLID)
                yTemp = computeConductivity(northCell, oldCell, southCell);
            if (upMatter == MatterState.SOLID && downMatter == MatterState.SOLID)
                zTemp = computeConductivity(upCell, oldCell, downCell);
            if (eastMatter == MatterState.SOLID && westMatter == MatterState.SOLID)
                xTemp = computeConductivity(eastCell, oldCell, westCell);
        } catch (IndexOutOfBoundsException ignored) {}
        return yTemp + zTemp + xTemp;
    }

    private double computeConvectionForFluid(State oldState, Cell oldCell, Vector3i cellIndex) {

        double fromDownToMe = 0;
        double fromMeToUp = 0;

        try {
            Cell cellUnder = oldState.getCell(IndexUtils.down(cellIndex));
            Cell cellAbove = oldState.getCell(IndexUtils.up(cellIndex));
            if (cellUnder.temperature() - oldCell.temperature() > 0) {
                fromDownToMe = CONVECTION_COEFFICIENT * tempDiff(oldCell, cellUnder) * deltaTime;
            }
            if (cellAbove.temperature() - oldCell.temperature() < 0) {
                fromMeToUp = -CONVECTION_COEFFICIENT * tempDiff(oldCell, cellAbove) * deltaTime;
            }
        } catch (IndexOutOfBoundsException ignored) {}
        return fromDownToMe + fromMeToUp;
    }

    private static double tempDiff(Cell cellOne, Cell cellTwo) {
        return Math.abs(cellOne.temperature() - cellTwo.temperature());
    }

}
