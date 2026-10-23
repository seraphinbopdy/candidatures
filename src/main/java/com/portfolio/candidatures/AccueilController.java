package com.portfolio.candidatures;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** La page d'accueil renvoie directement vers la liste des candidatures. */
@Controller
public class AccueilController {

    @GetMapping("/")
    public String accueil() {
        return "redirect:/candidatures";
    }
}
