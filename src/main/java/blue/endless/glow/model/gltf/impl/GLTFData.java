/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model.gltf.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Base64;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import blue.endless.glow.model.Vector2d;
import blue.endless.glow.model.Vector3d;

// Refer to https://registry.khronos.org/glTF/specs/2.0/glTF-2.0.html
public class GLTFData {
	//PrimitiveType
	public static final int GL_POINTS         = 0;
	public static final int GL_LINES          = 1;
	public static final int GL_LINE_LOOP      = 2;
	public static final int GL_LINE_STRIP     = 3;
	public static final int GL_TRIANGLES      = 4;
	public static final int GL_TRIANGLE_STRIP = 5;
	public static final int GL_TRIANGLE_FAN   = 6;
	//ComponentType
	public static final int GL_BYTE   = 5120;
	public static final int GL_UBYTE  = 5121;
	public static final int GL_SHORT  = 5122;
	public static final int GL_USHORT = 5123;
	public static final int GL_INT    = 5125;
	public static final int GL_FLOAT  = 5126;
	//Wrap
	public static final int GL_REPEAT = 10497;

	public String[] extensionsUsed = new String[0];
	public String[] extensionsRequired = new String[0];
	public GLTFAccessor[] accessors = new GLTFAccessor[0];
	public GLTFAnimation[] animations = new GLTFAnimation[0];
	public GLTFAsset asset = new GLTFAsset();
	public GLTFBuffer[] buffers = new GLTFBuffer[0];
	public GLTFBufferView[] bufferViews = new GLTFBufferView[0];
	public GLTFImage[] images = new GLTFImage[0];
	public GLTFMaterial[] materials = new GLTFMaterial[0];
	public GLTFMesh[] meshes = new GLTFMesh[0];
	public GLTFNode[] nodes = new GLTFNode[0];
	public GLTFSampler[] samplers = new GLTFSampler[0];
	public GLTFTexture[] textures = new GLTFTexture[0];
	public JsonObject extensions = null;
	public JsonElement extras = null;

	public GLTFData() {}

	public ByteBuffer getDataBuffer(int bufferView, byte[] binaryData) {
		if (bufferView<0 || bufferView>= bufferViews.length) throw new IllegalArgumentException("buffer argument must be between 0 and "+(bufferViews.length-1));
		GLTFBufferView view = bufferViews[bufferView];
		if (view.buffer<0 || view.buffer>=buffers.length) throw new IllegalStateException("bufferView points to nonexistant buffer #"+view.buffer);
		GLTFBuffer buf = buffers[view.buffer];

		String uri = buf.uri;
		if (uri.startsWith("data:application/octet-stream;base64,")) {
			uri = uri.substring("data:application/octet-stream;base64,".length());

			byte[] data = Base64.getDecoder().decode(uri);
			return ByteBuffer.wrap(data, view.byteOffset, view.byteLength).order(ByteOrder.LITTLE_ENDIAN);
		}

		if (uri.startsWith("data:application/gltf-buffer;base64,")) {
			uri = uri.substring("data:application/gltf-buffer;base64,".length());

			byte[] data = Base64.getDecoder().decode(uri);
			return ByteBuffer.wrap(data, view.byteOffset, view.byteLength).order(ByteOrder.LITTLE_ENDIAN);
		}

		if (view.buffer == 0 && binaryData != null) {
			return ByteBuffer.wrap(binaryData, view.byteOffset, view.byteLength).order(ByteOrder.LITTLE_ENDIAN);
		}

		throw new IllegalArgumentException("Data buffer is not backed by a data URI or binary");
	}

	/*
	 * IMPL NOTE FOR get*Access METHODS
	 * I'm assuming in this code that "byteStride" will be some multiple of sizeof(componentType).
	 * If this is not true then the data will immediately become misaligned.
	 *
	 * We do things this way because otherwise we'd need to re-slice the FloatBuffer from the underlying bytebuffer at
	 * every single element access.
	 */


	public Vector3d[] getVec3Access(int accessor, byte[] bindaryData) {
		if (accessor<0 || accessor>=accessors.length) throw new IllegalArgumentException("accessor argument must be between 0 and "+(accessors.length-1));
		if (!accessors[accessor].type.equals("VEC3")) throw new IllegalArgumentException("accessor must be a VEC3 type (is '"+accessors[accessor].type+"').");
		int elementCount = accessors[accessor].count;
		if (accessors[accessor].componentType == GL_FLOAT) {
			int buffer = accessors[accessor].bufferView;

			FloatBuffer floatBuffer = getDataBuffer(buffer, bindaryData).asFloatBuffer();
			int stride = bufferViews[accessors[accessor].bufferView].byteStride;
			if (stride==0) stride = 12;

			Vector3d[] result = new Vector3d[elementCount];

			for(int i=0; i<elementCount; i++) {
				if (stride!=12) floatBuffer.position((stride/4) * i);
				result[i] = new Vector3d(floatBuffer.get(), floatBuffer.get(), floatBuffer.get());
			}

			return result;

		} else {
			throw new IllegalArgumentException("Cannot unpack vec3's from componentType "+accessors[accessor].componentType);
		}
	}

	public Vector2d[] getVec2Access(int accessor, byte[] binaryData) {
		if (accessor<0 || accessor>=accessors.length) throw new IllegalArgumentException("accessor argument must be between 0 and "+(accessors.length-1));
		if (!accessors[accessor].type.equals("VEC2")) throw new IllegalArgumentException("accessor must be a VEC2 type (is '"+accessors[accessor].type+"').");
		int elementCount = accessors[accessor].count;
		if (accessors[accessor].componentType == GL_FLOAT) {
			int buffer = accessors[accessor].bufferView;

			FloatBuffer floatBuffer = getDataBuffer(buffer, binaryData).asFloatBuffer();
			int stride = bufferViews[accessors[accessor].bufferView].byteStride;
			if (stride==0) stride = 8;

			Vector2d[] result = new Vector2d[elementCount];

			for(int i=0; i<elementCount; i++) {
				if (stride!=8) floatBuffer.position((stride/4) * i);
				result[i] = new Vector2d(floatBuffer.get(), floatBuffer.get());
			}

			return result;

		} else {
			throw new IllegalArgumentException("Cannot unpack vec2's from componentType "+accessors[accessor].componentType);
		}
	}


	public int[] getScalarAccess(int accessor, byte[] binaryData) {
		if (accessor<0 || accessor>=accessors.length) throw new IllegalArgumentException("accessor argument must be between 0 and "+(accessors.length-1));
		if (!accessors[accessor].type.equals("SCALAR")) throw new IllegalArgumentException("accessor must be a SCALAR type (is '"+accessors[accessor].type+"').");
		int elementCount = accessors[accessor].count;
		if (accessors[accessor].componentType == GL_USHORT) {
			int buffer = accessors[accessor].bufferView;

			ShortBuffer shortBuffer = getDataBuffer(buffer, binaryData).asShortBuffer();
			int stride = bufferViews[accessors[accessor].bufferView].byteStride;
			if (stride==0) stride = 2;

			int[] result = new int[elementCount];

			for(int i=0; i<elementCount; i++) {
				if (stride!=2) shortBuffer.position((stride/2) * i);
				result[i] = shortBuffer.get() & 0xFFFF;
			}

			return result;
		} else if (accessors[accessor].componentType == GL_INT) {
			int buffer = accessors[accessor].bufferView;
			IntBuffer intBuffer = getDataBuffer(buffer, binaryData).asIntBuffer();
			int stride = bufferViews[accessors[accessor].bufferView].byteStride;
			if (stride==0) stride = 4;

			int[] result = new int[elementCount];

			for(int i=0; i<elementCount; i++) {
				if (stride!=4) intBuffer.position((stride/4) * i);
				result[i] = intBuffer.get();
			}

			return result;
		} else {
			throw new IllegalArgumentException("Cannot unpack scalars from componentType "+accessors[accessor].componentType);
		}
	}

	public static class GLTFAccessor {
		public int bufferView = 0;
		public int byteOffset = 0;
		public int componentType = 0;
		public boolean normalized = false;
		public int count = 0;
		public String type = "VOID";
		public float[] max = {0};
		public float[] min = {0};
		public GLTFAccessorSparse sparse = null;
		public String name = null;

		public GLTFAccessor() {}
	}

	public static class GLTFAccessorSparse {
		public int count = 0;
		public GLTFAccessorSparseIndices indices = new GLTFAccessorSparseIndices();
		public GLTFAccessorSparseValues values = new GLTFAccessorSparseValues();
	}

	public static class GLTFAccessorSparseIndices {
		public int bufferView = 0;
		public int byteOffset = 0;
		public int componentType = 0;

		public GLTFAccessorSparseIndices() {}
	}

	public static class GLTFAccessorSparseValues {
		public int bufferView = 0;
		public int byteOffset = 0;

		public GLTFAccessorSparseValues() {}
	}

	public static class GLTFAnimation {
		public GLTFAnimationChannel[] channels = new GLTFAnimationChannel[0];
		public GLTFAnimationSampler[] samplers = new GLTFAnimationSampler[0];
		public String name = null;

		public GLTFAnimation() {}
	}

	public static class GLTFAnimationChannel {
		public int sampler = 0;
		public GLTFAnimationChannelTarget target = new GLTFAnimationChannelTarget();

		public GLTFAnimationChannel() {}
	}

	public static class GLTFAnimationChannelTarget {
		public int node = -1;
		public String path = null;
		public JsonObject extensions = null;

		public GLTFAnimationChannelTarget() {}
	}

	public static class GLTFAnimationSampler {
		/**
		 * Accessor.
		 */
		public int input = 0;
		public String interpolation = "LINEAR";
		/**
		 * Accessor.
		 */
		public int output = 0;

		public GLTFAnimationSampler() {}
	}

	public static class GLTFAsset {
		public String version = "untitiled";
		public String generator = "unknown";
		public String copyright = null;

		public GLTFAsset() {}
	}

	public static class GLTFBuffer {
		public int byteLength = 0;
		public String uri = "";

		public GLTFBuffer() {}
	}

	public static class GLTFBufferView {
		public int buffer = 0;
		public int byteOffset = 0;
		public int byteLength = 0;
		public int target = 0; //34962 for ARRAY_BUFFER (e.g. positions, normals); 34963 for ELEMENT_BUFFER (e.g. indices)
		public int byteStride = 0;

		public GLTFBufferView() {}
	}

	public static class GLTFMaterial {
		public String name = null;
		public JsonObject extensions = null;
		public JsonElement extras = null;
		public GLTFPBRMetallicRoughness pbrMetallicRoughness = new GLTFPBRMetallicRoughness();
		public GLTFMaterialNormalTextureInfo normalTexture = new GLTFMaterialNormalTextureInfo();
		public GLTFMaterialOcclusionTextureInfo occlusionTexture = new GLTFMaterialOcclusionTextureInfo();
		public GLTFTextureInfo emissiveTexture = new GLTFTextureInfo();
		public float[] emissiveFactor = {0,0,0};
		public String alphaMode = "OPAQUE";
		public float alphaCutoff = 0.5f;
		public boolean doubleSided = false;

		public GLTFMaterial() {}
	}

	public static class GLTFPBRMetallicRoughness {
		public float[] baseColorFactor = {1,1,1,1};
		public GLTFTextureInfo baseColorTexture = new GLTFTextureInfo();
		public float metallicFactor = 1;
		public float roughnessFactor = 1;
		public GLTFTextureInfo metallicRoughnessTexture = new GLTFTextureInfo();

		public GLTFPBRMetallicRoughness() {}
	}

	public static class GLTFTextureInfo {
		public int index = 0;
		public int texCoord = 0;

		public GLTFTextureInfo() {}
	}

	public static class GLTFMaterialNormalTextureInfo {
		public int index = 0;
		public int texCoord = 0;
		public float scale = 1;

		public GLTFMaterialNormalTextureInfo() {}
	}

	public static class GLTFMaterialOcclusionTextureInfo {
		public int index = 0;
		public int texCoord = 0;
		public float strength = 1;

		public GLTFMaterialOcclusionTextureInfo() {}
	}

	/** Equivalent to glow Model */
	public static class GLTFMesh {
		public GLTFPrimitive[] primitives = new GLTFPrimitive[0];
		public int[] weights = new int[0];
		public String name = null;

		public GLTFMesh() {}
	}

	/** Equivalent to glow Mesh */
	public static class GLTFPrimitive {
		public int mode = 0;
		public int material = 0;
		public int indices = 0; //index buffer
		public GLTFPrimitiveAttributes attributes = new GLTFPrimitiveAttributes();
		public GLTFPrimitiveAttributes[] targets = new GLTFPrimitiveAttributes[0];

		public GLTFPrimitive() {}
	}

	/** sort of equivalent to Mesh.Vertex[] */
	public static class GLTFPrimitiveAttributes {
		public int POSITION = -1;
		public int NORMAL = -1;
		public int TANGENT = -1;
		public int TEXCOORD_0 = -1;
		public int TEXCOORD_1 = -1;
		public int COLOR_0 = -1;
		public int JOINTS_0 = -1;
		public int WEIGHTS_0 = -1;

		public GLTFPrimitiveAttributes() {}
	}

	public static class GLTFNode {
		public int[] children;
		public int skin;
		public float[] matrix = {1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1};
		public int mesh;
		public float[] rotation = {0,0,0,1};
		public float[] scale = {1,1,1};
		public float[] translation = {0,0,0};
		public float[] weights = new float[0];
		public String name = null;
		public JsonObject extensions = null;

		public GLTFNode() {}
	}

	public static class GLTFSampler {
		public int magFilter = 0;
		public int minFilter = 0;
		public int wrapS = GL_REPEAT;
		public int wrapT = GL_REPEAT;
		public String name = null;

		public GLTFSampler() {}
	}

	public static class GLTFSkin {
		public int inverseBindMatrices = 0;
		public int skeleton = 0;
		public int[] joints = new int[0];
		public String name = null;

		public GLTFSkin() {}
	}

	public static class GLTFTexture {
		public int sampler = 0;
		public int source = 0;
		public String name = null;
		public JsonObject extensions = null;

		public GLTFTexture() {}
	}

	public static class GLTFImage {
		public String mimeType = "image/png";
		public String uri = "#all";
		public int bufferView = -1;
		public String name = null;

		public GLTFImage() {}
	}
}
