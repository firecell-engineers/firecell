package pl.edu.agh.firecell.renderer.rendermode;

import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.camera.Camera;
import pl.edu.agh.firecell.renderer.mesh.InstancedCubeMesh;
import pl.edu.agh.firecell.renderer.mesh.MeshUtils;
import pl.edu.agh.firecell.renderer.mesh.Shader;

import static org.lwjgl.opengl.GL11.glDepthMask;

public class StandardRenderStrategy extends RenderStrategy {

    private final Shader opaqueMaterialShader;
    private final Shader fireShader;
    private final Shader smokeShader;

    public StandardRenderStrategy(Camera camera, Shader opaqueMaterialShader, Shader fireShader, Shader smokeShader) {
        super(camera);
        this.opaqueMaterialShader = opaqueMaterialShader;
        this.fireShader = fireShader;
        this.smokeShader = smokeShader;
    }

    @Override
    public void renderState(State state) {
        renderSolidsByMaterial(state);
        renderSmoke(state);
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

    private void renderSmoke(State state) {
        glDepthMask(false);
        var smokeCellsList = state.getIndexedCellsStream()
                .filter(indexedCell -> indexedCell.cell().material().equals(Material.AIR))
                .filter(indexedCell -> indexedCell.cell().smokeIndicator() > 0 && indexedCell.cell().smokeIndicator() <= 100)
                .toList();
        var mesh = new InstancedCubeMesh(MeshUtils.CUBE_VERTICES, smokeCellsList);
        smokeShader.bind();
        mesh.draw();
        glDepthMask(true);
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
