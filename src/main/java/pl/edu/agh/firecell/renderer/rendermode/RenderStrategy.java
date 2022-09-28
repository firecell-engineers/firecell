package pl.edu.agh.firecell.renderer.rendermode;

import org.joml.Vector3f;
import pl.edu.agh.firecell.model.IndexedCell;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.camera.Camera;

import java.util.Comparator;

public abstract class RenderStrategy {

    protected final Camera camera;

    public RenderStrategy(Camera camera) {
        this.camera = camera;
    }

    public abstract void renderState(State state);

    protected Comparator<IndexedCell> cameraDistanceCellComparator(Vector3f cameraPosition) {
        return Comparator.comparingDouble(
                indexedCell -> new Vector3f(indexedCell.index()).distance(cameraPosition)
        );
    }
}
