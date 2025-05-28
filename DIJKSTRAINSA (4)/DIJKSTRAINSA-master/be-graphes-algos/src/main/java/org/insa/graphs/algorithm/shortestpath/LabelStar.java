package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;

public class LabelStar extends Label {

    // Coût estimé restant (heuristique)
    private final float estimatedCost;

    public LabelStar(Node sommet, boolean marque, int cout_realise, Arc pere,
            float estimatedCost) {
        super(sommet, marque, cout_realise, pere);
        this.estimatedCost = estimatedCost;
    }

    /**
     * Retourne le coût total : coût réel + estimation
     */
    @Override
    public int getTotalCost() {
        return getCost() + Math.round(estimatedCost);
    }

    public float getEstimatedCost() {
        return this.estimatedCost;
    }
}
