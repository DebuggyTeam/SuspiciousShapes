package gay.debuggy.shapes.client.gltf;

/**
 * A typed view into a buffer view that contains raw binary data.
 */
public class GltfAccessor extends GltfChildOfRootProperty {
	//REQUIRED: componentType, count, type
	
	/**
	 * The index of the buffer view. When undefined, the accessor **MUST** be initialized with zeros; `sparse` property
	 * or extensions **MAY** override zeros with actual values.
	 */
	public int bufferView;
	
	/**
	 * The offset relative to the start of the buffer view in bytes. This **MUST** be a multiple of the size of the
	 * component datatype. This property **MUST NOT** be defined when `bufferView` is undefined.
	 */
	public int byteOffset = 0;
	
	/**
	 * The datatype of the accessor's components.  UNSIGNED_INT type **MUST NOT** be used for any accessor that is not
	 * referenced by `mesh.primitive.indices`.
	 */
	public int componentType;
	
	/**
	 * Specifies whether integer data values are normalized (`true`) to [0, 1] (for unsigned types) or to [-1, 1] (for
	 * signed types) when they are accessed. This property **MUST NOT** be set to `true` for accessors with `FLOAT` or
	 * `UNSIGNED_INT` component type.
	 */
	public boolean normalized = false;
	
	/**
	 * The number of elements referenced by this accessor, not to be confused with the number of bytes or number of
	 * components.
	 */
	public int count = 0;
	
	/**
	 * Specifies if the accessor's elements are scalars, vectors, or matrices.
	 */
	public int type;
	
	/**
	 * Maximum value of each component in this accessor.  Array elements **MUST** be treated as having the same data
	 * type as accessor's `componentType`. Both `min` and `max` arrays have the same length.  The length is determined
	 * by the value of the `type` property; it can be 1, 2, 3, 4, 9, or 16.\n\n`normalized` property has no effect on
	 * array values: they always correspond to the actual values stored in the buffer. When the accessor is sparse, this
	 * property **MUST** contain maximum values of accessor data with sparse substitution applied.
	 */
	public int[] max = { 0 };
	
	/**
	 * Minimum value of each component in this accessor.  Array elements **MUST** be treated as having the same data
	 * type as accessor's `componentType`. Both `min` and `max` arrays have the same length.  The length is determined
	 * by the value of the `type` property; it can be 1, 2, 3, 4, 9, or 16.\n\n`normalized` property has no effect on
	 * array values: they always correspond to the actual values stored in the buffer. When the accessor is sparse, this
	 * property **MUST** contain minimum values of accessor data with sparse substitution applied.
	 */
	public int[] min = { 0 };
	
	/**
	 * Sparse storage of elements that deviate from their initialization value.
	 */
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