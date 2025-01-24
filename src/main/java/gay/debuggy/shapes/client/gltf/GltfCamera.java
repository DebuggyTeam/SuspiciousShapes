package gay.debuggy.shapes.client.gltf;

import com.google.gson.JsonElement;

public class GltfCamera extends GltfChildOfRootProperty {
	//REQUIRED: type
	
	Orthographic orthographic;
	Perspective perspective;
	int type;
	
	public GltfCamera() {}
	
	public static class Orthographic extends GltfProperty {
		//REQUIRED: xmag, ymag, zfar, znear
		
		double xmag;
		double ymag;
		double zfar;
		double znear;
		JsonElement extensions;
		JsonElement extras;
		
		public Orthographic() {}
	}
	
	public static class Perspective extends GltfProperty {
		//REQUIRED: yfov, znear
		
		double aspectRatio;
		double yfov;
		double zfar;
		double znear;
		JsonElement extensions;
		JsonElement extras;
		
		public Perspective() {}
	}
}
