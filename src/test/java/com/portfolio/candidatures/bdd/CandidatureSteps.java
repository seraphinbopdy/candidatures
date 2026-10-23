package com.portfolio.candidatures.bdd;

import com.portfolio.candidatures.candidature.Candidature;
import com.portfolio.candidatures.candidature.CandidatureRepository;
import com.portfolio.candidatures.candidature.Statistiques;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Le code derrière chaque phrase du fichier candidatures.feature.
 *
 * Les annotations @Given / @When / @Then sont en anglais, mais Cucumber les relie
 * aux phrases françaises (« Étant donné », « Quand », « Alors », « Et ») :
 * seul le texte de la phrase compte, pas le mot-clé.
 */
public class CandidatureSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CandidatureRepository repository;

    /** La réponse de la dernière requête envoyée à l'application. */
    private MvcResult dernierResultat;

    /** Chaque scénario démarre sur une base vide : les scénarios ne dépendent pas les uns des autres. */
    @Before
    public void viderLaBase() {
        repository.deleteAll();
    }

    // ---------------------------------------------------------------- Étant donné

    @Given("aucune candidature enregistrée")
    public void aucuneCandidatureEnregistree() {
        assertThat(repository.count()).isZero();
    }

    @Given("une candidature chez {string} pour le poste {string}")
    public void uneCandidature(String entreprise, String poste) throws Exception {
        envoyerCandidature(entreprise, poste, "ENVOYEE");
    }

    @Given("les candidatures suivantes :")
    public void lesCandidaturesSuivantes(DataTable tableau) throws Exception {
        for (Map<String, String> ligne : tableau.asMaps()) {
            envoyerCandidature(ligne.get("entreprise"), ligne.get("poste"), ligne.get("statut"));
        }
    }

    // ---------------------------------------------------------------- Quand

    @When("j'ajoute une candidature chez {string} pour le poste {string}")
    public void jAjouteUneCandidature(String entreprise, String poste) throws Exception {
        dernierResultat = envoyerCandidature(entreprise, poste, "ENVOYEE");
    }

    @When("j'envoie le formulaire sans nom d'entreprise")
    public void jEnvoieLeFormulaireSansEntreprise() throws Exception {
        dernierResultat = envoyerCandidature("", "Ingénieur DevOps", "ENVOYEE");
    }

    @When("je passe la candidature chez {string} au statut {string}")
    public void jeChangeLeStatut(String entreprise, String statut) throws Exception {
        Long id = trouver(entreprise).getId();
        dernierResultat = mockMvc.perform(post("/candidatures/{id}/statut", id).param("statut", statut))
                .andReturn();
    }

    @When("je modifie le poste de la candidature chez {string} en {string}")
    public void jeModifieLePoste(String entreprise, String nouveauPoste) throws Exception {
        Candidature candidature = trouver(entreprise);
        dernierResultat = mockMvc.perform(post("/candidatures/{id}", candidature.getId())
                        .param("entreprise", candidature.getEntreprise())
                        .param("poste", nouveauPoste)
                        .param("dateCandidature", candidature.getDateCandidature().toString())
                        .param("statut", candidature.getStatut().name()))
                .andReturn();
    }

    @When("je supprime la candidature chez {string}")
    public void jeSupprime(String entreprise) throws Exception {
        Long id = trouver(entreprise).getId();
        dernierResultat = mockMvc.perform(post("/candidatures/{id}/supprimer", id)).andReturn();
    }

    @When("je filtre sur le statut {string}")
    public void jeFiltre(String statut) throws Exception {
        dernierResultat = mockMvc.perform(get("/candidatures").param("statut", statut)).andReturn();
    }

    @When("j'ouvre la liste des candidatures")
    public void jOuvreLaListe() throws Exception {
        dernierResultat = mockMvc.perform(get("/candidatures")).andReturn();
    }

    @When("je consulte la candidature numéro {long}")
    public void jeConsulte(Long id) throws Exception {
        dernierResultat = mockMvc.perform(get("/candidatures/{id}/modifier", id)).andReturn();
    }

    // ---------------------------------------------------------------- Alors

    @Then("je suis redirigé vers la liste")
    public void jeSuisRedirigeVersLaListe() {
        assertThat(dernierResultat.getResponse().getStatus()).isEqualTo(302);
        assertThat(dernierResultat.getResponse().getRedirectedUrl()).isEqualTo("/candidatures");
    }

    @Then("la liste contient {int} candidature(s)")
    public void laListeContient(int nombre) {
        assertThat(repository.count()).isEqualTo(nombre);
    }

    @Then("la candidature chez {string} a le statut {string}")
    public void aLeStatut(String entreprise, String statut) {
        assertThat(trouver(entreprise).getStatut().name()).isEqualTo(statut);
    }

    @Then("la candidature chez {string} a le poste {string}")
    public void aLePoste(String entreprise, String poste) {
        assertThat(trouver(entreprise).getPoste()).isEqualTo(poste);
    }

    @Then("le formulaire est réaffiché avec une erreur sur le champ {string}")
    public void leFormulaireEstReaffiche(String champ) {
        assertThat(dernierResultat.getResponse().getStatus()).isEqualTo(200);
        ModelAndView vue = dernierResultat.getModelAndView();
        assertThat(vue).isNotNull();
        assertThat(vue.getViewName()).isEqualTo("candidatures/formulaire");
        BindingResult erreurs = (BindingResult) vue.getModel().get(BindingResult.MODEL_KEY_PREFIX + "candidature");
        assertThat(erreurs).isNotNull();
        assertThat(erreurs.hasFieldErrors(champ)).isTrue();
    }

    @Then("je vois {int} candidature(s)")
    public void jeVois(int nombre) {
        List<?> candidatures = (List<?>) modele().get("candidatures");
        assertThat(candidatures).hasSize(nombre);
    }

    @Then("le taux de réponse affiché est de {int} %")
    public void leTauxDeReponseEst(int taux) {
        Statistiques stats = (Statistiques) modele().get("stats");
        assertThat(stats.tauxReponse()).isEqualTo(taux);
    }

    @Then("j'obtiens une erreur {int}")
    public void jObtiensUneErreur(int codeHttp) {
        assertThat(dernierResultat.getResponse().getStatus()).isEqualTo(codeHttp);
    }

    // ---------------------------------------------------------------- Outils

    /** Envoie le formulaire de création, exactement comme le ferait le navigateur. */
    private MvcResult envoyerCandidature(String entreprise, String poste, String statut) throws Exception {
        return mockMvc.perform(post("/candidatures")
                        .param("entreprise", entreprise)
                        .param("poste", poste)
                        .param("dateCandidature", LocalDate.now().toString())
                        .param("statut", statut))
                .andReturn();
    }

    private Candidature trouver(String entreprise) {
        return repository.findAll().stream()
                .filter(c -> c.getEntreprise().equals(entreprise))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Aucune candidature chez " + entreprise));
    }

    private Map<String, Object> modele() {
        assertThat(dernierResultat.getResponse().getStatus()).isEqualTo(200);
        ModelAndView vue = dernierResultat.getModelAndView();
        assertThat(vue).isNotNull();
        return vue.getModel();
    }
}
