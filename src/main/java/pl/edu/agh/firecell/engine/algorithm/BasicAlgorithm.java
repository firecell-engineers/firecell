package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.*;
import pl.edu.agh.firecell.model.util.IndexUtils;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final double deltaTime;
    public static final double CONDUCTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double GAMMA_PRIM_N1 = 1;

    public BasicAlgorithm(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        logger.debug("Computing at position: " + cellIndex);

        Cell oldCell = oldState.getCell(cellIndex);

        double newTemperature = computeNewTemperature(oldState, cellIndex, oldCell);
        boolean newFlammable = oldCell.flammable();
        int newBurningTime = oldCell.burningTime();
        Material newMaterial = oldCell.material();

        //computeFirePropagation();
        //computeSmokePropagation();

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                newMaterial
        );
    }

    private double computeNewTemperature(State oldState, Vector3i cellIndex, Cell oldCell) {
        return switch (oldCell.material().getMatterState()) {
            case SOLID -> oldCell.temperature() + computeConductivityFromAll(oldState, oldCell, cellIndex);
            case FLUID -> oldCell.temperature() + computeConvectionForFluid(oldState, oldCell, cellIndex);
        };
    }

    private double computeConductivity(Cell former, Cell middle, Cell latter) {
        // TODO: put them into class constants and give meaningful names

        return deltaTime * (
                GAMMA_PRIM_N1 * (middle.temperature() - former.temperature()) -
                        GAMMA_PRIM_N1 * (latter.temperature() - middle.temperature())
        );
    }

    private double computeConductivityFromAll(State oldState, Cell oldCell, Vector3i cellIndex) {

        double yTemp;
        double zTemp;
        double xTemp;

        try {

            yTemp = computeConductivity(
                    oldState.getCell(IndexUtils.north(cellIndex)),
                    oldCell,
                    oldState.getCell(IndexUtils.south(cellIndex)));

            zTemp = computeConductivity(
                    oldState.getCell(IndexUtils.up(cellIndex)),
                    oldCell,
                    oldState.getCell(IndexUtils.down(cellIndex)));

            xTemp = computeConductivity(
                    oldState.getCell(IndexUtils.east(cellIndex)),
                    oldCell,
                    oldState.getCell(IndexUtils.west(cellIndex)));

        } catch (IllegalArgumentException e) {
            logger.debug(String.valueOf(e));
            return oldCell.temperature();
        }

        return yTemp + zTemp + xTemp;
    }

    private double computeConvectionForFluid(State oldState, Cell oldCell, Vector3i cellIndex) {

        double fromDownToMe = 0;
        double fromMeToUp = 0;

        try {
            if (oldState.getCell(IndexUtils.south(cellIndex)).temperature() - oldCell.temperature() > 0) {
                fromDownToMe = CONDUCTION_COEFFICIENT * tempDiff(oldCell, oldState.getCell(IndexUtils.south(cellIndex))) * deltaTime;
            }
            if (oldState.getCell(IndexUtils.north(cellIndex)).temperature() - oldCell.temperature() < 0) {
                fromMeToUp = -CONDUCTION_COEFFICIENT * tempDiff(oldCell, oldState.getCell(IndexUtils.north(cellIndex))) * deltaTime;
            }
        } catch (IllegalArgumentException e){
            return oldCell.temperature();
        }
        return fromDownToMe + fromMeToUp;
    }

    private static double tempDiff(Cell cellOne, Cell cellTwo) {
        return Math.abs(cellOne.temperature() - cellTwo.temperature());
    }

}
