package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.MatterState;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;


public class BasicAlgorithm implements Algorithm {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final double deltaTime;
    public static final double CONVECTION_COEFFICIENT = 1.25;
    // should be dependent on the material in the future
    public static final double CONDUCTIVITY_COEFFICIENT = 0.02;
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
        int newRemainingHeightOfFirePillar = oldCell.remainingHeightOfFirePillar();
        boolean newPossibleToGoUp = oldCell.possibleToGoUp();

        try {
            if (oldState.getCell(IndexUtils.up(cellIndex)).material().equals(Material.WOOD)) {
                newPossibleToGoUp = false;
            }
        } catch (IndexOutOfBoundsException ignored){
            newPossibleToGoUp = false;
        }

        if(oldCell.material().equals(Material.WOOD) && newTemperature > 250) {
            newBurningTime ++;
        }
        if(oldCell.material().equals(Material.AIR)){
            try {
                Cell underCell = oldState.getCell(IndexUtils.down(cellIndex));
                if (underCell.flammable() && underCell.burningTime() > 0) {
                    newRemainingHeightOfFirePillar = underCell.remainingHeightOfFirePillar() - 1;
                    if (newRemainingHeightOfFirePillar > 0) {
                        // Rethink it
                        newTemperature = Math.max(newTemperature, 600);
                        newBurningTime++;
                    }
                } else {
                    Cell neighbourOnFire = doesNeighbourOnFire(oldState, cellIndex);
                    if (neighbourOnFire!=null) {
                        newRemainingHeightOfFirePillar = neighbourOnFire.remainingHeightOfFirePillar() - 1;
                        if (newRemainingHeightOfFirePillar > 0) {
                            newTemperature = Math.max(newTemperature, 600);
                            newBurningTime++;
                        }
                    }
                }
            }catch (IndexOutOfBoundsException ignored){}
        }

        if (newFlammable && newBurningTime > 0) {
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
                newPossibleToGoUp
        );
    }

    private Cell doesNeighbourOnFire(State oldState, Vector3i cellIndex){
        Cell northCell = oldState.getCell(IndexUtils.north(cellIndex));
        Cell southCell = oldState.getCell(IndexUtils.south(cellIndex));
        Cell westCell = oldState.getCell(IndexUtils.west(cellIndex));
        Cell eastCell = oldState.getCell(IndexUtils.east(cellIndex));
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

    private double computeNewTemperature(State oldState, Vector3i cellIndex, Cell oldCell) {
        return switch (oldCell.material().getMatterState()) {
            case SOLID -> oldCell.temperature() + computeConductivityFromAll(oldState, oldCell, cellIndex);
            case FLUID -> oldCell.temperature() + computeConvectionForFluid(oldState, oldCell, cellIndex);
        };
    }

    private double computeConductivity(Cell former, Cell middle, Cell latter) {
        return - deltaTime * CONDUCTIVITY_COEFFICIENT * (
                 2 * middle.temperature() - former.temperature() - latter.temperature()
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
            else if (northMatter == MatterState.SOLID)
                yTemp = computeConductivity(northCell, oldCell, oldCell.getCopy());
            else if (southMatter == MatterState.SOLID)
                yTemp = computeConductivity(oldCell.getCopy(), oldCell, southCell);

            if (upMatter == MatterState.SOLID && downMatter == MatterState.SOLID)
                zTemp = computeConductivity(upCell, oldCell, downCell);
            else if (upMatter == MatterState.SOLID)
                zTemp = computeConductivity(upCell, oldCell, oldCell.getCopy());
            else if (downMatter == MatterState.SOLID)
                zTemp = computeConductivity(oldCell.getCopy(), oldCell, downCell);

            if (eastMatter == MatterState.SOLID && westMatter == MatterState.SOLID)
                xTemp = computeConductivity(eastCell, oldCell, westCell);
            else if (eastMatter == MatterState.SOLID)
                xTemp = computeConductivity(eastCell, oldCell, oldCell.getCopy());
            else if (westMatter == MatterState.SOLID)
                xTemp = computeConductivity(oldCell.getCopy(), oldCell, westCell);
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
