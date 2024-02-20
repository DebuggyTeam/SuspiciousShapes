/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model.gltf;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import blue.endless.glow.model.Material;
import blue.endless.glow.model.Mesh;
import blue.endless.glow.model.Model;
import blue.endless.glow.model.ShaderAttribute;
import blue.endless.glow.model.Vector2d;
import blue.endless.glow.model.Vector3d;
import blue.endless.glow.model.gltf.impl.GLTFData;

public class GLTFLoader {
	private static final Gson GSON = new GsonBuilder()
		//.registerTypeAdapter(ModelTransformation.class, foo)
		.create();

	public static Model loadString(String json) throws IOException {
		return loadString(json, null);
	}

	public static Model loadString(String json, byte[] binaryData) throws IOException {
		return loadModel(loadRaw(json), binaryData);
	}

	public static Model loadModel(GLTFData gltfData, byte[] binaryData) throws IOException {

		Model result = new Model();

		Vector3d globalTranslate = new Vector3d(0, 0, 0);
		if (gltfData.asset.generator != null && gltfData.asset.generator.startsWith("Blockbench")) {
			globalTranslate = new Vector3d(0, -0.5, 0);
		}

		for(GLTFData.GLTFMesh mesh : gltfData.meshes) {
			for (GLTFData.GLTFPrimitive primitive : mesh.primitives) {
				int[] indexBuffer = gltfData.getScalarAccess(primitive.indices, binaryData);
				Vector3d[] positionBuffer = gltfData.getVec3Access(primitive.attributes.POSITION, binaryData);
				Vector2d[] uvBuffer = gltfData.getVec2Access(primitive.attributes.TEXCOORD_0, binaryData);
				Vector3d[] normalBuffer = gltfData.getVec3Access(primitive.attributes.NORMAL, binaryData);
				//TODO: look for ColorAccess

				GLTFData.GLTFMaterial gl_material = gltfData.materials[primitive.material];
				Material material = new Material();
				material.put(ShaderAttribute.METALNESS, (double) gl_material.pbrMetallicRoughness.metallicFactor);
				material.put(ShaderAttribute.ROUGHNESS, (double) gl_material.pbrMetallicRoughness.roughnessFactor);

				int texIndex = gl_material.pbrMetallicRoughness.baseColorTexture.index;
				GLTFData.GLTFTexture tex = (texIndex < gltfData.textures.length) ?
						gltfData.textures[texIndex] :
						new GLTFData.GLTFTexture();

				GLTFData.GLTFImage imageSource = (tex.source < gltfData.images.length) ?
						gltfData.images[tex.source] :
						new GLTFData.GLTFImage();

				material.put(ShaderAttribute.DIFFUSE_TEXTURE, imageSource.uri);

				for(int i=0; i<positionBuffer.length; i++) {
					Vector3d pos = positionBuffer[i];
					positionBuffer[i] = new Vector3d(pos.x() + globalTranslate.x(), pos.y() + globalTranslate.y(), pos.z() + globalTranslate.z());
				}

				result.getMeshes().add(new Mesh(material, positionBuffer, uvBuffer, normalBuffer, indexBuffer));
			}
		}

		return result;
	}

	public static GLTFData loadRaw(String json) {
		return GSON.fromJson(json, GLTFData.class);
		//return Jankson.builder().build().fromJson(json, GLTFData.class);
	}
}
