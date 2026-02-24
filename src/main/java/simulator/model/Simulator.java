package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable{

    //ATRIBUTOS
    private Factory<Animal> animalsFactory;
    private Factory<Region> regionsFactory;
    private RegionManager regionManager;
    private List<Animal> animals;
    private double time;

    //CONSTRUCTOR
    public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory, Factory<Region> regionsFactory) {
        this.animalsFactory = animalsFactory;
        this.regionsFactory = regionsFactory;
        this.regionManager = new RegionManager(cols, rows, width, height);
        this.animals = new ArrayList<>();
        this.time = 0.0;
    }


    //MÉTODOS DE REGIONES
    private void setRegion(int row, int col, Region r) {
        regionManager.setRegion(row, col, r);
    }

    public void setRegion(int row, int col, JSONObject rJson) {
        Region r = regionsFactory.createInstance(rJson);
        setRegion(row, col, r);
    }

    //MÉTODOS DE ANIMALES

    private void addAnimal(Animal a) {
        if (a != null) {
            animals.add(a);
            regionManager.registerAnimal(a);
        }
    }

    public void addAnimal(JSONObject aJson) {
        Animal a = animalsFactory.createInstance(aJson);
        addAnimal(a);
    }

    //GETTERS
    public MapInfo getMapInfo() {
        return regionManager;
    }

    public List<? extends AnimalInfo> getAnimals() {
        // Al usar "? extends AnimalInfo", Java permite devolver la lista de Animal 
        // pero tratada como solo lectura de información.
        return Collections.unmodifiableList(animals);
    }

    public double getTime() {
        return time;
    }


    // MOTOR DE SIMULACIÓN
    public void advance(double dt) {
        //avanzar tiempo
        time += dt;

        //quitar animales muertos
        //usamos lista auxiliar para no borrar elementos mientras iteramos
        List<Animal> deadAnimals = new ArrayList<>();
        for (Animal a : animals) {
            if (a.getState() == State.DEAD) {
                deadAnimals.add(a);
                regionManager.unregisterAnimal(a);
            }
        }
        animals.removeAll(deadAnimals);

        //para cada animal, update y y el gestor actualiza su región
        for (Animal a : animals) {
            a.update(dt);
            regionManager.updateAnimalRegion(a);
        }

        //actualizar todas las regiones
        regionManager.updateAllRegions(dt);

        //gestionar bebes
        //volvemos a usar lista auxiliar
        List<Animal> babies = new ArrayList<>();
        for (Animal a : animals) {
            if (a.isPregnant()) {
                Animal baby = a.deliverBaby();
                if (baby != null) {
                    babies.add(baby);
                }
            }
        }

        //añadir bebes a la simulación
        for (Animal baby : babies) {
            addAnimal(baby);
        }
    }

    //SALIDA JSON
    @Override
    public JSONObject asJSON() {
        JSONObject jo = new JSONObject();
        jo.put("time", time);
        jo.put("state", regionManager.asJSON()); 
        return jo;
    }



}
