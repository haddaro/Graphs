import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Iterator;


public class UndirectedGraph {

	private HashMap <Integer, GraphNode> nodes;

	/* ------------
	 * CONSTRUCTORS
	 * ------------
	 */
	
	public UndirectedGraph() {
		nodes = new HashMap<Integer, GraphNode>();
	}
	
	public UndirectedGraph(LinkedList<Integer> list) {
		for (int item: list) {
			GraphNode node = new GraphNode(item);
			nodes.put(item, node);
		}	
	}
	
	public UndirectedGraph(UndirectedGraph other) {
		this();
		//deep copy just the data of the nodes
		for (Map.Entry<Integer, GraphNode> entry : other.nodes.entrySet()) {
			GraphNode original = entry.getValue();
			GraphNode node = new GraphNode(original.getData());
			nodes.put(node.getData(), node);
			}
		//deep copy neighbours
		for (Map.Entry<Integer, GraphNode> entry : other.nodes.entrySet()) {
			GraphNode original = entry.getValue();
			GraphNode copy = nodes.get(original.getData());
			Iterator<GraphNode> i = original.getChildrenIterator();
			while (i.hasNext()) {
				int u = i.next().getData();
				GraphNode child = nodes.get(u);
				copy.addChild(child);
			}
		}
	}
	/* ---------------------
	 * STRING REPRESENTATION
	 * ---------------------
	 */
	
	public String toString() {
		StringBuilder ans = new StringBuilder();
		Set<Integer> nodesData = getNodes();
		Set<Edge> edges = getEdges();
		
		ans.append("Nodes:");
		ans.append("\n");
		for (int t : nodesData) {
			ans.append(t);
			ans.append(", ");
		}
		ans.setLength(ans.length()-2); //delete last comma
		ans.append("\n");
		ans.append("Edges:");
		ans.append("\n");
		for (Edge e : edges) {
			ans.append("(");
				ans.append(e.getFrom());
				ans.append(",");
				ans.append(e.getTo());
				ans.append("),");
			}
		ans.setLength(ans.length()-2);
		return ans.toString();
	}
	
	/* --------------
	 * EDIT THE GRAPH
	 * --------------
	 */
	
	public void addNode(int u) {
		if (nodes.containsKey(u))
			throw new GraphException("Node " + u + " is already in the graph");
		GraphNode node = new GraphNode(u);
		nodes.put(u, node);
	}
	
	public void connect(int u, int v) {
		checkNodes(u,v);
		nodes.get(u).addChild(nodes.get(v));
		nodes.get(v).addChild(nodes.get(u));
	}
	
	public void disconnect(int u, int v) {
		checkNodes(u,v);
		nodes.get(u).removeChild(nodes.get(v));
		nodes.get(v).removeChild(nodes.get(u));
	}
	
	public void delete(int u) {
		Iterator<GraphNode> i = nodes.get(u).getChildrenIterator();
		while (i.hasNext()) {
			GraphNode v = i.next();
			v.removeChild(nodes.get(u));
		}
		nodes.remove(u);
	}
	
	/*--------
	 * QUERIES
	 *--------
	 */
	public int ShortestDistanceFromTo(int from, int to) {
		HashMap<Integer, Boolean> visited = makeVisitedMap();
		HashMap<Integer, Integer> parent = makeParentMap();
		HashMap<Integer, Integer> layer = makeLayerMap();
		
		bfs (from, visited, parent, layer);
		return layer.get(to);
	}
	
	public boolean isEdge(int u, int v) {
		checkNodes(u,v);
		if (u == v)
			return false;
		GraphNode from;
		GraphNode to;
		if (nodes.get(u).getOutDegree() < nodes.get(v).getOutDegree()) {
			from = nodes.get(u);
			to = nodes.get(v);
		}
		else {
			from = nodes.get(v);
			to = nodes.get(u);
		}
		return from.isChild(to);
	}
		
	/* -------
	 * GETTERS
	 * -------
	 */
	
	public Set<Integer> getNodes() {
		return nodes.keySet();
	}
	
	public Set<Edge> getEdges(){
		Set<Edge> edges = new HashSet<>();
		for (int from : nodes.keySet()) {
			Iterator<GraphNode> i = nodes.get(from).getChildrenIterator();
			while (i.hasNext()) {
				int to = i.next().getData();
				Edge e = new Edge(from, to);
				if (!(edges.contains(e.getRevEdge(e))))
					edges.add(e);
			}
		}
		return edges;
	}
	
	public HashSet<Integer> getConnectedComponentOf(int u) {
		checkNode(u);
		HashMap<Integer, Boolean> visited = makeVisitedMap();
		HashMap<Integer, Integer> parent = makeParentMap();
		
		dfs(u, visited, parent);
		
		HashSet<Integer> nodesData = new HashSet<>();
		
		for (int t : visited.keySet()) {
			if (Boolean.TRUE.equals(visited.get(t)))
				nodesData.add(t);
		}
		return nodesData;
	}
	
	public DirectedGraph dfsTree(int from){
		checkNode(from);
		HashMap<Integer, Boolean> visited = makeVisitedMap();
		HashMap<Integer, Integer> parent = makeParentMap();
		
		dfs(from, visited, parent);
		
		return makeTree(from, parent);
	}
	
	public DirectedGraph bfsTree(int from) {
		checkNode(from);
		HashMap<Integer, Boolean> visited = makeVisitedMap();
		HashMap<Integer, Integer> parent = makeParentMap();
		HashMap<Integer, Integer> layer = makeLayerMap();
		
		bfs(from, visited, parent, layer);
		
		return makeTree(from, parent);
	}
	
	public Iterator<Integer> iterateV(){
		return nodes.keySet().iterator();
	}
	
	public Iterator<Edge> iterateE(){
		Set<Edge> temp = getEdges();
		return temp.iterator();
	}
	
	/* ----------------
	 * GRAPH ALGORITHMS
	 * ----------------
	 */
	
	/* ------------------- DEAPTH FIRST SEARCH -------------------*/
	protected void dfs(int u, HashMap<Integer, Boolean> visited, HashMap<Integer, Integer> parent) {
		if ((Boolean.TRUE.equals(visited.get(u))))
			return;
		visited.put(u, true);
		Iterator<GraphNode> i = nodes.get(u).getChildrenIterator();
		while(i.hasNext()){
			int child = i.next().getData();
			if (Boolean.FALSE.equals(visited.get(child))) {
				parent.put(child, u);
				dfs(child, visited, parent);
			}
		}	
	}
	
	/* ------------------- BREADTH FIRST SEARCH -------------------*/
	protected void bfs(int u, HashMap<Integer, Boolean> visited, HashMap<Integer, Integer> parent, HashMap<Integer, Integer> layer) {		
		checkNode(u);
		layer.put(u, 0);
		visited.put(u,  true);
		Queue<Integer> qu = new LinkedList<Integer>();
		qu.add(u);
		while(!qu.isEmpty()) {
			int current = qu.poll();
			//visit every child and push to the queue:
			Iterator<GraphNode> i = nodes.get(current).getChildrenIterator();
			while (i.hasNext()) {
				int child = i.next().getData();
				if (Boolean.FALSE.equals(visited.get(child))) {
					visited.put(child, true);
					parent.put(child, current);
					layer.put(child, layer.get(current)+1);
					qu.add(child);	
				}
			}
		}
	}
	
	/* -------
	 * UTILITY
	 * -------
	 */
	
	protected void checkNode(int u) {
		if (!(nodes.containsKey(u)))
			throw new GraphException("Can't find node " + u);		
	}
	
	protected void checkNodes(int u, int v) {
		checkNode(u);
		checkNode(v);
	}
	
	protected DirectedGraph makeTree(int root, HashMap<Integer, Integer> parent){
		DirectedGraph tree = new DirectedGraph();
		for (int key : parent.keySet())
			tree.addNode(key);
		for (int key : parent.keySet()) {
			if (parent.get(key) != null) 
				tree.connectFromTo(parent.get(key), key);
		}
		return tree;
	}

	//basically the same as the copy constructor, used to create other types of graph from an undirected graph
	public HashMap<Integer, GraphNode> getNodesForGraph() {
		HashMap<Integer, GraphNode> allNodes = new HashMap<>();
		
		for (Map.Entry<Integer, GraphNode> entry : nodes.entrySet()) {
			GraphNode original = entry.getValue();
			GraphNode node = new GraphNode(original.getData());
			allNodes.put(node.getData(), node);
			}
		for (Map.Entry<Integer, GraphNode> entry : nodes.entrySet()) {
			GraphNode original = entry.getValue();
			GraphNode copy = allNodes.get(original.getData());
			Iterator<GraphNode> i = original.getChildrenIterator();
			while (i.hasNext()) {
				int u = i.next().getData();
				GraphNode child = allNodes.get(u);
				copy.addChild(child);
			}
		}
		return allNodes;
	}
	
	//private functions that create data structures for the traversals:
	protected HashMap<Integer, Boolean> makeVisitedMap(){
		HashMap<Integer, Boolean> visited = new HashMap<>();
		for (int t : nodes.keySet())
			visited.put(t, false);
		return visited;
	}
	
	protected HashMap<Integer, Integer> makeParentMap(){
		HashMap<Integer, Integer> parent = new HashMap<>();
		for (int t : nodes.keySet())
			parent.put(t, null);
		return parent;
	}
	
	private HashMap<Integer, Integer> makeLayerMap(){
		HashMap<Integer, Integer> layer = new HashMap<>();
		for (int t : nodes.keySet())
			layer.put(t, -1);
		return layer;
	}

	

	
	
	
	
}