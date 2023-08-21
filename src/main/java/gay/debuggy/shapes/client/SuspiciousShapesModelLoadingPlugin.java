package gay.debuggy.shapes.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin.Context;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import blue.endless.glow.model.Mesh;
import blue.endless.glow.model.Model;
import blue.endless.glow.model.ShaderAttribute;
import blue.endless.glow.model.gltf.GLTFLoader;
import gay.debuggy.shapes.client.ProcessedModelData.Node;
import gay.debuggy.shapes.client.schema.BlockModelPlus;
import gay.debuggy.shapes.client.schema.ModelTransformationDeserializer;
import gay.debuggy.shapes.client.schema.TransformationDeserializer;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public class SuspiciousShapesModelLoadingPlugin implements PreparableModelLoadingPlugin<UnprocessedModelData> {
	private final Set<String> FORBIDDEN_PARENTS = Set.of(
			"minecraft:item/generated",
			"minecraft:builtin/generated",
			"minecraft:builtin/entity",
			"item/generated",
			"builtin/generated",
			"builtin/entity"
			);
	
	private static final Identifier GLTF_LOADER_KEY = new Identifier(SuspiciousShapesClient.MODID, "gltf");
	private static final Identifier OBJ_LOADER_KEY = new Identifier(SuspiciousShapesClient.MODID, "obj");
	
	@Override
	public void onInitializeModelLoader(UnprocessedModelData data, Context pluginContext) {
		//System.out.println("InitializeModelLoader (with supplied data)...");
		
		final long start = System.nanoTime();
		
		//Transform data into organized models
		ProcessedModelData processed = new ProcessedModelData();
		for(UnprocessedModelData.Node node : data.resources) {
			if (node.location().getPath().endsWith(".json")) {
				//Create a BlockModelPlus from this json
				try {
					if (node.data() == null || node.data().isBlank()) {
						processed.errors.add(new ProcessedModelData.ErrorNode(node.location(), "Node was empty before processing.", new IllegalArgumentException()));
						continue; //let all its descendants be missingno.
					}
					
					BlockModelPlus model = new GsonBuilder()
							//These type adapters are cleaned-up copies of Mojang code since it's private
							.registerTypeAdapter(Transformation.class, new TransformationDeserializer())
							.registerTypeAdapter(ModelTransformation.class, new ModelTransformationDeserializer())
							.create().fromJson(node.data(), BlockModelPlus.class);
					
					if (model == null) {
						processed.errors.add(new ProcessedModelData.ErrorNode(node.location(), "Data was null after processing.", new IllegalArgumentException()));
						continue; //Yeah let's skip this one from adding to our nodetree, let all its descendants be missingno.
					}
					
					var jsonNode = new ProcessedModelData.JsonNode(node.location(), model);
					processed.byId.put(jsonNode.id, jsonNode);
					
					/*
					 * For now we're skipping adding any BlockModelPlus roots to the tree and just keeping them in byId.
					 * Then later we can prune null-parent jsonNodes from byId and compact the map to slim down the
					 * memory footprint.
					 */
					if (model.parent == null) {
						//processed.roots.add(jsonNode);
					} else {
						if (FORBIDDEN_PARENTS.contains(model.parent)) {
							//Don't bother resolving the parent. Just make it a root.
							//processed.roots.add(jsonNode);
							continue;
						}
						
						try {
							ProcessedModelData.Node possibleParent = processed.byId.get(new Identifier(model.parent));
							if (possibleParent != null) {
								//If this is the attachment point between a model and a gltf, gate it through the model loader key
								if (possibleParent instanceof ProcessedModelData.GltfNode) {
									if (!GLTF_LOADER_KEY.toString().equals(model.loader) && !OBJ_LOADER_KEY.toString().equals(model.loader)) {
										continue; //It'll still show up in the ID map but be banished from any root attachments.
									}
								}
								
								possibleParent.children.add(jsonNode);
								jsonNode.parent = possibleParent;
							} else {
								processed.detached.add(jsonNode);
							}
						} catch (InvalidIdentifierException ex2) {
							processed.errors.add(new ProcessedModelData.ErrorNode(jsonNode.location, "Parent was an invalid identifier.", ex2));
						}
					}
				
				} catch (JsonSyntaxException ex) {
					processed.errors.add(new ProcessedModelData.ErrorNode(node.location(), "Syntax error parsing block-model data", ex));
				}
				
			} else if (node.location().getPath().endsWith(".gltf")) {
				try {
					Model model = GLTFLoader.loadString(node.data());
					ProcessedModelData.GltfNode gltfNode = new ProcessedModelData.GltfNode(node.location(), model);
					
					processed.byId.put(gltfNode.id, gltfNode);
					processed.roots.add(gltfNode);
				} catch (IOException ex) {
					processed.errors.add(new ProcessedModelData.ErrorNode(node.location(), "I/O error processing gltf data.", ex));
				}
			}
		}
		
		data = null; //Give GC the opportunity to throw away all that String data if possible
		
		int toReattach = processed.detached.size();
		while (processed.detached.size() > 0) {
			Node node = processed.detached.remove(0);
			if (node instanceof ProcessedModelData.JsonNode jsonNode) {
				try {
					String rawParentId = jsonNode.blockModelPlus.parent;
					//if (rawParentId.endsWith(".gltf")) rawParentId = rawParentId.substring(0, ".gltf".length());
					Identifier parentId = new Identifier(rawParentId); //For the purposes of locating in the id map
					
					Node possiblyParent = processed.byId.get(parentId);
					if (possiblyParent != null) {
						node.parent = possiblyParent;
						possiblyParent.children.add(node);
					} else {
						processed.errors.add(new ProcessedModelData.ErrorNode(node.location, "Couldn't find parent '"+parentId+"' for node "+node.id, new IllegalArgumentException()));
					}
				} catch (InvalidIdentifierException ex) {
					processed.errors.add(new ProcessedModelData.ErrorNode(node.location, "Parent was an invalid identifier. Shouldn't happen at this loading stage!", new IllegalStateException()));
				}
			} else {
				processed.errors.add(new ProcessedModelData.ErrorNode(node.location, "Invalid detached node. Shouldn't happen!", new IllegalStateException()));
			}
		}
		
		processed.byId = processed.byId.entrySet().stream()
				.filter(it -> it.getValue().hasPathToRoot(processed.roots))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		
		int numActualNodes = processed.roots.stream().mapToInt(ProcessedModelData.Node::treeSize).sum();
		
		if (processed.byId.size() != numActualNodes && Config.instance.log_level.value() > Config.LogLevel.NO_ERRORS.value()) {
			SuspiciousShapesClient.LOGGER.warn("LUT has "+processed.byId.size()+" entries but node tree has "+numActualNodes+" nodes.");
		}
		
		final long elapsed = (System.nanoTime() - start) / 1_000_000;
		
		if (Config.instance.log_level.value() > Config.LogLevel.QUIET.value()) {
			SuspiciousShapesClient.LOGGER.info("Processed "+processed.roots.size()+" roots, "+ toReattach + " detached nodes, encountered "+processed.errors.size()+" errors, and pruned to "+numActualNodes+" actual nodes ("+elapsed+" msec).");
			
			if (Config.instance.log_level.value() > Config.LogLevel.NO_ERRORS.value()) {
				if (processed.errors.size()>0) {
					SuspiciousShapesClient.LOGGER.error("There were "+processed.errors+" errors loading data:");
					for(ProcessedModelData.ErrorNode err : processed.errors) {
						
						SuspiciousShapesClient.LOGGER.error("    "+err.id()+": "+err.message());
					}
				}
			}
		}
		
		pluginContext.resolveModel().register(new GLTFModelResolver(processed));
	}
	
	public static CompletableFuture<UnprocessedModelData> loadData(ResourceManager loader, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			long start = System.nanoTime();
			UnprocessedModelData result = new UnprocessedModelData();
			
			Map<Identifier, Resource> blockModelResources = loader.findResources("models/block", (it) -> it.getPath().endsWith(".json") || it.getPath().endsWith(".gltf"));
			for(Map.Entry<Identifier, Resource> entry : blockModelResources.entrySet()) {
				try (InputStream in = entry.getValue().open()) {
					String value = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					result.resources.add(new UnprocessedModelData.Node(entry.getKey(), value));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			blockModelResources = null;
			
			Map<Identifier, Resource> itemModelResources = loader.findResources("models/item", (it) -> it.getPath().endsWith(".json") || it.getPath().endsWith(".gltf"));
			for(Map.Entry<Identifier, Resource> entry : itemModelResources.entrySet()) {
				try (InputStream in = entry.getValue().open()) {
					String value = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					result.resources.add(new UnprocessedModelData.Node(entry.getKey(), value));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			long elapsed = (System.nanoTime() - start) / 1_000_000;
			if (Config.instance.log_level.value() > Config.LogLevel.QUIET.value()) {
				SuspiciousShapesClient.LOGGER.info("Acquired "+result.resources.size()+" model resources ("+elapsed+" msec).");
			}
			
			return result;
		}, executor);
	}
	
	public static class GLTFModelResolver implements ModelResolver {
		private final ProcessedModelData data;
		
		public GLTFModelResolver(ProcessedModelData data) {
			this.data = data;
		}
		
		@Override
		public @Nullable UnbakedModel resolveModel(Context context) {
			ProcessedModelData.Node node = data.byId.get(context.id());
			
			if (node != null) {
				if (node instanceof ProcessedModelData.GltfNode) return null; //Don't provde direct references!
				
				//System.out.println("Providing "+context.id());
				
				try {
					Node rootNode = node.getRoot();
					
					if (rootNode instanceof ProcessedModelData.GltfNode gltfRoot) {
						Model glowModelInstance = gltfRoot.model.copy();
						GlowUnbakedModel glowUnbakedModel = new GlowUnbakedModel(glowModelInstance, node.id);
						
						//Fill in data from all the children, from root down to the child
						List<Node> path = node.getPathFromRoot();
						path.remove(0); //Don't add the gltf node's attributes to itself.
						for(Node pathNode : path) {
							if (pathNode instanceof ProcessedModelData.JsonNode jsonNode) {
								BlockModelPlus bmp = jsonNode.blockModelPlus;
								glowUnbakedModel.provideTextures(bmp.textures);
								if (bmp.display != null) glowUnbakedModel.setModelTransformation(bmp.display);
								if (bmp.colorIndexes != null) {
									for(int i=0; i<bmp.colorIndexes.length; i++) {
										int colorIndex = bmp.colorIndexes[i];
										if (i >= glowModelInstance.getMeshes().size()) break;
										glowModelInstance.getMeshes().get(i).getMaterial().put(ShaderAttribute.COLOR_INDEX, colorIndex);
									}
								}
								if (bmp.uvlock) {
									for(Mesh mesh : glowModelInstance) {
										mesh.getMaterial().put(ShaderAttribute.UV_LOCK, Boolean.TRUE);
									}
								}
							}
						}
						
						//The model should now be fully configured.
						return glowUnbakedModel;
						
					} else {
						//Shouldn't happen
						SuspiciousShapesClient.LOGGER.warn("Suspicious state detected for the model at '"+rootNode.location.toString()+"' - non-gltf/non-obj root was never pruned!");
						return null;
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			
			return null;
		}
		
	}
}
