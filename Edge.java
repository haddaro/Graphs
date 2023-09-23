
public class Edge {
	
	//instance variables of node are final to avoid redirection of nodes which can be risky
	private Tuple<Integer, Integer> vertices;
	private int weight;
	
	public Edge(int from, int to) {
		vertices = new Tuple<>(from, to);
		weight = 0;
	}
	
	public Edge(int from, int to, int weight) {
		this(from, to);
		this.weight = weight;	
	}
	
	public Edge (Edge other){
		vertices = new Tuple(other.getFrom(), other.getTo());
		weight = other.weight;
	}
	
	public void setWeight(int w) {
		weight = w;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public int getFrom() {
		return vertices.getFirst();
	}
	
	public int getTo() {
		return vertices.getSecond();
	}
	
	public Edge getRevEdge(Edge e) {
		return new Edge(e.getTo(), e.getFrom());
	}
	
	public Tuple<Integer, Integer> getUV() { 
		return vertices;
	}
	
	public String toString() {
		return ("w(" + vertices.getFirst() + "," + vertices.getSecond() + ")=" + weight);
	}
}
