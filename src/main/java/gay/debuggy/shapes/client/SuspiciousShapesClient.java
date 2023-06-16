package gay.debuggy.shapes.client;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public class SuspiciousShapesClient implements ClientModInitializer {
	
	public static final Logger LOGGER = LoggerFactory.getLogger("Suspicious Shapes");

	@Override
	public void onInitializeClient(ModContainer mod) {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm->new GLTFModelProvider(rm));
	}
	
}