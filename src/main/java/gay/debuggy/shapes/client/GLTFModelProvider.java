package gay.debuggy.shapes.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.GsonBuilder;

import blue.endless.glow.model.gltf.GLTFLoader;
import gay.debuggy.shapes.client.schema.BlockModelPlus;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelResourceProvider;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class GLTFModelProvider implements ModelResourceProvider {
	private final ResourceManager resourceManager;
	
	public GLTFModelProvider(ResourceManager rm) {
		resourceManager = rm;
	}

	@Override
	public @Nullable UnbakedModel loadModelResource(Identifier resourceId, ModelProviderContext context) throws ModelProviderException {
		if (!resourceId.getPath().endsWith(".gltf")) {
			Optional<String> maybeChild = Resources.loadString(resourceManager, new Identifier(resourceId.getNamespace(), "models/"+resourceId.getPath()+".json"));
			if (maybeChild.isEmpty()) {
				return null; //Let vanilla handle it, and then throw an error
			}
			
			BlockModelPlus modelChild = new GsonBuilder().create().fromJson(maybeChild.get(), BlockModelPlus.class);
			if (modelChild.parent == null || !modelChild.parent.endsWith(".gltf")) {
				return null; //Let vanilla handle it
			}
			
			//Load vanillaPlus model
			System.out.println("Operating in blockmodel-plus mode.");
			Identifier parentId = new Identifier(modelChild.parent);
			Optional<String> maybeParent = Resources.loadString(resourceManager, new Identifier(parentId.getNamespace(), "models/"+parentId.getPath()));
			if (maybeParent.isEmpty()) {
				System.out.println("Couldn't find "+resourceId);
				// Provide the default missingno cube
				return context.loadModel(ModelLoader.MISSING_ID);
			}
			
			
			try {
				GlowUnbakedModel result = new GlowUnbakedModel(GLTFLoader.loadString(maybeParent.get()));
				result.provideTextures(modelChild.textures);
				return result;
				
			} catch (IOException e) {
				e.printStackTrace();
				return context.loadModel(ModelLoader.MISSING_ID);
			}
		}
		
		System.out.println("### ACTIVE: "+resourceId+" ###");
		
		Optional<Resource> maybeResource = resourceManager.getResource(new Identifier(resourceId.getNamespace(), "models/"+resourceId.getPath()));
		if (maybeResource.isEmpty()) {
			System.out.println("Couldn't find "+resourceId);
			return null;
		}
		
		try (InputStream in = maybeResource.get().open()) {
			
			String resData = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			System.out.println("Providing resource");
			return new GlowUnbakedModel(GLTFLoader.loadString(resData));
			
		} catch (IOException e) {
			System.out.println("FFFFFFFFFFFF - "+e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
}
