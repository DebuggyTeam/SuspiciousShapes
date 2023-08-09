package gay.debuggy.shapes.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.Identifier;

public class UnprocessedModelData {
	public List<Node> resources = new ArrayList<>();
	//public List<Node> items = new ArrayList<>();
	
	public static record Node(Identifier location, String data) {}
}
