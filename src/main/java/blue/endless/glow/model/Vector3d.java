/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model;

public record Vector3d(double x, double y, double z) {
	public Vector3d translate(double x, double y, double z) {
		return new Vector3d(this.x+x, this.y+y, this.z+z);
	}
}
