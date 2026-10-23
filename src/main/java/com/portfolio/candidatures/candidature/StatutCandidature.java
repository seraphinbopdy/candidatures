package com.portfolio.candidatures.candidature;

/**
 * Les étapes possibles d'une candidature.
 * Le nom (ENVOYEE...) est stocké en base, le libellé est affiché à l'écran.
 */
public enum StatutCandidature {

    ENVOYEE("Envoyée"),
    ENTRETIEN("Entretien"),
    OFFRE("Offre"),
    REFUSEE("Refusée");

    private final String libelle;

    StatutCandidature(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
