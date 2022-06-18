package pl.edu.agh.firecell.model;

public record Cell(
        double temperature,
        double conductivityCoefficient,
        int burningTime,
        boolean flammable,
        Material material
) {
}
