import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphNodeTest {

	GraphNode<Integer> parent;
	GraphNode<Integer> child;
	
	@BeforeAll
	void setup() {
		GraphNode<Integer> parent = new GraphNode<>(1);
		GraphNode<Integer> child = new GraphNode<>(2);
	}
	
	
	@Test
	void childGetsAdded() {
		parent.addChild(child);
		assertTrue(parent.isChild(child), "is child the child of parent");
		assertEquals(1, child.getInDegree(), "in degree of child");
		assertEquals(0, child.getOutDegree(), "out degree of child");
		assertEquals(0, parent.getInDegree(), "in degree of parent");
		assertEquals(0, parent.getOutDegree(), "out degree of parent");	
	}
	
	@Test
	void childGetsDeleted() {
		parent.removeChild(child);
		assertFalse(parent.isChild(child), "is child the child of parent");
		assertEquals(0 , child.getInDegree(), "in degree of child");
		assertEquals(0, child.getOutDegree(), "out degree of child");
		assertEquals(0, parent.getInDegree(), "in degree of parent");
		assertEquals(0, parent.getOutDegree(), "out degree of parent");
	}
	
	

}
