package pl.edu.agh.firecell.core.statebuilder.dialog.form;

import pl.edu.agh.firecell.core.statebuilder.ElementWrapper;

public interface ElementForm {
    void buildGui();

    ElementWrapper createElement();

    boolean matchesElement(ElementWrapper element);

    void setElement(ElementWrapper element);
}
