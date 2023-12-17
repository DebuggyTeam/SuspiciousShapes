package gay.debuggy.shapes.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;

public class SuspiciousShapesClient implements ClientModInitializer {
	public static final String MODID = "suspicious_shapes";
	public static final Logger LOGGER = LoggerFactory.getLogger("Suspicious Shapes");

	@Override
	public void onInitializeClient() {
		Config.init();
		
		PreparableModelLoadingPlugin.register(SuspiciousShapesModelLoadingPlugin::loadData, new SuspiciousShapesModelLoadingPlugin());
	}
	
}