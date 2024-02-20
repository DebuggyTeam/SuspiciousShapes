package blue.endless.glow.model.glb;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import blue.endless.glow.model.Model;
import blue.endless.glow.model.gltf.GLTFLoader;

public class GLBLoader {
	public static Model load(InputStream is) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(is);
		DataInputStream dis = new DataInputStream(bis);
		// readInt is big-endian
		int magic = dis.readInt();
		if (magic != 0x676C5446) throw new IOException("Not GLB data");
		int _version = swap(dis.readInt());
		int fileLength = swap(dis.readInt());

		int jsonChunkLength = swap(dis.readInt());
		int jsonChunkType = dis.readInt();
		if (jsonChunkType != 0x4A534F4E) throw new IOException("First chunk is not json");

		byte[] jsonBytes = new byte[jsonChunkLength];
		dis.readFully(jsonBytes);
		String json = new String(jsonBytes, StandardCharsets.UTF_8);

		if (fileLength > jsonChunkLength + 12) {
			// binary blob is included too
			int binChunkLength = swap(dis.readInt());
			int binChunkType = dis.readInt();
			if (binChunkType != 0x42494E00) throw new IOException("Second chunk is not binary");

			byte[] bin = new byte[binChunkLength];
			return GLTFLoader.loadString(json, bin);
		} else {
			return GLTFLoader.loadString(json);
		}
	}

	private static int swap(int in) {
		return ((in >> 24) & 0x000000FF) | ((in >> 8) & 0x0000FF00) | ((in << 8) & 0x00FF0000) |
			((in << 24) & 0xFF000000);
	}
}
