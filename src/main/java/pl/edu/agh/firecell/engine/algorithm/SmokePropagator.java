package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

import static pl.edu.agh.firecell.engine.algorithm.FirePropagator.isCellBurning;

public class SmokePropagator {

    private final double deltaTime;

    public SmokePropagator(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public int computeNewSmokeIndicator(State oldState, Vector3i cellIndex, Cell oldCell) {

        double smokeDifference = getSmokeIndicatorDifference(oldCell, oldState, cellIndex);

        int smokeFromFire = 0;
        if(oldState.hasCell(NeighbourUtils.down(cellIndex)) &&
                isCellBurning(oldState.getCell(NeighbourUtils.down(cellIndex))) &&
                oldState.getCell(NeighbourUtils.down(cellIndex)).isSolid()){
            smokeFromFire = oldState.getCell(NeighbourUtils.down(cellIndex)).material().smokeCoe();
        }

        return (int)Math.min(oldCell.smokeIndicator() + deltaTime*(smokeDifference + smokeFromFire), 100);
    }

    private double getSmokeIndicatorDifference (Cell oldCell, State oldState, Vector3i cellIndex){
        final Int smokeInCell = new Int(oldCell.smokeIndicator());
        return NeighbourUtils.neighboursStream(cellIndex)
                .filter(oldState::hasCell)
                .map(neighbourIndex -> {
                    if(NeighbourUtils.up(cellIndex).equals(neighbourIndex)){
                        // neighbour above
                        // try to put to him as much as possible
                        double toGive = Math.min(oldCell.smokeIndicator(), 100-oldState.getCell(neighbourIndex).smokeIndicator());
                        smokeInCell.value -= toGive;

                        if(oldState.getCell(neighbourIndex).smokeIndicator() > oldCell.smokeIndicator()) {
                            toGive -= 0.05 * oldState.getCell(neighbourIndex).smokeIndicator();
                        }
                        return -toGive;
                    }
                    if(NeighbourUtils.north(cellIndex).equals(neighbourIndex) ||
                            NeighbourUtils.south(cellIndex).equals(neighbourIndex) ||
                            NeighbourUtils.west(cellIndex).equals(neighbourIndex) ||
                            NeighbourUtils.east(cellIndex).equals(neighbourIndex)){
                        // to neighbours
                        if(oldState.getCell(neighbourIndex).smokeIndicator() > oldCell.smokeIndicator()){
                            // we want to take from
                            int hisLost = 0;
                            if(oldState.hasCell(NeighbourUtils.up(neighbourIndex))) {
                                hisLost = Math.min(oldState.getCell(neighbourIndex).smokeIndicator(),
                                        100 - oldState.getCell(NeighbourUtils.up(neighbourIndex)).smokeIndicator());
                            }
                            int toShare = oldState.getCell(neighbourIndex).smokeIndicator() - hisLost;
                            return toShare/5.0;
                        } else {
                            double toGive = Math.min(smokeInCell.value/5, 100 - oldState.getCell(neighbourIndex).smokeIndicator());
                            smokeInCell.value -= toGive;
                            return -toGive;
                        }
                    }
                    if(NeighbourUtils.down(cellIndex).equals(neighbourIndex)){
                        double toGet = Math.min(oldState.getCell(neighbourIndex).smokeIndicator(), 100-oldCell.smokeIndicator());
                        return toGet;
                    }
                    return 0.0;
                }).mapToDouble(Double::doubleValue).sum();
    }

    class Int{
        public int value;
        public Int(int val){
            this.value = val;
        }
        public Int(){
            this(0);
        }
    }
}
