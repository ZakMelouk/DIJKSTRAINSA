package org.insa.graphs.algorithm.utils;

import java.util.ArrayList;
import java.util.Arrays;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.algorithm.shortestpath.DijkstraAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.ShortestPathSolution;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.RoadInformation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DijkstraAlgorithmTest {

    private Graph createGraph(Node[] nodes) {
        return new Graph("test-id", "test-map", Arrays.asList(nodes), null);
    }

    private ShortestPathSolution runDijkstra(Graph graph, Node origin,
            Node destination) {
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(0);
        ShortestPathData data =
                new ShortestPathData(graph, origin, destination, inspector);
        DijkstraAlgorithm algo = new DijkstraAlgorithm(data);
        return algo.run();
    }

    private void link(Node from, Node to, float length) {
        RoadInformation info = new RoadInformation(
                RoadInformation.RoadType.UNCLASSIFIED, null, true, 1, null);
        Node.linkNodes(from, to, length, info, new ArrayList<>());
    }

    @Test
    public void testSimplePath() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        link(n0, n1, 10);
        link(n1, n2, 5);
        Graph graph = createGraph(new Node[] { n0, n1, n2 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n2);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(15.0, sol.getPath().getLength(), 0.001);
        assertEquals(2, sol.getPath().getArcs().size());
    }

    @Test
    public void testOptimalDetour() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        link(n0, n1, 100);
        link(n0, n2, 5);
        link(n2, n1, 5);
        Graph graph = createGraph(new Node[] { n0, n1, n2 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n1);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(10.0, sol.getPath().getLength(), 0.001);
        assertEquals(2, sol.getPath().getArcs().size());
    }

    @Test
    public void testSameOriginDestination() {
        Node n0 = new Node(0, null);
        Graph graph = createGraph(new Node[] { n0 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n0);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertTrue(sol.getPath().isEmpty());
        assertEquals(0.0, sol.getPath().getLength(), 0.001);
    }

    @Test
    public void testNoPathAvailable() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Graph graph = createGraph(new Node[] { n0, n1 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n1);
        assertEquals(Status.INFEASIBLE, sol.getStatus());
    }

    @Test
    public void testCycleInGraph() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        link(n0, n1, 2);
        link(n1, n2, 2);
        link(n2, n0, 2);
        Graph graph = createGraph(new Node[] { n0, n1, n2 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n2);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(4.0, sol.getPath().getLength(), 0.001);
    }

    @Test
    public void testLongLinearPath() {
        Node[] nodes = new Node[10];
        for (int i = 0; i < 10; i++)
            nodes[i] = new Node(i, null);
        for (int i = 0; i < 9; i++)
            link(nodes[i], nodes[i + 1], 1.0f);
        Graph graph = createGraph(nodes);
        ShortestPathSolution sol = runDijkstra(graph, nodes[0], nodes[9]);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(9.0, sol.getPath().getLength(), 0.001);
        assertEquals(9, sol.getPath().getArcs().size());
    }

    @Test
    public void testDeadEndTrap() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        Node n3 = new Node(3, null);
        link(n0, n1, 1);
        link(n1, n2, 1);
        link(n0, n3, 100);
        Graph graph = createGraph(new Node[] { n0, n1, n2, n3 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n2);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(2.0, sol.getPath().getLength(), 0.001);
    }

    @Test
    public void testEqualCostDifferentArcs() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        Node n3 = new Node(3, null);
        link(n0, n1, 5);
        link(n1, n3, 5);
        link(n0, n2, 3);
        link(n2, n3, 7);
        Graph graph = createGraph(new Node[] { n0, n1, n2, n3 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n3);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(10.0, sol.getPath().getLength(), 0.001);
        assertEquals(2, sol.getPath().getArcs().size());
    }

    @Test
    public void testVeryHighWeights() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        link(n0, n1, 1_000_000);
        link(n1, n2, 2_000_000);
        Graph graph = createGraph(new Node[] { n0, n1, n2 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n2);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(3_000_000.0, sol.getPath().getLength(), 0.001);
    }

    @Test
    public void testCycleWithForcedDetour() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        Node n3 = new Node(3, null);
        link(n0, n1, 1);
        link(n1, n2, 1);
        link(n2, n3, 1);
        link(n3, n1, 1);
        link(n0, n2, 100);
        Graph graph = createGraph(new Node[] { n0, n1, n2, n3 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n3);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(3.0, sol.getPath().getLength(), 0.001);
    }

    @Test
    public void testIsolatedMiddleNode() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        link(n0, n1, 10);
        Graph graph = createGraph(new Node[] { n0, n1, n2 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n2);
        assertEquals(Status.INFEASIBLE, sol.getStatus());
    }

    @Test
    public void testOneWayGraph() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        link(n0, n1, 5);
        Graph graph = createGraph(new Node[] { n0, n1 });
        ShortestPathSolution sol = runDijkstra(graph, n1, n0);
        assertEquals(Status.INFEASIBLE, sol.getStatus());
    }

    @Test
    public void testRedundantEdges() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        link(n0, n1, 10);
        link(n0, n1, 5);
        Graph graph = createGraph(new Node[] { n0, n1 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n1);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(5.0, sol.getPath().getLength(), 0.001);
    }

    @Test
    public void testSelfLoop() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        link(n0, n0, 100);
        link(n0, n1, 10);
        Graph graph = createGraph(new Node[] { n0, n1 });
        ShortestPathSolution sol = runDijkstra(graph, n0, n1);
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(10.0, sol.getPath().getLength(), 0.001);
    }

    @Test
    public void testDenseGraph() {
        int n = 6;
        Node[] nodes = new Node[n];
        for (int i = 0; i < n; i++)
            nodes[i] = new Node(i, null);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j)
                    link(nodes[i], nodes[j], i + j);
            }
        }
        Graph graph = createGraph(nodes);
        ShortestPathSolution sol = runDijkstra(graph, nodes[0], nodes[5]);
        assertEquals(Status.OPTIMAL, sol.getStatus());
    }
}
