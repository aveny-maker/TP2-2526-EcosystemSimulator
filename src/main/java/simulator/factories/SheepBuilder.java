package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Sheep;

public class SheepBuilder extends AnimalBuilder{

    public SheepBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("sheep", "Sheep animal", strategyFactory);
    }

    @Override
    protected void fillInData(JSONObject o) {
        super.fillInData(o); // Esto añade lo del padre (mate_strategy y pos)
        o.put("danger_strategy", "danger strategy (optional)"); // Y añadimos lo nuestro
    }

    @Override
    protected Animal create_instance(SelectionStrategy mateStrategy, Vector2D pos, JSONObject data) {
        // La estrategia común (mate) y la posición ya nos la da el padre.
        // Solo nos preocupamos de leer la nuestra propia:
        SelectionStrategy dangerStrategy = new SelectFirst();
        if (data.has("danger_strategy")) {
            dangerStrategy = strategyFactory.createInstance(data.getJSONObject("danger_strategy"));
        }

        // Crear y devolver la oveja
        return new Sheep(mateStrategy, dangerStrategy, pos);
    }
}
