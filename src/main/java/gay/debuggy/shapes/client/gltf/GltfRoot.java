package gay.debuggy.shapes.client.gltf;

/**
 * This is the schema for an entire glTF file, or for the json (non-buffer) section of a GLB file.
 */
public class GltfRoot extends GltfProperty {
	//REQUIRED: asset
	
	String[] extensionsUsed;
	String[] extensionsRequired;
	GltfAccessor[] accessors;
	GltfAnimation[] animations;
	GltfAsset asset;
	GltfBuffer[] buffers;
	GltfBufferView[] bufferViews;
	GltfCamera[] cameras;
	GltfImage[] images;
	GltfMaterial[] materials;
	//GltfMesh[] meshes;
	//GltfNode[] nodes;
	//GltfImage.Sampler[] samplers;
	//GltfScene scene;
	//GltfScene[] scenes;
	//GltfSkin[] skins;
	//GltfTexture[] textures;
	
	public GltfRoot() {}
}
