import java.util.Iterator;
import java.util.LinkedList;

public class GraphNode {
	
	private int data;
	private LinkedList<GraphNode> adjacency;
	private int outDegree;
	private int inDegree;
	
	public GraphNode(int data) {
		this.data = data;
		adjacency = new LinkedList<GraphNode>();
		outDegree = 0;
		inDegree = 0;
	}
	
	public GraphNode(int data, LinkedList<GraphNode> adjacency) {
		this.data = data;
		this.adjacency = adjacency;
		outDegree = adjacency.size();
		inDegree = 0;
	}
	
	public GraphNode(GraphNode other) {
		this(other.data);
		for (GraphNode u : other.adjacency)
			adjacency.add(u);	
	}
	
	public int getData() {
		return data;
	}
	
	public void setData(int data) {
		this.data = data;
	}
	
	public int getInDegree() {
		return inDegree;
	}
	public int getOutDegree() {
		return outDegree;
	}
	
	public Iterator<GraphNode> getChildrenIterator(){
		return adjacency.iterator();
	}
	
	public void addChild(GraphNode node) {
		adjacency.add(node);
		outDegree++;
		node.inDegree++;
	}
	
	public void removeChild(GraphNode node) {
		if (adjacency.contains(node)){
			adjacency.remove(node);
			outDegree--;
			node.inDegree--;
		}
	}
	
	public boolean isChild(GraphNode node) {
		return adjacency.contains(node);
	}
}