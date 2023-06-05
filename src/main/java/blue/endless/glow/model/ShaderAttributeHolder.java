/*
 * This file is part of Glow ( https://github.com/playsawdust/glow-base ), used under the Mozilla Public License.
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package blue.endless.glow.model;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

public interface ShaderAttributeHolder {
	public @Nullable <T> T get(ShaderAttribute<T> attribute);
	
	public default <T> T get(ShaderAttribute<T> attribute, T fallback) {
		T t = get(attribute);
		return (t==null) ? fallback : t;
	}
	
	public ImmutableMap<ShaderAttribute<?>, Object> getAll();
}
