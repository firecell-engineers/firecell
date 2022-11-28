package pl.edu.agh.firecell.core.statebuilder.element;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joml.Vector2i;
import org.joml.Vector3i;
import pl.edu.agh.firecell.model.material.Material;

import java.util.List;

public class RoomElement extends AbstractCompositeElement {

    private final Vector2i position;
    private final Vector3i size;

    @JsonCreator
    public RoomElement(@JsonProperty("position") Vector2i position,
                       @JsonProperty("size") Vector3i size) {
        this.position = position;
        this.size = size;
    }

    @Override
    protected List<Element> getChildElements() {
        return List.of(
                new FloorElement(position, new Vector2i(size.x, size.z)),
                new CuboidElement(new Vector3i(position.x, 0, position.y), new Vector3i(size.x, size.y, 1), Material.WOOD),
                new CuboidElement(new Vector3i(position.x, 0, position.y), new Vector3i(1, size.y, size.z), Material.WOOD),
                new CuboidElement(new Vector3i(position.x + size.x - 1, 0, position.y), new Vector3i(1, size.y, size.z), Material.WOOD),
                new CuboidElement(new Vector3i(position.x, 0, position.y + size.z - 1), new Vector3i(size.x, size.y, 1), Material.WOOD)
        );
    }

    public Vector2i getPosition() {
        return position;
    }

    public Vector3i getSize() {
        return size;
    }
}
