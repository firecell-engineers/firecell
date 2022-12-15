package pl.edu.agh.firecell.core.dialog;

import imgui.ImGui;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RoomListDialog extends AbstractDialog {

    private final List<String> savedRoomNames;
    private final Consumer<String> selectHandler;
    private String selectedRoom = null;

    public RoomListDialog(String title, List<String> savedRooms, Consumer<String> selectHandler) {
        super(title);
        this.savedRoomNames = savedRooms;
        this.selectHandler = selectHandler;
    }

    @Override
    protected void buildGui() {
        if (ImGui.beginListBox("Saved rooms##listbox")) {
            for (String roomName : savedRoomNames) {
                if (ImGui.selectable(roomName, Objects.equals(selectedRoom, roomName))) {
                    selectedRoom = roomName;
                }
            }
        }
        ImGui.endListBox();
        if (ImGui.button("Select") && selectedRoom != null) {
            selectHandler.accept(selectedRoom);
        }
        if (ImGui.button("Cancel")) {
            selectHandler.accept(null);
        }
    }
}
