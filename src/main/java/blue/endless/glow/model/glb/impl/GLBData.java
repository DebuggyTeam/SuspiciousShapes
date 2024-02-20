package blue.endless.glow.model.glb.impl;

import org.jetbrains.annotations.Nullable;

import blue.endless.glow.model.gltf.impl.GLTFData;

public class GLBData {
	public GLTFData data;
	public byte @Nullable [] binaryData;

	public GLBData(GLTFData data, byte @Nullable [] binaryData) {
		this.data = data;
		this.binaryData = binaryData;
	}
}
