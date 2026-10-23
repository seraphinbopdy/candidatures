package com.portfolio.candidatures.candidature;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Levée quand on demande une candidature qui n'existe pas : Spring répond alors 404. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class CandidatureIntrouvableException extends RuntimeException {

    public CandidatureIntrouvableException(Long id) {
        super("Candidature introuvable : " + id);
    }
}
