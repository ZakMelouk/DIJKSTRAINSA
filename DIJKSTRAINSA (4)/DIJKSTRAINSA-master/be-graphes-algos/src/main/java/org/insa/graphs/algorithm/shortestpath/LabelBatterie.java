package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;

/**
 * Classe LabelBatterie étendue pour prendre en compte l'autonomie restante et le lien
 * vers le label précédent pour reconstruire le chemin complet.
 */
public class LabelBatterie extends Label {

    // Autonomie restante du véhicule électrique (en mètres)
    private int autonomieRestante;

    // Lien vers le label précédent (utile si un sommet est atteint par plusieurs
    // chemins)
    private LabelBatterie previousLabel;

    public LabelBatterie(Node sommet, boolean marque, int coutRealise, Arc pere,
            int autonomieRestante) {
        super(sommet, marque, coutRealise, pere);
        this.autonomieRestante = autonomieRestante;
        this.previousLabel = null;
    }

    public int getAutonomieRestante() {
        return autonomieRestante;
    }

    public void setAutonomieRestante(int autonomie) {
        this.autonomieRestante = autonomie;
    }

    public LabelBatterie getPreviousLabel() {
        return previousLabel;
    }

    public void setPreviousLabel(LabelBatterie previous) {
        this.previousLabel = previous;
    }
}
