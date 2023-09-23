import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WeightedGraphTests {
	
	WeightedGraph g;
	
	@BeforeEach
	void setup() {
		g = new WeightedGraph();
		for (int i = 0; i < 5; i++) {
			g.addNode(i);
		}
		//Graph taken from picture here: https://www.techiedelight.com/wp-content/uploads/weighted-edges.png
		g.connectFromTo(0, 4, 1);
		g.connectFromTo(0, 1, 3);
		g.connectFromTo(1, 2, 1);
		g.connectFromTo(1, 3, 3);
		g.connectFromTo(1, 4, 1);
		g.connectFromTo(4, 2, 2);
		g.connectFromTo(4, 3, 1);
		
	}
	
	@Test
	void findEdges() {
		assertTrue(g.isEdgeFromTo(0, 4));
		assertTrue(g.isEdgeFromTo(0, 1));
		assertTrue(g.isEdgeFromTo(1, 2));
		assertTrue(g.isEdgeFromTo(1, 3));
		assertTrue(g.isEdgeFromTo(1, 4));
		assertTrue(g.isEdgeFromTo(4, 2));
		assertTrue(g.isEdgeFromTo(4, 3));
		
		assertFalse(g.isEdgeFromTo(0, 3));
		assertFalse(g.isEdgeFromTo(4, 0));
		assertFalse(g.isEdgeFromTo(2, 1));
	}
	
	@Test
	void reviewWeights() {
		assertEquals(1, g.weightEdgeFromTo(0, 4));
		assertEquals(3, g.weightEdgeFromTo(0, 1));
		assertEquals(1, g.weightEdgeFromTo(1, 2));
		assertEquals(3, g.weightEdgeFromTo(1, 3));
		assertEquals(1, g.weightEdgeFromTo(1, 4));
		assertEquals(2, g.weightEdgeFromTo(4, 2));
		assertEquals(1, g.weightEdgeFromTo(4, 3));
	}
	
	@Test
	@DisplayName ("remove and restore node")
	void removeAndRestoreNode() {
		
		int inDegOf2 = g.getInDegree(2);
		int inDegOf3 = g.getInDegree(3);
		int outDegOf0 = g.getOutDegree(0);
		int outDegOf1 = g.getOutDegree(1);
		
		g.removeNode(4);
		
		assertEquals(inDegOf2-1, g.getInDegree(2));
		assertEquals(inDegOf3-1, g.getInDegree(3));
		assertEquals(outDegOf0-1, g.getOutDegree(0));
		assertEquals(outDegOf1-1, g.getOutDegree(1));
		
		g.addNode(4);
		
		g.connectFromTo(0, 4, 1);
		g.connectFromTo(1, 4, 1);
		g.connectFromTo(4, 2, 2);
		g.connectFromTo(4, 3, 1);
		
		assertEquals(inDegOf2, g.getInDegree(2));
		assertEquals(inDegOf3, g.getInDegree(3));
		assertEquals(outDegOf0, g.getOutDegree(0));
		assertEquals(outDegOf1, g.getOutDegree(1));
	}
	
	@Test
	void dagRecognized() {
		assertTrue(g.isDAG());
		
		g.connectFromTo(3, 1);
		
		assertFalse(g.isDAG());
	}
		
	@Test
	void reachability() {
		assertTrue(g.canReachFromTo(0, 3), "0 to 3");
		assertTrue(g.canReachFromTo(0, 3), "0 to 4");
		assertTrue(g.canReachFromTo(0, 3), "4 to 3");
		assertTrue(g.canReachFromTo(0, 3), "0 to 2");
		assertFalse(g.canReachFromTo(1, 0), "1 to 0");
		assertFalse(g.canReachFromTo(3, 2), "3 to 2");
	}
	
	@Test
	void checkDijkstras() {
		assertEquals(3, g.WeightOfShortestPathFromTo(0,2));
		assertEquals(2, g.WeightOfShortestPathFromTo(0,3));
	}
	
	@Test
	void checkWeights() {
		assertFalse(g.uniqueWeights(), "unique");
		assertTrue(g.positiveWeights(),"positive");
	}
	
	@Test
	void checkBellmanFord() {
		g.setWeight(1 ,3 ,-2);
		assertEquals(1, g.WeightOfShortestPathFromTo(0,3));
		
		g.setWeight(1 ,3 ,-1);
		assertEquals(2, g.WeightOfShortestPathFromTo(0,3));
	}
	
	@Test
	@DisplayName ("detect negative cycle")
	void detectNegCycle() {
		assertFalse(g.hasNegativeCycle());
		g.addNode(5);
		g.connectFromTo(3, 5, -1);
		g.connectFromTo(5, 4, -1);
		assertTrue(g.hasNegativeCycle());
		g.setWeight(5, 4, 1);
		assertFalse(g.hasNegativeCycle());
	}
	

	
}
