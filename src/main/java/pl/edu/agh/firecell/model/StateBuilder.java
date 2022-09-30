package pl.edu.agh.firecell.model;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

// TODO: FIRE-22: Tests for StateBuilder
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
        IndexUtils.range(position, new Vector3i(-1).add(position).add(size))
                .map(expandedIndex -> IndexUtils.flattenIndex(expandedIndex, spaceSize))
                .forEach(flatIndex -> {
                    if(material==Material.WOOD)
                        cells.set(flatIndex, new Cell(15, 0, true, material, 5));
                    else
                        cells.set(flatIndex, new Cell(15, 0, true, material));
                });
        return this;
    }

    public StateBuilder igniteCuboid(Vector3i position, Vector3i size) {
        IndexUtils.range(position, new Vector3i(-1).add(position).add(size))
                .map(expandedIndex -> IndexUtils.flattenIndex(expandedIndex, spaceSize))
                .filter(index -> cells.get(index).flammable())
                .forEach(index -> {
                    var oldCell = cells.get(index);
                    cells.set(index, new Cell(600,
                            0, oldCell.flammable(), oldCell.material(), oldCell.remainingHeightOfFirePillar()));
                });
        return this;
    }

    public StateBuilder reset() {
        this.cells = entireAir();
        return this;
    }

    private List<Cell> entireAir() {
        return new ArrayList<>(IntStream.range(0, spaceSize.x * spaceSize.y * spaceSize.z)
                .mapToObj(idx -> new Cell(20, 0, true, Material.AIR))
                .toList());
    }
}
