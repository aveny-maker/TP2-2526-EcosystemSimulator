package simulator.factories;

import org.json.JSONObject;

import simulator.misc.Vector2D;
import simulator.model.Animal;
import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;
import simulator.model.Wolf;

public class WolfBuilder extends AnimalBuilder{

    public WolfBuilder(Factory<SelectionStrategy> strategyFactory) {
        super("wolf", "Wolf animal", strategyFactory);
    }

    @Override
    protected void fillInData(JSONObject o) {
       super.fillInData(o); // esto ya lo pone el padre, mate_strategy y pos
       o.put("hunt_strategy", "hunt strategy (optional)"); // Y añadimos lo nuestro
    }


    @Override
    protected Animal create_instance(SelectionStrategy mateStrategy, Vector2D pos, JSONObject data) {
        // La estrategia común (mate) y la posición ya nos la da el padre ya calculadas.
        // Solo nos preocupamos de leer la nuestra propia:
        SelectionStrategy huntStrategy = new SelectFirst();
        if (data.has("hunt_strategy")) {
            huntStrategy = strategyFactory.createInstance(data.getJSONObject("hunt_strategy"));
        }

        // Construir y devolver el lobo
        return new Wolf(mateStrategy, huntStrategy, pos);
    }

}
