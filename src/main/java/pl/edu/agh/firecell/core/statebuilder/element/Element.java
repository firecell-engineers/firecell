package pl.edu.agh.firecell.core.statebuilder.element;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;

import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "@type")
@JsonSubTypes({
        @Type(value = CuboidElement.class, name = "cuboid")
})
public interface Element {
    void addTo(List<Cell> cells, Vector3i spaceSize);
}
