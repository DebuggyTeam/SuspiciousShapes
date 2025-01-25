package gay.debuggy.shapes.client.gltf;

/**
 * A keyframe animation.
 */
public class GltfAnimation extends GltfChildOfRootProperty {
	//REQUIRED: channels, samplers
	
	/**
	 * An array of animation channels. An animation channel combines an animation sampler with a target property being
	 * animated. Different channels of the same animation **MUST NOT** have the same targets.
	 */
	Channel[] channels;
	
	/**
	 * An array of animation samplers. An animation sampler combines timestamps with a sequence of output values and
	 * defines an interpolation algorithm.
	 */
	Sampler[] samplers;
	
	public GltfAnimation() {}
	
	
	/**
	 * An animation channel combines an animation sampler with a target property being animated.
	 */
	public static class Channel extends GltfProperty {
		//REQUIRED: sampler, target
		
		/**
		 * The index of a sampler in this animation used to compute the value for the target, e.g., a node's
		 * translation, rotation, or scale (TRS).
		 */
		int sampler;
		
		/**
		 * The descriptor of the animated property.
		 */
		ChannelTarget target;
		
		public Channel() {}
	}
	
	/**
	 * The descriptor of the animated property.
	 */
	public static class ChannelTarget extends GltfProperty {
		//REQUIRED: path (node may be undefined if the animation channel is governed by extensions)
		
		/**
		 * The index of the node to animate. When undefined, the animated object **MAY** be defined by an extension.
		 */
		int node;
		
		/**
		 * The name of the node's TRS property to animate, or the `\"weights\"` of the Morph Targets it instantiates.
		 * For the `\"translation\"` property, the values that are provided by the sampler are the translation along the
		 * X, Y, and Z axes. For the `\"rotation\"` property, the values are a quaternion in the order (x, y, z, w),
		 * where w is the scalar. For the `\"scale\"` property, the values are the scaling factors along the X, Y, and Z
		 * axes.
		 */
		String path;
		
		public ChannelTarget() {}
	}
	
	/**
	 * An animation sampler combines timestamps with a sequence of output values and defines an interpolation algorithm.
	 */
	public static class Sampler extends GltfProperty {
		//REQUIRED: input, output
		
		/**
		 * The index of an accessor containing keyframe timestamps. The accessor **MUST** be of scalar type with
		 * floating-point components. The values represent time in seconds with `time[0] >= 0.0`, and strictly
		 * increasing values, i.e., `time[n + 1] > time[n]`.
		 */
		int input;
		
		/**
		 * Interpolation algorithm.
		 */
		Interpolation interpolation = Interpolation.LINEAR;
		
		/**
		 * The index of an accessor, containing keyframe output values.
		 */
		int output;
		
		public Sampler() {}
	}
	
	/**
	 * Interpolation algorithm.
	 */
	public static enum Interpolation {
		/**
		 * The animated values are linearly interpolated between keyframes. When targeting a rotation, spherical linear
		 * interpolation (slerp) **SHOULD** be used to interpolate quaternions. The number of output elements **MUST**
		 * equal the number of input elements.
		 */
		LINEAR,
		
		/**
		 * The animated values remain constant to the output of the first keyframe, until the next keyframe. The number
		 * of output elements **MUST** equal the number of input elements.
		 */
		STEP,
		
		/**
		 * The animation's interpolation is computed using a cubic spline with specified tangents. The number of output
		 * elements **MUST** equal three times the number of input elements. For each input element, the output stores
		 * three elements, an in-tangent, a spline vertex, and an out-tangent. There **MUST** be at least two keyframes
		 * when using this interpolation.
		 */
		CUBICSPLINE;
	}
}
