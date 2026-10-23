-- Première version du schéma. Flyway exécute ce script une seule fois
-- et le note dans la table flyway_schema_history.
-- Pour toute évolution future : créer V2__..., ne jamais modifier ce fichier.

CREATE TABLE candidature (
    id               BIGSERIAL     PRIMARY KEY,
    entreprise       VARCHAR(100)  NOT NULL,
    poste            VARCHAR(100)  NOT NULL,
    date_candidature DATE          NOT NULL,
    statut           VARCHAR(20)   NOT NULL,
    lien_offre       VARCHAR(500),
    notes            VARCHAR(2000),
    CONSTRAINT ck_candidature_statut CHECK (statut IN ('ENVOYEE', 'ENTRETIEN', 'OFFRE', 'REFUSEE'))
);

CREATE INDEX idx_candidature_statut ON candidature (statut);
