package com.portfolio.candidatures.candidature;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Accès à la base. Spring Data génère les requêtes SQL à partir du nom des méthodes.
 */
public interface CandidatureRepository extends JpaRepository<Candidature, Long> {

    List<Candidature> findAllByOrderByDateCandidatureDescIdDesc();

    List<Candidature> findByStatutOrderByDateCandidatureDescIdDesc(StatutCandidature statut);

    long countByStatut(StatutCandidature statut);
}
