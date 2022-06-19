package pl.edu.agh.firecell.model;

import org.joml.Vector3i;

public record SimulationConfig(
        Vector3i size,
        State initialState,
        double stepTime
) {}
