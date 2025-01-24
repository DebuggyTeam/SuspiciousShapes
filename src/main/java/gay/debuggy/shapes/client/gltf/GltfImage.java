package gay.debuggy.shapes.client.gltf;

public class GltfImage extends GltfChildOfRootProperty {
	//REQUIRED: EITHER bufferView and mimeType, OR uri
	
	String uri;
	String mimeType;
	int bufferView;
	
	public GltfImage() {}
}
