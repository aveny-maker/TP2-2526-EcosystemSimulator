package simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import simulator.factories.Factory;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

    //ATRIBUTOS
    private Factory<Animal> animalsFactory;
    private Factory<Region> regionsFactory;
    private RegionManager regionManager;
    private List<Animal> animals;
    private double time;
    private List<EcoSysObserver> observers; // Lista de observadores

    //CONSTRUCTOR
    public Simulator(int cols, int rows, int width, int height, Factory<Animal> animalsFactory, Factory<Region> regionsFactory) {
        this.animalsFactory = animalsFactory;
        this.regionsFactory = regionsFactory;
        this.regionManager = new RegionManager(cols, rows, width, height);
        this.animals = new ArrayList<>();
        this.time = 0.0;
        this.observers = new java.util.ArrayList<>();
    }


    //MÉTODOS DE REGIONES
    private void setRegion(int row, int col, Region r) {
        regionManager.setRegion(row, col, r);

        //notificación para los observadores
        notifyOnRegionSet(row, col, r);
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

        //notificación para los observadores
        notifyOnAnimalAdded(a);
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
            if (a.getState() == Animal.State.DEAD) {
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

        //notificación para los observadores
        notifyOnAdvance(dt);

    }



    //RESETEAR SIMULACIÓN
    public void reset(int cols, int rows, int width, int height){
        this.animals.clear();
        this.regionManager = new RegionManager(cols, rows, width, height);
        this.time = 0.0;

        //notificación para los observadores
        notifyOnReset();
    }

    //SALIDA JSON
    @Override
    public JSONObject asJSON() {
        JSONObject jo = new JSONObject();
        jo.put("time", time);
        jo.put("state", regionManager.asJSON()); 
        return jo;
    }



    //MÉTODOS PARA OBSERVADORES

    @Override
    public void addObserver(EcoSysObserver o) {
        if (!observers.contains(o)) {
            observers.add(o);
            o.onRegister(time, regionManager, new java.util.ArrayList<>(animals));
        }
    }

    @Override
    public void removeObserver(EcoSysObserver o) {
        observers.remove(o);
    }


    //MÉTODOS DE NOTIFICACIÓN

    private void notifyOnAdvance(double dt) {
        List<AnimalInfo> animalsInfo = new java.util.ArrayList<>(animals);
        for (EcoSysObserver o : observers) {
            o.onAdvance(time, regionManager, animalsInfo, dt);
        }
    }

    private void notifyOnReset() {
        List<AnimalInfo> animalsInfo = new java.util.ArrayList<>(animals);
        for (EcoSysObserver o : observers) {
            o.onReset(time, regionManager, animalsInfo);
        }
    }

    private void notifyOnAnimalAdded(Animal a) {
        List<AnimalInfo> animalsInfo = new java.util.ArrayList<>(animals);
        for (EcoSysObserver o : observers) {
            o.onAnimalAdded(time, regionManager, animalsInfo, a);
        }
    }

    private void notifyOnRegionSet(int row, int col, Region r) {
        // Aquí no hace falta copiar la lista de animales
        for (EcoSysObserver o : observers) {
            o.onRegionSet(row, col, regionManager, r);
        }
    }





}
