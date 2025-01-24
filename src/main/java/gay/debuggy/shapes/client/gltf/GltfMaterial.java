package gay.debuggy.shapes.client.gltf;

public class GltfMaterial extends GltfChildOfRootProperty {
	
	public PbrMetallicRoughness pbrMetallicRoughness;
	public NormalTextureInfo normalTexture;
	public OcclusionTextureInfo occlusionTexture;
	public EmissiveTextureInfo emissiveTexture;
	
	
	public GltfMaterial() {}
	
	/**
	 * A set of parameter values that are used to define the metallic-roughness material model from Physically-Based Rendering (PBR) methodology.
	 */
	public static class PbrMetallicRoughness extends GltfProperty {
		
		/**
		 * The factors for the base color of the material.
		 * 
		 * <p>This value defines linear multipliers for the sampled texels of the base color texture.
		 */
		public double[] baseColorFactor = { 1.0, 1.0, 1.0, 1.0 };
		
		/**
		 * The base color texture.
		 * 
		 * <p>The first three components (RGB) **MUST** be encoded with the sRGB transfer function.
		 * They specify the base color of the material. If the fourth component (A) is present, it
		 * represents the linear alpha coverage of the material. Otherwise, the alpha coverage is equal
		 * to `1.0`. The `material.alphaMode` property specifies how alpha is interpreted. The stored
		 * texels **MUST NOT** be premultiplied. When undefined, the texture **MUST** be sampled as
		 * having `1.0` in all components.
		 */
		public GltfTextureInfo baseColorTexture;
		
		public PbrMetallicRoughness() {}
	}
	
	public static class NormalTextureInfo extends GltfTextureInfo {
		
		/**
		 * The scalar parameter applied to each normal vector of the normal texture.
		 * 
		 * <p>This value scales the normal vector in X and Y directions using the formula:
		 * 
		 * <pre>
		 *   scaledNormal =  normalize((<sampled normal texture value> * 2.0 - 1.0) * vec3(<normal scale>, <normal scale>, 1.0))
		 * </pre>
		 */
		public double scale = 1.0;
		
		public NormalTextureInfo() {}
	}
	
	public static class OcclusionTextureInfo extends GltfTextureInfo {
		/**
		 * A scalar multiplier controlling the amount of occlusion applied.
		 * 
		 * <p>A value of `0.0` means no occlusion. A value of `1.0` means full occlusion.
		 * This value affects the final occlusion value as:
		 * 
		 * <pre>
		 *   1.0 + strength * (<sampled occlusion texture value> - 1.0)
		 * </pre>
		 */
		public double strength = 1.0;
		
		public OcclusionTextureInfo() {}
	}
	
	public static class EmissiveTextureInfo extends GltfTextureInfo {
		
	}
}
