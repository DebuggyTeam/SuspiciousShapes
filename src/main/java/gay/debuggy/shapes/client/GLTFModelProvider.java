package gay.debuggy.shapes.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.google.gson.GsonBuilder;

import blue.endless.glow.model.ShaderAttribute;
import blue.endless.glow.model.gltf.GLTFLoader;
import gay.debuggy.shapes.client.schema.BlockModelPlus;
import gay.debuggy.shapes.client.schema.ModelTransformationDeserializer;
import gay.debuggy.shapes.client.schema.TransformationDeserializer;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;



public class GLTFModelProvider implements ModelResourceProvider {
	private final Set<String> FORBIDDEN_PARENTS = Set.of(
			"minecraft:item/generated",
			"minecraft:builtin/generated",
			"item/generated",
			"builtin/generated"
			);
	private final ResourceManager resourceManager;
	
	public GLTFModelProvider(ResourceManager rm) {
		resourceManager = rm;
	}

	@Override
	public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
		return loadModelResource(resourceId, context, new HashSet<>());
	}
		
	public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context, Set<String> alreadyTraversed) throws ModelProviderException {
		if (resourceId.getPath().endsWith(".gltf")) {
			// Simple glTF model load
			
			Optional<Resource> maybeResource = resourceManager.getResource(new Identifier(resourceId.getNamespace(), "models/"+resourceId.getPath()));
			if (maybeResource.isEmpty()) {
				return null;
			}
			
			try (InputStream in = maybeResource.get().open()) {
				
				String resData = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				return new GlowUnbakedModel(GLTFLoader.loadString(resData), resourceId);
				
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		
		} else if (resourceId.getPath().endsWith(".obj")) {
			// obj model - no .mtl will be loaded!
			
			@SuppressWarnings("unused")
			Optional<Resource> maybeResource = resourceManager.getResource(new Identifier(resourceId.getNamespace(), "models/"+resourceId.getPath()));
			
			// TODO: Obj loader
			
			return null;
			
		} else {
			
			Optional<String> maybeChild = Resources.loadString(resourceManager, new Identifier(resourceId.getNamespace(), "models/"+resourceId.getPath()+".json"));
			if (maybeChild.isEmpty()) {
				return null; //Let vanilla handle it, and then throw an error
			}
			
			BlockModelPlus modelChild = new GsonBuilder()
					//These type adapters are cleaned-up copies of Mojang code since it's private
					.registerTypeAdapter(Transformation.class, new TransformationDeserializer())
					.registerTypeAdapter(ModelTransformation.class, new ModelTransformationDeserializer())
					.create().fromJson(maybeChild.get(), BlockModelPlus.class);
			if (modelChild.parent == null || FORBIDDEN_PARENTS.contains(modelChild.parent)) {
				return null; //Let vanilla handle it
			}
			
			Identifier parentId = new Identifier(modelChild.parent);
			if (alreadyTraversed.contains(parentId.toString())) {
				throw new ModelProviderException("Encountered a circular reference while resolving model '"+resourceId+"' - resources loaded: "+alreadyTraversed);
			}
			alreadyTraversed.add(parentId.toString());
			
			UnbakedModel parentModel = loadModelResource(parentId, context, alreadyTraversed);
			//UnbakedModel parentModel = context.loadModel(new Identifier(modelChild.parent));
			if (parentModel instanceof GlowUnbakedModel glowParent) {
				// Operating in blockmodel-plus mode since an ancestor was ours
				glowParent.provideTextures(modelChild.textures);
				if (modelChild.display != null) glowParent.setModelTransformation(modelChild.display);
				if (modelChild.colorIndexes != null) {
					for(int i=0; i<modelChild.colorIndexes.length; i++) {
						int colorIndex = modelChild.colorIndexes[i];
						if (i >= glowParent.model.getMeshes().size()) break;
						glowParent.model.getMeshes().get(i).getMaterial().put(ShaderAttribute.COLOR_INDEX, colorIndex);
					}
				}
				
				return glowParent;
			} else {
				return null;
			}
			
		}
	}
}
