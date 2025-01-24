package gay.debuggy.shapes.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import blue.endless.glow.model.Material;
import blue.endless.glow.model.Mesh;
import blue.endless.glow.model.Model;
import blue.endless.glow.model.ShaderAttribute;
import blue.endless.glow.model.Vector2d;
import blue.endless.glow.model.Vector3d;

public class ObjLoader {
	public static Model loadString(String data) throws IOException {
		
		List<Vector3d> positions = new ArrayList<>();
		List<Vector2d> uvs = new ArrayList<>();
		List<Vector3d> normals = new ArrayList<>();
		
		String materialLine = "#texture";
		
		List<Mesh.Face> faces = new ArrayList<>();
		
		for(String line : data.lines().toList()) {
			line = line.trim();
			if (line.isBlank()) continue;
			
			if (line.startsWith("#")) continue; // ignore comments
			if (line.startsWith("mtllib ")) continue; // ignore materials for now.
			if (line.startsWith("v ")) {
				line = line.substring("v ".length());
				String[] parts = line.split(" ");
				if (parts.length != 3) continue; // Either this file is not three-dimensional, or this file has errors.
				try {
					Vector3d pos = new Vector3d(
							Double.parseDouble(parts[0]),
							Double.parseDouble(parts[1]),
							Double.parseDouble(parts[2])
							);
				
					positions.add(pos);
				} catch (Throwable t) {
					throw new IOException("There was a problem parsing a vertex coordinate.", t);
				}
				
				continue;
			}
			
			if (line.startsWith("vt ")) {
				line = line.substring("vt ".length());
				String[] parts = line.split(" ");
				if (parts.length != 2) continue; // Either this file is not three-dimensional, or this file has errors.
				try {
					Vector2d pos = new Vector2d(
							Double.parseDouble(parts[0]),
							Double.parseDouble(parts[1])
							);
				
					uvs.add(pos);
				} catch (Throwable t) {
					throw new IOException("There was a problem parsing a vertex texture coordinate.", t);
				}
				
				continue;
			}
			
			if (line.startsWith("vn ")) {
				line = line.substring("vn ".length());
				String[] parts = line.split(" ");
				if (parts.length != 3) continue; // Either this file is not three-dimensional, or this file has errors.
				try {
					Vector3d normal = new Vector3d(
							Double.parseDouble(parts[0]),
							Double.parseDouble(parts[1]),
							Double.parseDouble(parts[2])
							);
				
					normals.add(normal);
				} catch (Throwable t) {
					throw new IOException("There was a problem parsing a vertex normal.", t);
				}
				
				continue;
			}
			
			if (line.startsWith("usemtl ")) {
				line = line.substring("usemtl ".length());
				
				materialLine = line;
				
				continue;
			}
			
			if (line.startsWith("f ")) {
				line = line.substring("f ".length());
				Mesh.Face face = new Mesh.Face();
				
				String[] parts = line.split(" ");
				
				try {
					for(String vertexString : parts) {
						String[] vertexParts = vertexString.split(Pattern.quote("/"));
						
						Mesh.Vertex vertex = new Mesh.Vertex();
						
						vertex.put(ShaderAttribute.POSITION, positions.get(Integer.parseInt(vertexParts[0])));
						
						if (!vertexParts[1].isBlank()) {
							vertex.put(ShaderAttribute.TEXCOORD, uvs.get(Integer.parseInt(vertexParts[1])));
						} else {
							vertex.put(ShaderAttribute.TEXCOORD, new Vector2d(0,0));
						}
						
						if (!vertexParts[2].isBlank()) {
							vertex.put(ShaderAttribute.NORMAL, normals.get(Integer.parseInt(vertexParts[2])));
						} // TODO: If the normal is missing, calculate it from the positions
						
						face.vertices.add(vertex);
					}
					
					faces.add(face);
					
				} catch (Throwable t) {
					continue;
				}
			}
			
		}
		
		Material material = new Material();
		material.put(ShaderAttribute.DIFFUSE_TEXTURE, materialLine);
		
		//TODO: Variant of Mesh that supports quads and is more in line with how Minecraft works
		
		return null;
	}
}
