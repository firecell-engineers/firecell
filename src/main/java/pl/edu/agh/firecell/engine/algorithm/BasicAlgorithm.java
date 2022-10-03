package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.HashMap;
import java.util.Map;

import static pl.edu.agh.firecell.model.MatterState.FLUID;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final double deltaTime;
    public static final double CONVECTION_COEFFICIENT = 0.1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 0.1;
    private final Map<String, Integer> smokeCoefficients = new HashMap<>();
    private final Map<String, Integer> smokeCoefficientsHorizontalEscalation = new HashMap<>();
    public static final int MAX_BURNING_TIME = 5;
    public static final double MIN_BURNING_TEMPERATURE = 100;

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

        // temperature propagation
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
        return oldCell.temperature() + deltaTime *
                switch (oldCell.material().getMatterState()) {
                    case SOLID -> computeConduction(oldState, oldCell, cellIndex);
                    case FLUID -> computeConvection(oldState, oldCell, cellIndex);
                };
    }

    private double computeConduction(State oldState, Cell oldCell, Vector3i cellIndex) {
        return NeighbourUtils.neighboursStream(cellIndex)
                .filter(oldState::hasCell)
                .map(oldState::getCell)
                .filter(Cell::isSolid)
                .mapToDouble(neighbour -> computeConductivityWithNeighbour(oldCell, neighbour))
                .sum();
    }

    private double computeConvection(State oldState, Cell oldCell, Vector3i cellIndex) {
        return NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Y)
                .filter(oldState::hasCell)
                .map(oldState::getCell)
                .mapToDouble(neighbourCell -> computeConvectionWithNeighbour(oldCell, neighbourCell))
                .sum();
    }

    private static double computeConductivityWithNeighbour(Cell oldCell, Cell neighbour) {
        return CONDUCTIVITY_COEFFICIENT * (oldCell.temperature() - neighbour.temperature());
    }

    private static double computeConvectionWithNeighbour(Cell oldCell, Cell neighbour) {
        return CONVECTION_COEFFICIENT * (oldCell.temperature() - neighbour.temperature());
    }
}
