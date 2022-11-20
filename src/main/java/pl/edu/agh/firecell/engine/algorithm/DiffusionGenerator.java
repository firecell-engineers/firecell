package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

public class DiffusionGenerator {
    private final double deltaTime;
    public DiffusionGenerator(double deltaTime){
        this.deltaTime = deltaTime;
    }

    public double temperatureUpdate(State oldState, Vector3i cellIndex, double currentTemperature) {
        int neighbourWeight = 1;
        int mainWeight = 20;
        return (NeighbourUtils.neighboursStream(cellIndex)
                .map(index -> oldState.hasCell(index)&&oldState.getCell(index).isFluid()?
                        oldState.getCell(index):
                        new Cell(currentTemperature, 0, true, Material.WOOD))
                .map(neighbourCell -> deltaTime*neighbourWeight*neighbourCell.temperature())
                .mapToDouble(Double::doubleValue).sum() + mainWeight*currentTemperature)/(6*deltaTime*neighbourWeight+mainWeight);
    }

}
