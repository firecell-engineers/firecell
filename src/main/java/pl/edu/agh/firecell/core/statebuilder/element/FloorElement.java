package pl.edu.agh.firecell.core.statebuilder.element;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joml.Vector2i;
import org.joml.Vector3i;
import pl.edu.agh.firecell.model.material.Material;

import java.util.Collections;
import java.util.List;

public class FloorElement extends AbstractCompositeElement {
    private final Vector2i position;
    private final Vector2i size;

    @JsonCreator
    public FloorElement(@JsonProperty("position") Vector2i position, @JsonProperty("size") Vector2i size) {
        this.size = size;
        this.position = position;
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
