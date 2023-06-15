package gay.debuggy.shapes.client;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;

public class SuspiciousShapesClient implements ClientModInitializer {
	
	@Override
	public void onInitializeClient(ModContainer mod) {
		ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm->new GLTFModelProvider(rm));
		//ModelLoadingRegistry.INSTANCE.registerVariantProvider(rm->new VanillaPlusVariantProvider(rm));
	}
	
}