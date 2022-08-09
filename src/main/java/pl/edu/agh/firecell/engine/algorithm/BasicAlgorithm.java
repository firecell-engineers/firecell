package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.*;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.HashMap;
import java.util.Map;

import static pl.edu.agh.firecell.model.MatterState.FLUID;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final double deltaTime;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 1;
    private final Map<String, Integer> smokeCoefficients = new HashMap<>();
    private final Map<String, Integer> smokeCoefficientsHorizontalEscalation = new HashMap<>();

    public BasicAlgorithm(double deltaTime) {
        this.deltaTime = deltaTime;

        smokeCoefficients.put("down", 25);
        smokeCoefficients.put("up", 100);
        smokeCoefficients.put("north", 50);
        smokeCoefficients.put("south", 50);
        smokeCoefficients.put("west", 50);
        smokeCoefficients.put("east", 50);

        smokeCoefficientsHorizontalEscalation.put("down", 100);
        smokeCoefficientsHorizontalEscalation.put("up", 0);
        smokeCoefficientsHorizontalEscalation.put("north", 50);
        smokeCoefficientsHorizontalEscalation.put("south", 50);
        smokeCoefficientsHorizontalEscalation.put("west", 50);
        smokeCoefficientsHorizontalEscalation.put("east", 50);
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

        Cell cellAbove = oldState.getCell(IndexUtils.up(cellIndex));
        if(cellAbove.material().getMatterState() != FLUID || cellAbove.smokeIndicator()>=99){
            devotedSmoke = getDevotedSmoke(oldCell, oldState, cellIndex, smokeCoefficientsHorizontalEscalation);
            deliveredSmoke = getDeliveredSmoke(oldCell, oldState, cellIndex, smokeCoefficientsHorizontalEscalation);
        } else {
            devotedSmoke = getDevotedSmoke(oldCell, oldState, cellIndex, smokeCoefficients);
            deliveredSmoke = getDeliveredSmoke(oldCell, oldState, cellIndex, smokeCoefficients);
        }

        return oldCell.smokeIndicator() - devotedSmoke + deliveredSmoke;
    }

    private int getDevotedSmoke(Cell oldCell, State oldState, Vector3i cellIndex, Map<String, Integer> coe){
        return (int) (
                DFunction(oldCell, oldState.getCell(IndexUtils.down(cellIndex)), coe.get("down")) +
                DFunction(oldCell, oldState.getCell(IndexUtils.up(cellIndex)), coe.get("up")) +
                DFunction(oldCell, oldState.getCell(IndexUtils.south(cellIndex)), coe.get("south")) +
                DFunction(oldCell, oldState.getCell(IndexUtils.north(cellIndex)), coe.get("north")) +
                DFunction(oldCell, oldState.getCell(IndexUtils.west(cellIndex)), coe.get("west")) +
                DFunction(oldCell, oldState.getCell(IndexUtils.east(cellIndex)), coe.get("east")));
    }

    private int getDeliveredSmoke(Cell oldCell, State oldState, Vector3i cellIndex, Map<String, Integer> coe){
        return (int) (
                DFunction(oldState.getCell(IndexUtils.down(cellIndex)), oldCell, coe.get("down")) +
                DFunction(oldState.getCell(IndexUtils.up(cellIndex)), oldCell, coe.get("up")) +
                DFunction(oldState.getCell(IndexUtils.south(cellIndex)), oldCell, coe.get("south")) +
                DFunction(oldState.getCell(IndexUtils.north(cellIndex)), oldCell, coe.get("north")) +
                DFunction(oldState.getCell(IndexUtils.west(cellIndex)), oldCell, coe.get("west")) +
                DFunction(oldState.getCell(IndexUtils.east(cellIndex)), oldCell, coe.get("east")));
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
