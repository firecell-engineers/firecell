package pl.edu.agh.firecell.core.diagnostics;

import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;

public class DiagnosticsManager {

    private State state;

    private double totalTemperature;
    private double solidsTemperature;
    private double airTemperature;
    private double totalSmokeValue;
    private double maxSmokeValue;
    private double minSmokeValue;

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
        return solidsTemperature / (double)solidsCellsCount;
    }

    public double averageAirTemperature() {
        return airTemperature / (double)airCellsCount;
    }

    public double totalSmokeValue(){ return totalSmokeValue; }

    public double minSmokeValue(){ return minSmokeValue; }

    public double maxSmokeValue(){ return maxSmokeValue; }

    private void processState() {
        totalTemperature = 0.0;
        solidsTemperature = 0.0;
        airTemperature = 0.0;
        totalSmokeValue = 0.0;
        maxSmokeValue = 0.0;
        minSmokeValue = 0.0;

        solidsCellsCount = 0;
        airCellsCount = 0;
        burningCellsCount = 0;

        state.cells().forEach(cell -> {
            totalTemperature += cell.temperature();
            totalSmokeValue += cell.smokeIndicator();

            maxSmokeValue = Math.max(maxSmokeValue, cell.smokeIndicator());
            minSmokeValue = Math.min(minSmokeValue, cell.smokeIndicator());

            if (cell.material().equals(Material.AIR)) {
                airTemperature += cell.temperature();
                airCellsCount += 1;
            }

            if (cell.isSolid()) {
                solidsTemperature += cell.temperature();
                solidsCellsCount += 1;
            }

            if (cell.burningTime() > 0) {
                burningCellsCount += 1;
            }
        });
    }
}
