package pl.edu.agh.firecell.core.statebuilder.dialog;

public interface Dialog {
    void render();

    void setVisible(boolean visible);

    boolean isVisible();

    void setTitle(String title);
}