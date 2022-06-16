package pl.edu.agh.firecell.model;

public record Cell(
        double temperature,
        double conductivityCoefficient,
        int burningTime,
        boolean flammable,
        MatterState type,
        Material material
        ) {
}
