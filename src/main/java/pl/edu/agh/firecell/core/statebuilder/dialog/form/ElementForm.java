package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;
import pl.edu.agh.firecell.model.StateBuilder;

public interface ElementForm {
    void buildGui();

    ElementWrapper createElement(StateBuilder stateBuilder);
}
