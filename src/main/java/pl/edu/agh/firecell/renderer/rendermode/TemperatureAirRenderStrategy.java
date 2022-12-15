package pl.edu.agh.firecell.renderer.rendermode;

import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.camera.Camera;
import pl.edu.agh.firecell.renderer.mesh.InstancedCubeMesh;
import pl.edu.agh.firecell.renderer.mesh.MeshUtils;
import pl.edu.agh.firecell.renderer.mesh.Shader;

import static org.lwjgl.opengl.GL11.glDepthMask;

public class TemperatureAirRenderStrategy extends RenderStrategy {

    private final Shader opaqueMaterialShader;
    private final Shader transparentTempShader;

    public TemperatureAirRenderStrategy(Camera camera, Shader opaqueMaterialShader, Shader transparentTempShader) {
        super(camera);
        this.opaqueMaterialShader = opaqueMaterialShader;
        this.transparentTempShader = transparentTempShader;
    }

    @Override
    public void renderState(State state) {
        renderSolidsByMaterial(state);
        renderAirByTemperature(state);
    }

    private void renderSolidsByMaterial(State state) {
        var notAirCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> !indexedCell.cell().material().equals(Material.AIR))
                .toList();
        var mesh = new InstancedCubeMesh(MeshUtils.CUBE_VERTICES, notAirCellsList);
        opaqueMaterialShader.bind();
        mesh.draw();
    }

    protected void renderAirByTemperature(State state) {
        glDepthMask(false);
        var airCells = state.getIndexedCellsStream()
                .filter(indexedCell -> indexedCell.cell().material().equals(Material.AIR))
                .sorted(cameraDistanceCellComparator(camera.position()))
                .toList();
        var mesh = new InstancedCubeMesh(MeshUtils.CUBE_VERTICES, airCells);
        transparentTempShader.bind();
        mesh.draw();
        glDepthMask(true);
    }
}
