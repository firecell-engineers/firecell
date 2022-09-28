package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;
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
    public static final int MAX_BURNING_TIME = 5;

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

        Cell oldCell = oldState.getCell(cellIndex);

        double newTemperature = computeNewTemperature(oldState, cellIndex, oldCell);
        boolean newFlammable  = oldCell.flammable();
        int newBurningTime    = oldCell.burningTime();
        Material newMaterial  = oldCell.material();
        int newSmokeIndicator = computeNewSmokeIndicator(oldState, cellIndex, oldCell);

        // computeFirePropagation();
        // computeSmokePropagation();
        //computeFirePropagation();
        if(newFlammable){
            newSmokeIndicator = Math.min(100, newSmokeIndicator + newMaterial.smokeCoe());
        }

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
                newMaterial,
                newSmokeIndicator
        );
    }

    private int computeNewSmokeIndicator(State oldState, Vector3i cellIndex, Cell oldCell){
        int devotedSmoke, deliveredSmoke;

        try {
            Cell cellAbove = oldState.getCell(IndexUtils.up(cellIndex));
            if (cellAbove.material().getMatterState() != FLUID || cellAbove.smokeIndicator() >= 99) {
                devotedSmoke = getDevotedSmoke(oldCell, oldState, cellIndex, smokeCoefficientsHorizontalEscalation);
                deliveredSmoke = getDeliveredSmoke(oldCell, oldState, cellIndex, smokeCoefficientsHorizontalEscalation);
            } else {
                devotedSmoke = getDevotedSmoke(oldCell, oldState, cellIndex, smokeCoefficients);
                deliveredSmoke = getDeliveredSmoke(oldCell, oldState, cellIndex, smokeCoefficients);
            }
        } catch (IndexOutOfBoundsException e){
            devotedSmoke = 0;
            deliveredSmoke = 0;
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

    private double DFunction(Cell from, Cell to, int coe){
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
