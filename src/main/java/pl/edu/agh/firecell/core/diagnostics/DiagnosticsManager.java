package pl.edu.agh.firecell.core.diagnostics;

import pl.edu.agh.firecell.engine.algorithm.AlgorithmUtils;
import pl.edu.agh.firecell.model.State;
import pl.edu.agh.firecell.model.material.Material;

public class DiagnosticsManager {

    private State state;

    private double totalTemperature;
    private double solidsTemperature;
    private double airTemperature;
    private double totalSmokeValue;
    private double maxSmokeValue;
    private double minSmokeValue;
    private double totalOxygenValue;
    private double maxOxygenValue;
    private double minOxygenValue;

    private int solidsCellsCount;
    private int airCellsCount;
    private int burningCellsCount;

    public DiagnosticsManager(State state) {
        updateState(state);
    }

    public void updateState(State state) {
        this.state = state;
        processState();
    }

    public double totalTemperature() {
        return totalTemperature;
    }

    public double solidsTemperature() {
        return solidsTemperature;
    }

    public double airTemperature() {
        return airTemperature;
    }

    public int burningCellsCount() {
        return burningCellsCount;
    }

    public double averageTemperature() {
        return totalTemperature / state.cells().size();
    }

    public double averageSolidsTemperature() {
        return solidsTemperature / (double) solidsCellsCount;
    }

    public double averageAirTemperature() {
        return airTemperature / (double) airCellsCount;
    }

    public double totalSmokeValue() {
        return totalSmokeValue;
    }

    public double minSmokeValue() {
        return minSmokeValue;
    }

    public double maxSmokeValue() {
        return maxSmokeValue;
    }

    public double totalOxygenValue() {
        return totalOxygenValue;
    }

    public double minOxygenValue() {
        return minOxygenValue;
    }

    public double maxOxygenValue() {
        return maxOxygenValue;
    }

    private void processState() {
        totalTemperature = 0.0;
        solidsTemperature = 0.0;
        airTemperature = 0.0;
        totalSmokeValue = 0.0;
        maxSmokeValue = 0.0;
        minSmokeValue = Double.MAX_VALUE;
        totalOxygenValue = 0.0;
        maxOxygenValue = 0.0;
        minOxygenValue = Double.MAX_VALUE;

        solidsCellsCount = 0;
        airCellsCount = 0;
        burningCellsCount = 0;

        state.cells().forEach(cell -> {
            totalTemperature += cell.temperature();

            if (cell.material().equals(Material.AIR)) {
                airTemperature += cell.temperature();
                airCellsCount += 1;
                totalSmokeValue += cell.smokeIndicator();
                totalOxygenValue += cell.oxygenLevel();
                maxOxygenValue = Math.max(maxOxygenValue, cell.oxygenLevel());
                minOxygenValue = Math.min(minOxygenValue, cell.oxygenLevel());
                maxSmokeValue = Math.max(maxSmokeValue, cell.smokeIndicator());
                minSmokeValue = Math.min(minSmokeValue, cell.smokeIndicator());
            }

            if (cell.isSolid()) {
                solidsTemperature += cell.temperature();
                solidsCellsCount += 1;
            }

            if (AlgorithmUtils.isCellBurning(cell)) {
                burningCellsCount += 1;
            }
        });
    }

}
