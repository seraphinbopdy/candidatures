# Suivi de candidatures

Application de suivi de candidatures d'emploi, déployée avec une chaîne DevOps complète :

- **Spring Boot 3.5** (Java 21) + **Thymeleaf** pour l'interface
- **PostgreSQL 17**, schéma géré par **Flyway**
- **Tests BDD** avec **Cucumber** et **Gherkin** en français, exécutés contre un vrai PostgreSQL
- **Docker** (image multi-stage) et **Docker Compose**
- **Caddy** en reverse proxy avec HTTPS automatique (Let's Encrypt)
- **Jenkins** : tests, build et déploiement automatiques à chaque commit
- Hébergé sur un **VPS Contabo**

```
git push ─► Jenkins ─► Tests Cucumber (PostgreSQL) ─► Build image ─► Déploiement ─► Vérification
                                                                         │
Internet ─► Caddy (HTTPS, 80/443) ─► Spring Boot (8080) ─► PostgreSQL ◄──┘
```

---

## Contenu du projet

| Fichier / dossier | Rôle |
| --- | --- |
| `src/main/java/...` | L'application : entité, repository, service, contrôleurs |
| `src/main/resources/templates/` | Les pages HTML (Thymeleaf) |
| `src/main/resources/db/migration/` | Les scripts SQL Flyway (création des tables) |
| `src/test/resources/features/` | Les scénarios de test en français (Gherkin) |
| `src/test/java/.../bdd/` | Le code Java derrière chaque phrase des scénarios |
| `Dockerfile` | Construit l'image de l'application (build, test, image finale) |
| `docker-compose.yml` | Lancement en local sur ton PC |
| `docker-compose.test.yml` | Lancement des tests avec un PostgreSQL jetable |
| `docker-compose.prod.yml` | Production sur le VPS : PostgreSQL + application + Caddy |
| `Caddyfile` | Configuration du reverse proxy et du HTTPS |
| `Jenkinsfile` | Le pipeline CI/CD |
| `jenkins/` | L'installation de Jenkins sur le VPS |
| `.env.template` | Modèle du fichier `.env` qui contient tes secrets |
| `.gitignore` | Fichiers que Git ne doit jamais envoyer sur GitHub (dont `.env`) |
| `.dockerignore` | Fichiers que Docker ne met jamais dans l'image |

---

## Étape 1 — Lancer l'application sur ton PC (≈ 15 min)

Seul **Docker** est nécessaire : pas besoin d'installer Java ni Maven.

Dans ton terminal WSL, place-toi dans le dossier du projet :

```bash
cd /mnt/c/TEST/candidatures        # adapte le chemin
cp .env.template .env
docker compose up --build
```

Le premier lancement prend quelques minutes (téléchargement des dépendances Maven).
Quand tu vois `Started CandidaturesApplication`, ouvre **http://localhost:8080**.

Arrêter : `Ctrl + C` puis `docker compose down` (les données sont conservées dans le volume `pgdata`).
Tout effacer, données comprises : `docker compose down -v`.

### Lancer les tests Cucumber

```bash
docker compose -p candidatures-tests -f docker-compose.test.yml up --build --abort-on-container-exit --exit-code-from tests
docker compose -p candidatures-tests -f docker-compose.test.yml down -v
```

Tu dois voir défiler les 8 scénarios en vert puis `BUILD SUCCESS`.
Ce qui se passe : Docker lance un PostgreSQL de test vide (en mémoire), puis un conteneur qui exécute `mvn verify` contre cette base. C'est exactement ce que fera Jenkins.

> Arrête l'application locale avant (`docker compose down`) si ton PC manque de mémoire.

---

## Étape 2 — Publier le code sur GitHub (≈ 5 min)

1. Sur github.com, crée un dépôt **public** nommé `candidatures`, **sans** README.
2. Dans le dossier du projet :

```bash
git init
git add .
git commit -m "Application de suivi de candidatures"
git branch -M main
git remote add origin https://github.com/TON-COMPTE/candidatures.git
git push -u origin main
```

GitHub demande un mot de passe : utilise un **token** (GitHub → Settings → Developer settings → Personal access tokens), pas ton mot de passe.

Vérifie sur GitHub que le fichier `.env` **n'apparaît pas**. S'il apparaît, c'est que ton `.gitignore` n'a pas été pris en compte.

---

## Étape 3 — Préparer le VPS Contabo (≈ 20 min)

### 3.1 Le domaine DuckDNS

1. Sur **duckdns.org**, connecte-toi avec GitHub ou Google.
2. Crée un sous-domaine, par exemple `neveu-candidatures` → `neveu-candidatures.duckdns.org`.
3. Dans « current ip », mets l'**IP de ton VPS** et clique sur **update ip**.

Vérifie depuis ton PC : `nslookup neveu-candidatures.duckdns.org` doit répondre l'IP du VPS.

### 3.2 Sécuriser le serveur

Connexion en root (mot de passe reçu par email de Contabo) :

```bash
ssh root@IP_DU_VPS
```

Sur le VPS :

```bash
# Mises à jour
apt update && apt upgrade -y

# Un utilisateur dédié, pour ne plus travailler en root
adduser deploy
usermod -aG sudo deploy

# Pare-feu : seulement SSH, HTTP et HTTPS
ufw allow OpenSSH
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable

# Docker
curl -fsSL https://get.docker.com | sh
usermod -aG docker deploy
```

Déconnecte-toi (`exit`), puis reconnecte-toi avec le nouvel utilisateur : `ssh deploy@IP_DU_VPS`.
Vérifie : `docker ps` doit fonctionner sans `sudo`.

> Sécurité : Docker contourne le pare-feu `ufw` pour les ports qu'il publie. C'est pour ça que seuls Caddy (80/443) publie des ports, que PostgreSQL n'en publie aucun, et que Jenkins n'écoute que sur `127.0.0.1`.

---

## Étape 4 — Premier déploiement à la main (≈ 15 min)

Le faire une fois à la main permet de comprendre ce que Jenkins automatisera ensuite.

Sur le VPS (en `deploy`) :

```bash
git clone https://github.com/TON-COMPTE/candidatures.git
cd candidatures
cp .env.template .env
nano .env
```

Dans `.env`, change :
- `POSTGRES_PASSWORD` : un long mot de passe (par exemple le résultat de `openssl rand -base64 24`)
- `DOMAIN` : ton domaine DuckDNS

Enregistre (`Ctrl + O`, `Entrée`, `Ctrl + X`), puis :

```bash
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f caddy
```

Dans les logs de Caddy, attends `certificate obtained successfully` (`Ctrl + C` pour quitter les logs).
Ouvre **https://ton-domaine.duckdns.org** : l'application est en ligne, en HTTPS.

**Garde une copie de ce fichier `.env`** (sans le partager) : tu vas le donner à Jenkins à l'étape suivante.

---

## Étape 5 — Jenkins (≈ 40 min)

### 5.1 Installer Jenkins sur le VPS

```bash
sudo mkdir -p /var/jenkins_home
sudo chown 1000:1000 /var/jenkins_home      # 1000 = l'utilisateur jenkins dans le conteneur

cd ~/candidatures/jenkins
echo "DOCKER_GID=$(getent group docker | cut -d: -f3)" > .env
docker compose up -d --build
```

Récupère le mot de passe administrateur initial :

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 5.2 Ouvrir Jenkins depuis ton PC

Jenkins n'est pas exposé sur Internet. On y accède par un **tunnel SSH**. Dans un terminal de ton PC (PowerShell ou WSL), laisse tourner :

```bash
ssh -L 9090:localhost:8080 deploy@IP_DU_VPS
```

Puis ouvre **http://localhost:9090** sur ton PC.

1. Colle le mot de passe initial.
2. Choisis **Install suggested plugins**.
3. Crée ton compte administrateur.

### 5.3 Donner le fichier .env à Jenkins

**Administrer Jenkins → Credentials → System → Global credentials → Add Credentials** :

- Kind : **Secret file**
- File : ton fichier `.env` de production (le même que sur le VPS)
- ID : `candidatures-env` (exactement ce nom, utilisé par le Jenkinsfile)

### 5.4 Créer le pipeline

**Nouveau Item** → nom `candidatures` → **Pipeline** → OK, puis dans la section Pipeline :

- Definition : **Pipeline script from SCM**
- SCM : **Git**
- Repository URL : `https://github.com/TON-COMPTE/candidatures.git`
- Branch Specifier : `*/main`
- Script Path : `Jenkinsfile`

Enregistre, puis clique sur **Lancer un build**. Le premier build prend 5 à 10 minutes.

Les 4 étapes doivent passer au vert : **Tests Cucumber → Build image → Déploiement → Vérification**.
Le rapport Cucumber est disponible dans les artefacts du build (`rapports/cucumber.html`).

À partir de maintenant, Jenkins vérifie GitHub toutes les 2 minutes : **chaque `git push` déclenche le pipeline**.

### 5.5 Tester la chaîne complète

Sur ton PC, change un texte visible (par exemple « Recherche d'emploi DevOps » dans `src/main/resources/templates/fragments/layout.html`), puis :

```bash
git commit -am "Changement du sous-titre"
git push
```

Quelques minutes plus tard, le changement est en ligne. Essaie aussi de **casser un test** volontairement : le pipeline s'arrête au rouge et **la production n'est pas touchée**.

---

## Opérations courantes

| Je veux… | Commande (sur le VPS) |
| --- | --- |
| Voir l'état des conteneurs | `docker ps` |
| Lire les logs de l'application | `docker logs -f candidatures-app-1` |
| Lire les logs de Jenkins | `docker logs -f jenkins` |
| Ouvrir la base de données | `docker exec -it candidatures-db-1 psql -U candidatures` |
| Sauvegarder la base | `docker exec candidatures-db-1 pg_dump -U candidatures candidatures > sauvegarde.sql` |
| Lister les versions déployées | `docker images candidatures` |

### Revenir à une version précédente (rollback)

Chaque build Jenkins produit une image numérotée (`candidatures:12`, `candidatures:13`…). Pour revenir à la version 12, dans Jenkins : ouvre le build n°12 et clique sur **Rejouer**. Ou directement sur le VPS :

```bash
cd /var/jenkins_home/workspace/candidatures
IMAGE_TAG=12 docker compose -f docker-compose.prod.yml --env-file ~/candidatures/.env up -d --no-build
```

---

## Dépannage

| Symptôme | Cause probable | Solution |
| --- | --- | --- |
| Caddy : `challenge failed` | Le domaine ne pointe pas vers le VPS, ou port 80 fermé | Vérifier `nslookup` et `sudo ufw status` |
| L'app redémarre en boucle | Mauvais mot de passe de base de données | `docker logs candidatures-app-1`, vérifier `.env` |
| Jenkins : `permission denied ... docker.sock` | `DOCKER_GID` absent ou faux | Refaire l'étape 5.1 puis `docker compose up -d` |
| Jenkins : `Could not find credentials` | ID du credential différent | L'ID doit être exactement `candidatures-env` |
| Tests : `Connection refused` | PostgreSQL de test pas prêt | Relancer ; vérifier `docker compose -f docker-compose.test.yml logs db-test` |
| Le PC rame pendant les tests | Docker manque de mémoire | Arrêter l'application locale avant de lancer les tests |

---

## Pour l'entretien

> « J'ai développé une application Spring Boot avec PostgreSQL, testée en BDD avec Cucumber et Gherkin contre une vraie base PostgreSQL lancée dans Docker. L'application est conteneurisée avec un Dockerfile multi-stage et un utilisateur non root. Sur mon VPS, Caddy gère le HTTPS automatiquement devant l'application, et la base n'est jamais exposée sur Internet. Jenkins exécute à chaque commit les tests, construit une image versionnée, déploie avec Docker Compose et vérifie le endpoint de santé. Les secrets sont stockés dans les credentials Jenkins, jamais dans Git, et je peux revenir à n'importe quelle version précédente grâce aux images numérotées. »

**Prochaine étape** : remplacer Docker Compose par Kubernetes (k3s) sur le même VPS.
