package pl.edu.agh.firecell.core.statebuilder;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

// TODO: FIRE-22: Tests for StateBuilder
public class StateBuilder {

    private final Vector3i spaceSize;
    private final List<Element> elements = new ArrayList<>();
//    private List<Cell> cells;

    public StateBuilder(Vector3i spaceSize) {
        this.spaceSize = spaceSize;
    }

    public State build() {
        List<Cell> cells = entireAir();
        elements.forEach(element -> element.addTo(cells));
        return new State(cells, spaceSize);
    }

    public List<Element> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public Element getLastElement() {
        return elements.size() > 0 ? elements.get(elements.size() - 1) : null;
    }

    public void removeElement(Element element) {
        elements.remove(element);
    }

    public StateBuilder addCuboid(Vector3i position, Vector3i size, Material material) {
        elements.add(new CuboidElement(position, size, material));
        return this;
    }

    public StateBuilder igniteCuboid(Vector3i position, Vector3i size) {
        elements.add(new IgnitionElement(position, size));
        return this;
    }

    public StateBuilder reset() {
        elements.clear();
        return this;
    }

    private List<Cell> entireAir() {
        return new ArrayList<>(IntStream.range(0, spaceSize.x * spaceSize.y * spaceSize.z)
                .mapToObj(idx -> new Cell(20, 0, true, Material.AIR))
                .toList());
    }

    public interface Element {
        void addTo(List<Cell> cells);
    }

    private class CuboidElement implements Element {
        private final Vector3i position;
        private final Vector3i size;
        private final Material material;

        private CuboidElement(Vector3i position, Vector3i size, Material material) {
            this.position = position;
            this.size = size;
            this.material = material;
        }

        @Override
        public void addTo(List<Cell> cells) {
            IndexUtils.range(position, new Vector3i(-1).add(position).add(size))
                    .map(expandedIndex -> IndexUtils.flattenIndex(expandedIndex, spaceSize))
                    .forEach(flatIndex -> cells.set(flatIndex, new Cell(15, 0, true, material)));
        }
    }

    private class IgnitionElement implements Element {
        private final Vector3i position;
        private final Vector3i size;

        private IgnitionElement(Vector3i position, Vector3i size) {
            this.position = position;
            this.size = size;
        }

        @Override
        public void addTo(List<Cell> cells) {
            IndexUtils.range(position, new Vector3i(-1).add(position).add(size))
                    .map(expandedIndex -> IndexUtils.flattenIndex(expandedIndex, spaceSize))
                    .filter(index -> cells.get(index).flammable())
                    .forEach(index -> {
                        var oldCell = cells.get(index);
                        cells.set(index, new Cell(400, 1, oldCell.flammable(), oldCell.material()));
                    });
        }
    }
}
