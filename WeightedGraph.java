import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

public class WeightedGraph extends DirectedGraph {
		
	protected HashMap<Tuple<Integer,Integer>, Edge> weights;
	private final int INFINITY = (Integer.MAX_VALUE)/10;
	private final int SOURCE = Integer.MIN_VALUE;
	private final int MINUS_INFINITY = (Integer.MIN_VALUE)/10;
	
	
	
	public WeightedGraph(){
		weights = new HashMap<>();
	}
	//Constructs a weighted graph from a directed graph, with edges equally set to w
	public WeightedGraph(DirectedGraph g, int w) {
		super(g);
		Iterator<Edge> i = this.iterateE();
		while (i.hasNext()) {
			Edge e = i.next();
			e.setWeight(w);
			weights.put(e.getUV(), e);		
		}
	}
	//Constructs a weighted graph from a directed graph, with edges equally set to 0
	public WeightedGraph(DirectedGraph g) {
		this(g, 0);
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
			ans.append(e);
			}
		ans.setLength(ans.length()-1);
		return ans.toString();
	}
	
	/* --------------
	 * EDIT THE GRAPH
	 * --------------
	 */
	
	public void connectFromTo(int from, int to, int w) {
		if (!isEdgeFromTo(from, to)) {
			Edge e = new Edge(from, to, w);
			nodes.get(from).addChild(nodes.get(to));
			weights.put(new Tuple<>(from, to), e);
		}
		else
			throw new GraphException("Edge from " + from + " to " + to + " already exists");
	}
	
	@Override
	public void connectFromTo(int from, int to) {
		connectFromTo(from, to, 0);
	}
	
	@Override
	public void disconnectFromTo(int from, int to) {
		checkEdge(from, to);
		nodes.get(from).removeChild(nodes.get(to));
		weights.remove(new Tuple<>(from, to));
	}
	
	public void setWeight(int from, int to, int w) {
		checkEdge(from, to);
		weights.get(new Tuple(from, to)).setWeight(w);
	}
	
	/* ----------------
	 * GRAPH ALGORITHMS
	 * ----------------
	 */
	
	/* ------------------- DIJKSTRA'S -------------------*/
	public HashMap<Integer, Integer> Dijkstras(int source, Integer stop, HashMap<Integer, Integer> pred){
		
		//Init the return map
		HashMap<Integer, Integer> distances = new HashMap<>();
		
		//Init the received map to store predecessors
		for (int t : nodes.keySet()) {
			pred.put(t, null);
		}
		
		//Init a map to store estimated distances from 
		for (int t : nodes.keySet()) {
			distances.put(t, INFINITY);
		}
		
		//The distance from the source is 0
		distances.put(source, 0);
		
		//Put all nodes in a priority queue with a custom comparator that looks at their currently-known distance from s
		// (The more efficient Dijkstra's is implemented with a Fibonacci heap, while this one uses Java's pq implemented with a min-heap)
		PriorityQueue<Integer> S = new PriorityQueue<Integer>(
				(s1, s2) -> Integer.compare(distances.get(s1), distances.get(s2))
		);
		
		for(int t: distances.keySet()) {
			S.add(t);
		}
		
		while (!S.isEmpty()) {
			//Greedily take the node closest to the source
			int selected = S.poll();
			//Sometimes we do not need ALL the distances:
			if (selected != stop) {
				//We are now sure of the distance from the source to the selected node
				//See if we found better paths to its children
				Iterator<Integer> i = iterateChildren(selected);
				while (i.hasNext()) {
					int child = i.next();
					int discoveredDistance = distances.get(selected) + weightEdgeFromTo(selected, child);
					if (discoveredDistance < distances.get(child)) {
						pred.put(child, selected);
						distances.put(child, discoveredDistance);
						//Make sure that child changes position in the priority queue if needed
						S.remove(child);
						S.add(child);
					}
				}	
			}
		}
		return distances;
	}
	
	/* ------------------- BELLMAN FORD -------------------*/
	public HashMap<Integer, Integer> bellmanFord(int source, HashMap<Integer, Integer> pred){
			
			//Init the return map
			HashMap<Integer, Integer> distances = new HashMap<>();
			
			//Init the received map to store predecessors
			for (int t : nodes.keySet()) {
				pred.put(t, null);
			}
			
			//Init a map to store estimated distances from 
			for (int t : nodes.keySet()) {
				distances.put(t, INFINITY);
			}
			
			//The distance from the source is 0
			distances.put(source, 0);
			
			int V = getNodesNum();
			
			for (int i = 0; i < V-1; i++) {
				//Relax all edges
				Iterator<Edge> j = iterateE();
				while (j.hasNext()) {
					Edge e = j.next();
					int from = e.getFrom();
					int to = e.getTo();
					if (distances.get(from) + e.getWeight() < distances.get(to)){
						distances.put(to, (distances.get(from) + e.getWeight()));
						pred.put(to, from);
					}
				}
			}
			//After |V|-1 iteration, any distance that keeps updating is indicative of a negative cycle:
			Iterator<Edge> j = iterateE();
			while (j.hasNext()) {
				Edge e = j.next();
				int from = e.getFrom();
				int to = e.getTo();
				if (distances.get(from) + e.getWeight() < distances.get(to)){
					distances.put(to, MINUS_INFINITY);
				}
			}
			return distances;
	}
	
	/* ------------------- Johnson's -------------------*/
	public HashMap<Tuple<Integer,Integer>, Integer> johnsons(){
		
		//Create a copy of the graph
		WeightedGraph copy = new WeightedGraph(this);
		
		//Add a source node
		int s = SOURCE;
		
		if (isNode(SOURCE))
			s = searchAlternative(SOURCE);
		
		//Connect the source to all nodes with weight 0
		Iterator<Integer> i = copy.iterateV();
		while (i.hasNext()) {
			copy.connectFromTo(s, i.next());
		}
		
		//Run Bellman-Ford from the source to determine the weight to add to each edge
		HashMap<Integer, Integer> pred = new HashMap<>();
		HashMap<Integer, Integer> distances = copy.bellmanFord(s, pred);
		
		//Check for negative cycles:
		for (Integer key : distances.keySet()) {
			if (distances.get(key) == MINUS_INFINITY)
				throw new GraphException("Graph has negative cycle, can't answer query");
		}
		
		//Add to each edge (u,v) the value: distance s->u - distance s->v
		Iterator<Edge> iterEdge = copy.iterateE(); //iterate the original edges while changing the copied ones
		while (iterEdge.hasNext()) {
			Edge e = iterEdge.next();
			int u = e.getFrom();
			int v = e.getTo();
			int su = distances.get(u);
			int sv = distances.get(v);
			e.setWeight(e.getWeight() + su - sv);
		}
		
		//Let go of the source
		copy.removeNode(s);
		
		//Make sure that now all the weights are positive
		if (!copy.positiveWeights())
			throw new GraphException("Johnson failed. fire dev");
		
		//Init a map for the answer
		HashMap<Tuple<Integer, Integer>, Integer> ans = new HashMap<>();
				
		//Run dijkstras from every vertex to every vertex and store shortest distance
		pred.clear();
		Iterator<Integer> iterVer = copy.iterateV();
		while (iterVer.hasNext()) {
			int u = iterVer.next();
			HashMap<Integer, Integer> localDist = copy.Dijkstras(u, null, pred);
			for (int v : localDist.keySet())
				ans.put(new Tuple(u, v), localDist.get(v));
		}
		return ans;
	}
	
	/*--------
	 * QUERIES
	 *--------
	 */
	
	public boolean uniqueWeights() {
		ArrayList<Edge> ar = getSortedEdges();
		for (int i=1; i < ar.size(); i++) {
			if (ar.get(i).getWeight() == ar.get(i-1).getWeight())
				return false;
		}
		return true;	
	}
	
	public boolean positiveWeights() {
		for (Edge e : weights.values()) {
			if (e.getWeight() < 0)
				return false;
		}
		return true;
	}
	
	public boolean hasNegativeCycle() {
		HashMap<Integer, Integer> pred = new HashMap<>();
		int source = getRandNode();
		HashMap<Integer, Integer> distances = bellmanFord(source, pred);
		for (Integer key : distances.keySet()) {
			if (distances.get(key) == MINUS_INFINITY)
				return true;
		}
		return false;
	}
	
	public int weightEdgeFromTo(int from, int to) {
		checkNodes(from,to);
		return (weights.get(new Tuple(from, to))).getWeight();
	}
	
	public int WeightOfShortestPathFromTo(int from, int to) {
		HashMap<Integer, Integer> pred = new HashMap<>();
		HashMap<Integer, Integer> pathWeight;
		if (positiveWeights())
			pathWeight = Dijkstras(from, to, pred);
		else
			pathWeight = bellmanFord(from, pred);
		return pathWeight.get(to);
	}
	
	//checks if every edge has an opposite edge with the same weight
	@Override
	public boolean isBiDirectional() {
		Iterator<Edge> i = iterateE();
		while(i.hasNext()) {
			Edge e = i.next();
			if (!(isEdgeFromTo(e.getTo(), e.getFrom())))
				return false;
			else if (weightEdgeFromTo(e.getTo(), e.getFrom()) != e.getWeight())
				return false;
		}
		return true;
	}
	
	/*public HashMap<Integer, Integer> WeightOfAllShortestPaths() 
		if (positiveWeights())
			floydWarshall();
		else
			johnsons();
		return pathWeight;
	}*/
	
	/* -------
	 * GETTERS
	 * -------
	 */
	
	public Set<Integer> getNodes() {
		return nodes.keySet();
	}
	
	@Override
	public Set<Edge> getEdges(){
		Set<Edge> edges = new HashSet<>();
		for (Edge e : weights.values()) 
			edges.add(new Edge(e.getFrom(), e.getTo(), e.getWeight()));
		return edges;
	}
	
	public Iterator<Integer> iterateV(){
		return nodes.keySet().iterator();
	}
	
	@Override
	public Iterator<Edge> iterateE(){
		return weights.values().iterator();
	}
	
	public Edge getEdegeFromTo(int from, int to) {
		checkEdge(from, to);
		return new Edge(weights.get(new Tuple(from, to)));
	}
	
	public Edge getAnyEdge() {
		Iterator<Edge> i = iterateE();
		return i.next();
	}
	
	/* -------
	 * UTILITY
	 * -------
	 */
	
	protected void checkEdge(int u, int v) {
		checkNodes(u, v);
		if (!(weights.containsKey(new Tuple(u, v))))
			throw new GraphException("Graph does not contain edge from " + u + " to " + v);
	}
	
	protected ArrayList<Edge> getSortedEdges() {
		ArrayList<Edge> ans = new ArrayList<>(weights.values());
		Collections.sort(ans, Comparator.comparingInt(Edge :: getWeight));
		return ans;
	}
	

	protected boolean doesCloseCircle(Edge e) {
		return canReachFromTo(e.getTo(), e.getFrom());
	}
	
	protected void addEdge(Edge e) {
		addNode(e.getFrom());
		addNode(e.getTo());
		connectFromTo(e.getFrom(), e.getTo(), e.getWeight());
	}
}