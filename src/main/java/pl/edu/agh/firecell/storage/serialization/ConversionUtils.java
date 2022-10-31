package pl.edu.agh.firecell.storage.serialization;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.material.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.storage.proto.ProtoCell;
import pl.edu.agh.firecell.storage.proto.ProtoMaterial;
import pl.edu.agh.firecell.storage.proto.ProtoState;
import pl.edu.agh.firecell.storage.proto.ProtoVector3i;

import java.util.List;

public class ConversionUtils {

    public static ProtoState convertToProto(State state) {
        ProtoCell.Builder cellBuilder = ProtoCell.newBuilder();
        List<ProtoCell> cells = state.cells().stream().map(cell -> convertToProto(cellBuilder, cell)).toList();
        return ProtoState.newBuilder()
                .addAllCells(cells)
                .setSpaceSize(convertToProto(state.spaceSize()))
                .build();
    }

    public static Cell convertFromProto(ProtoCell proto) {
        return new Cell(proto.getTemperature(), proto.getBurningTime(), proto.getFlammable(), convertFromProto(proto.getMaterial()));
    }

    public static Material convertFromProto(ProtoMaterial proto) {
        return switch (proto) {
            case AIR -> Material.AIR;
            case WOOD -> Material.WOOD;
            case CELLULAR_CONCRETE -> Material.CELLULAR_CONCRETE;
            default -> throw new IllegalStateException("Unexpected value: " + proto);
        };
    }

    public static ProtoCell convertToProto(ProtoCell.Builder builder, Cell cell) {
        return builder
                .setTemperature(cell.temperature())
                .setBurningTime(cell.burningTime())
                .setFlammable(cell.flammable())
                .setMaterial(convertToProto(cell.material()))
                .build();
    }

    public static ProtoMaterial convertToProto(Material material) {
        return switch (material) {
            case WOOD -> ProtoMaterial.WOOD;
            case AIR -> ProtoMaterial.AIR;
            case CELLULAR_CONCRETE -> ProtoMaterial.CELLULAR_CONCRETE;
            default -> throw new IllegalStateException("Unexpected value: " + material);
        };
    }

    public static ProtoVector3i convertToProto(Vector3i vector) {
        return ProtoVector3i.newBuilder()
                .setX(vector.x)
                .setY(vector.y)
                .setZ(vector.z)
                .build();
    }

    public static State convertFromProto(ProtoState proto) {
        return new State(convertFromProto(proto.getCellsList()), convertFromProto(proto.getSpaceSize()));
    }

    public static Vector3i convertFromProto(ProtoVector3i proto) {
        return new Vector3i(proto.getX(), proto.getY(), proto.getZ());
    }

    public static List<Cell> convertFromProto(List<ProtoCell> proto) {
        return proto.stream().map(ConversionUtils::convertFromProto).toList();
    }
}
