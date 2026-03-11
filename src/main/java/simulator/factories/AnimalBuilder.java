package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public abstract class AnimalBuilder extends Builder<Animal> {
    // La hacemos protected para que los hijos puedan usarla para sus estrategias específicas
    protected Factory<SelectionStrategy> strategyFactory;

    public AnimalBuilder(String typeTag, String desc, Factory<SelectionStrategy> strategyFactory) {
        super(typeTag, desc);
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected void fillInData(JSONObject o) {
        // ponemos la información que es COMÚN a todos los animales
        o.put("mate_strategy", "mate strategy (optional)");
        o.put("pos", "position as { x_range: [x1, x2], y_range: [y1, y2] } (optional)");
    }

    @Override
    protected Animal createInstance(JSONObject data) {
        // leer la estrategia común (mate_strategy)
        SelectionStrategy mateStrategy = new SelectFirst();
        if (data.has("mate_strategy")) {
            mateStrategy = strategyFactory.createInstance(data.getJSONObject("mate_strategy"));
        }

        // leer la posición común
        Vector2D pos = null;
        if (data.has("pos")) {
            JSONObject posJson = data.getJSONObject("pos");
            JSONArray xRange = posJson.getJSONArray("x_range");
            JSONArray yRange = posJson.getJSONArray("y_range");

            double x = xRange.getDouble(0) + Utils.RAND.nextDouble() * (xRange.getDouble(1) - xRange.getDouble(0));
            double y = yRange.getDouble(0) + Utils.RAND.nextDouble() * (yRange.getDouble(1) - yRange.getDouble(0));
            
            pos = new Vector2D(x, y);
        }

        // le pasamos lo que hemos leído al hijo, junto con el JSON entero, 
        // por si el hijo tiene que leer sus estrategias específicas.
        return create_instance(mateStrategy, pos, data);
    }

    // Este es el método que obligamos a hacer a WolfBuilder y SheepBuilder
    protected abstract Animal create_instance(SelectionStrategy mateStrategy, Vector2D pos, JSONObject data);

}
