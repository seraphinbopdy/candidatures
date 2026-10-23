package com.portfolio.candidatures.candidature;

/**
 * Les chiffres affichés en haut de la liste.
 *
 * @param tauxReponse pourcentage de candidatures qui ont reçu une réponse (entretien, offre ou refus)
 */
public record Statistiques(long total, long envoyees, long entretiens, long offres, long refusees, int tauxReponse) {
}
