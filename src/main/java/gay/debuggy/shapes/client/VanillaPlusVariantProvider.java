package gay.debuggy.shapes.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import blue.endless.glow.model.Matrix4d;
import blue.endless.glow.model.Mesh;
import blue.endless.glow.model.Model;
import blue.endless.glow.model.gltf.GLTFLoader;
import net.fabricmc.fabric.api.client.model.ModelProviderContext;
import net.fabricmc.fabric.api.client.model.ModelProviderException;
import net.fabricmc.fabric.api.client.model.ModelVariantProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;

class VanillaPlusVariantProvider implements ModelVariantProvider {
	
	private @Nullable ResourceManager resourceManager = null;
	private Gson gson = new GsonBuilder().create();
	
	public VanillaPlusVariantProvider attachResourceManager(ResourceManager rm) {
		resourceManager = rm;
		return this;
	}
	
	@Override
	public @Nullable UnbakedModel loadModelVariant(ModelIdentifier modelId, ModelProviderContext context) throws ModelProviderException {
		
		Identifier itemId = new Identifier(modelId.getNamespace(), modelId.getPath());
		String variant = modelId.getVariant();
		
		System.out.println(variant);
		
		/*
		if (variant.equals("inventory")) {
			Item item = Registries.ITEM.get(itemId);
			if (item instanceof BlockItem blockItem) {
				Block block = blockItem.getBlock();
				Identifier blockId = Registries.BLOCK.getId(block); //Usually the same as itemId BUT NOT REQUIRED TO BE
				
				//load up the blockstate json
				VanillaPlusBlockState.Variant blockStateDesc = loadVariant(itemId, variant);
				if (blockStateDesc==null) {
					//Load up model from the default model location
					Model model = loadModelFrom(modelId.getNamespace(), "models/block");
					return null; //loadModelFrom(modelId.getNamespace(), "models/block/"+modelId.getPath());
				} else {
					Identifier id = new Identifier(blockStateDesc.model);
					if (id.getPath().endsWith(".gltf")) {
						//Short circuit into gltf
					}
					//return loadModelFrom(blockStateDesc.model
				}
			}
			//if (item==Items.AIR || item==null) return null;
			
		} else {
			
			
			
			
			
			
		}*/
		/*
		Item item = Registry.ITEM.get(itemId);
		if (item==Items.AIR) item = null;
		Block block = Registry.BLOCK.get(itemId);
		if (block==Blocks.AIR) block = null;
		
		if (item==null && block==null) return null;
		
		if (item instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			
			//Load BlockState if it exists
			Identifier itemStateLocation = new Identifier(modelId.getNamespace(), "itemstates/"+modelId.getPath()+".json");
			Optional<Resource> maybeItemState = resourceManager.getResource(itemStateLocation);
			if (maybeItemState.isPresent()) {
				try {
					String itemStateString = new String(maybeItemState.get().open().readAllBytes(), StandardCharsets.UTF_8);
					ModelCryptidBlockState state = gson.fromJson(itemStateString, ModelCryptidBlockState.class);
					
					ModelCryptidBlockState.Variant inventoryVariant = state.variants.get("inventory");
					if (inventoryVariant!=null) return getModelVariant(inventoryVariant);
					
					return null;
				} catch (IOException ex) {
					
					//TODO: Address errors
					
					return null;
				}
			}
		
		}
		
		if (!modelId.getNamespace().equals(ConventionalCubesMod.MODID)) return null;
		
		GroupedBlock block = coveredBlocks.get(new Identifier(modelId.getNamespace(), modelId.getPath()));
		if (block==null) {
			//System.out.println("Not a covered block: "+modelId.getPath());
			return null;
		} else {
			//System.out.println("Covered block: "+modelId.getPath()+" - fetching #"+modelId.getVariant());
			
			Optional<Resource> maybeBlockstateJson = resourceManager.getResource(new Identifier(modelId.getNamespace(), "blockstates/"+modelId.getPath()+".json"));
			if (maybeBlockstateJson.isPresent()) {
				try {
					String blockstateFile = new String(maybeBlockstateJson.get().open().readAllBytes(), StandardCharsets.UTF_8);
					ModelCryptidBlockState state = gson.fromJson(blockstateFile, ModelCryptidBlockState.class);
					
					//System.out.println("Got blockstate json for '"+modelId.getNamespace()+":"+modelId.getPath()+" > "+state.toString());
					
					String neededVariant = modelId.getVariant();
					if (neededVariant.equals("inventory")) {
						if (!state.variants.containsKey("inventory")) {
							if (state.variants.containsKey("")) {
								neededVariant = "";
							} else {
								String defaultStateString = block.getDefaultState().getEntries().entrySet().stream().map(PROPERTY_MAP_PRINTER).collect(Collectors.joining(","));
								//System.out.println("Providing default state #"+defaultStateString);
								neededVariant = defaultStateString;
							}
						}
					}
					
					if (state.variants.containsKey(neededVariant)) {
						ModelCryptidBlockState.Variant variant = state.variants.get(neededVariant);
						Identifier realModelIdentifier = new Identifier(variant.model);
						//System.out.println("Identified variant model "+realModelIdentifier);
						
						if (realModelIdentifier.getPath().endsWith(".gltf")) {
							
							Model model = GLTFModelProvider.loadModel(realModelIdentifier, resourceManager);
							Matrix4d centerBlockbenchModel = Matrix4d.translate(0, -0.5, 0);
							model.transform(centerBlockbenchModel);
							
							//Rotate model
							if (variant.x!=0) {
								double xRadians = variant.x * Math.PI/180;
								Matrix4d rot = Matrix4d.pitch(xRadians);
								model.transform(rot);
							}
							
							if (variant.y!=0) {
								double yRadians = variant.y * Math.PI/180;
								Matrix4d rot = Matrix4d.yaw(yRadians);
								model.transform(rot);
							}
							
							if (variant.z!=0) {
								double zRadians = variant.z * Math.PI/180;
								Matrix4d rot = Matrix4d.roll(zRadians);
								model.transform(rot);
							}
							
							Matrix4d unCenterModel = Matrix4d.translate(0.5, 0.5, 0.5);
							model.transform(unCenterModel);
							
							//System.out.println("Providing model.");
							return new GlowUnbakedModel(model);
						} else {
							//System.out.println("Unknown model type. Skipping.");
							return null;
						}
						
						
						
						//if (realModelIdentifier.getNamespace().equals(modelId.getNamespace()) && realModelIdentifier.getPath().equals(modelId.getPath())) {
						//	System.out.println("Blockstate json refers to the same model as the ID requested, leaving this one to vanilla.");
						//} else {
						//	System.out.println("Identified variant model "+realModelIdentifier+" to load for this model.");
						//}
					} else {
						System.out.println("Variant "+modelId+" wasn't defined in the blockstate json. Skipping.");
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}*/
		
		
		
		return null;
	}
	
	/*
	private VanillaPlusBlockState.Variant loadVariant(Identifier id, String variant) {
		Optional<Resource> maybeState = resourceManager.getResource(id);
		if (maybeState.isEmpty()) return null;
		
		try {
			String stateFile = new String(maybeState.get().open().readAllBytes(), StandardCharsets.UTF_8);
			
			
			
			
		} catch (IOException e) {
			e.printStackTrace(); //This is an unusual circumstance so it's okay to log
			return null;
		}
		
		return null;
	}*/
	
	private @Nullable Model loadModelFrom(String namespace, String path) {
		//TODO: if json is specified, perhaps we should skip the gltf load?
		
		String gltfExtended = (path.endsWith(".gltf")) ? path : path+".gltf";
		Optional<Resource> maybeGltf = resourceManager.getResource(new Identifier(namespace, gltfExtended));
		if (maybeGltf.isPresent()) {
			//TODO: Load in the glTF model
			
			return null;
		} else {
			if (!gltfExtended.equals(path)) {
				//see if there's a vanilla-plus json model instead
				String jsonExtended = (path.endsWith(".json")) ? path : path+".json";
				Optional<Resource> maybeJson = resourceManager.getResource(new Identifier(namespace, jsonExtended));
				if (maybeJson.isPresent()) {
					//TODO: Load in the vanilla+ model
					
					return null;
				} else {
					return null;
				}
			} else {
				//glTF was explicitly specified; don't fallback onto a standard model
				return null;
			}
		}
	}
	
	private @Nullable Model loadGltfModelFrom(String namespace, String path) {
		if (!path.endsWith(".gltf")) path = path+".gltf";
		Optional<Resource> maybeGltf = resourceManager.getResource(new Identifier(namespace, path));
		if (maybeGltf.isPresent()) {
			try {
				String modelString = new String(maybeGltf.get().open().readAllBytes(), StandardCharsets.UTF_8);
				return GLTFLoader.loadString(modelString);
			} catch (IOException ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	private @Nullable Model loadVanillaPlusModelFrom(String namespace, String path) {
		if (!path.endsWith(".json")) path = path+".json";
		Optional<Resource> maybeJson = resourceManager.getResource(new Identifier(namespace, path));
		if (maybeJson.isPresent()) {
			try {
				String modelString = new String(maybeJson.get().open().readAllBytes(), StandardCharsets.UTF_8);
				return GLTFLoader.loadString(modelString);
			} catch (IOException ex) {
				ex.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}
	
	private static String getStateName(BlockState blockState) {
		return blockState.getEntries().entrySet().stream().map(PROPERTY_MAP_PRINTER).collect(Collectors.joining(","));
	}
	
	private static final Function<Entry<Property<?>, Comparable<?>>, String> PROPERTY_MAP_PRINTER = new Function<Entry<Property<?>, Comparable<?>>, String>() {
		public String apply(@Nullable Entry<Property<?>, Comparable<?>> entry) {
			if (entry == null) {
				return "<NULL>";
			} else {
				Property<?> property = (Property)entry.getKey();
				return property.getName() + "=" + this.nameValue(property, (Comparable<?>)entry.getValue());
			}
		}

		private <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
			return property.name((T)value);
		}
	};
}