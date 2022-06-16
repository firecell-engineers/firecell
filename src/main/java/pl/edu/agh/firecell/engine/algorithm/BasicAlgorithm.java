package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.engine.BasicEngineRunnable;
import pl.edu.agh.firecell.model.*;
import pl.edu.agh.firecell.model.util.IndexUtils;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(BasicEngineRunnable.class);
    private final double deltaTime = 0.5;
    public static final double convectionCoefficient = 5;
    public static final double GAMMA_PRIM_N1 = 1;
    public static final double GAMMA_PRIM_N2 = 1;

    public BasicAlgorithm() {

    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        logger.debug("Computing at position: " + cellIndex);

        Cell oldCell = oldState.getCell(cellIndex);

        double newTemperature = oldCell.temperature();
        boolean newFlammable = oldCell.flammable();
        int newBurningTime = oldCell.burningTime();
        MatterState newType = oldCell.type();
        Material newMaterial = oldCell.material();

        switch (oldCell.type()) {
            case SOLID -> {
                newTemperature = oldCell.temperature() + computeConductivityFromAll(oldState, oldCell, cellIndex);

                if (!oldCell.flammable()) {
                    break;
                }
                // is flammable


            }
            case FLUID -> {
                newTemperature = oldCell.temperature() + computeConvectionForFluid(oldState, oldCell, cellIndex);
            }
        }

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                newType,
                newMaterial
        );
    }

    private double computeConductivity(Cell former, Cell middle, Cell latter) {
        // TODO: put them into class constants and give meaningful names

        return deltaTime * (
                GAMMA_PRIM_N1 * (middle.temperature() - former.temperature()) -
                        GAMMA_PRIM_N2 * (latter.temperature() - middle.temperature())
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
            logger.info(String.valueOf(e));
            return oldCell.temperature();
        }

        return yTemp + zTemp + xTemp;
    }

    private double computeConvectionForFluid(State oldState, Cell oldCell, Vector3i cellIndex) {

        double fromDownToMe = 0;
        double fromMeToUp = 0;

        if (oldState.getCell(IndexUtils.down(cellIndex)).temperature() - oldCell.temperature() > 0) {
            fromDownToMe = convectionCoefficient * tempDiff(oldCell, oldState.getCell(IndexUtils.down(cellIndex))) * deltaTime;
        }
        if (oldState.getCell(IndexUtils.up(cellIndex)).temperature() - oldCell.temperature() < 0) {
            fromMeToUp = convectionCoefficient * tempDiff(oldCell, oldState.getCell(IndexUtils.down(cellIndex))) * deltaTime;
        }

        return fromDownToMe + fromMeToUp;
    }

    private static double tempDiff(Cell cellOne, Cell cellTwo) {
        return Math.abs(cellOne.temperature() - cellTwo.temperature());
    }

    public double getDeltaTime() {
        return deltaTime;
    }
}
