package gay.debuggy.shapes.client.gltf;

import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GltfProperty {
	// extension values MUST be objects
	public HashMap<String, JsonObject> extensions = new HashMap<>();
	
	// extras values SHOULD be objects, but don't have to be
	public HashMap<String, JsonElement> extras = new HashMap<>();
	
	public GltfProperty() {}
}
