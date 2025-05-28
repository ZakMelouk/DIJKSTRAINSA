package org.insa.graphs.algorithm.utils;

import java.util.ArrayList;
import java.util.Arrays;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.ArcInspector;
import org.insa.graphs.algorithm.ArcInspectorFactory;
import org.insa.graphs.algorithm.shortestpath.DijkstraBatterieAlgorithm;
import org.insa.graphs.algorithm.shortestpath.ShortestPathData;
import org.insa.graphs.algorithm.shortestpath.ShortestPathSolution;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.RoadInformation;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DijkstraBatterieAlgorithmTest {

    private Graph createGraph(Node[] nodes) {
        return new Graph("test-id", "test-map", Arrays.asList(nodes), null);
    }

    private void link(Node from, Node to, float length, RoadInformation.RoadType type) {
        RoadInformation info = new RoadInformation(type, null, true, 1, null);
        Node.linkNodes(from, to, length, info, new ArrayList<>());
    }

    private ShortestPathSolution runDijkstraBatterie(Graph graph, Node origin,
            Node destination) {
        ArcInspector inspector = ArcInspectorFactory.getAllFilters().get(0);
        ShortestPathData data =
                new ShortestPathData(graph, origin, destination, inspector);
        DijkstraBatterieAlgorithm algo = new DijkstraBatterieAlgorithm(data);
        return algo.run();
    }

    @Test
    public void testPathWithRechargeNeeded() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        Node n3 = new Node(3, null);

        link(n0, n1, 150000, RoadInformation.RoadType.MOTORWAY);
        link(n0, n2, 210000, RoadInformation.RoadType.RESIDENTIAL);
        link(n1, n3, 50000, RoadInformation.RoadType.MOTORWAY);
        link(n2, n3, 10000, RoadInformation.RoadType.RESIDENTIAL);

        Graph graph = createGraph(new Node[] { n0, n1, n2, n3 });

        ShortestPathSolution sol = runDijkstraBatterie(graph, n0, n3);

        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(2, sol.getPath().getArcs().size());
        assertEquals(n0, sol.getPath().getArcs().get(0).getOrigin());
        assertEquals(n1, sol.getPath().getArcs().get(0).getDestination());
        assertEquals(n1, sol.getPath().getArcs().get(1).getOrigin());
        assertEquals(n3, sol.getPath().getArcs().get(1).getDestination());
        assertEquals(200000.0, sol.getPath().getLength(), 1e-3);
    }

    @Test
    public void testNoPathDueToBatteryLimit() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);

        link(n0, n1, 210000, RoadInformation.RoadType.RESIDENTIAL);

        Graph graph = createGraph(new Node[] { n0, n1 });

        ShortestPathSolution sol = runDijkstraBatterie(graph, n0, n1);

        assertEquals(Status.INFEASIBLE, sol.getStatus());
    }

    @Test
    public void testSameOriginDestination() {
        Node n0 = new Node(0, null);
        Graph graph = createGraph(new Node[] { n0 });

        ShortestPathSolution sol = runDijkstraBatterie(graph, n0, n0);

        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertTrue(sol.getPath().isEmpty());
        assertEquals(0.0, sol.getPath().getLength(), 1e-3);
    }

    // --- Nouveaux tests ---

    @Test
    public void testRechargeOnMultipleStations() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        Node n3 = new Node(3, null);
        Node n4 = new Node(4, null);

        link(n0, n1, 150000, RoadInformation.RoadType.MOTORWAY); // recharge
        link(n1, n2, 150000, RoadInformation.RoadType.MOTORWAY); // recharge
        link(n2, n3, 10000, RoadInformation.RoadType.RESIDENTIAL);
        link(n3, n4, 5000, RoadInformation.RoadType.RESIDENTIAL);

        Graph graph = createGraph(new Node[] { n0, n1, n2, n3, n4 });

        ShortestPathSolution sol = runDijkstraBatterie(graph, n0, n4);

        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(4, sol.getPath().getArcs().size());
        assertEquals(315000.0, sol.getPath().getLength(), 1e-3);
    }

    @Test
    public void testCycleWithRecharge() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);

        link(n0, n1, 100000, RoadInformation.RoadType.MOTORWAY);
        link(n1, n2, 100000, RoadInformation.RoadType.RESIDENTIAL);
        link(n2, n0, 100000, RoadInformation.RoadType.MOTORWAY);

        Graph graph = createGraph(new Node[] { n0, n1, n2 });

        ShortestPathSolution sol = runDijkstraBatterie(graph, n0, n2);

        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(2, sol.getPath().getArcs().size());
        assertEquals(200000.0, sol.getPath().getLength(), 1e-3);
    }

    @Test
    public void testNoRechargeNeeded() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);

        link(n0, n1, 100000, RoadInformation.RoadType.RESIDENTIAL);

        Graph graph = createGraph(new Node[] { n0, n1 });

        ShortestPathSolution sol = runDijkstraBatterie(graph, n0, n1);

        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(1, sol.getPath().getArcs().size());
        assertEquals(100000.0, sol.getPath().getLength(), 1e-3);
    }

    @Test
    public void testDeadEndWithRecharge() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);

        link(n0, n1, 150000, RoadInformation.RoadType.MOTORWAY);
        link(n1, n2, 60000, RoadInformation.RoadType.RESIDENTIAL);

        Graph graph = createGraph(new Node[] { n0, n1, n2 });

        ShortestPathSolution sol = runDijkstraBatterie(graph, n0, n2);

        // Le chemin est possible car recharge en n1
        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(2, sol.getPath().getArcs().size());
        assertEquals(210000.0, sol.getPath().getLength(), 1e-3);
    }

    @Test
    public void testMultiplePathsSameCost() {
        Node n0 = new Node(0, null);
        Node n1 = new Node(1, null);
        Node n2 = new Node(2, null);
        Node n3 = new Node(3, null);

        link(n0, n1, 100000, RoadInformation.RoadType.MOTORWAY);
        link(n1, n3, 100000, RoadInformation.RoadType.RESIDENTIAL);
        link(n0, n2, 100000, RoadInformation.RoadType.MOTORWAY);
        link(n2, n3, 100000, RoadInformation.RoadType.RESIDENTIAL);

        Graph graph = createGraph(new Node[] { n0, n1, n2, n3 });

        ShortestPathSolution sol = runDijkstraBatterie(graph, n0, n3);

        assertEquals(Status.OPTIMAL, sol.getStatus());
        assertEquals(2, sol.getPath().getArcs().size());
        assertEquals(200000.0, sol.getPath().getLength(), 1e-3);
    }

}
