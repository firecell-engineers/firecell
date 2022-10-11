package pl.edu.agh.firecell.core.statebuilder;

import pl.edu.agh.firecell.core.statebuilder.dialog.form.CuboidElementForm;
import pl.edu.agh.firecell.core.statebuilder.dialog.form.ElementForm;

import java.util.function.Supplier;

public enum ElementType {
    CUBOID(CuboidElementForm::new);

    private final Supplier<ElementForm> formFactory;

    ElementType(Supplier<ElementForm> formFactory) {
        this.formFactory = formFactory;
    }

    public Supplier<ElementForm> getFormFactory() {
        return formFactory;
    }
}
