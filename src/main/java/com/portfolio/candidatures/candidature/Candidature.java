package com.portfolio.candidatures.candidature;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Une candidature envoyée à une entreprise.
 * La table est créée par Flyway (src/main/resources/db/migration).
 */
@Entity
@Table(name = "candidature")
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'entreprise est obligatoire")
    @Size(max = 100, message = "100 caractères maximum")
    @Column(nullable = false, length = 100)
    private String entreprise;

    @NotBlank(message = "Le poste est obligatoire")
    @Size(max = 100, message = "100 caractères maximum")
    @Column(nullable = false, length = 100)
    private String poste;

    @NotNull(message = "La date est obligatoire")
    @PastOrPresent(message = "La date ne peut pas être dans le futur")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "date_candidature", nullable = false)
    private LocalDate dateCandidature;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutCandidature statut;

    @URL(message = "Le lien doit être une adresse web valide (https://...)")
    @Size(max = 500, message = "500 caractères maximum")
    @Column(name = "lien_offre", length = 500)
    private String lienOffre;

    @Size(max = 2000, message = "2000 caractères maximum")
    @Column(length = 2000)
    private String notes;

    public Candidature() {
    }

    /** Une candidature vierge pour le formulaire : datée d'aujourd'hui, statut « Envoyée ». */
    public static Candidature nouvelle() {
        Candidature candidature = new Candidature();
        candidature.setDateCandidature(LocalDate.now());
        candidature.setStatut(StatutCandidature.ENVOYEE);
        return candidature;
    }

    /** Recopie les champs modifiables depuis une autre candidature (utilisé lors d'une modification). */
    public void mettreAJourDepuis(Candidature autre) {
        this.entreprise = autre.entreprise;
        this.poste = autre.poste;
        this.dateCandidature = autre.dateCandidature;
        this.statut = autre.statut;
        this.lienOffre = autre.lienOffre;
        this.notes = autre.notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public LocalDate getDateCandidature() {
        return dateCandidature;
    }

    public void setDateCandidature(LocalDate dateCandidature) {
        this.dateCandidature = dateCandidature;
    }

    public StatutCandidature getStatut() {
        return statut;
    }

    public void setStatut(StatutCandidature statut) {
        this.statut = statut;
    }

    public String getLienOffre() {
        return lienOffre;
    }

    public void setLienOffre(String lienOffre) {
        this.lienOffre = lienOffre;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
