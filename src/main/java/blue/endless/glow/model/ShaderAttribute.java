/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model;

public class ShaderAttribute<T> {
	protected final String name;
	
	public ShaderAttribute(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public static final ShaderAttribute<String> DIFFUSE_TEXTURE = new ShaderAttribute<>("diffuse_texture");
	public static final ShaderAttribute<Vector3d> POSITION = new ShaderAttribute<>("position");
	public static final ShaderAttribute<Vector2d> TEXCOORD = new ShaderAttribute<>("uv");
	public static final ShaderAttribute<Vector3d> NORMAL   = new ShaderAttribute<>("normal");
	
	public static final ShaderAttribute<Double> ROUGHNESS = new ShaderAttribute<>("roughness");
	public static final ShaderAttribute<Double> METALNESS = new ShaderAttribute<>("metalness");
	
	//Additional minecraft attributes
	public static final ShaderAttribute<Integer> COLOR_INDEX = new ShaderAttribute<>("color_index");
	public static final ShaderAttribute<Integer> DIFFUSE_COLOR = new ShaderAttribute<>("diffuse_color");
}
