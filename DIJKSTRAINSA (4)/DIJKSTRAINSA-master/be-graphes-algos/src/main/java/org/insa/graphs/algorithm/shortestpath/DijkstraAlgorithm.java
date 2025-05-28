package org.insa.graphs.algorithm.shortestpath;

import java.util.ArrayList;
import java.util.Collections;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.algorithm.utils.ElementNotFoundException;
import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Graph;
import org.insa.graphs.model.Node;
import org.insa.graphs.model.Path;


public class DijkstraAlgorithm extends ShortestPathAlgorithm {

    public DijkstraAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
    protected ShortestPathSolution doRun() {

        final ShortestPathData data = getInputData();
        final Graph graph = data.getGraph();
        final int nbNodes = graph.size();
        ShortestPathSolution solution = null;

        Label[] labels = new Label[nbNodes];
        BinaryHeap<Label> tas = new BinaryHeap<>();

        Node origine = data.getOrigin();

        labels[origine.getId()] = new Label(origine, false, 0, null); // coût = 0
                                                                      // pour
                                                                      // l'origine
        tas.insert(labels[origine.getId()]);

        notifyOriginProcessed(origine);

        Node destination = data.getDestination();

        while (!tas.isEmpty()) {
            Label labelCourant = tas.deleteMin();// sUPPRIME LE SOMMET AVEC LA PRIORITÉ
                                                 // LA PLUS FAIBLE
            int idCourant = labelCourant.getSommet().getId(); // Recupere l'id du sommet
                                                              // courant
            labelCourant.setMarked(true); // Marque le sommet
            Node nodeCourant = graph.get(idCourant); // Recupere le sommet lié à l'id
            notifyNodeMarked(nodeCourant);

            if (nodeCourant.equals(destination))
                break;// ALgo fini

            for (Arc arc : nodeCourant.getSuccessors()) { // on parcours les arcs depuis
                                                          // le sommet qu'on traite

                if (!data.isAllowed(arc))
                    continue;

                Node succ = arc.getDestination();
                int idSucc = succ.getId();
                int nouveauCout =
                        labelCourant.getCout_realise() + (int) data.getCost(arc);

                if (labels[idSucc] == null) { // si le label n'est pas encore cree pour
                                              // le sommet destination de l'arc: on le
                                              // cree
                    labels[idSucc] = new Label(succ, false, Integer.MAX_VALUE, null);
                }

                Label labelSucc = labels[idSucc];
                int a = labelSucc.getCout_realise();
                if (!labelSucc.isMarked()
                        && nouveauCout < labelSucc.getCout_realise()) { // si le
                                                                        // successeur
                                                                        // n'est pas
                                                                        // marqué et on
                                                                        // a trv un
                                                                        // meilleur
                                                                        // cout: on le
                                                                        // met à jour
                                                                        // dans le label
                                                                        // et le tas
                    labelSucc.setCost(nouveauCout);
                    labelSucc.setPere(arc);

                    // Gérer mise à jour dans le tas
                    // on essaie d'enlever l'element, si il est pas la on receptionne
                    // l'exception et on fait rien
                    try {
                        tas.remove(labelSucc);
                    }
                    catch (ElementNotFoundException e) {
                        // L'élément n'était pas dans le tas, on peut continuer
                    }
                    tas.insert(labelSucc);
                    notifyNodeReached(succ);
                }
            }
        }

        // Construction du chemin si le nœud destination a été marqué
        Label labelDest = labels[destination.getId()];
        if (labelDest == null || !labelDest.isMarked()) {// Verification de isMArked,
                                                         // par rapport aux contraintes
                                                         // filtres, isAllowed
            Status status = Status.INFEASIBLE; // Infaisable
            solution = new ShortestPathSolution(data, status);
        }
        else {
            // reconstruction du chemin similaire a bellmanford
            // La destination a été atteinte, on reconstruit le chemin.
            notifyDestinationReached(destination);

            // Reconstruire le chemin depuis les arcs pères
            ArrayList<Arc> arcs = new ArrayList<>();
            Arc arc = labels[destination.getId()].getPere();
            while (arc != null) {
                arcs.add(arc);
                arc = labels[arc.getOrigin().getId()].getPere();
            }

            // Inverser les arcs pour avoir le chemin dans le bon sens
            Collections.reverse(arcs);

            // Créer la solution
            Path path = new Path(graph, arcs);
            solution = new ShortestPathSolution(data, Status.OPTIMAL, path);

        }

        return solution;
    }

}
