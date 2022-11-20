package pl.edu.agh.firecell.model.material;

import pl.edu.agh.firecell.model.exception.ConductionCoefficientException;

import java.util.HashMap;
import java.util.Map;

public class MaterialConductionMap {

    private final Map<Pair, Double> coefficientMap;

    public MaterialConductionMap() {
        coefficientMap = new HashMap<>();
    //==================================================================================================================
        coefficientMap.put(new Pair(Material.AIR, Material.AIR),                                            /**/ 0.008);
        coefficientMap.put(new Pair(Material.AIR, Material.WOOD),                                            /**/ 0.02);
        coefficientMap.put(new Pair(Material.AIR, Material.CELLULAR_CONCRETE),                              /**/ 0.015);
    //==================================================================================================================
        coefficientMap.put(new Pair(Material.WOOD, Material.WOOD),                                            /**/ 0.2);
        coefficientMap.put(new Pair(Material.WOOD, Material.CELLULAR_CONCRETE),                              /**/ 0.15);
    //==================================================================================================================
        coefficientMap.put(new Pair(Material.CELLULAR_CONCRETE, Material.CELLULAR_CONCRETE),                  /**/ 0.1);
    //==================================================================================================================
        try {
            validate();
        } catch (ConductionCoefficientException e) {
            e.printStackTrace();
        }
    }

    public Double getCoefficient(Material m1, Material m2) {
        Pair p = m1.ordinal() < m2.ordinal()?new Pair(m1, m2):new Pair(m2, m1);
        return coefficientMap.get(p);
    }

    public int getNumOfCoefficients(){
        return coefficientMap.size();
    }

    private void validate() throws ConductionCoefficientException {
        int numOfMaterials = Material.values().length;
        for(Material m1: Material.values()){
            for(Material m2: Material.values()){
                if(getCoefficient(m1, m2) == null){
                    throw new ConductionCoefficientException(
                            "[checkConductionCoefficientProvider] There is lack of "
                                    + m1 + ", " + m2 + " material conduction coefficient");
                }
            }
        }
        if(getNumOfCoefficients() != (numOfMaterials*(numOfMaterials+1))/2)
            throw new ConductionCoefficientException(
                    "[checkConductionCoefficientProvider] There is incorrect number " +
                            "of relations between materials, expected value: " +
                            (numOfMaterials*(numOfMaterials+1))/2 + ", but got: " + getNumOfCoefficients());
    }
}

record Pair(Material left, Material right){}
