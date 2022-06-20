package pl.edu.agh.firecell.model;

public enum Material {
    WOOD(MatterState.SOLID, 3, 4, 0),
    AIR(MatterState.FLUID, 0, 0, 0);

    private final MatterState matterState;
    private final double ignitionTemperature;
    private final double autoIgnitionTemperature;
    private final double fireSpreadSpeed;

    Material(MatterState matterState, double ignitionTemperature, double autoIgnitionTemperature, double fireSpreadSpeed){
        this.matterState = matterState;
        this.ignitionTemperature = ignitionTemperature;
        this.autoIgnitionTemperature = autoIgnitionTemperature;
        this.fireSpreadSpeed = fireSpreadSpeed;
    }

    public MatterState matterState() {
        return matterState;
    }

    public double ignitionTemperature() {
        return ignitionTemperature;
    }

    public double autoIgnitionTemperature() {
        return autoIgnitionTemperature;
    }

    public double fireSpreadSpeed(){
        return fireSpreadSpeed;
    }
}