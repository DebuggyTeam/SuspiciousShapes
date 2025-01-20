package gay.debuggy.shapes.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import blue.endless.glow.model.Mesh;
import blue.endless.glow.model.Model;
import blue.endless.glow.model.ShaderAttribute;
import blue.endless.glow.model.Vector2d;
import blue.endless.glow.model.Vector3d;
import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class GlowUnbakedModel implements UnbakedModel {
	
	private final Identifier id;
	protected final Model model;
	private List<String> textures = new ArrayList<>();
	private String particleId = null;
	private Map<String, Sprite> sprites = new HashMap<>();
	private ModelTransformation transform = ModelHelper.MODEL_TRANSFORM_BLOCK;
	
	public GlowUnbakedModel(Model glowModel, Identifier id) {
		this.model = glowModel;
		this.id = id;
		for(Mesh mesh : model) {
			String texId = mesh.getMaterial().get(ShaderAttribute.DIFFUSE_TEXTURE);
			if (texId!=null) textures.add(texId);
		}
	}
	
	public Identifier getResourceId() {
		return id;
	}
	
	@Override
	public Collection<Identifier> getModelDependencies() {
		//Does not depend on other models
		return ImmutableList.of();
	}
	
	
	@Override
	public BakedModel bake(Baker modelBaker, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer) {
		
		sprites.clear();
		Sprite particleSprite = null;
		if (particleId != null) particleSprite = resolveSprite(particleId, textureGetter);
		
		int numMeshes = Iterators.size(model.iterator());
		while(textures.size()<numMeshes) {
			textures.add(TextureManager.MISSING_IDENTIFIER.toString());
		}
		
		for(String s : textures) {
			Sprite cur = resolveSprite(s, textureGetter, sprites);
			if (particleSprite == null) particleSprite = cur;
		}
		
		if (particleSprite == null) particleSprite = resolveMissingno(textureGetter);
		
		
		//The following doesn't really make things run on Sodium without Indium, but it makes it stop crashing.
		Renderer renderer = (RendererAccess.INSTANCE.hasRenderer()) ? RendererAccess.INSTANCE.getRenderer() : IndigoRenderer.INSTANCE;
		MeshBuilder meshBuilder = renderer.meshBuilder();
		QuadEmitter emitter = meshBuilder.getEmitter();
		int texIndex = 0;
		
		
		Matrix4f matrix = rotationContainer.getRotation().getMatrix();
		Quaternionf leftRotation = rotationContainer.getRotation().getLeftRotation();
		Quaternionf rightRotation = rotationContainer.getRotation().getRightRotation();
		
		
		for(Mesh mesh : model) {
			String texId = textures.get(texIndex);
			texIndex++;
			
			int colorIndex = mesh.getMaterial().get(ShaderAttribute.COLOR_INDEX, -1);
			int bakeFlags = MutableQuadView.BAKE_NORMALIZED;
			if (mesh.getMaterial().get(ShaderAttribute.UV_LOCK, false)) {
				bakeFlags |= MutableQuadView.BAKE_LOCK_UV;
			}
			//boolean ao = mesh.getMaterial().get(ShaderAttribute.APPLY_AO, Boolean.TRUE);
			
			//int colorIndex = 0;
			for(Mesh.Face face : mesh.createQuadList()) {
				
				Iterator<Mesh.Vertex> verts = face.iterator();
				if (!verts.hasNext()) continue;
				Mesh.Vertex v1 = verts.next();
				if (!verts.hasNext()) continue;
				Mesh.Vertex v2 = verts.next();
				if (!verts.hasNext()) continue;
				Mesh.Vertex v3 = verts.next();
				Mesh.Vertex v4 = (verts.hasNext()) ? verts.next() : v3;
				
				//emitter.pos(0, toJoml3(v1.get(ShaderAttribute.POSITION)));
				//emitter.pos(1, toJoml3(v2.get(ShaderAttribute.POSITION)));
				//emitter.pos(2, toJoml3(v3.get(ShaderAttribute.POSITION)));
				//emitter.pos(3, toJoml3(v4.get(ShaderAttribute.POSITION)));
				
				//The continues: We bail early from face processing if at any point we discover not enough vertices.
				v1 = transform(v1, matrix, leftRotation, rightRotation);
				v2 = transform(v2, matrix, leftRotation, rightRotation);
				v3 = transform(v3, matrix, leftRotation, rightRotation);
				v4 = transform(v4, matrix, leftRotation, rightRotation);
				
				emitter.nominalFace(directionFromVector(v1.get(ShaderAttribute.NORMAL)));
				
				emit(v1, emitter, 0);
				emit(v2, emitter, 1);
				emit(v3, emitter, 2);
				emit(v4, emitter, 3);
				
				if (texId!=null) {
					Sprite sprite = sprites.get(texId);
					emitter.spriteBake(sprite, bakeFlags);
				}
				
				emitter.colorIndex(colorIndex);
				emitter.emit();
			}
		}
		
		return new BakedMeshModel(particleSprite, transform, meshBuilder.build());
	}
	
	public static Vector4f toJoml(Vector3d vec) {
		return new Vector4f((float) vec.x(), (float) vec.y(), (float) vec.z(), 1);
	}
	
	public static Vector3d toGlow(Vector4f vec) {
		return new Vector3d(vec.x, vec.y, vec.z);
	}
	
	public static Vector3f toJoml3(Vector3d vec) {
		return new Vector3f((float) vec.x(), (float) vec.y(), (float) vec.z());
	}
	
	public void setModelTransformation(ModelTransformation transform) {
		Transformation thirdPersonLeftHand = select(this.transform, transform, ModelTransformationMode.THIRD_PERSON_LEFT_HAND);
		Transformation thirdPersonRightHand = select(this.transform, transform, ModelTransformationMode.THIRD_PERSON_RIGHT_HAND);
		if (thirdPersonLeftHand == Transformation.IDENTITY) thirdPersonLeftHand = thirdPersonRightHand;
		
		Transformation firstPersonLeftHand = select(this.transform, transform, ModelTransformationMode.FIRST_PERSON_LEFT_HAND);
		Transformation firstPersonRightHand = select(this.transform, transform, ModelTransformationMode.FIRST_PERSON_RIGHT_HAND);
		if (firstPersonLeftHand == Transformation.IDENTITY) firstPersonLeftHand = firstPersonRightHand;
		
		Transformation head = select(this.transform, transform, ModelTransformationMode.HEAD);
		Transformation gui = select(this.transform, transform, ModelTransformationMode.GUI);
		Transformation ground = select(this.transform, transform, ModelTransformationMode.GROUND);
		Transformation fixed = select(this.transform, transform, ModelTransformationMode.FIXED);
		
		this.transform = new ModelTransformation(thirdPersonLeftHand, thirdPersonRightHand, firstPersonLeftHand, firstPersonRightHand, head, gui, ground, fixed);
	}
	
	private Transformation select(ModelTransformation self, ModelTransformation other, ModelTransformationMode mode) {
		return (other.isTransformationDefined(mode)) ?
				other.getTransformation(mode) :
				self.getTransformation(mode);
	}
	
	public static Mesh.Vertex transform(Mesh.Vertex v, Matrix4f matrix, Quaternionf leftRotation, Quaternionf rightRotation) {
		//Make a copy
		Mesh.Vertex result = new Mesh.Vertex();
		result.putAll(v);
		
		//Extract the position
		Vector4f pos = toJoml(v.get(ShaderAttribute.POSITION, new Vector3d(0,0,0)));
		
		//Translate / rotate / translate
		// Shapes are pre-centered around the origin by the model loader, so we can skip the first translate.
		pos.mul(matrix);
		pos.add(0.5f, 0.5f, 0.5f, 0f);
		
		//Extract and rotate the normal
		Vector4f normal = toJoml(v.get(ShaderAttribute.NORMAL, new Vector3d(0,1,0)));
		normal = normal.rotate(leftRotation).rotate(rightRotation);
		
		//TODO: Figure out which direction the vertex lies on and apply UVLock
		
		//Stuff the new position back into the copy
		result.put(ShaderAttribute.POSITION, toGlow(pos));
		result.put(ShaderAttribute.NORMAL, toGlow(normal));
		
		return result;
	}
	
	public Direction directionFromVector(Vector3d vec) {
		double dx = Math.abs(vec.x());
		double dy = Math.abs(vec.y());
		double dz = Math.abs(vec.z());
		
		if (dx>dy && dx>dz) {
			return (vec.x()>0) ? Direction.EAST : Direction.WEST;
		} else if (dz>dy) {
			return (vec.z()>0) ? Direction.SOUTH : Direction.NORTH;
		} else {
			
			return (vec.y()>0) ? Direction.UP : Direction.DOWN;
		}
	}
	
	private static Sprite missingno = null;
	public static Sprite resolveMissingno(Function<SpriteIdentifier, Sprite> textureGetter) {
		if (missingno == null) {
			missingno = textureGetter.apply(new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, TextureManager.MISSING_IDENTIFIER));
		}
		
		return missingno;
	}
	
	public static @Nullable Sprite resolveSprite(String id, Function<SpriteIdentifier, Sprite> textureGetter) {
		String namespace = "minecraft";
		String path = "";
		
		if (id.contains(":")) {
			String[] parts = id.split(":");
			namespace = parts[0];
			path = parts[1];
		} else {
			namespace = "minecraft";
			path = id;
		}

		try {
			Sprite sprite = textureGetter.apply(new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of(namespace, path)));
			if (sprite == null) {
				return resolveMissingno(textureGetter);
			} else {
				return sprite;
			}
		} catch (Exception ex) {
			return resolveMissingno(textureGetter);
		}
	}
	
	public static @Nullable Sprite resolveSprite(String id, Function<SpriteIdentifier, Sprite> textureGetter, Map<String, Sprite> dest) {
		String namespace = "minecraft";
		String path = "";
		
		if (id.contains(":")) {
			String[] parts = id.split(":");
			namespace = parts[0];
			path = parts[1];
		} else {
			namespace = "minecraft";
			path = id;
		}
		
		//Is the path an up-reference?
		if (path.startsWith("#")) {
			SuspiciousShapesClient.LOGGER.warn("Unresolved up-reference "+Identifier.of(id)+" in model "+id);
			dest.put(id, resolveMissingno(textureGetter));
			/*
			path = path.substring(1);
			try {
				Sprite sprite = textureGetter.apply(new Material(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier(namespace, path)));
				if (sprite == null) {
					dest.put(id, resolveMissingno(textureGetter));
				} else {
					dest.put(id, sprite);
					return sprite;
				}
			} catch (Exception ex) {
				dest.put(id, resolveMissingno(textureGetter));
			}*/
		} else {
			try {
				Sprite sprite = textureGetter.apply(new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, Identifier.of(namespace, path)));
				if (sprite == null) {
					SuspiciousShapesClient.LOGGER.warn("Can't find texture "+Identifier.of(id)+" referenced from model "+id);
					dest.put(id, resolveMissingno(textureGetter));
				} else {
					dest.put(id, sprite);
					return sprite;
				}
			} catch (Exception ex) {
				dest.put(id, resolveMissingno(textureGetter));
			}
		}
		return null;
	}
	
	private static void emit(Mesh.Vertex v, QuadEmitter emitter, int index) {
		Vector3d pos = v.get(ShaderAttribute.POSITION, new Vector3d(0,0,0));
		Vector2d tex = v.get(ShaderAttribute.TEXCOORD, new Vector2d(0,0));
		Vector3d norm = v.get(ShaderAttribute.NORMAL, new Vector3d(0,1,0));
		Integer vertexColor = v.get(ShaderAttribute.DIFFUSE_COLOR, null);
		int col = (vertexColor!=null) ? vertexColor : -1;
		
		
		
		emitter
			.pos(index, (float) pos.x(), (float) pos.y(), (float) pos.z())
			.uv(index, (float) tex.x(), (float) tex.y())
			.color(index, col)
			.normal(index, (float) norm.x(), (float) norm.y(), (float) norm.z());
	}

	@Override
	public void setParents(Function<Identifier, UnbakedModel> models) {
		// TODO: Actually resolve the parents!
		
	}

	public void provideTextures(Map<String, String> textures) {
		for(Map.Entry<String, String> entry : textures.entrySet()) {
			int tries = 0;
			while(true) {
				int index = this.textures.indexOf("#"+entry.getKey());
				if (index>=0) {
					this.textures.set(index, entry.getValue());
				} else {
					break;
				}
				tries++;
				if (tries>40) break;
			}
		}
	}

}
