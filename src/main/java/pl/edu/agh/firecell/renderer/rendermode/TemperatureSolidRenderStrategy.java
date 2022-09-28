package pl.edu.agh.firecell.renderer.rendermode;

import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.camera.Camera;
import pl.edu.agh.firecell.renderer.mesh.InstancedCubeMesh;
import pl.edu.agh.firecell.renderer.mesh.MeshUtils;
import pl.edu.agh.firecell.renderer.mesh.Shader;

import static org.lwjgl.opengl.GL11.glDepthMask;

public class TemperatureSolidRenderStrategy extends RenderStrategy {

    private final Shader transparentTempShader;

    public TemperatureSolidRenderStrategy(Camera camera, Shader transparentTempShader) {
        super(camera);
        this.transparentTempShader = transparentTempShader;
    }

    @Override
    public void renderState(State state) {
        renderSolidsByTemperature(state);
    }

    protected void renderSolidsByTemperature(State state) {
        glDepthMask(false);
        var airCells = state.getIndexedCellsStream()
                .filter(indexedCell -> !indexedCell.cell().material().equals(Material.AIR))
                .sorted(cameraDistanceCellComparator(camera.position()))
                .toList();
        var mesh = new InstancedCubeMesh(MeshUtils.CUBE_VERTICES, airCells);
        transparentTempShader.bind();
        mesh.draw();
        glDepthMask(true);
    }
}
