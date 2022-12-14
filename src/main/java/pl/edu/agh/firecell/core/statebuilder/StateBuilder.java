package pl.edu.agh.firecell.core.statebuilder;

import org.joml.Vector3i;
import pl.edu.agh.firecell.core.statebuilder.element.CuboidElement;
import pl.edu.agh.firecell.core.statebuilder.element.Element;
import pl.edu.agh.firecell.core.statebuilder.element.IgnitionElement;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;

import java.util.ArrayList;
import java.util.List;

import static pl.edu.agh.firecell.core.util.StateUtils.entireAir;

// TODO: FIRE-22: Tests for StateBuilder
public class StateBuilder {

    private Vector3i spaceSize;
    private final List<Element> elements = new ArrayList<>();

    public StateBuilder(Vector3i spaceSize) {
        this.spaceSize = spaceSize;
    }

    public State build() {
        List<Cell> cells = entireAir(spaceSize);
        elements.forEach(element -> element.addTo(cells, spaceSize));
        return new State(cells, spaceSize);
    }

    public void setSpaceSize(Vector3i spaceSize) {
        this.spaceSize = spaceSize;
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

    public void clear() {
        elements.clear();
    }
}
