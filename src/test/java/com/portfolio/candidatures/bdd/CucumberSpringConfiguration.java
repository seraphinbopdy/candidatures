package com.portfolio.candidatures.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Démarre l'application Spring Boot complète pour les tests, connectée au PostgreSQL de test
 * (adresse fournie par la variable DB_URL, voir docker-compose.test.yml).
 * MockMvc permet d'envoyer de vraies requêtes HTTP à l'application sans ouvrir de port.
 */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
}
