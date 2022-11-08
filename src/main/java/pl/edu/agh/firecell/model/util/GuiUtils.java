package pl.edu.agh.firecell.model.util;

import imgui.ImGui;

import java.util.function.Function;

public class GuiUtils {
    public static final String NULL_COMBOBOX_OPTION = "-- select --";

    public static <T> T comboBox(String label, Function<T, String> optionMapper, T currentElement, T[] elements) {
        T newElement = currentElement;
        if (ImGui.beginCombo(label, currentElement != null ? optionMapper.apply(currentElement) : NULL_COMBOBOX_OPTION)) {
            for (T element : elements) {
                boolean isSelected = newElement == element;
                if (ImGui.selectable(optionMapper.apply(element), isSelected)) {
                    newElement = element;
                }
                if (isSelected) {
                    ImGui.setItemDefaultFocus();
                }
            }
            ImGui.endCombo();
        }
        return newElement;
    }
}
