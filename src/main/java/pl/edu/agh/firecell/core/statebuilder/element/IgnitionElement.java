package pl.edu.agh.firecell.core.statebuilder.element;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.util.IndexUtils;

import java.util.List;

public record IgnitionElement(Vector3i position, Vector3i size) implements BaseCuboidElement {

    @Override
    public void addTo(List<Cell> cells, Vector3i spaceSize) {
        IndexUtils.range(position, new Vector3i(-1).add(position).add(size))
                .filter(expandedIndex -> IndexUtils.isInSpaceSize(expandedIndex, spaceSize))
                .map(expandedIndex -> IndexUtils.flattenIndex(expandedIndex, spaceSize))
                .filter(index -> cells.get(index).flammable())
                .forEach(index -> {
                    var oldCell = cells.get(index);
                    cells.set(index, new Cell(oldCell.material().getBurningTemperature(), 1,
                            oldCell.flammable(), oldCell.material(), oldCell.remainingFirePillar()));
                });
    }
}
