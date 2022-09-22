package pl.edu.agh.firecell.renderer;

import pl.edu.agh.firecell.model.State;

public interface Renderer {
    void render(State state, double frameTime);
    void setRenderMode(RenderMode renderMode);
    void dispose();
}
