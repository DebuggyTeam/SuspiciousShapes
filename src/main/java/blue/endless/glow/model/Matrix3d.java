/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model;

public record Matrix3d(double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9) {
	
	public Vector3d transform(Vector3d vec) {
		return new Vector3d(
				vec.x()*m1 + vec.y()*m2 + vec.z()*m3,
				vec.x()*m4 + vec.y()*m5 + vec.z()*m6,
				vec.x()*m7 + vec.y()*m8 + vec.z()*m9
				);
	}
	
	
	/*
	 * Note: A 3x3 matrix cannot do translations.
	 */
	
	
	
	public static Matrix3d roll(double theta) {
		return new Matrix3d(
				Math.cos(theta), -Math.sin(theta), 0,
				Math.sin(theta),  Math.cos(theta), 0,
				0,                0,               1
				);
	}
	
	public static Matrix3d yaw(double theta) {
		return new Matrix3d(
				 Math.cos(theta), 0, Math.sin(theta),
				 0,               1, 0,
				-Math.sin(theta), 0, Math.cos(theta)
				);
	}
	
	public static Matrix3d pitch(double theta) {
		return new Matrix3d(
				1, 0,                0,
				0, Math.cos(theta), -Math.sin(theta),
				0, Math.sin(theta),  Math.cos(theta)
				);
	}
}
