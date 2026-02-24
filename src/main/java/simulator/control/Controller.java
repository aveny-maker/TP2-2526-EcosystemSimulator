package simulator.control;
import java.io.OutputStream;
import java.io.PrintStream;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.Simulator;
// import simulator.view.SimpleObjectViewer; // Se importará cuando hagamos la vista

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
        if (data.has("regions")) {
            JSONArray regions =data.getJSONArray("regions");
            for (int i = 0; i <regions.length(); i++) {
                JSONObject rObj = regions.getJSONObject(i);

                JSONArray rowRange = rObj.getJSONArray("row");
                JSONArray colRange = rObj.getJSONArray("col");
                JSONObject spec = rObj.getJSONObject("spec");

                int rf = rowRange.getInt(0);
                int rt = rowRange.getInt(1);
                int cf = colRange.getInt(0);
                int ct = colRange.getInt(1);

                //asignamos la region a todas las casillas del rango
                for (int r = rf; r<=rt; r++) {
                    for (int c = cf; c<=ct; c++) {
                        sim.setRegion(r, c, spec);
                    }
                }
            }
        }


        // cargamos los animales
        if (data.has("animals")) {
            JSONArray animals = data.getJSONArray("animals");
            for (int i =0; i<animals.length(); i++) {
                JSONObject aObj = animals.getJSONObject(i);
                int amount = aObj.getInt("amount");
                JSONObject spec = aObj.getJSONObject("spec");

                //bucle para añadir n animales de esta especificación
                for (int j =0; j<amount; j++) {
                    sim.addAnimal(spec);
                }
            }
        }
    }

    //EJECUCIÓN SIMULADOR
    public void run (double t, double dt, boolean sv, OutputStream out ) {
        //guardar estado inicial
        JSONObject initState = sim.asJSON();

        //configurar y abrir el visor si sv es true
        /*
        SimpleObjectViewer view = null;
        if (sv) {
            view = new SimpleObjectViewer("[ECOSYSTEM]", 
                sim.getMapInfo().getWidth(), sim.getMapInfo().getHeight(), 
                sim.getMapInfo().getCols(), sim.getMapInfo().getRows());
            view.update(toListOfObjViewer(sim.getAnimals()), sim.getTime(), dt);
        }
        */

        //bucle principal simulación 
        while (sim.getTime() <= t) {
            sim.advance(dt);

            //si el visor esta activo, se actualiza en cada paso
            /*
            if (sv) {
                view.update(toListOfObjViewer(sim.getAnimals()), sim.getTime(), dt);
            }
            */
        }

        //cerrar el visor (si se ha abierto)
        /*
        if (sv) {
            view.close();
        }
        */

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
}
