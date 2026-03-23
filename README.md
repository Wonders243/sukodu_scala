#  Sudoku Scala - Projet IED

##  Description

Ce projet est une application de Sudoku développée en **Scala** avec **ScalaFX**.
Elle permet de générer, jouer, sauvegarder et résoudre des grilles de Sudoku avec une interface graphique interactive.

---

##  Fonctionnalités

*  Génération aléatoire de grilles de Sudoku
*  Plusieurs niveaux de difficulté (Facile, Moyen, Difficile)
*  Annulation des coups
*  Vérification des erreurs
*  Aide contextuelle (affichage des chiffres)
*  Chronomètre en temps réel
*  Compteur d’erreurs
*  Sauvegarde et chargement de parties
*  Puzzles pré-générés
*  Interface graphique avec mise en évidence des erreurs

---

## Structure du projet

```
src/
 └── main/
     └── scala/
         ├── Main.scala              // Interface graphique et logique principale
         ├── GameState.scala         // Gestion de l’état du jeu (temps, erreurs, undo)
         ├── SudokuBoard.scala       // Représentation de la grille
         ├── SudokuGenerator.scala   // Génération de grilles
         └── SudokuValidator.scala   // Vérification des règles du Sudoku

docs/
 └── ... (documentation Scaladoc)

build.sbt
```

---

##  Lancer le projet

###  Prérequis

* Java 8+
* SBT installé

###  Exécution

```bash
sbt run
```

---

##  Documentation

La documentation a été générée automatiquement avec **Scaladoc** :

```bash
sbt doc
```

 Disponible dans :

```
docs/index.html
```

---

##  Sauvegarde des parties

Les sauvegardes sont stockées dans :

```
src/saves/
```

Fonctionnalités :

* sauvegarde avec nom personnalisé
* chargement des parties existantes

---

##  Fonctionnement

* La grille est représentée par une classe `SudokuBoard`
* Les règles sont validées avec `SudokuValidator`
* L’état du jeu est géré via `GameState`
* L’interface est construite avec **ScalaFX**

---

##  Problèmes rencontrés

* Gestion des événements JavaFX (mise à jour des champs)
* Synchronisation du timer avec l’interface
* Validation en temps réel des entrées utilisateur

---

##  Améliorations possibles

* Interface plus avancée (animations, thèmes)
* Sauvegarde JSON ou base de données
* Mode multijoueur ou challenge
* methode ia pour proposer des aides pertinents

---

##  Auteur

Projet réalisé dans le cadre du cours de programmation concurente encadré par Mr. Azzag
Master 1 Informatique et Big data
IED - Université Paris 8

---

## Contribution

Les contributions sont les bienvenues !

---

### Comment contribuer

1. copier le projet

2. Créer une branche :

   ```bash
   git checkout -b feature/ma-fonctionnalite
   ```
3. Commit les modifications :

   ```bash
   git commit -m "Ajout d'une fonctionnalité"
   ```
4. Push :

   ```bash
   git push origin feature/ma-fonctionnalite
   ```
5. Ouvrir une Pull Request

---

### Règles de contribution

* Respecter la structure du projet
* Documenter les nouvelles fonctionnalités
* Tester le code avant de proposer une modification

---

##  Conclusion

Ce projet met en œuvre :

* programmation orientée objet
* gestion d’interface graphique
* manipulation d’événements
* logique algorithmique (Sudoku)

---
