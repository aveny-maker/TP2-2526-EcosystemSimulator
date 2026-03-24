package simulator.factories;
 
import org.json.JSONObject;

import simulator.model.DefaultRegion;
import simulator.model.Region;
 
public class DefaultRegionBuilder extends Builder<Region> {

    public DefaultRegionBuilder() {
        super("default", "Default region");
    }

    @Override
    protected Region createInstance(JSONObject data) {
        return new DefaultRegion();
    }  

    @Override
    protected void fillInData(JSONObject o) {
        // La default no necesita parámetros extra en data, pero hay que avisar de la descripción
        // No hace falta meter nada en o para los datos si no requiere nada.
    }

}
