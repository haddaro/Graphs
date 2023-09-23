import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

/* ------------
 * CONSTRUCTORS
 * ------------
 */
public class DirectedGraph {
	protected HashMap <Integer, GraphNode> nodes;
	
	public DirectedGraph() {
		nodes = new HashMap<Integer, GraphNode>();
	}
	
	//Constructs a directed graph by converting each edge of an undirected graph to two opposing edges
	public DirectedGraph(UndirectedGraph g) {
		nodes = g.getNodesForGraph();
	}
	
	//Copy constructor
	public DirectedGraph(DirectedGraph other) {
		this();
		for (Map.Entry<Integer, GraphNode> entry : other.nodes.entrySet()) {
			GraphNode original = entry.getValue();
			GraphNode node = new GraphNode(original.getData());
			nodes.put(node.getData(), node);
			}
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
		ans.setLength(ans.length()-1);
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
	
	public void connectFromTo(int from, int to) {
		if (!isEdgeFromTo(from, to))
			nodes.get(from).addChild(nodes.get(to));
		else
			throw new GraphException("Edge from " + from + " to " + to + " already exists");
		
	}
	
	public void disconnectFromTo(int from, int to) {
		if (isEdgeFromTo(from, to))
			nodes.get(from).removeChild(nodes.get(to));
		else
			throw new GraphException("No edge from " + from + " to " + to);
	}
	
	public void disconnect(int u) {
		checkNode(u);
		Iterator<Integer> i = iterateChildren(u);
		
		//Disconnect all outgoing edges:
		while(i.hasNext()) {
			int child = i.next();
			disconnectFromTo(u, child);
		}
		
		//Disconnect all incoming edges:
		for (int t : nodes.keySet()){
			if (isEdgeFromTo(t, u))
				disconnectFromTo(t, u);
		}
	}
	
	public void removeNode(int u) {
		disconnect(u); 
		nodes.remove(u);
	}
	
	public int getInDegree(int u) {
		return nodes.get(u).getInDegree();
	}
	
	public int getOutDegree(int u) {
		return nodes.get(u).getOutDegree();
	}
	
	/*--------
	 * QUERIES
	 *--------
	 */
	public boolean isDAG() {
		return topologicalSort()!=null;
	}
	
	public boolean isEdgeFromTo(int from, int to) {
		if (nodes.keySet().contains(from) && nodes.keySet().contains(to))
			return nodes.get(from).isChild(nodes.get(to));
		return false;
	}
	
	public boolean canReachFromTo(int from, int to) {
		HashMap<Integer, Boolean> visited = new HashMap<>();
		for (int t : nodes.keySet())
			visited.put(t, false);
		canReachFromTo(from, to, visited);
		return canReachFromTo(from, to, visited);
	}
	
	public boolean isNode(int u) {
		return nodes.keySet().contains(u);
	}
	
	//checks if every edge has an opposite edge
	public boolean isBiDirectional() {
		Iterator<Edge> i = iterateE();
		while(i.hasNext()) {
			Edge e = i.next();
			if (!(isEdgeFromTo(e.getTo(), e.getFrom())))
				return false;
		}
		return true;
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
				edges.add(e);
			}
		}
		return edges;
	}
	
	public int getNodesNum() {
		return nodes.keySet().size();
	}
	
	public int getEdgesNum() {
		return getEdges().size();
	}
	
	public Iterator<Integer> iterateV(){
		return nodes.keySet().iterator();
	}
	
	public Iterator<Edge> iterateE(){
		Set<Edge> temp = getEdges();
		return temp.iterator();
	}
	
	public Iterator<Integer> iterateChildren(int u){
		Set<Integer> temp = getAllChildren(u);
		return temp.iterator();
	}
	
	public Set<Integer> getAllChildren(int u) {
		Set<Integer> children = new HashSet<>();
		Iterator<GraphNode> i = nodes.get(u).getChildrenIterator();
		while (i.hasNext())
			children.add(i.next().getData());
		return children;
	}
	
	public DirectedGraph getReverse(){
		DirectedGraph rev = new DirectedGraph();
		for (int t : nodes.keySet())
			rev.addNode(t);
		for (int parent : nodes.keySet()) {
			Iterator<GraphNode> i = nodes.get(parent).getChildrenIterator();
			while (i.hasNext()) {
				int child = i.next().getData();
				rev.connectFromTo(child, parent);
			}
		}
		return rev;
	}
	
	/* ----------------
	 * GRAPH ALGORITHMS
	 * ----------------
	 */
	
	/* ------------------- TOPOLOGICAL SORT -------------------*/
	public ArrayList<Integer> topologicalSort(){
		DirectedGraph copy = new DirectedGraph(this);
		ArrayList<Integer> ans = new ArrayList<>();
		LinkedList<Integer> zeroDeg = new LinkedList<>();
		for (int t : copy.nodes.keySet()) {
			if(copy.nodes.get(t).getInDegree() == 0)
				zeroDeg.add(t);
		}
		while (!zeroDeg.isEmpty()) {
			int selected = zeroDeg.removeFirst();
			ans.add(selected);
			copy.removeNode(selected);
			Iterator<Integer> i = iterateChildren(selected);
			while(i.hasNext()) {
				int child = i.next();
				if (copy.getInDegree(child) == 0)
					zeroDeg.add(child);
			}
		}
		if (copy.nodes.isEmpty())
			return ans;
		else 
			return null;
	}
	
	/* ------------------- KOSARAJU-SHARIR -------------------*/
	public ArrayList<LinkedList<Integer>> getStronglyConnectedComponents(){
		//Make an array for every SOC:
		ArrayList<LinkedList<Integer>> ans = new ArrayList<>();
		
		//Compute G-rev:
		DirectedGraph gRev = getReverse();
		
		//The stack will be used for the nodes of gRev, in order of their time of leaving
		Stack<Integer> s = new Stack<>();
		
		//Init a data structure to help the traversal
		HashMap<Integer, Boolean> visited = new HashMap<>();
		for (int t : nodes.keySet())
			visited.put(t, false);
		
		//Traverse the graph recursively (and begin again when stuck) and put a vertex in the stack once we "leave" it
		for (int u : gRev.nodes.keySet()) {
			if (Boolean.FALSE.equals(visited.get(u)))
				gRev.directedDFS1(u, visited, s);
		}
		
		//Init visited again because we traverse another graph:
		for (int t : nodes.keySet())
			visited.put(t, false);
		
		while(!s.isEmpty()) {
			int v = s.pop();
			if (Boolean.FALSE.equals(visited.get(v))){
				LinkedList<Integer> soc = new LinkedList<>();
				//And everyone reachable from v in the original graph, including v
				directedDFS2(v, visited, soc);	
				//Add soc to the general answer
				ans.add(soc);
			}	
		}
		return ans;
	}	
	
	/* -------
	 * UTILITY
	 * -------
	 */
	
	protected void checkNode(int u) {
		if (!(isNode(u)))
			throw new GraphException("Can't find node " + u);		
	}
	
	protected void checkNodes(int u, int v) {
		checkNode(u);
		checkNode(v);
	}
		
	private void directedDFS1(int current, HashMap<Integer, Boolean> visited, Stack<Integer> s) {
		visited.put(current, true);
		Iterator<GraphNode> i = nodes.get(current).getChildrenIterator();
		while (i.hasNext()) {
			int next = i.next().getData();
			if (Boolean.FALSE.equals(visited.get(next)))
				directedDFS1(next, visited, s);
		}
		s.push(current);
	}
	
	private void directedDFS2(int current, HashMap<Integer, Boolean> visited, LinkedList<Integer> soc) {
		visited.put(current, true);
		Iterator<GraphNode> i = nodes.get(current).getChildrenIterator();
		while (i.hasNext()) {
			int next = i.next().getData();
			if (Boolean.FALSE.equals(visited.get(next)))
				directedDFS2(next, visited, soc);
		}
		soc.add(current);
	}
	
	private boolean canReachFromTo(int current, Integer stop, HashMap<Integer, Boolean> visited) {
		System.out.println();
		
		if (current == stop)
			return true;
		else {
			visited.put(current, true);
			Iterator<GraphNode> i = nodes.get(current).getChildrenIterator();
			while (i.hasNext()) {
				int next = i.next().getData();
				if (Boolean.FALSE.equals(visited.get(next)))
					return canReachFromTo(next, stop, visited);
			}
		}
		return false;
	}
	
	protected Integer getRandNode() {
		Random rand = new Random();
		int winner = rand.nextInt(nodes.size());
		int i = 0;
		for (int t : nodes.keySet()){
			if (i == winner)
				return t;
			i++;
		}
		return null; 
	}
}
