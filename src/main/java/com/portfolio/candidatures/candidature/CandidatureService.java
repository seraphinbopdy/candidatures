package com.portfolio.candidatures.candidature;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * La logique métier : le contrôleur s'occupe du web, le service des règles, le repository de la base.
 */
@Service
@Transactional
public class CandidatureService {

    private final CandidatureRepository repository;

    public CandidatureService(CandidatureRepository repository) {
        this.repository = repository;
    }

    /** Toutes les candidatures, ou seulement celles d'un statut, les plus récentes d'abord. */
    @Transactional(readOnly = true)
    public List<Candidature> lister(StatutCandidature statut) {
        if (statut == null) {
            return repository.findAllByOrderByDateCandidatureDescIdDesc();
        }
        return repository.findByStatutOrderByDateCandidatureDescIdDesc(statut);
    }

    @Transactional(readOnly = true)
    public Candidature trouver(Long id) {
        return repository.findById(id).orElseThrow(() -> new CandidatureIntrouvableException(id));
    }

    public Candidature creer(Candidature candidature) {
        candidature.setId(null); // une création ne doit jamais écraser une candidature existante
        return repository.save(candidature);
    }

    public Candidature modifier(Long id, Candidature valeurs) {
        Candidature existante = trouver(id);
        existante.mettreAJourDepuis(valeurs);
        return existante; // sauvegardée automatiquement à la fin de la transaction
    }

    public void changerStatut(Long id, StatutCandidature statut) {
        trouver(id).setStatut(statut);
    }

    public void supprimer(Long id) {
        repository.delete(trouver(id));
    }

    @Transactional(readOnly = true)
    public Statistiques statistiques() {
        long total = repository.count();
        long envoyees = repository.countByStatut(StatutCandidature.ENVOYEE);
        long entretiens = repository.countByStatut(StatutCandidature.ENTRETIEN);
        long offres = repository.countByStatut(StatutCandidature.OFFRE);
        long refusees = repository.countByStatut(StatutCandidature.REFUSEE);
        int tauxReponse = total == 0 ? 0 : Math.round((total - envoyees) * 100f / total);
        return new Statistiques(total, envoyees, entretiens, offres, refusees, tauxReponse);
    }
}
