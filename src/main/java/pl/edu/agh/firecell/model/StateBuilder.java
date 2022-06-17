package pl.edu.agh.firecell.model;

import org.joml.Vector3f;
import org.joml.Vector3i;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class StateBuilder {

    private final Vector3i spaceSize;
    private List<Cell> cells;

    public StateBuilder(Vector3i spaceSize) {
        this.spaceSize = spaceSize;
        this.cells = entireAir();
    }

    public State getResult() {
        return new State(cells, spaceSize);
    }

    public StateBuilder addCuboid(Vector3i position, Vector3i size, Material material) {
        Vector3i end = new Vector3i(0).add(position).add(size).sub(new Vector3i(1));
        IndexUtils.range(position, end)
                .map(expandedIndex -> IndexUtils.flattenIndex(expandedIndex, spaceSize))
                .forEach(flatIndex -> cells.set(flatIndex, new Cell(0, 0, 0, false, material)));
        return this;
    }

    public StateBuilder reset() {
        this.cells = entireAir();
        return this;
    }

    private List<Cell> entireAir() {
        return new ArrayList<>(IntStream.range(0, spaceSize.x * spaceSize.y * spaceSize.z)
                .mapToObj(idx -> new Cell(0, 0, 0, false, Material.AIR))
                .toList());
    }
}
