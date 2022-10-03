package pl.edu.agh.firecell.core.diagnostics;

import pl.edu.agh.firecell.model.Material;
import pl.edu.agh.firecell.model.State;

public class DiagnosticsManager {

    private final State state;
    private final double frameTime;

    private double totalTemperature;
    private double solidsTemperature;
    private double airTemperature;

    private int solidsCellsCount;
    private int airCellsCount;
    private int burningCellsCount;

    public DiagnosticsManager(State state, double frameTime) {
        this.frameTime = frameTime;
        this.state = state;
        processState();
    }

    public double framerate() {
        return 1.0 / frameTime;
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

    private void processState() {
        totalTemperature = 0.0;
        solidsTemperature = 0.0;
        airTemperature = 0.0;

        solidsCellsCount = 0;
        airCellsCount = 0;
        burningCellsCount = 0;

        state.cells().forEach(cell -> {
            totalTemperature += cell.temperature();

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
