package pl.edu.agh.firecell.renderer;

import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.renderer.rendermode.RenderMode;

public interface Renderer {
    void render(State state, double frameTime);
    void setRenderMode(RenderMode renderMode);
    void dispose();
}
