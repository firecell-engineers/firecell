package pl.edu.agh.firecell.model;

import org.joml.Vector3i;
import pl.edu.agh.firecell.core.statebuilder.StateBuilder;

import java.util.List;

public record Room(Vector3i spaceSize, String name, List<StateBuilder.Element> elements) {
}
