package simulator.control;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.AnimalInfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.Simulator;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {

    //ATRIBUTOS
    private Simulator sim;

    //CONSTRUCTOR
    public Controller(Simulator sim) {
        this.sim = sim;
    }

    //MÉTODOS

    //CARGA DE DATOS
    public void loadData(JSONObject data) {

        // cargamos las regiones (primero porque si un animal cae en una casilla sin región daría error)
        setRegions(data);


        // cargamos los animales
        setAnimals(data);
        
    }

    //MÉTODO PÚBLICO PARA CAMBIAR REGIONES
    public void setRegions(JSONObject rs) {
        if (rs.has("regions")) {
            JSONArray regions = rs.getJSONArray("regions");
            for (int i = 0; i < regions.length(); i++) {
                JSONObject rObj = regions.getJSONObject(i);

                JSONArray rowRange = rObj.getJSONArray("row");
                JSONArray colRange = rObj.getJSONArray("col");
                JSONObject spec = rObj.getJSONObject("spec");

                int rf = rowRange.getInt(0);
                int rt = rowRange.getInt(1);
                int cf = colRange.getInt(0);
                int ct = colRange.getInt(1);

                // Asignamos la region a todas las casillas del rango
                for (int r = rf; r <= rt; r++) {
                    for (int c = cf; c <= ct; c++) {
                        sim.setRegion(r, c, spec);
                    }
                }
            }
        }
    }

    //MÉTODO PÚBLICO PARA CAMBIAR ANIMALES
    public void setAnimals(JSONObject data) {
        if (data.has("animals")) {
            JSONArray animals = data.getJSONArray("animals");
            for (int i = 0; i < animals.length(); i++) {
                JSONObject aObj = animals.getJSONObject(i);
                int amount = aObj.getInt("amount");
                JSONObject spec = aObj.getJSONObject("spec");

                for (int j = 0; j < amount; j++) {
                    sim.addAnimal(spec);
                }
            }
        }
    }



    // MÉTODO AUXILIAR PARA EL VISOR
    private List<ObjInfo> toAnimalsInfo(List<? extends AnimalInfo> animals) {
        List<ObjInfo> ol = new ArrayList<>(animals.size());
        for (AnimalInfo a : animals) {
            // El tamaño del cuadrado dependerá de la edad del animal
            int size = (int) Math.round(a.getAge()) + 2;
            ol.add(new ObjInfo(a.getGeneticCode(), (int) a.getPosition().getX(), (int) a.getPosition().getY(),size));
        }
        return ol;
    }

    //EJECUCIÓN SIMULADOR
    public void run (double t, double dt, boolean sv, OutputStream out ) {
        //guardar estado inicial
        JSONObject initState = sim.asJSON();

        //configurar y abrir el visor si sv es true
        SimpleObjectViewer view = null;
        if (sv) {
            MapInfo m = sim.getMapInfo();
            view = new SimpleObjectViewer("[ECOSYSTEM]", m.getWidth(), m.getHeight(), m.getCols(), m.getRows());
            view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
        }

        //bucle principal simulación 
        while (sim.getTime() <= t) {
            sim.advance(dt);

            //si el visor esta activo, se actualiza en cada paso
            if (sv) {
                view.update(toAnimalsInfo(sim.getAnimals()), sim.getTime(), dt);
            }
               
        }

        //cerrar el visor (si se ha abierto)
        if (sv) {
            view.close();
        }

        //guardar estado final
        JSONObject finalState = sim.asJSON();


        //construir JSON de salida
        JSONObject result = new JSONObject();
        result.put("in", initState);
        result.put("out", finalState);

        //escribir resultado en el OutputStream
        PrintStream p = new PrintStream(out);
        p.println(result.toString(3));
    }


    //MÉTODOS PARA LOS OBSERVADORES
    public void addObserver(EcoSysObserver o) {
        sim.addObserver(o);
    }

    public void removeObserver(EcoSysObserver o) {
        sim.removeObserver(o);
    }

    public void reset(int cols, int rows, int width, int height) {
        sim.reset(cols, rows, width, height);
    }

    public void advance(double dt) {
        sim.advance(dt);
    }
}
