package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.HashMap;
import java.util.Map;

import static pl.edu.agh.firecell.model.MatterState.FLUID;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemperaturePropagator temperaturePropagator;
    private final FirePropagator firePropagator;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 0.1;
    private final Map<String, Integer> smokeCoefficients = new HashMap<>();
    private final Map<String, Integer> smokeCoefficientsHorizontalEscalation = new HashMap<>();
    public static final double MIN_BURNING_TEMPERATURE = 100;
    public static final int MAX_BURNING_TIME = 50;


    public BasicAlgorithm(double deltaTime) {
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

        this.temperaturePropagator = new TemperaturePropagator(deltaTime);
        this.firePropagator = new FirePropagator();
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);
        // NOTE: Following method calls are dependent on each other, the order matters.
        double newTemperature = temperaturePropagator.computeNewTemperature(oldState, cellIndex, oldCell);
        int newBurningTime = firePropagator.computeBurningTime(oldState, oldCell, cellIndex, newTemperature);
        int newRemainingHeightOfFirePillar = firePropagator.computeFirePillar(oldState, oldCell, cellIndex, oldCell.remainingFirePillar());
        boolean newFlammable = firePropagator.computeNewFlammable(oldCell, newBurningTime);
        newTemperature = temperaturePropagator.updateTemperatureBasedOnFire(oldState, oldCell, cellIndex, newTemperature, newBurningTime);
        int newSmokeIndicator = computeNewSmokeIndicator(oldState, cellIndex, oldCell);
        // boolean isBurned = false; || maybe in future

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
                oldCell.material(),
                newRemainingHeightOfFirePillar,
                newSmokeIndicator
        );
    }

    private int computeNewSmokeIndicator(State oldState, Vector3i cellIndex, Cell oldCell){
        int devotedSmoke, deliveredSmoke;

        try {
            Cell cellAbove = oldState.getCell(NeighbourUtils.up(cellIndex));
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
                DFunction(oldCell, oldState.getCell(NeighbourUtils.down(cellIndex)), coe.get("down")) +
                DFunction(oldCell, oldState.getCell(NeighbourUtils.up(cellIndex)), coe.get("up")) +
                DFunction(oldCell, oldState.getCell(NeighbourUtils.south(cellIndex)), coe.get("south")) +
                DFunction(oldCell, oldState.getCell(NeighbourUtils.north(cellIndex)), coe.get("north")) +
                DFunction(oldCell, oldState.getCell(NeighbourUtils.west(cellIndex)), coe.get("west")) +
                DFunction(oldCell, oldState.getCell(NeighbourUtils.east(cellIndex)), coe.get("east")));
    }

    private int getDeliveredSmoke(Cell oldCell, State oldState, Vector3i cellIndex, Map<String, Integer> coe){
        return (int) (
                DFunction(oldState.getCell(NeighbourUtils.down(cellIndex)), oldCell, coe.get("down")) +
                DFunction(oldState.getCell(NeighbourUtils.up(cellIndex)), oldCell, coe.get("up")) +
                DFunction(oldState.getCell(NeighbourUtils.south(cellIndex)), oldCell, coe.get("south")) +
                DFunction(oldState.getCell(NeighbourUtils.north(cellIndex)), oldCell, coe.get("north")) +
                DFunction(oldState.getCell(NeighbourUtils.west(cellIndex)), oldCell, coe.get("west")) +
                DFunction(oldState.getCell(NeighbourUtils.east(cellIndex)), oldCell, coe.get("east")));
    }

    private double DFunction(Cell from, Cell to, int coe){
        return coe * Math.min((double) (1/6)*from.smokeIndicator(), (double) (1/6)*(100-to.smokeIndicator()));
    }

    public static boolean isUpNeighbourAir(State state, Vector3i cellIndex){
        Vector3i upIndex = NeighbourUtils.up(cellIndex);
        return state.hasCell(upIndex) && state.getCell(upIndex).material().equals(Material.AIR);
    }

}
