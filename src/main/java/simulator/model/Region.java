package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public abstract class Region implements Entity, RegionInfo, FoodSupplier {
    protected List<Animal> animals;

    //CONSTRUCTOR
    public Region() {
        this.animals = new LinkedList<>();
    }

    //MÉTODOS
    public final void addAnimal(Animal a) {
        if (a != null && !animals.contains(a)) {
            animals.add(a);
        }
    }

    public final void removeAnimal(Animal a) {
        if (a != null) {
            animals.remove(a);
        }
    }

    public final List<Animal> getAnimals() {
        return Collections.unmodifiableList(animals);
    }

    @Override
    public void update(double dt) {
        // Vacío por defecto
    }

    @Override
    public List<AnimalInfo> getAnimalsInfo() {
        return new ArrayList<>(animals); 
    }

    @Override
    public JSONObject asJSON() {
        JSONObject jo = new JSONObject();
        JSONArray ja = new JSONArray();
        for (Animal a : animals) {
            ja.put(a.asJSON());
        }
        jo.put("animals", ja);
        return jo;
    }


}
