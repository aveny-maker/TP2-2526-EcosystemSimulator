package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends Builder<Animal>{

    private Factory<SelectionStrategy> strategyFactory;

    public WolfBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("wolf", "Wolf animal");
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected void fillInData(JSONObject o) {
        o.put("mate_strategy", "mate strategy (optional)");
        o.put("hunt_strategy", "hunt strategy (optional)");
        o.put("pos", "position as { x_range: [x1, x2], y_range: [y1, y2] } (optional)");
    }


    @Override
    protected Animal createInstance(JSONObject data) {
        //estrategias por defecto
        SelectionStrategy mateStrategy = new SelectFirst();
        SelectionStrategy huntStrategy = new SelectFirst();

        // leer estrategias
        if (data.has("mate_strategy")) {
            mateStrategy = strategyFactory.createInstance(data.getJSONObject("mate_strategy"));
        }
        if (data.has("hunt_strategy")) {
            huntStrategy = strategyFactory.createInstance(data.getJSONObject("hunt_strategy"));
        }

        //leer la posición
        Vector2D pos = null;
        if (data.has("pos")) {
            JSONObject posJson = data.getJSONObject("pos");
            JSONArray xRange = posJson.getJSONArray("x_range");
            JSONArray yRange = posJson.getJSONArray("y_range");

            double x = xRange.getDouble(0) + Utils.RAND.nextDouble() * (xRange.getDouble(1) - xRange.getDouble(0));
            double y = yRange.getDouble(0) + Utils.RAND.nextDouble() * (yRange.getDouble(1) - yRange.getDouble(0));
            
            pos = new Vector2D(x, y);
        }

        //construir y devolver el lobo
        return new Wolf(mateStrategy, huntStrategy, pos);
    }


}
