package pl.edu.agh.firecell.core.statebuilder;

import pl.edu.agh.firecell.core.statebuilder.dialog.form.*;
import pl.edu.agh.firecell.core.statebuilder.element.*;

import java.util.function.Supplier;

public enum ElementType {
    CUBOID(CuboidElementForm::new, CuboidElement.class),
    FLOOR(FloorElementForm::new, FloorElement.class),
    ROOM(RoomElementForm::new, RoomElement.class),
    IGNITION(IgnitionElementForm::new, IgnitionElement.class);

    private final Supplier<ElementForm> formFactory;
    private final Class<? extends Element> elementClass;

    ElementType(Supplier<ElementForm> formFactory, Class<? extends Element> elementClass) {
        this.formFactory = formFactory;
        this.elementClass = elementClass;
    }

    public Supplier<ElementForm> getFormFactory() {
        return formFactory;
    }

    public static ElementType determineType(Class<? extends Element> elementClass) {
        for (ElementType type : values()) {
            if (type.elementClass == elementClass) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported element: %s".formatted(elementClass));
    }
}
