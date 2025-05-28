package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Collections;

import org.insa.graphs.algorithm.AbstractInputData.Mode;
import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.algorithm.utils.ElementNotFoundException;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;
import org.insa.graphs.model.Point;

public class AStarAlgorithm extends DijkstraAlgorithm {

    public AStarAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
    protected ShortestPathSolution doRun() {

        final ShortestPathData data = getInputData();
        final Graph graph = data.getGraph();
        final int nbNodes = graph.size();
        ShortestPathSolution solution = null;

        // Tableau des labels (LabelStar) et tas binaire
        LabelStar[] labels = new LabelStar[nbNodes];
        BinaryHeap<Label> tas = new BinaryHeap<>();

        Node origine = data.getOrigin();
        labels[origine.getId()] = new LabelStar(origine, false, 0, null, 0); // coût
        // réel =
        // 0,
        // heuristique
        // = 0
        tas.insert(labels[origine.getId()]);

        notifyOriginProcessed(origine);

        Node destination = data.getDestination();

        while (!tas.isEmpty()) {
            LabelStar labelCourant = (LabelStar) tas.deleteMin(); // supprime sommet
            // avec coût total
            // minimal (g + h)
            int idCourant = labelCourant.getSommet().getId();
            labelCourant.setMarked(true);
            Node nodeCourant = graph.get(idCourant);
            notifyNodeMarked(nodeCourant);

            if (nodeCourant.equals(destination)) {
                break; // destination atteinte
            }

            for (Arc arc : nodeCourant.getSuccessors()) {
                Node succ = arc.getDestination();
                int idSucc = succ.getId();

                if (!data.isAllowed(arc))
                    continue;


                // Coût réel du chemin passant par labelCourant + arc
                int nouveauCoutReel = labelCourant.getCost() + (int) data.getCost(arc);


                Mode mode = data.getMode(); // Shortest/Fastest


                // Calcul de l'heuristique pour successeur (distance euclidienne à
                // destination)
                float distanceRestante =
                        (float) Point.distance(succ.getPoint(), destination.getPoint());

                float heuristique;

                if (mode == Mode.TIME) {
                    // Fastest path : diviser par vitesse max
                    float vitesseMax = data.getMaximumSpeed();
                    heuristique = (float) (distanceRestante / vitesseMax);
                }
                else {
                    // Shortest path : heuristique = distance
                    heuristique = (float) distanceRestante;
                }


                if (labels[idSucc] == null) {
                    labels[idSucc] = new LabelStar(succ, false, Integer.MAX_VALUE, null,
                            heuristique);
                }

                LabelStar labelSucc = labels[idSucc];
                // Mise à jour si meilleur coût réel trouvé et non marqué
                if (!labelSucc.isMarked() && nouveauCoutReel < labelSucc.getCost()) {
                    labelSucc.setCost(nouveauCoutReel);
                    labelSucc.setPere(arc);
                    // Met à jour l'heuristique (au cas où ça change, mais ici c'est
                    // constant)
                    // labels[idSucc].estimatedCost = heuristique; -> impossible car
                    // final, donc on set à la création

                    try {
                        tas.remove(labelSucc);
                    }
                    catch (ElementNotFoundException e) {
                        // Pas dans le tas, pas de problème
                    }
                    tas.insert(labelSucc);
                    notifyNodeReached(succ);
                }
            }
        }

        // Reconstruction du chemin si destination marquée
        LabelStar labelDest = labels[destination.getId()];
        if (labelDest == null || !labelDest.isMarked()) {
            solution = new ShortestPathSolution(data, Status.INFEASIBLE);
        }
        else {
            notifyDestinationReached(destination);

            ArrayList<Arc> arcs = new ArrayList<>();
            Arc arc = labelDest.getPere();
            while (arc != null) {
                arcs.add(arc);
                arc = labels[arc.getOrigin().getId()].getPere();
            }
            Collections.reverse(arcs);

            Path path = new Path(graph, arcs);
            solution = new ShortestPathSolution(data, Status.OPTIMAL, path);
        }

        return solution;
    }
}
