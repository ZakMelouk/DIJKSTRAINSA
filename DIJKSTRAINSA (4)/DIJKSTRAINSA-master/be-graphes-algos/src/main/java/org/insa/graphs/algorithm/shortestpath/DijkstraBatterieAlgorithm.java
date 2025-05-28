package org.insa.graphs.algorithm.shortestpath;

import java.util.*;

import org.insa.graphs.algorithm.AbstractSolution.Status;
import org.insa.graphs.algorithm.utils.BinaryHeap;
import org.insa.graphs.model.*;

/**
 * Algorithme de Dijkstra adapté à un véhicule électrique avec batterie limitée.
 * Recharge possible sur certains sommets (stations).
 */
public class DijkstraBatterieAlgorithm extends ShortestPathAlgorithm {

    // Autonomie maximale de la batterie (en mètres, ici 200km = 200000m)
    private final int autonomieMax = 200000;

    // Ensemble des identifiants des sommets où une station de recharge est disponible

    public DijkstraBatterieAlgorithm(ShortestPathData data) {
        super(data);
    }

    @Override
    protected ShortestPathSolution doRun() {
        final ShortestPathData data = getInputData();
        final Graph graph = data.getGraph();
        final Node origin = data.getOrigin();
        final Node destination = data.getDestination();

        Node origine = data.getOrigin();
        // Tas de priorité contenant les labels à explorer
        BinaryHeap<Label> tas = new BinaryHeap<>();
        // Map : pour chaque sommet, liste de labels atteints avec différentes
        // autonomies
        Map<Integer, List<LabelBatterie>> labelsParSommet = new HashMap<>();

        // Initialisation du label de départ
        labelsParSommet.put(origin.getId(), new ArrayList<>(
                List.of(new LabelBatterie(origin, false, 0, null, autonomieMax))));
        tas.insert(labelsParSommet.get(origin.getId()).get(0));
        notifyOriginProcessed(origin);

        LabelBatterie labelDestination = null;

        // Boucle principale
        while (!tas.isEmpty()) {
            // Extraire le label avec le plus petit coût
            LabelBatterie current = (LabelBatterie) tas.deleteMin();
            current.setMarked(true);
            Node u = current.getSommet();

            if (u.equals(destination)) {
                // On a atteint la destination
                labelDestination = current;
                break;
            }

            notifyNodeMarked(u);

            // Parcourir les arcs sortants
            for (Arc arc : u.getSuccessors()) {
                if (!data.isAllowed(arc))
                    continue;

                Node v = arc.getDestination();
                int cost = (int) data.getCost(arc);
                int newAutonomie = current.getAutonomieRestante() - cost;

                // Si l'arc dépasse l'autonomie restante → on ignore ce chemin
                if (newAutonomie < 0)
                    continue;

                // Si on arrive sur une station, on recharge
                if (arc.getRoadInformation()
                        .getType() == RoadInformation.RoadType.MOTORWAY) {
                    newAutonomie = autonomieMax;
                }

                // Nouveau coût accumulé
                int newCost = current.getCout_realise() + cost;

                // Création d’un nouveau label
                LabelBatterie succ =
                        new LabelBatterie(v, false, newCost, arc, newAutonomie);
                succ.setPreviousLabel(current);

                // Vérifie s'il existe déjà un label équivalent ou meilleur
                boolean meilleur = true;
                List<LabelBatterie> labelsV =
                        labelsParSommet.getOrDefault(v.getId(), new ArrayList<>());

                for (LabelBatterie existing : labelsV) {
                    if (existing.getAutonomieRestante() >= succ.getAutonomieRestante()
                            && existing.getCost() <= succ.getCost()) {
                        meilleur = false;
                        break;
                    }
                }

                if (meilleur) {
                    tas.insert(succ);
                    labelsV.add(succ);
                    labelsParSommet.put(v.getId(), labelsV);
                    notifyNodeReached(v);
                }
            }
        }

        // Si aucune solution n’a été trouvée
        if (labelDestination == null) {
            return new ShortestPathSolution(data, Status.INFEASIBLE);
        }

        notifyDestinationReached(destination);

        // Reconstruction du chemin à partir des labels chaînés
        List<Arc> arcs = new ArrayList<>();
        LabelBatterie current = labelDestination;

        while (current.getPere() != null) {
            arcs.add(current.getPere());
            current = current.getPreviousLabel();
        }

        Collections.reverse(arcs);

        // Construction de l’objet Path
        Path path = new Path(graph, arcs);
        return new ShortestPathSolution(data, Status.OPTIMAL, path);
    }
}
