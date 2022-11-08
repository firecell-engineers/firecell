package pl.edu.agh.firecell.core.statebuilder;

import pl.edu.agh.firecell.core.statebuilder.dialog.form.CuboidElementForm;
import pl.edu.agh.firecell.core.statebuilder.dialog.form.ElementForm;
import pl.edu.agh.firecell.core.statebuilder.dialog.form.FloorElementForm;
import pl.edu.agh.firecell.core.statebuilder.dialog.form.RoomElementForm;
import pl.edu.agh.firecell.core.statebuilder.element.CuboidElement;
import pl.edu.agh.firecell.core.statebuilder.element.Element;
import pl.edu.agh.firecell.core.statebuilder.element.FloorElement;
import pl.edu.agh.firecell.core.statebuilder.element.RoomElement;

import java.util.function.Supplier;

public enum ElementType {
    CUBOID(CuboidElementForm::new, CuboidElement.class),
    FLOOR(FloorElementForm::new, FloorElement.class),
    ROOM(RoomElementForm::new, RoomElement.class);

    private final Supplier<ElementForm> formFactory;
    private final Class<? extends Element> elementClass;

    ElementType(Supplier<ElementForm> formFactory, Class<? extends Element> elementClass) {
        this.formFactory = formFactory;
        this.elementClass = elementClass;
    }

    public Supplier<ElementForm> getFormFactory() {
        return formFactory;
    }

    public static ElementType determineType(Element element) {
        for (ElementType type : values()) {
            if (type.elementClass == element.getClass()) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported element: %s".formatted(element.getClass()));
    }
}
