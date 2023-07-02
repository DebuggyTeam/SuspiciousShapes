package gay.debuggy.shapes.client;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext.QuadTransform;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

public class BakedMeshModel implements BakedModel, FabricBakedModel {
	protected final List<Mesh> meshes;
	protected final Sprite particleSprite;
	protected ModelTransformation transform = ModelTransformation.NONE;
	
	public BakedMeshModel(Sprite particleSprite, ModelTransformation transform, Mesh... meshes) {
		this.meshes = ImmutableList.copyOf(meshes);
		this.particleSprite = particleSprite;
		if (transform!=null) this.transform = transform;
	}
	
	//implements BakedModel {
	
		@Override
		public List<BakedQuad> getQuads(BlockState state, Direction face, RandomGenerator random) {
			return ImmutableList.of();
		}
	
		@Override
		public boolean useAmbientOcclusion() {
			return true;
		}
	
		@Override
		public boolean hasDepth() {
			return true;
		}
	
		@Override
		public boolean isSideLit() {
			return true;
		}
	
		@Override
		public boolean isBuiltin() {
			return false;
		}
	
		@Override
		public Sprite getParticleSprite() {
			return particleSprite;
		}
	
		@Override
		public ModelTransformation getTransformation() {
			return transform;
		}
	
		@Override
		public ModelOverrideList getOverrides() {
			return ModelOverrideList.EMPTY;
		}
	
	//}
	
	//implements FabricBakedModel {
		
		@Override
		public boolean isVanillaAdapter() {
			return false;
		}
	
		@Override
		public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
			for(Mesh mesh : meshes) {
				context.meshConsumer().accept(mesh);
			}
		}
	
		@Override
		public void emitItemQuads(ItemStack stack, Supplier<RandomGenerator> randomSupplier, RenderContext context) {
			for(Mesh mesh : meshes) {
				context.meshConsumer().accept(mesh);
			}
		}

	//}
}
