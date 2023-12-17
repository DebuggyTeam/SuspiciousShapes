package gay.debuggy.shapes.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public class Config {
	public static transient Config instance = new Config();
	
	public LogLevel log_level = LogLevel.NO_ERRORS;
	
	public static enum LogLevel {
		QUIET(0),
		NO_ERRORS(1),
		VERBOSE(2);
		
		private final int value;
		
		private LogLevel(int v) {
			value = v;
		}
		
		public int value() {
			return value;
		}
	}
	
	public static Config init() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(SuspiciousShapesClient.MODID+".json");
		
		if (!Files.exists(configPath)) {
			return instance;
		}
		
		try (BufferedReader in = Files.newBufferedReader(configPath)) {
			instance = new GsonBuilder().create().fromJson(in, Config.class);
		} catch (IOException ex) {
			SuspiciousShapesClient.LOGGER.warn("Could not load config file.", ex);
		}
		
		return instance;
	}
}
