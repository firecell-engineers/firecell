package pl.edu.agh.firecell.model.material;

import pl.edu.agh.firecell.model.exception.ConductionCoefficientException;

import java.util.HashMap;
import java.util.Map;

public class MaterialConductionMap {

    private final Map<UnorderedMaterialPair, Double> coefficientMap;

    public MaterialConductionMap() throws ConductionCoefficientException {
        coefficientMap = new HashMap<>();
        //==================================================================================================================
        coefficientMap.put(new UnorderedMaterialPair(Material.AIR, Material.AIR),                                            /**/ 0.008);
        coefficientMap.put(new UnorderedMaterialPair(Material.AIR, Material.WOOD),                                            /**/ 0.02);
        coefficientMap.put(new UnorderedMaterialPair(Material.AIR, Material.CELLULAR_CONCRETE),                              /**/ 0.015);
        //==================================================================================================================
        coefficientMap.put(new UnorderedMaterialPair(Material.WOOD, Material.WOOD),                                            /**/ 0.2);
        coefficientMap.put(new UnorderedMaterialPair(Material.WOOD, Material.CELLULAR_CONCRETE),                              /**/ 0.15);
        //==================================================================================================================
        coefficientMap.put(new UnorderedMaterialPair(Material.CELLULAR_CONCRETE, Material.CELLULAR_CONCRETE),                  /**/ 0.1);
        //==================================================================================================================
        validate();
    }

    public double getCoefficient(Material m1, Material m2) {
        return coefficientMap.get(new UnorderedMaterialPair(m1, m2));
    }

    public int coefficientMapSize() {
        return coefficientMap.size();
    }

    private void validate() throws ConductionCoefficientException {
        int numOfMaterials = Material.values().length;
        if (coefficientMapSize() != (numOfMaterials * (numOfMaterials + 1)) / 2)
            throw new ConductionCoefficientException(
                    "[MaterialConductionMap] There is incorrect number " +
                            "of relations between materials, expected value: " +
                            (numOfMaterials * (numOfMaterials + 1)) / 2 + ", but got: " + coefficientMapSize());
    }
}
