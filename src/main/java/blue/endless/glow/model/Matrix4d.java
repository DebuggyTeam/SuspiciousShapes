/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model;

public record Matrix4d(
		double m1,  double m2,  double m3,  double m4,
		double m5,  double m6,  double m7,  double m8,
		double m9,  double m10, double m11, double m12,
		double m13, double m14, double m15, double m16
		) {
	
	public Vector3d transform(Vector3d vec) {
		return new Vector3d(
				vec.x()*m1 + vec.y()*m2 + vec.z()*m3 + m4,
				vec.x()*m5 + vec.y()*m6 + vec.z()*m7 + m8,
				vec.x()*m9 + vec.y()*m10 + vec.z()*m11 + m12
				//Ignore m13-m16 because w starts as 1, and if it doesn't end as 1 then we won't be able to detect it.
				);
	}
	
	public static Matrix4d roll(double theta) {
		return new Matrix4d(
				Math.cos(theta), -Math.sin(theta), 0, 0,
				Math.sin(theta),  Math.cos(theta), 0, 0,
				0,                0,               1, 0,
				0,                0,               0, 1
				);
	}
	
	public static Matrix4d yaw(double theta) {
		return new Matrix4d(
				 Math.cos(theta), 0, Math.sin(theta), 0,
				 0,               1, 0,               0,
				-Math.sin(theta), 0, Math.cos(theta), 0,
				 0,               0, 0,               1
				);
	}
	
	public static Matrix4d pitch(double theta) {
		return new Matrix4d(
				1, 0,                0,               0,
				0, Math.cos(theta), -Math.sin(theta), 0,
				0, Math.sin(theta),  Math.cos(theta), 0,
				0, 0,                0,               1
				);
	}
	
	public static Matrix4d translate(double x, double y, double z) {
		return new Matrix4d(
				1, 0, 0, x,
				0, 1, 0, y,
				0, 0, 1, z,
				0, 0, 0, 1
				);
	}
}
