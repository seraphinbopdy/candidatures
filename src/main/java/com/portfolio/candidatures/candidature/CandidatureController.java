package com.portfolio.candidatures.candidature;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Les pages web. Chaque méthode renvoie le nom d'un template Thymeleaf
 * (src/main/resources/templates/...) ou une redirection.
 */
@Controller
@RequestMapping("/candidatures")
public class CandidatureController {

    private static final String VUE_LISTE = "candidatures/liste";
    private static final String VUE_FORMULAIRE = "candidatures/formulaire";
    private static final String REDIRECTION_LISTE = "redirect:/candidatures";

    private final CandidatureService service;

    public CandidatureController(CandidatureService service) {
        this.service = service;
    }

    /** Disponible dans toutes les pages : la liste des statuts pour les filtres et les menus. */
    @ModelAttribute("statuts")
    public StatutCandidature[] statuts() {
        return StatutCandidature.values();
    }

    @GetMapping
    public String lister(@RequestParam(required = false) StatutCandidature statut, Model model) {
        model.addAttribute("candidatures", service.lister(statut));
        model.addAttribute("stats", service.statistiques());
        model.addAttribute("statutFiltre", statut);
        return VUE_LISTE;
    }

    @GetMapping("/nouvelle")
    public String nouvelle(Model model) {
        model.addAttribute("candidature", Candidature.nouvelle());
        return VUE_FORMULAIRE;
    }

    @PostMapping
    public String creer(@Valid @ModelAttribute("candidature") Candidature candidature,
                        BindingResult erreurs,
                        RedirectAttributes redirection) {
        if (erreurs.hasErrors()) {
            candidature.setId(null);
            return VUE_FORMULAIRE;
        }
        service.creer(candidature);
        redirection.addFlashAttribute("message", "Candidature chez " + candidature.getEntreprise() + " ajoutée.");
        return REDIRECTION_LISTE;
    }

    @GetMapping("/{id}/modifier")
    public String formulaireModification(@PathVariable Long id, Model model) {
        model.addAttribute("candidature", service.trouver(id));
        return VUE_FORMULAIRE;
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Long id,
                           @Valid @ModelAttribute("candidature") Candidature candidature,
                           BindingResult erreurs,
                           RedirectAttributes redirection) {
        if (erreurs.hasErrors()) {
            candidature.setId(id);
            return VUE_FORMULAIRE;
        }
        service.modifier(id, candidature);
        redirection.addFlashAttribute("message", "Candidature chez " + candidature.getEntreprise() + " modifiée.");
        return REDIRECTION_LISTE;
    }

    @PostMapping("/{id}/statut")
    public String changerStatut(@PathVariable Long id, @RequestParam StatutCandidature statut) {
        service.changerStatut(id, statut);
        return REDIRECTION_LISTE;
    }

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes redirection) {
        service.supprimer(id);
        redirection.addFlashAttribute("message", "Candidature supprimée.");
        return REDIRECTION_LISTE;
    }
}
