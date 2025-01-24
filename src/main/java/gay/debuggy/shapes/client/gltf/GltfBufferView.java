package gay.debuggy.shapes.client.gltf;

public class GltfBufferView extends GltfChildOfRootProperty {
	//REQUIRED: buffer, byteLength
	
	int buffer;
	int byteOffset = 0;
	int byteLength;
	int byteStride;
	int target;
	
	public GltfBufferView() {}
}
