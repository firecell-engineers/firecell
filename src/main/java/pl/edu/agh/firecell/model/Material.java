package pl.edu.agh.firecell.model;


import static pl.edu.agh.firecell.model.MatterState.FLUID;
import static pl.edu.agh.firecell.model.MatterState.SOLID;

public enum Material {
    // TODO:
    // consider putting them into constant
    AIR(0, 0, 0, FLUID),
    WOOD(3, 4, 0, SOLID);

    private final double ignitionTemperature;
    private final double autoIgnitionTemperature;
    private final double fireSpreadSpeed;
    private final MatterState matterState;

    Material(double ignitionTemperature, double autoIgnitionTemperature, double fireSpreadSpeed, MatterState matterState){
        this.ignitionTemperature = ignitionTemperature;
        this.autoIgnitionTemperature = autoIgnitionTemperature;
        this.fireSpreadSpeed = fireSpreadSpeed;
        this.matterState = matterState;
    }

    public double ignitionTemperature() {
        return ignitionTemperature;
    }

    public double autoIgnitionTemperature() {
        return autoIgnitionTemperature;
    }

    public double fireSpreadSpeed(){ return fireSpreadSpeed;}

    public MatterState getMatterState() {return matterState;}

}

