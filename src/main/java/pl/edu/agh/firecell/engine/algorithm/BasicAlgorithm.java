package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.*;
import pl.edu.agh.firecell.model.util.IndexUtils;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final double deltaTime;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 1;

    public BasicAlgorithm(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        logger.debug("Computing at position: " + cellIndex);

        Cell oldCell = oldState.getCell(cellIndex);

        double newTemperature = computeNewTemperature(oldState, cellIndex, oldCell);
        boolean newFlammable  = oldCell.flammable();
        int newBurningTime    = oldCell.burningTime();
        Material newMaterial  = oldCell.material();
        int newSmokeIndicator = computeNewSmokeIndicator(oldState, cellIndex, oldCell);

        //computeFirePropagation();
        if(newFlammable){
            newSmokeIndicator = Math.min(100, newSmokeIndicator + newMaterial.smokeCoe());
        }

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                newMaterial,
                newSmokeIndicator
        );
    }

    private int computeNewSmokeIndicator(State oldState, Vector3i cellIndex, Cell oldCell){
        int devotedSmoke, deliveredSmoke;
        devotedSmoke = (int)
                (
                        DFunction(oldCell, oldState.getCell(IndexUtils.down(cellIndex)), 25) +
                        DFunction(oldCell, oldState.getCell(IndexUtils.up(cellIndex)), 100) +
                        DFunction(oldCell, oldState.getCell(IndexUtils.south(cellIndex)), 50) +
                        DFunction(oldCell, oldState.getCell(IndexUtils.north(cellIndex)), 50) +
                        DFunction(oldCell, oldState.getCell(IndexUtils.west(cellIndex)), 50) +
                        DFunction(oldCell, oldState.getCell(IndexUtils.east(cellIndex)), 50)
                );
        deliveredSmoke = (int)
                (
                        DFunction(oldState.getCell(IndexUtils.down(cellIndex)), oldCell, 25) +
                        DFunction(oldState.getCell(IndexUtils.up(cellIndex)), oldCell, 100) +
                        DFunction(oldState.getCell(IndexUtils.south(cellIndex)), oldCell, 50) +
                        DFunction(oldState.getCell(IndexUtils.north(cellIndex)), oldCell, 50) +
                        DFunction(oldState.getCell(IndexUtils.west(cellIndex)), oldCell, 50) +
                        DFunction(oldState.getCell(IndexUtils.east(cellIndex)), oldCell, 50)
                );

        return oldCell.smokeIndicator() - devotedSmoke + deliveredSmoke;
    }

    double DFunction(Cell from, Cell to, int coe){
        return coe * Math.min((double) (1/6)*from.smokeIndicator(), (double) (1/6)*(100-to.smokeIndicator()));
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

        } catch (IndexOutOfBoundsException e) {
            logger.debug(String.valueOf(e));
            return 0;
        }

        return yTemp + zTemp + xTemp;
    }

    private double computeConvectionForFluid(State oldState, Cell oldCell, Vector3i cellIndex) {

        double fromDownToMe = 0;
        double fromMeToUp = 0;

        try {
            if (oldState.getCell(IndexUtils.down(cellIndex)).temperature() - oldCell.temperature() > 0) {
                fromDownToMe = CONVECTION_COEFFICIENT * tempDiff(oldCell, oldState.getCell(IndexUtils.down(cellIndex))) * deltaTime;
            }
            if (oldState.getCell(IndexUtils.up(cellIndex)).temperature() - oldCell.temperature() < 0) {
                fromMeToUp = -CONVECTION_COEFFICIENT * tempDiff(oldCell, oldState.getCell(IndexUtils.up(cellIndex))) * deltaTime;
            }
        } catch (IndexOutOfBoundsException e) {
            return oldCell.temperature();
        }
        return fromDownToMe + fromMeToUp;
    }

    private static double tempDiff(Cell cellOne, Cell cellTwo) {
        return Math.abs(cellOne.temperature() - cellTwo.temperature());
    }

}
