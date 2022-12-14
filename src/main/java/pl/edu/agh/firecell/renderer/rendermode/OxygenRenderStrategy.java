package pl.edu.agh.firecell.renderer.rendermode;

import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.camera.Camera;
import pl.edu.agh.firecell.renderer.mesh.InstancedCubeMesh;
import pl.edu.agh.firecell.renderer.mesh.MeshUtils;
import pl.edu.agh.firecell.renderer.mesh.Shader;

import static org.lwjgl.opengl.GL11.glDepthMask;

public class OxygenRenderStrategy extends RenderStrategy{
    private final Shader opaqueMaterialShader;
    private final Shader transparentOxygenShader;

    public OxygenRenderStrategy(Camera camera, Shader opaqueMaterialShader, Shader transparentOxygenShader) {
        super(camera);
        this.opaqueMaterialShader = opaqueMaterialShader;
        this.transparentOxygenShader = transparentOxygenShader;
    }

    @Override
    public void renderState(State state) {
        renderSolidsByMaterial(state);
        renderOxygenByLevel(state);
    }

    private void renderSolidsByMaterial(State state) {
        var notAirCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> !indexedCell.cell().material().equals(Material.AIR))
                .toList();
        var mesh = new InstancedCubeMesh(MeshUtils.CUBE_VERTICES, notAirCellsList);
        opaqueMaterialShader.bind();
        mesh.draw();
    }

    protected void renderOxygenByLevel(State state) {
        glDepthMask(false);
        var airCells = state.getIndexedCellsStream()
                .filter(indexedCell -> indexedCell.cell().material().equals(Material.AIR))
                .sorted(cameraDistanceCellComparator(camera.position()))
                .toList();
        var mesh = new InstancedCubeMesh(MeshUtils.CUBE_VERTICES, airCells);
        transparentOxygenShader.bind();
        mesh.draw();
        glDepthMask(true);
    }



}
