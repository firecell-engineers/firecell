package pl.edu.agh.firecell.core.statebuilder;

import org.joml.Vector3i;

import java.util.List;

public record StateBlueprint(Vector3i spaceSize, String name, List<ElementWrapper> elements) {
}
