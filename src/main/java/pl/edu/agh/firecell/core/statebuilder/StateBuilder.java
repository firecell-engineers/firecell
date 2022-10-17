package pl.edu.agh.firecell.core.statebuilder;

import org.joml.Vector3i;
import pl.edu.agh.firecell.core.statebuilder.element.CuboidElement;
import pl.edu.agh.firecell.core.statebuilder.element.Element;
import pl.edu.agh.firecell.core.statebuilder.element.IgnitionElement;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;

import java.util.ArrayList;
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
        elements.forEach(element -> element.addTo(cells, spaceSize));
        return new State(cells, spaceSize);
    }

    public Element getLastElement() {
        return elements.size() > 0 ? elements.get(elements.size() - 1) : null;
    }

    public void removeElement(Element element) {
        elements.remove(element);
    }

    public void addElement(Element element) {
        elements.add(element);
    }

    public StateBuilder addCuboid(Vector3i position, Vector3i size, Material material) {
        elements.add(new CuboidElement(position, size, material));
        return this;
    }

    public StateBuilder igniteCuboid(Vector3i position, Vector3i size) {
        elements.add(new IgnitionElement(position, size));
        return this;
    }

    private List<Cell> entireAir() {
        return new ArrayList<>(IntStream.range(0, spaceSize.x * spaceSize.y * spaceSize.z)
                .mapToObj(idx -> new Cell(20, 0, true, Material.AIR))
                .toList());
    }
}
