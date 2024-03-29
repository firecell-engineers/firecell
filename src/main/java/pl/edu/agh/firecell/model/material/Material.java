package pl.edu.agh.firecell.model.material;


import static pl.edu.agh.firecell.model.material.MatterState.FLUID;
import static pl.edu.agh.firecell.model.material.MatterState.SOLID;

public enum Material {
    // TODO:
    // consider putting them into constant
    AIR(0, 0, FLUID, 900, 0),
    WOOD(3, 250, SOLID, 550, 30),
    CELLULAR_CONCRETE(0, 0, SOLID, 0, 0);
    //Adding new Material remember to put it also to ConductionCoefficientProvider
    //and all switches :(

    private final double ignitionTemperature;
    private final double autoIgnitionTemperature;
    private final MatterState matterState;
    private final double burningTemperature;
    private final int smokeCoefficient;

    Material(double ignitionTemperature,
             double autoIgnitionTemperature,
             MatterState matterState,
             double burningTemperature,
             int smokeCoefficient) {
        this.ignitionTemperature = ignitionTemperature;
        this.autoIgnitionTemperature = autoIgnitionTemperature;
        this.matterState = matterState;
        this.burningTemperature = burningTemperature;
        this.smokeCoefficient = smokeCoefficient;
    }

    public double ignitionTemperature() {
        return ignitionTemperature;
    }

    public double autoIgnitionTemperature() {
        return autoIgnitionTemperature;
    }

    public int smokeCoe() {
        return smokeCoefficient;
    }

    public MatterState getMatterState() {
        return matterState;
    }

    public double getBurningTemperature() {
        return burningTemperature;
    }

}

