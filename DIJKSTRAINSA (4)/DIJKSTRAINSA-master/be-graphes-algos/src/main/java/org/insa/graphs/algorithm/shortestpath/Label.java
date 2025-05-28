package org.insa.graphs.algorithm.shortestpath;

import org.insa.graphs.model.Arc;
import org.insa.graphs.model.Node;

public class Label implements Comparable<Label> {

    Node sommet;
    boolean marque;
    int cout_realise;

    Arc pere;

    Node node;

    public Label(Node sommet, boolean marque, int cout_realise, Arc pere) {
        this.sommet = sommet;
        this.marque = marque;
        this.cout_realise = cout_realise;
        this.pere = pere;
    }

    public Node getSommet() {
        return sommet;
    }

    public Node getNode() {
        return node;
    }

    public boolean isMarked() {
        return marque;
    }

    public void setMarked(boolean b) {
        marque = b;
    }

    public int getCout_realise() {
        return cout_realise;
    }

    public Arc getPere() {
        return pere;
    }

    public int getCost() {
        return cout_realise;
    }

    /*
     * Méthode générique pour calculer le coût total
     */
    public int getTotalCost() {
        return getCost(); // Par défaut, juste le coût réel (en Dijkstra)
    }

    public int compareTo(Label other) {
        return Integer.compare(this.getTotalCost(), other.getTotalCost());
    }

    public void setCost(int cost) {
        cout_realise = cost;
    }

    public void setPere(Arc pere) {
        this.pere = pere;
    }

}
