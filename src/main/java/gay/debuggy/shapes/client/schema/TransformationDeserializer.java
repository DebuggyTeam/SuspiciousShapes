package gay.debuggy.shapes.client.schema;

import java.lang.reflect.Type;

import org.joml.Vector3f;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.MathHelper;

public class TransformationDeserializer implements JsonDeserializer<Transformation> {
	private static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
	private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
	private static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
	public static final float MAX_TRANSLATION = 5.0F;
	public static final float MAX_SCALE = 4.0F;

	public Transformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		JsonObject obj = jsonElement.getAsJsonObject();
		Vector3f rotation = this.parseVector3f(obj, "rotation", DEFAULT_ROTATION);
		Vector3f translation = this.parseVector3f(obj, "translation", DEFAULT_TRANSLATION);
		translation.mul(0.0625F);
		translation.set(MathHelper.clamp(translation.x, -5.0F, 5.0F), MathHelper.clamp(translation.y, -5.0F, 5.0F), MathHelper.clamp(translation.z, -5.0F, 5.0F));
		Vector3f scale = this.parseVector3f(obj, "scale", DEFAULT_SCALE);
		scale.set(MathHelper.clamp(scale.x, -4.0F, 4.0F), MathHelper.clamp(scale.y, -4.0F, 4.0F), MathHelper.clamp(scale.z, -4.0F, 4.0F));
		return new Transformation(rotation, translation, scale);
	}

	private Vector3f parseVector3f(JsonObject json, String key, Vector3f defaultValue) {
		if (!json.has(key)) {
			return defaultValue;
		} else {
			JsonArray elements = JsonHelper.getArray(json, key);
			if (elements.size() != 3) {
				throw new JsonParseException("Expected 3 " + key + " values, found: " + elements.size());
			} else {
				float[] elementsArray = new float[3];

				for(int i = 0; i < elementsArray.length; ++i) {
					elementsArray[i] = JsonHelper.asFloat(elements.get(i), key + "[" + i + "]");
				}

				return new Vector3f(elementsArray[0], elementsArray[1], elementsArray[2]);
			}
		}
	}
}
