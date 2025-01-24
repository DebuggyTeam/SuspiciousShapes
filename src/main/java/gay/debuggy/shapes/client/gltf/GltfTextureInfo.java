package gay.debuggy.shapes.client.gltf;

/**
 * Reference to a texture.
 */
public class GltfTextureInfo extends GltfProperty {
	
	/**
	 * The index of the texture
	 **/
	public int index;
	
	/**
	 * The set index of texture's TEXCOORD attribute used for texture coordinate mapping.
	 * 
	 * <p>This integer value is used to construct a string in the format `TEXCOORD_<set index>`
	 * which is a reference to a key in `mesh.primitives.attributes` (e.g. a value of `0`
	 * corresponds to `TEXCOORD_0`). A mesh primitive **MUST** have the corresponding texture
	 * coordinate attributes for the material to be applicable to it.
	 */
	public int texCoord;
	
	public GltfTextureInfo() {}
}
