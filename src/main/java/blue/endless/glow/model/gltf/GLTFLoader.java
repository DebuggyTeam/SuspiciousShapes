/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model.gltf;

import java.io.IOException;

import com.google.gson.GsonBuilder;

import blue.endless.glow.model.Material;
import blue.endless.glow.model.Mesh;
import blue.endless.glow.model.Model;
import blue.endless.glow.model.ShaderAttribute;
import blue.endless.glow.model.Vector2d;
import blue.endless.glow.model.Vector3d;
import blue.endless.glow.model.gltf.impl.GLTFData;

public class GLTFLoader {
	public static Model loadString(String json) throws IOException {
		GLTFData gltfData = new GsonBuilder().create().fromJson(json, GLTFData.class);
		//GLTFData gltfData = Jankson.builder().build().fromJson(json, GLTFData.class);
		
		Model result = new Model();
		
		for(GLTFData.GLTFMesh mesh : gltfData.meshes) {
			for (GLTFData.GLTFPrimitive primitive : mesh.primitives) {
				int[] indexBuffer = gltfData.getScalarAccess(primitive.indices);
				Vector3d[] positionBuffer = gltfData.getVec3Access(primitive.attributes.POSITION);
				Vector2d[] uvBuffer = gltfData.getVec2Access(primitive.attributes.TEXCOORD_0);
				Vector3d[] normalBuffer = gltfData.getVec3Access(primitive.attributes.NORMAL);
				
				GLTFData.GLTFMaterial gl_material = gltfData.materials[primitive.material];
				Material material = new Material();
				material.put(ShaderAttribute.METALNESS, (double) gl_material.pbrMetallicRoughness.metallicFactor);
				material.put(ShaderAttribute.ROUGHNESS, (double) gl_material.pbrMetallicRoughness.roughnessFactor);
				int texIndex = gl_material.pbrMetallicRoughness.baseColorTexture.index;
				GLTFData.GLTFTexture tex = gltfData.textures[texIndex];
				//if (tex.name!=null) {
				//	material.put(ShaderAttribute.DIFFUSE_TEXTURE, tex.name);
				//} else {
					GLTFData.GLTFImage imageSource = gltfData.images[tex.source];
					material.put(ShaderAttribute.DIFFUSE_TEXTURE, imageSource.uri);
				//}
				
				result.getMeshes().add(new Mesh(material, positionBuffer, uvBuffer, normalBuffer, indexBuffer));
			}
		}
		
		return result;
	}
}
