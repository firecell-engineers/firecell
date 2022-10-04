package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final TemperatureCalculation temperatureCalculation;
    public static final double CONVECTION_COEFFICIENT = 1;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 0.2;
    public static final int MAX_BURNING_TIME = 50;


    public BasicAlgorithm(double deltaTime) {
        this.temperatureCalculation = new TemperatureCalculation(deltaTime);
    }

    @Override
    public Cell compute(State oldState, Vector3i cellIndex) {

        Cell oldCell = oldState.getCell(cellIndex);

        double newTemperature = temperatureCalculation.computeNewTemperature(oldState, cellIndex, oldCell);
        boolean newFlammable = oldCell.flammable();
        int newBurningTime = oldCell.burningTime();
        int newRemainingHeightOfFirePillar = oldCell.remainingHeightOfFirePillar();
        boolean newPossibleToGoUp = oldCell.possibleToGoUp();
        boolean isBurned = false;

        newPossibleToGoUp = isPossibleToGoUp(oldState, cellIndex, newPossibleToGoUp);

        // wood section
        if(oldCell.material().equals(Material.WOOD)) {
            //set wood on fire
            if (newBurningTime == 0 && newTemperature > 250 && MAX_BURNING_TIME != 0) {
                newBurningTime++;
                newTemperature = 550;
            }
            //keep wood on fire
            if (newBurningTime > 0 && newBurningTime <= MAX_BURNING_TIME) {
                newBurningTime++;
                newTemperature = Math.max(550, newTemperature);
            }
            //wood is carbonated
            if (newBurningTime > MAX_BURNING_TIME) {
                isBurned = true;
                newFlammable = false;
            }
        }

        // air section
        if(oldCell.material().equals(Material.AIR)){
            try {
                Cell underCell = oldState.getCell(NeighbourUtils.down(cellIndex));
                if (underCell.flammable() && underCell.burningTime() > 0) {
                    newRemainingHeightOfFirePillar = underCell.remainingHeightOfFirePillar() - 1;
                    if (newRemainingHeightOfFirePillar > 0) {
                        // Rethink it
                        newTemperature = Math.max(newTemperature, 600);
                        newBurningTime++;
                    }
                } else {
                    Cell neighbourOnFire = doesNeighbourOnFire(oldState, cellIndex);
                    if (neighbourOnFire!=null && !neighbourOnFire.possibleToGoUp()) {
                        newRemainingHeightOfFirePillar = neighbourOnFire.remainingHeightOfFirePillar() - 1;
                        if (newRemainingHeightOfFirePillar > 0) {
                            newTemperature = Math.max(newTemperature, 600);
                            newBurningTime++;
                        }
                    }
                }
            }catch (IndexOutOfBoundsException ignored){}
        }

        return new Cell(
                newTemperature,
                newBurningTime,
                newFlammable,
                oldCell.material(),
                newRemainingHeightOfFirePillar,
                newPossibleToGoUp
        );
    }

    private boolean isPossibleToGoUp(State oldState, Vector3i cellIndex, boolean newPossibleToGoUp) {
        try {
            if (oldState.getCell(NeighbourUtils.up(cellIndex)).material().getMatterState().equals(MatterState.SOLID)) {
                newPossibleToGoUp = false;
            }
        } catch (IndexOutOfBoundsException ignored){
            newPossibleToGoUp = false;
        }
        return newPossibleToGoUp;
    }

    private Cell doesNeighbourOnFire(State oldState, Vector3i cellIndex){
        Cell northCell = oldState.getCell(NeighbourUtils.north(cellIndex));
        Cell southCell = oldState.getCell(NeighbourUtils.south(cellIndex));
        Cell westCell = oldState.getCell(NeighbourUtils.west(cellIndex));
        Cell eastCell = oldState.getCell(NeighbourUtils.east(cellIndex));
        if(doesHorizontalFire(northCell))
            return northCell;
        if(doesHorizontalFire(southCell))
            return southCell;
        if(doesHorizontalFire(westCell))
            return westCell;
        if(doesHorizontalFire(eastCell))
            return eastCell;
        return null;
    }

    private boolean doesHorizontalFire(Cell cell){
        return cell.burningTime() > 0 && cell.remainingHeightOfFirePillar() > 0 && !cell.possibleToGoUp();
    }

}
