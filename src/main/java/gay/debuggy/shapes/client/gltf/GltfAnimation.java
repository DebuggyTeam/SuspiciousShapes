package gay.debuggy.shapes.client.gltf;

public class GltfAnimation extends GltfChildOfRootProperty {
	//REQUIRED: channels, samplers
	
	Channel[] channels;
	Sampler[] samplers;
	
	public GltfAnimation() {}
	
	public static class Channel extends GltfProperty {
		//REQUIRED: sampler, target
		
		int sampler;
		ChannelTarget target;
		
		public Channel() {}
	}
	
	public static class ChannelTarget extends GltfProperty {
		//REQUIRED: path (node may be undefined if the animation channel is governed by extensions)
		
		int node;
		String path;
		
		public ChannelTarget() {}
	}
	
	public static class Sampler extends GltfProperty {
		//REQUIRED: input, output
		
		int input;
		String interpolation = "LINEAR";
		int output;
		
		public Sampler() {}
	}
}
