package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import java.util.Optional;

import static pl.edu.agh.firecell.engine.algorithm.FirePropagator.isCellBurning;

public class SmokePropagator {

    private final double deltaTime;

    public SmokePropagator(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public double computeNewSmokeIndicator(State oldState, Vector3i cellIndex, Cell oldCell) {

        double smokeDifference = getSmokeIndicatorDifference(oldCell, oldState, cellIndex);

        int smokeFromFire = 0;
        if(oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).isSolid()){
            smokeFromFire = oldState.getCell(NeighbourUtils.down(cellIndex)).material().smokeCoe();
        }
        Optional<Cell> northCell = Optional.empty();
        Optional<Cell> southCell = Optional.empty();
        Optional<Cell> westCell = Optional.empty();
        Optional<Cell> eastCell = Optional.empty();
        if(oldState.hasCell(NeighbourUtils.west(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.west(cellIndex)))) {
            westCell = Optional.of(oldState.getCell(NeighbourUtils.west(cellIndex)));
        }
        if(oldState.hasCell(NeighbourUtils.east(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.east(cellIndex)))) {
            eastCell = Optional.of(oldState.getCell(NeighbourUtils.east(cellIndex)));
        }
        if(oldState.hasCell(NeighbourUtils.north(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.north(cellIndex)))) {
            northCell = Optional.of(oldState.getCell(NeighbourUtils.north(cellIndex)));
        }
        if(oldState.hasCell(NeighbourUtils.south(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.south(cellIndex)))) {
            southCell = Optional.of(oldState.getCell(NeighbourUtils.south(cellIndex)));
        }
        if(southCell.isPresent()) smokeFromFire+=southCell.get().material().smokeCoe()/4;
        if(northCell.isPresent()) smokeFromFire+=northCell.get().material().smokeCoe()/4;
        if(westCell.isPresent()) smokeFromFire+=westCell.get().material().smokeCoe()/4;
        if(eastCell.isPresent()) smokeFromFire+=eastCell.get().material().smokeCoe()/4;

        return (Math.max(0,Math.min(oldCell.smokeIndicator() + (smokeDifference + smokeFromFire), 100)));
    }

    private double getSmokeIndicatorDifference (Cell oldCell, State oldState, Vector3i cellIndex){
        double divider = 4.0;
        return NeighbourUtils.neighboursStream(cellIndex)
                .filter(oldState::hasCell)
                .filter(neighbourIndex -> oldState.getCell(neighbourIndex).isFluid())
                .map(neighbourIndex -> {
                    if(NeighbourUtils.up(cellIndex).equals(neighbourIndex)){
                        return - Math.min(oldCell.smokeIndicator(), 100-oldState.getCell(neighbourIndex).smokeIndicator());
                    }
                    if(isHorizontal(cellIndex, neighbourIndex)){
                        double difference = 0;
                        // my neighbour has more smoke than I
                        if(shouldITake(oldState, neighbourIndex)){
                            difference += Math.min(oldState.getCell(neighbourIndex).smokeIndicator()/divider,
                                    (100 - oldState.getCell(cellIndex).smokeIndicator())/divider);
                        }
                        if(shouldITake(oldState, cellIndex)){
                            difference -= Math.min(oldState.getCell(cellIndex).smokeIndicator()/divider,
                                    (100 - oldState.getCell(neighbourIndex).smokeIndicator())/divider);
                        }
                        if(oldState.getCell(neighbourIndex).smokeIndicator() == oldCell.smokeIndicator()){
                            difference = 0;
                        }
                        return difference;
                    }
                    if(NeighbourUtils.down(cellIndex).equals(neighbourIndex)){
                        return (double)Math.min(oldState.getCell(neighbourIndex).smokeIndicator(),
                                100 - oldCell.smokeIndicator());
                    }
                    return 0.0;
                }).mapToDouble(Double::doubleValue).sum();
    }

    private boolean shouldITake(State oldState, Vector3i index) {
        return (oldState.hasCell(NeighbourUtils.up(index)) && oldState.getCell(NeighbourUtils.up(index)).smokeIndicator() > 80) ||
                (oldState.hasCell(NeighbourUtils.up(index)) && oldState.getCell(NeighbourUtils.up(index)).isSolid()) ||
                (!oldState.hasCell(NeighbourUtils.up(index)));
    }

    private boolean isHorizontal(Vector3i cellIndex, Vector3i neighbourIndex) {
        return NeighbourUtils.north(cellIndex).equals(neighbourIndex) ||
                NeighbourUtils.south(cellIndex).equals(neighbourIndex) ||
                NeighbourUtils.west(cellIndex).equals(neighbourIndex) ||
                NeighbourUtils.east(cellIndex).equals(neighbourIndex);
    }

}
