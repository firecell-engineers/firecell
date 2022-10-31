package pl.edu.agh.firecell.renderer.rendermode;

import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.camera.Camera;
import pl.edu.agh.firecell.renderer.mesh.InstancedCubeMesh;
import pl.edu.agh.firecell.renderer.mesh.MeshUtils;
import pl.edu.agh.firecell.renderer.mesh.Shader;

import static org.lwjgl.opengl.GL11.glDepthMask;

public class StandardRenderStrategy extends RenderStrategy {

    private final Shader opaqueMaterialShader;
    private final Shader fireShader;

    public StandardRenderStrategy(Camera camera, Shader opaqueMaterialShader, Shader fireShader) {
        super(camera);
        this.opaqueMaterialShader = opaqueMaterialShader;
        this.fireShader = fireShader;
    }

    @Override
    public void renderState(State state) {
        renderSolidsByMaterial(state);
        renderFire(state);
    }

    private void renderSolidsByMaterial(State state) {
        var notAirCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> !indexedCell.cell().material().equals(Material.AIR))
                .toList();
        var mesh = new InstancedCubeMesh(MeshUtils.CUBE_VERTICES, notAirCellsList);
        opaqueMaterialShader.bind();
        mesh.draw();
    }

    private void renderFire(State state) {
        glDepthMask(false);
        var burningCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> indexedCell.cell().burningTime() > 0
                        && indexedCell.cell().material().equals(Material.AIR))
                .sorted(cameraDistanceCellComparator(camera.position()))
                .toList();
        var mesh = new InstancedCubeMesh(MeshUtils.CUBE_VERTICES, burningCellsList);
        fireShader.bind();
        mesh.draw();
        glDepthMask(true);
    }
}
