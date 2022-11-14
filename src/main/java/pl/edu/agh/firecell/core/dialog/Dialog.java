package pl.edu.agh.firecell.core.dialog;

public interface Dialog {
    void render();

    void setVisible(boolean visible);

    void setTitle(String title);

    void setFlags(int flags);
}