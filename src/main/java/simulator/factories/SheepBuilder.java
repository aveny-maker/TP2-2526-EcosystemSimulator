package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends Builder<Animal> {

    //necesitamos la factoría de estrategias para construir sus estrategias internas
    private Factory<SelectionStrategy> strategyFactory;

    public SheepBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("sheep", "Sheep animal");
        this.strategyFactory = strategyFactory;
    }

    @Override
    protected void fillInData(JSONObject o) {
        o.put("mate_strategy", "mate strategy (optional)");
        o.put("danger_strategy", "danger strategy (optional)");
        o.put("pos", "position as { x_range: [x1, x2], y_range: [y1, y2] } (optional)");
    }

    @Override
    protected Animal createInstance(JSONObject data) {
        // estrategios por defecto
        SelectionStrategy mateStrategy = new SelectFirst();
        SelectionStrategy dangerStrategy = new SelectFirst();

        // leer estrategias del JSON si existen
        if (data.has("mate_strategy")) {
            mateStrategy = strategyFactory.createInstance(data.getJSONObject("mate_strategy"));
        }
        if (data.has("danger_strategy")) {
            dangerStrategy = strategyFactory.createInstance(data.getJSONObject("danger_strategy"));
        }

        //leer y calcular posición
        Vector2D pos = null;
        if (data.has("pos")) {
            JSONObject posJson = data.getJSONObject("pos");
            JSONArray xRange = posJson.getJSONArray("x_range");
            JSONArray yRange = posJson.getJSONArray("y_range");

            // Generar un número aleatorio dentro de los rangos [min, max]
            double x = xRange.getDouble(0) + Utils.RAND.nextDouble() * (xRange.getDouble(1) - xRange.getDouble(0));
            double y = yRange.getDouble(0) + Utils.RAND.nextDouble() * (yRange.getDouble(1) - yRange.getDouble(0));

            pos = new Vector2D(x, y);
        }

        //crear y devolver la oveja
        return new Sheep(mateStrategy, dangerStrategy, pos);
    }




}
