# language: fr
Fonctionnalité: Suivi de mes candidatures
  En tant que candidat à un poste DevOps
  Je veux enregistrer et suivre mes candidatures
  Afin de savoir où j'en suis dans ma recherche d'emploi

  Contexte:
    Étant donné aucune candidature enregistrée

  Scénario: Ajouter une candidature
    Quand j'ajoute une candidature chez "Capgemini" pour le poste "Ingénieur DevOps"
    Alors je suis redirigé vers la liste
    Et la liste contient 1 candidature
    Et la candidature chez "Capgemini" a le statut "ENVOYEE"

  Scénario: Un formulaire incomplet est refusé
    Quand j'envoie le formulaire sans nom d'entreprise
    Alors le formulaire est réaffiché avec une erreur sur le champ "entreprise"
    Et la liste contient 0 candidature

  Scénario: Faire avancer une candidature
    Étant donné une candidature chez "Sopra Steria" pour le poste "DevOps junior"
    Quand je passe la candidature chez "Sopra Steria" au statut "ENTRETIEN"
    Alors la candidature chez "Sopra Steria" a le statut "ENTRETIEN"

  Scénario: Corriger une candidature
    Étant donné une candidature chez "Thales" pour le poste "Administrateur système"
    Quand je modifie le poste de la candidature chez "Thales" en "Ingénieur SRE"
    Alors la candidature chez "Thales" a le poste "Ingénieur SRE"

  Scénario: Supprimer une candidature
    Étant donné une candidature chez "Orange" pour le poste "Ingénieur cloud"
    Quand je supprime la candidature chez "Orange"
    Alors la liste contient 0 candidature

  Scénario: Filtrer les candidatures par statut
    Étant donné les candidatures suivantes :
      | entreprise | poste          | statut    |
      | Atos       | DevOps         | ENTRETIEN |
      | Accenture  | Cloud engineer | ENVOYEE   |
      | OVHcloud   | SRE            | ENTRETIEN |
    Quand je filtre sur le statut "ENTRETIEN"
    Alors je vois 2 candidatures

  Scénario: Calculer le taux de réponse
    Étant donné les candidatures suivantes :
      | entreprise | poste          | statut    |
      | Airbus     | DevOps         | ENVOYEE   |
      | Safran     | SRE            | ENTRETIEN |
      | Dassault   | Platform eng.  | OFFRE     |
      | Renault    | Cloud engineer | REFUSEE   |
    Quand j'ouvre la liste des candidatures
    Alors je vois 4 candidatures
    Et le taux de réponse affiché est de 75 %

  Scénario: Consulter une candidature qui n'existe pas
    Quand je consulte la candidature numéro 999999
    Alors j'obtiens une erreur 404
