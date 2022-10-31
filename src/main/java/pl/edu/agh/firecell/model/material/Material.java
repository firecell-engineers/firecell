package pl.edu.agh.firecell.model.material;


import static pl.edu.agh.firecell.model.material.MatterState.FLUID;
import static pl.edu.agh.firecell.model.material.MatterState.SOLID;

public enum Material {
    // TODO:
    // consider putting them into constant
    AIR(0, 0, 0, FLUID, 900),
    WOOD(3, 250, 0, SOLID, 550),
    CELLULAR_CONCRETE(10000, 10000, 0, SOLID, 0);
    //Adding new Material remember to put it also to ConductionCoefficientProvider
    //and all switches :(

    private final double ignitionTemperature;
    private final double autoIgnitionTemperature;
    private final double fireSpreadSpeed;
    private final MatterState matterState;
    private final double burningTemperature;

    Material(double ignitionTemperature,
             double autoIgnitionTemperature,
             double fireSpreadSpeed,
             MatterState matterState,
             double burningTemperature){
        this.ignitionTemperature = ignitionTemperature;
        this.autoIgnitionTemperature = autoIgnitionTemperature;
        this.fireSpreadSpeed = fireSpreadSpeed;
        this.matterState = matterState;
        this.burningTemperature = burningTemperature;
    }

    public double ignitionTemperature() {
        return ignitionTemperature;
    }

    public double autoIgnitionTemperature() {
        return autoIgnitionTemperature;
    }

    public double fireSpreadSpeed(){ return fireSpreadSpeed;}

    public MatterState getMatterState() {return matterState;}

    public double getBurningTemperature() {return burningTemperature;}

}

