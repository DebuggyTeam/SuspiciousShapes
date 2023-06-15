package gay.debuggy.shapes.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class Resources {
	public static Optional<String> loadString(ResourceManager resourceManager, Identifier id) {
		Optional<Resource> maybeResource = resourceManager.getResource(id);
		if (maybeResource.isEmpty()) return Optional.empty();
		try(InputStream in = maybeResource.get().open()) {
			return Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException ex) {
			return Optional.empty();
		}
	}
}
