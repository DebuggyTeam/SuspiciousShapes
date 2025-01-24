package gay.debuggy.shapes.client.gltf;

public class GltfAccessor extends GltfChildOfRootProperty {
	//REQUIRED: componentType, count, type
	
	public int bufferView;
	public int byteOffset = 0;
	public int componentType;
	public boolean normalized = false;
	public int count = 0;
	public int type;
	public int[] max = { 0 };
	public int[] min = { 0 };
	public Sparse sparse;
	
	public GltfAccessor() {}
	
	
	public static class Sparse extends GltfProperty {
		//REQUIRED: count, indices, values
		
		public int count;
		public SparseIndices indices;
		public SparseValues values;
		
		public Sparse() {}
	}
	
	public static class SparseIndices extends GltfProperty {
		//REQUIRED: bufferView, componentType
		
		public int bufferView;
		public int byteOffset = 0;
		public int componentType;
		
		public SparseIndices() {}
	}
	
	public static class SparseValues extends GltfProperty {
		//REQUIRED: bufferView
		
		public int bufferView;
		public int byteOffset = 0;
		
		public SparseValues() {}
	}
}