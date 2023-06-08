package gay.debuggy.shapes;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SuspiciousShapesMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Suspicious Shapes");

	@Override
	public void onInitialize(ModContainer mod) {
		LOGGER.info("Hello Quilt world from {}!", mod.metadata().name());
		
		Block block = new Block(QuiltBlockSettings.copyOf(Blocks.STONE));
		Registry.register(Registries.BLOCK, new Identifier("suspicious_shapes", "test"), block);
		Registry.register(Registries.ITEM, new Identifier("suspicious_shapes", "test"), new BlockItem(block, new QuiltItemSettings()));
	}
}
