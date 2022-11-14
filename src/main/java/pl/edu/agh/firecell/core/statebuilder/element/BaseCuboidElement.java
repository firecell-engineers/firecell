package pl.edu.agh.firecell.core.statebuilder.element;

import org.joml.Vector3i;

public interface BaseCuboidElement extends Element {
    Vector3i position();

    Vector3i size();
}
