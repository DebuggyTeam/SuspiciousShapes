package gay.debuggy.shapes.client;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.HashSet;

import blue.endless.glow.model.Model;
import gay.debuggy.shapes.client.schema.BlockModelPlus;

import net.minecraft.util.Identifier;

public class ProcessedModelData {
	public List<ErrorNode> errors = new ArrayList<>();
	public List<Node> detached = new ArrayList<>();
	public List<Node> roots = new ArrayList<>();
	
	public Map<Identifier, Node> byId = new HashMap<>();
	
	public static class Node {
		Identifier location;
		Identifier id;
		GlowUnbakedModel model;
		String error = null;
		Node parent;
		List<Node> children = new ArrayList<>();
		
		public Node(Identifier location) {
			this.location = location;
			String namespace = location.getNamespace();
			String path = location.getPath();
			if (path.startsWith("models/")) path = path.substring("models/".length());
			if (path.endsWith(".json")) {
				path = path.substring(0, path.length() - ".json".length());
			}// else if (path.endsWith(".gltf")) {
			//	path = path.substring(0, path.length() - ".gltf".length());
			//}
			this.id = new Identifier(namespace, path);
		}
		
		public int treeSize() {
			return 1 + children.stream().mapToInt(Node::treeSize).sum();
		}
		
		public boolean hasPathToRoot(List<Node> roots) {
			HashSet<Identifier> traversed = new HashSet<>();
			Node cur = this;
			while(cur.parent != null) {
				cur = cur.parent;
				if (traversed.contains(cur.location)) throw new IllegalStateException("Circular reference detected.");
				traversed.add(cur.location);
			}
			return roots.contains(cur);
		}
		
		public Node getRoot() {
			HashSet<Identifier> traversed = new HashSet<>();
			Node cur = this;
			while(cur.parent != null) {
				cur = cur.parent;
				if (traversed.contains(cur.location)) throw new IllegalStateException("Circular reference detected.");
				traversed.add(cur.location);
			}
			return cur;
		}
		
		public List<Node> getPathFromRoot() {
			ArrayList<Node> traversed = new ArrayList<>();
			Node cur = this;
			traversed.add(cur);
			while (cur.parent != null) {
				cur = cur.parent;
				if (traversed.contains(cur)) throw new IllegalStateException("Circular reference detected.");
				traversed.add(cur);
			}
			
			return Lists.reverse(traversed);
		}
	}
	
	public static class JsonNode extends Node {
		BlockModelPlus blockModelPlus;
		
		public JsonNode(Identifier location, BlockModelPlus model) {
			super(location);
			this.blockModelPlus = model;
		}
	}
	
	public static class GltfNode extends Node {
		Model model;
		
		public GltfNode(Identifier location, Model model) {
			super(location);
			this.model = model;
		}
	}
	
	public static record ErrorNode(Identifier id, String message, Throwable t) {}
}
