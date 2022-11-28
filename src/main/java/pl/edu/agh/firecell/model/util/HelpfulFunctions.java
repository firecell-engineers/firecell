package pl.edu.agh.firecell.model.util;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.material.Material;

import java.util.stream.Stream;

public class HelpfulFunctions {

    public static boolean isUpNeighbourAir(State state, Vector3i cellIndex) {
        Vector3i upIndex = NeighbourUtils.up(cellIndex);
        return state.hasCell(upIndex) && state.getCell(upIndex).material().equals(Material.AIR);
    }

    public static boolean isCellBurning(Cell cell) {
        return cell.flammable() && cell.burningTime() > 0;
    }

    public static Stream<Vector3i> getBurningHorizontalNeighbours(State oldState, Vector3i cellIndex) {
        return Stream
                .concat(NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.X), NeighbourUtils.neighboursStream(cellIndex, NeighbourUtils.Axis.Z))
                .filter(neighbourIndex -> oldState.hasCell(neighbourIndex) &&
                        isCellBurning(oldState.getCell(neighbourIndex)) &&
                        oldState.getCell(neighbourIndex).remainingFirePillar() > 0 &&
                        !isUpNeighbourAir(oldState, neighbourIndex));
    }

    public static double tempDiffAbs(Cell cellOne, Cell cellTwo) {
        return Math.abs(tempDiff(cellOne, cellTwo));
    }

    public static double tempDiff(Cell cellOne, Cell cellTwo) {
        return cellOne.temperature() - cellTwo.temperature();
    }

}
