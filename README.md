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

