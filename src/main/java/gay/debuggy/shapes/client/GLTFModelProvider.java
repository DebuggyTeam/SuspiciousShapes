package gay.debuggy.shapes.client;

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
import net.minecraft.client.render.model.ModelLoader;
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
	
	private static final Identifier GLTF_LOADER_KEY = new Identifier(SuspiciousShapesClient.MODID, "gltf");
	private static final Identifier OBJ_LOADER_KEY = new Identifier(SuspiciousShapesClient.MODID, "obj");
	
	private final ResourceManager resourceManager;
	
	public GLTFModelProvider(ResourceManager rm) {
		resourceManager = rm;
	}

	@Override
	public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
		return loadModelResource(resourceId, context, new HashSet<>());
	}
	
	public @Nullable UnbakedModel loadGltfResourceDirectly(Identifier resourceId, ModelProviderContext context, Set<String> alreadyTraversed) throws ModelProviderException {
		Optional<Resource> maybeResource = resourceManager.getResource(new Identifier(resourceId.getNamespace(), "models/"+resourceId.getPath()));
		if (maybeResource.isEmpty()) {
			return context.loadModel(ModelLoader.MISSING_ID);
		}
		
		try (InputStream in = maybeResource.get().open()) {
			
			String resData = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			return new GlowUnbakedModel(GLTFLoader.loadString(resData), resourceId);
			
		} catch (Exception e) {
			e.printStackTrace();
			return context.loadModel(ModelLoader.MISSING_ID);
		}
	}
	
	public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context, Set<String> alreadyTraversed) throws ModelProviderException {
		if (resourceId.toString().endsWith(".gltf")) return null; //STOP DOING THIS, VANILLA
		
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
		
		//if (modelChild.loader == null || !VALID_LOADER_KEYS.contains(modelChild.loader)) return null; // Require a valid "loader" key to prevent other mods from exploding
		
		Identifier parentId = new Identifier(modelChild.parent);
		if (alreadyTraversed.contains(parentId.toString())) {
			throw new ModelProviderException("Encountered a circular reference while resolving model '"+resourceId+"' - resources loaded: "+alreadyTraversed);
		}
		
		alreadyTraversed.add(parentId.toString());
		
		UnbakedModel parentModel = null;
		
		if (modelChild.loader!=null && modelChild.loader.equals(GLTF_LOADER_KEY.toString())) {
			parentModel = loadGltfResourceDirectly(parentId, context, alreadyTraversed);
		} else if (modelChild.loader!=null && modelChild.loader.equals(OBJ_LOADER_KEY.toString())) {
			parentModel = null; //TODO: Load Obj
		} else {
			parentModel = loadModelResource(parentId, context, alreadyTraversed);
		}
		
		if (parentModel == null) return null;
		
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
