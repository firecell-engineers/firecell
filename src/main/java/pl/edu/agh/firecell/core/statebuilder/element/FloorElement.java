package pl.edu.agh.firecell.core.statebuilder.element;

import org.joml.Vector2i;
import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Material;

import java.util.Collections;
import java.util.List;

public class FloorElement extends AbstractCompositeElement {
    private final Vector2i position;
    private final Vector2i size;

    public FloorElement(Vector2i position, Vector2i size) {
        this.position = position;
        this.size = size;
    }

    @Override
    protected List<Element> getChildElements() {
        return Collections.singletonList(new CuboidElement(new Vector3i(position.x, 0, position.y),
                new Vector3i(size.x, 1, size.y), Material.WOOD));
    }

    public Vector2i getPosition() {
        return position;
    }

    public Vector2i getSize() {
        return size;
    }
}
