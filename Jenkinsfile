// Pipeline CI/CD : à chaque nouveau commit sur GitHub, Jenkins
//   1. lance les tests Cucumber contre un vrai PostgreSQL
//   2. construit l'image Docker de l'application
//   3. la déploie sur le VPS
//   4. vérifie que l'application répond
// Si une étape échoue, les suivantes ne sont pas exécutées : rien de cassé n'arrive en production.

pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
        timeout(time: 30, unit: 'MINUTES')
    }

    // Jenkins regarde GitHub toutes les 2 minutes et lance le pipeline s'il y a du nouveau
    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        IMAGE = 'candidatures'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        TESTS = 'docker compose -p candidatures-tests -f docker-compose.test.yml'
    }

    stages {

        stage('Tests Cucumber') {
            steps {
                sh "${TESTS} up --build --abort-on-container-exit --exit-code-from tests"
            }
            post {
                always {
                    // On récupère les rapports Cucumber sortis du conteneur de test
                    sh """
                        rm -rf rapports && mkdir -p rapports
                        docker cp \$(${TESTS} ps -aq tests):/app/target/cucumber-reports/. rapports/ || true
                        ${TESTS} down -v --remove-orphans || true
                    """
                    junit testResults: 'rapports/cucumber.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'rapports/**', allowEmptyArchive: true
                }
            }
        }

        stage('Build image') {
            steps {
                sh "docker build -t ${IMAGE}:${IMAGE_TAG} -t ${IMAGE}:latest ."
            }
        }

        stage('Déploiement') {
            steps {
                // Le fichier .env de production est stocké dans Jenkins (credential "Secret file"),
                // jamais dans Git.
                withCredentials([file(credentialsId: 'candidatures-env', variable: 'ENV_FILE')]) {
                    sh 'docker compose -f docker-compose.prod.yml --env-file "$ENV_FILE" up -d --no-build --remove-orphans'
                }
            }
        }

        stage('Vérification') {
            steps {
                // On interroge /actuator/health depuis le réseau Docker de l'application
                sh '''
                    for i in $(seq 1 30); do
                        if docker run --rm --network candidatures_default curlimages/curl -fs http://app:8080/actuator/health | grep -q '"UP"'; then
                            echo "Application en ligne (version $IMAGE_TAG)"
                            exit 0
                        fi
                        echo "En attente du démarrage... ($i/30)"
                        sleep 5
                    done
                    echo "L'application ne répond pas : voir docker logs candidatures-app-1"
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            // Supprime les anciennes images inutilisées pour ne pas remplir le disque du VPS
            sh 'docker image prune -f'
        }
        failure {
            echo 'Échec du pipeline : la version en production n\'a pas été remplacée si les tests ont échoué.'
        }
    }
}
