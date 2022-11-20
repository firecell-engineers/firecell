package pl.edu.agh.firecell.core.statebuilder.element;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;

import java.util.List;

public abstract class AbstractCompositeElement implements Element {

    @Override
    public void addTo(List<Cell> cells, Vector3i spaceSize) {
        getChildElements().forEach(element -> element.addTo(cells, spaceSize));
    }

    protected abstract List<Element> getChildElements();
}
