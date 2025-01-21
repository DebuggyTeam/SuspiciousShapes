package gay.debuggy.shapes.client.schema;

import java.util.HashMap;

import com.google.gson.annotations.SerializedName;

import net.minecraft.client.render.model.json.ModelTransformation;

public class BlockModelPlus {
	public String parent = null;
	
	@SerializedName("suspicious_shapes:loader")
	public String loader = null;
	public boolean uvlock = false;
	public HashMap<String, String> textures = new HashMap<>();
	public ModelTransformation display = ModelTransformation.NONE;
	public int[] colorIndexes = null;
}
