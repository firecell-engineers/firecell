package pl.edu.agh.firecell.core.util;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class StateUtils {
    public static List<Cell> entireAir(Vector3i spaceSize) {
        return new ArrayList<>(IntStream.range(0, spaceSize.x * spaceSize.y * spaceSize.z)
                .mapToObj(idx -> new Cell(20, 0, true, Material.AIR, 0))
                .toList());
    }

    public static State emptyState(Vector3i spaceSize) {
        return new State(entireAir(spaceSize), spaceSize);
    }
}
