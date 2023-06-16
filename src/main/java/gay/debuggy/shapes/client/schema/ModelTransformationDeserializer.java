package gay.debuggy.shapes.client.schema;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;

public class ModelTransformationDeserializer implements JsonDeserializer<ModelTransformation> {
	public ModelTransformation deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = jsonElement.getAsJsonObject();
		Transformation rightHand = this.parseModelTransformation(context, obj, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND);
		Transformation leftHand  = this.parseModelTransformation(context, obj, ModelTransformationMode.THIRD_PERSON_LEFT_HAND);
		if (leftHand == Transformation.IDENTITY) {
			leftHand = rightHand;
		}

		Transformation firstPersonRight = this.parseModelTransformation(context, obj, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND);
		Transformation firstPersonLeft  = this.parseModelTransformation(context, obj, ModelTransformationMode.FIRST_PERSON_LEFT_HAND);
		if (firstPersonLeft == Transformation.IDENTITY) {
			firstPersonLeft = firstPersonRight;
		}

		Transformation head = this.parseModelTransformation(context, obj, ModelTransformationMode.HEAD);
		Transformation gui = this.parseModelTransformation(context, obj, ModelTransformationMode.GUI);
		Transformation ground = this.parseModelTransformation(context, obj, ModelTransformationMode.GROUND);
		Transformation fixed = this.parseModelTransformation(context, obj, ModelTransformationMode.FIXED);
		return new ModelTransformation(
			leftHand, rightHand, firstPersonLeft, firstPersonRight, head, gui, ground, fixed
		);
	}

	private Transformation parseModelTransformation(JsonDeserializationContext ctx, JsonObject json, ModelTransformationMode mode) {
		String modeName = mode.asString();
		return json.has(modeName) ? ctx.deserialize(json.get(modeName), Transformation.class) : Transformation.IDENTITY;
	}
}