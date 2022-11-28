package pl.edu.agh.firecell.engine.algorithm;

import org.joml.Vector3i;
import pl.edu.agh.firecell.model.Cell;
import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.util.NeighbourUtils;

public class DiffusionGenerator {
    private final double deltaTime;

    public DiffusionGenerator(double deltaTime) {
        this.deltaTime = deltaTime;
    }

    public double smokeUpdate(State oldState, Vector3i cellIndex, double currentSmoke) {
        int upNeighbourWeight = 1;
        int downNeighbourWeight = 1;
        int mainWeight = 100;
        Vector3i indexAbove = NeighbourUtils.up(cellIndex);
        Vector3i indexUnder = NeighbourUtils.down(cellIndex);
        Cell underCell = oldState.hasCell(indexUnder) ? oldState.getCell(indexUnder) : null;
        Cell aboveCell = oldState.hasCell(indexAbove) ? oldState.getCell(indexAbove) : null;
        if ((underCell == null || !underCell.isFluid()) ||
                (aboveCell == null || !aboveCell.isFluid())) {
            return currentSmoke;
        }

        return (downNeighbourWeight * underCell.smokeIndicator()
                + mainWeight * currentSmoke
                + upNeighbourWeight * aboveCell.smokeIndicator())
                / (upNeighbourWeight + mainWeight + downNeighbourWeight);
    }

    public double temperatureUpdate(State oldState, Vector3i cellIndex, double currentTemperature) {
        int neighbourWeight = 1;
        int mainWeight = 20;
        return (NeighbourUtils.neighboursStream(cellIndex)
                .map(index -> oldState.hasCell(index) && oldState.getCell(index).isFluid() ?
                        oldState.getCell(index) :
                        new Cell(currentTemperature, 0, true, Material.WOOD))
                .map(neighbourCell -> deltaTime * neighbourWeight * neighbourCell.temperature())
                .mapToDouble(Double::doubleValue).sum() + mainWeight * currentTemperature) / (6 * deltaTime * neighbourWeight + mainWeight);
    }

    public double oxygenUpdate(State oldState, Vector3i cellIndex, double currentOxygen){
        double neighbourWeight = 1;
        double mainWeight = 1;
        double sumOfWeights = 6 * neighbourWeight + mainWeight;
        return currentOxygen - deltaTime * (currentOxygen - (NeighbourUtils.neighboursStream(cellIndex)
                .map(index -> oldState.hasCell(index) && oldState.getCell(index).isFluid() ?
                        oldState.getCell(index).oxygenLevel() * neighbourWeight/sumOfWeights : currentOxygen * neighbourWeight/sumOfWeights)
                .mapToDouble(Double::doubleValue).sum() + mainWeight/sumOfWeights * currentOxygen));
    }
}