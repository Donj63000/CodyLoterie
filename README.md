# CodyLoterie

Ce dépôt contient une application JavaFX permettant de réaliser des tirages au sort grâce à une roue. Les participants, les gains et les bonus peuvent être configurés directement dans l'interface graphique. Le projet utilise Maven et se lance via la classe `Launcher`.

## Transition vers une nouvelle roue
Nous prévoyons de migrer vers une roue dédiée aux événements PVP. Cette transition marquera le début d'un chantier pour adapter le fonctionnement actuel à ces futurs besoins.

## Lancement rapide
1. Installer Maven (voir `setup.sh`).
2. Compiler et exécuter :
   ```bash
   mvn javafx:run
   ```

## Structure du dépôt
- `src/main/java/` : code source de l'application.
- `src/main/resources/` : ressources (images).
- `pom.xml` : configuration Maven.

## Spécifications de la roulette PVP
1. Objectif général
Créer un outil qui génère aléatoirement :

un malus global (valable pour les deux combattants à chaque tour),

deux malus individuels (un par combattant).
L’outil doit rendre le combat équitable quel que soit le niveau ou l’équipement des joueurs, tout en pimentant la partie.

2. Types de roulettes à implémenter
Roulette globale

Tire une règle générale (ex. : « Pas de CàC », « Combat miroir », etc.).

S’applique simultanément aux deux joueurs.

Roulette individuelle

Tire un malus parmi la liste des malus individuels (ex. : « Sort principal interdit », « Narcoleptique », etc.).

Tirage indépendant pour chaque joueur, à chaque combat.

3. Listes de base à intégrer
3.1 Malus individuels
Sort principal interdit

Tu boites sévère (–2 PM)

Enragé (fin de tour au corps-à-corps)

Trop peureux (≥ 6 PO)

Panoplie imposée

Sorcier myope (≤ 3 PO)

Narcoleptique (skip 1 tour tous les 3 tours)

Écho étrange (1 sort/tour, toujours différent)

Oubli du familier

Aucun sort élémentaire (seulement neutres ou utilitaires)

3.2 Règles globales
Pas de CàC

Pas de sort à > 5 PO

Pas de boost/soin

1 sort/tour

PM limité (≤ 3 PM)

Pas d’invocations

Pas d’emplacement libre (combat direct CàC)

Combat miroir (même panoplie imposée)

Combat inversé (chaque joueur définit les sorts de l’adversaire)

Combat à nue (aucun équipement)

4. Interface utilisateur
Menu principal avec :

Bouton « Roulette globale »

Bouton « Roulette individuelle »

Possibilité d’ouvrir la roulette individuelle dans une sous-interface distincte, ou bien d’afficher les deux roulettes ensemble, selon préférence.

Affichage clair du résultat tiré pour chaque rôle (global / individuel).

5. Configuration et modularité
Liste par défaut préchargée (les malus et règles ci-dessus).

Fonctionnalités d’administration :

Ajouter un nouveau malus ou une nouvelle règle

Modifier ou supprimer un élément existant

Permettre à Moulbourn d’« injecter » facilement ses propres sets de malus à chaque test, ou de les modifier au fil des idées nouvelles.

6. Architecture et documentation
Code ultra-documenté : commentaires clairs, explication de la logique interne.

Modularité :

Séparer la logique de tirage, la gestion des listes, et la présentation UI.

Faciliter la maintenance et l’évolution mensuelle (nouveaux malus, nouvelles règles, etc.).

7. Processus d’utilisation lors d’un combat
Charger les listes de malus individuels et de règles globales.

Lancer la roulette globale → appliquer le malus à tous les joueurs.

Pour chaque joueur :

Lancer la roulette individuelle → appliquer son malus.

Vérifier le respect des règles (globales + individuel).

En cas de non-respect, le joueur déclare forfait et l’autre est déclaré vainqueur.
