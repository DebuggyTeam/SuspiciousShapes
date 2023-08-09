/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Model implements Iterable<Mesh> {
	protected Material environment = new Material();
	protected List<Mesh> meshes = new ArrayList<>();
	
	public ShaderAttributeHolder getEnvironment() {
		return environment;
	}
	
	public void transform(Matrix3d matrix) {
		for(Mesh mesh : meshes) mesh.transform(matrix);
	}
	
	public void transform(Matrix4d matrix) {
		for(Mesh mesh : meshes) mesh.transform(matrix);
	}
	
	public List<Mesh> getMeshes() {
		return meshes;
	}
	
	@Override
	public Iterator<Mesh> iterator() {
		return meshes.iterator();
	}
	
	public Model copy() {
		Model result = new Model();
		result.environment.putAll(environment);
		for(Mesh m : meshes) {
			result.meshes.add(m.copy());
		}
		
		return result;
	}
}
