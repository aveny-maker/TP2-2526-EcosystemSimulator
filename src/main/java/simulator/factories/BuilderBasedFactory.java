package simulator.factories;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {

    private Map<String, Builder<T>> builders;
    private List<JSONObject> buildersInfo;


    //CONSTRUCTORES
    //vacía
    public BuilderBasedFactory() {
        this.builders = new HashMap<>();
        this.buildersInfo = new LinkedList<>();
    }

    // constructora que recibe una lista de constructores
    public BuilderBasedFactory(List<Builder<T>> builders) {
        this(); // Llama a la constructora vacía para inicializar las listas
        for (Builder<T> b : builders) {
            addBuilder(b);
        }
    }


    //MÉTODOS
    // Añadir un nuevo constructor al diccionario
    public void addBuilder(Builder<T> b) {
        builders.put(b.getTypeTag(), b);
        buildersInfo.add(b.getInfo());
    }

    // Método para crear una instancia a partir de un JSONObject
    @Override
    public T createInstance(JSONObject info) {
        if (info == null) {
            throw new IllegalArgumentException("'info' cannot be null");
        }

        //buscamos el tipo de objeto que queremos crear
        String type = info.getString("type");
        
        // buscamos el constructor adecuado en nuestro mapa
        Builder<T> builder = builders.get(type);

        if (builder != null) {
            //extraemos los datos (si no hay "data", pasamos un JSON vacío)
            JSONObject data = info.has("data") ? info.getJSONObject("data") : new JSONObject();
            
            //pedimos al constructor que cree la instancia
            T instance = builder.createInstance(data);
            
            if (instance != null) {
                return instance;
            }
        }

        // Si no hay constructor para ese tipo o falló al crearlo
        throw new IllegalArgumentException("Unrecognized 'info': " + info.toString());
    }

    @Override
	public List<JSONObject> getInfo() {
		return Collections.unmodifiableList(buildersInfo);
	}

}
