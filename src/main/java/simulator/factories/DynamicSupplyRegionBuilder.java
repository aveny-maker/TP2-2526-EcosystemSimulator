package simulator.factories;

import org.json.JSONObject;

import simulator.model.DynamicSupplyRegion;
import simulator.model.Region;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

    public DynamicSupplyRegionBuilder() {
        super("dynamic", "Dynamic supply region");
    }

    @Override
    protected void fillInData(JSONObject o) {
        o.put("factor", "food increase factor (optional, default 2.0)");
        o.put("food", "initial amount of food (optional, default 100.0)");
    }

    @Override
    protected Region createInstance(JSONObject data) {
        // Valores por defecto
        double factor = 2.0;
        double food = 100.0;

        // Leer del JSON si existen
        if (data.has("factor")) {
            factor = data.getDouble("factor");
        }
        if (data.has("food")) {
            food = data.getDouble("food");
        }

        return new DynamicSupplyRegion(food, factor);
    }


}
