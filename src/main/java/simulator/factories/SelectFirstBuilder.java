package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectFirst;
import simulator.model.SelectionStrategy;

public class SelectFirstBuilder extends Builder<SelectionStrategy> {

    public SelectFirstBuilder() {
        super("first", "Select first strategy");
    }

    @Override
    protected SelectionStrategy createInstance(JSONObject data) {
        // Como no necesita datos adicionales, simplemente devolvemos una nueva instancia
        return new SelectFirst();
    }

}
