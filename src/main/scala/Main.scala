import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.control._
import scalafx.scene.layout._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color
import scalafx.scene.text.Font
import java.time.Instant
import java.io._
import scala.collection.mutable
import scala.util.Random
import scalafx.scene.input.KeyCode.B

object Main extends JFXApp3 {
  
  // État du jeu
  private val board = new SudokuBoard()
  private val originalBoard = new SudokuBoard()
  private val preGeneratedPuzzles = SudokuGenerator.getPreGeneratedPuzzles(10)

  private var currentPuzzleIndex = 0
  private var gridPane: GridPane = _
  private var gameState: GameState = _
  private var textFields: Array[Array[TextField]] = _
  private var statusLabel: Label = _
  private var timeLabel: Label = _
  private var errorsLabel: Label = _
  private var timerThread: Thread = _
  private var running = true
  var updatingBoard = false
  
  /* 
   * Démarre l'application.
    * Initialise la partie et crée l'interface utilisateur.
   */
  override def start(): Unit = {
    initializeGame()
    createUI()
  }
  
  /* 
   * Initialise le jeu.
   * Génère un puzzle Sudoku et copie le plateau original.
   */
  private def initializeGame(): Unit = {

    SudokuGenerator.generatePuzzle(board, 40)
    
    for (i <- 0 until 9; j <- 0 until 9) {
      originalBoard.grid(i)(j) = board.grid(i)(j)
    }
  
    // Initialiser l'état du jeu
    gameState = GameState(
      Board = board,
      originalBoard = originalBoard.copy(),
      startTime = Instant.now(),
      errors = 0,
      moveHistory = mutable.Stack()
    )
    // Démarrer le timer
    startTimer()
  }
  /*
    * Démarre un thread qui met à jour le label du temps toutes les secondes.
    * Le thread s'arrête lorsque la partie est terminée ou que l'application est fermée. 
  */
  private def startTimer(): Unit = {

    timerThread = new Thread {

      override def run(): Unit = {

        while (running) {
          
          javafx.application.Platform.runLater(() => {
            if (timeLabel != null) {
              val elapsed = gameState.getElapsedTime
              val seconds = elapsed.getSeconds
              val minutes = seconds / 60
              val remainingSeconds = seconds % 60
              timeLabel.text = f"Temps: $minutes%d:${remainingSeconds}%02d"
            }
          })
          Thread.sleep(1000)
        }
      }
    }
    timerThread.setDaemon(true)
    timerThread.start()
  }
  
  /* 
   * Crée l'interface utilisateur principale.
   * Comprend la grille de Sudoku, les boutons de contrôle, les labels d'information et le menu.
   */
  private def createUI(): Unit = {

    textFields = Array.ofDim[TextField](9, 9)
    
    // Grille principale
    gridPane = createGridPane()
    
    // Panneau de contrôle
    val controlPanel = createControlPanel()
    
    // Panneau d'information
    val infoPanel = createInfoPanel()
    
    // Menu
    val menuBar = createMenuBar()
    
    // Layout principal
    val rootPane = new BorderPane() {
      top = new VBox {
        children = Seq(menuBar, controlPanel)
      }
      center = new ScrollPane {
        content = gridPane
        padding = Insets(20)
      }
      bottom = infoPanel
    }
    
    stage = new JFXApp3.PrimaryStage {
      title = "Sudoku Scala - Projet IED"
      scene = new Scene(700, 800) {
        root = rootPane
      }
      
      onCloseRequest = _ => {
        running = false
        sys.exit(0)
      }
    }
  }
  
  /* 
    * Crée le panneau de grille principal.
    * Chaque cellule est un TextField avec des styles pour les blocs 3x3.
    * Les cases pré-remplies sont non éditables et ont un style différent. 
  */
  private def createGridPane(): GridPane = {
    val gridPane = new GridPane() {
      hgap = 2
      vgap = 2
      padding = Insets(5)
      alignment = Pos.Center
    }
    
    for (row <- 0 until 9; col <- 0 until 9) {
      val field = createTextField(row, col)
      textFields(row)(col) = field
      gridPane.add(field, col, row)
    }
    
    updateBoardDisplay()
    gridPane
  }
  
  /* 
   * Crée un TextField pour une cellule donnée, avec des styles et des gestionnaires d'événements.
   * Les cases pré-remplies sont non éditables et ont un style différent. Les changements de texte sont validés et les erreurs sont surlignées.
   * Lorsque le TextField perd le focus, le style est réinitialisé.   
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   * @return Le TextField créé pour la cellule spécifiée
   */
  private def createTextField(row: Int, col: Int): TextField = {
  new TextField {
    prefWidth = 60.0
    prefHeight = 60.0
    font = Font(24)
    alignment = Pos.Center
    
    // Style pour les blocs 3x3
    style = createBorderStyle(row, col)
    
    // Valeur initiale
    text = if (board.get(row, col) == 0) "" else board.get(row, col).toString
    
    // Les cases pré-remplies ne sont pas modifiables
    editable = board.get(row, col) == 0
    if (!editable.value) { 
      style = style.value + " -fx-background-color: #ffffff;"
    }
    
    // Gestionnaire d'événements
    text.onChange { (_, _, newValue) =>
      if (!updatingBoard) handleTextFieldChange(row, col, newValue)
    }
    
    // Focus perdu : nettoyer le style
    focused.onChange { (_, _, newFocus) =>
      if (!newFocus) {
        style = createBorderStyle(row, col)
        if (!editable.value) {  // CORRECTION : ajouter .value
          style = style.value + " -fx-background-color: #ffffff;"
        }
      }
    }
  }
  }
  
  /* 
   * Crée le style de bordure pour une cellule donnée, en fonction de sa position dans la grille.
   *
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   * @return La chaîne de style pour la bordure
   */
  private def createBorderStyle(row: Int, col: Int): String = {

    var top = 1
    var right = 1
    var bottom = 1
    var left = 1

    // Bordures épaisses entre blocs 3x3
    if (col % 3 == 2 && col != 8) right = 3
    if (col % 3 == 0 && col != 0) left = 3
    if (row % 3 == 2 && row != 8) bottom = 3
    if (row % 3 == 0 && row != 0) top = 3

    s"-fx-border-color: black; -fx-border-width: $top $right $bottom $left;"
  }
  
  /* 
   * Gère les changements de texte pour un TextField donné.
   * Valide les entrées, met à jour le plateau de jeu et le style en conséquence, et gère les erreurs.
   * 
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   * @param newValue Le nouveau texte
   */
  private def handleTextFieldChange(row: Int, col: Int, newValue: String): Unit = {
    // Effacer le style
    textFields(row)(col).style = createBorderStyle(row, col)
  
    // Valider l'entrée : doit être vide ou un chiffre entre 1 et 9
    if (newValue.nonEmpty && !newValue.matches("[1-9]?")) {
      textFields(row)(col).text = ""
      return
    }
    
    // Si c'est un chiffre valide, vérifier la validité du mouvement
    if (newValue.matches("[1-9]")) {
      val num = newValue.toInt
      val previousValue = board.get(row, col)
      
      if (SudokuValidator.isValid(board, row, col, num)) {
        // Mouvement valide
        board.set(row, col, num)
        gameState.saveMove(row, col, previousValue)
        
        // Vérifier si la grille est complète
        if (board.isComplete) {
          showVictory()
        }
      } else {
        // Erreur
        textFields(row)(col).style = createBorderStyle(row, col) + " -fx-background-color: #fbb3b3;"
        gameState.errors += 1
        errorsLabel.text = s"Erreurs: ${gameState.errors}"
      }
      
    } else {
      // Effacer la cellule
      val previousValue = board.get(row, col)
      if (previousValue != 0) {
        board.set(row, col, 0)
        gameState.saveMove(row, col, previousValue)
      }
    }
  }

  /* 
   * Affiche les informations à propos du jeu.
   */
  private def about(): Unit = {
    new Alert(Alert.AlertType.Information) {
      title = "À propos"
      headerText = "Sudoku Scala"
      contentText = """
        Projet de Programmation Scala
        IED - Université Paris 8
        
        Version 1.0
        
        Fonctionnalités :
        1. Génération aléatoire de grilles
        2. 3 niveaux de difficulté
        3. Sauvegarde/Chargement
        4. Annulation des coups
        5. Aide contextuelle
        6. Chronomètre
        7. Compteur d'erreurs
      """.stripMargin
    }.showAndWait()
  }
  
  /* 
   * Crée le panneau de contrôle avec les boutons pour démarrer une nouvelle partie, annuler, vérifier et obtenir de l'aide.
   */
  private def createControlPanel(): HBox = {

    new HBox(10) {
      alignment = Pos.Center
      padding = Insets(15)
      children = Seq(
        new Button("Nouveau (Facile)") {
          onAction = _ => newGame(35)
        },
        new Button("Nouveau (Moyen)") {
          onAction = _ => newGame(50)
        },
        new Button("Nouveau (Difficile)") {
          onAction = _ => newGame(64)
        },
        new Button("Annuler") {
          onAction = _ => undo()
        },
        new Button("Vérifier") {
          onAction = _ => validateAll()
        },
        new Button("Aide") {
          onAction = _ => Help()
        }
      )
    }
  }
  
  /* 
   * Crée le panneau d'information avec le temps écoulé, le nombre d'erreurs et le statut du jeu.
   */
  private def createInfoPanel(): HBox = {

    timeLabel = new Label("Temps: 0:00") {
      font = Font(14)
      prefWidth = 150
    }
    
    errorsLabel = new Label("Erreurs: 0") {
      font = Font(14)
      prefWidth = 150
    }
    
    statusLabel = new Label("Prêt") {
      font = Font(14)
      prefWidth = 300
    }
    
    new HBox(20) {
      alignment = Pos.Center
      padding = Insets(15)
      children = Seq(timeLabel, errorsLabel, statusLabel)
    }
  }
  
  /* 
   * Crée la barre de menu avec les options pour gérer les fichiers, les puzzles, l'affichage et l'aide.
   */
  private def createMenuBar(): MenuBar = {
    new MenuBar {
      menus = Seq(
        new Menu("Fichier") {
          items = Seq(
            new MenuItem("Nouvelle partie") {
              onAction = _ => newGame(40)
            },
            new MenuItem("Sauvegarder") {
              onAction = _ => saveGame()
            },
            new MenuItem("Charger") {
              onAction = _ => loadGame()
            },
            new SeparatorMenuItem(),
            new MenuItem("Quitter") {
              onAction = _ => sys.exit(0)
            }
          )
        },

        new Menu("Puzzles") {
          items = (0 until preGeneratedPuzzles.length).map { i =>
            new MenuItem(s"Puzzle ${i + 1}") {
              onAction = _ => loadPreGeneratedPuzzle(i)
            }
          }.toSeq
        },
        new Menu("Affichage") {
          items = Seq(
            new CheckMenuItem("Surligner les erreurs") {
              selected = true
              onAction = _ => highlightErrors(selected())
            },
            new MenuItem("Afficher les chiffres") {
              onAction = _ => showDigitDialog()
            }
          )
        },
        new Menu("Aide") {
          items = Seq(
            new MenuItem("Règles du jeu") {
              onAction = _ => showRules()
            },
            new MenuItem("À propos") {
              onAction = _ => about
          ()
            }
          )
        }
      )
    }
  }
  
  /* 
   * Démarre une nouvelle partie avec une grille générée aléatoirement selon la difficulté choisie.
   * Sauvegarde la partie actuelle avant de réinitialiser le plateau et de générer une nouvelle grille.
   * Met à jour l'affichage et les labels d'information.
   *
   * @param difficulty Le nombre de cases pré-remplies (plus le nombre est élevé, plus la partie est facile)
   */
  private def newGame(difficulty: Int): Unit = {
    
    saveGame() // Sauvegarder la partie actuelle avant de commencer une nouvelle
    board.reset()
    originalBoard.reset()

    SudokuGenerator.generatePuzzle(board, difficulty)
    
    for (i <- 0 until 9; j <- 0 until 9) {
      originalBoard.grid(i)(j) = board.grid(i)(j)
    }
    
    gameState = GameState(
      Board = board,
      originalBoard = originalBoard.copy(),
      startTime = Instant.now(),
      errors = 0,
      moveHistory = mutable.Stack()
    )
    
    updateBoardDisplay()
    errorsLabel.text = "Erreurs: 0"
    statusLabel.text = "Nouvelle partie"
  }
  
  /* 
   * Annule le dernier mouvement effectué par le joueur.
   */
  private def undo(): Unit = {
    gameState.undo() match {
      case Some((row, col, previousValue)) =>
        board.set(row, col, previousValue)
        updateBoardDisplay()
        statusLabel.text = "Annulation effectuée"
      case None =>
        statusLabel.text = "Plus rien à annuler"
    }
  }
  
  /* 
   * Valide toutes les cases du plateau et affiche les erreurs.
   */
  private def validateAll(): Unit = {
    val errors = SudokuValidator.validateBoard(board)
    var errorCount = 0
    
    for (row <- 0 until 9; col <- 0 until 9) {
      if (errors(row)(col)) {
        textFields(row)(col).style =  (row, col) + " -fx-background-color: #ffcccc;"
        errorCount += 1
      } else if (board.get(row, col) != 0) {
        textFields(row)(col).style = createBorderStyle(row, col) + " -fx-background-color: #ccffcc;"
      }
    }
    
    if (errorCount == 0) {
      statusLabel.text = "Tout est correct !"
    } else {
      statusLabel.text = s"$errorCount erreur(s) trouvée(s)"
    }
  }
  
  // Affiche une boîte de dialogue pour aider le joueur à reveler les occurrences d'un chiffre sur la grille.
  private def Help(): Unit = {
    val digit = new TextInputDialog() {
      title = "Aide"
      headerText = "Entrez un chiffre compris " +
        "entre 1 et 9 pour voir toutes ses occurrences sur la grille"
      contentText = "Chiffre:"
    }.showAndWait()
    
    digit.foreach { d =>
      if (d.matches("[1-9]")) {
        // Réinitialiser les styles
        for (row <- 0 until 9; col <- 0 until 9) {
          textFields(row)(col).style = createBorderStyle(row, col)
        }
        
        SudokuGenerator.fillNumber(board, d.toInt) 
        updatingBoard = true
        updateBoardDisplay()
        updatingBoard = false

        // Surligner les occurrences
        for (row <- 0 until 9; col <- 0 until 9 ) {
          if (board.get(row, col) == d.toInt) {
            textFields(row)(col).style = createBorderStyle(row, col) + " -fx-background-color: #c9ef84d6;"
          }
        }
        
        statusLabel.text = s"Cases avec le chiffre $d mises en évidence"
        
      }
    }
  }
  
  /* 
   * Met en évidence les erreurs sur le plateau.
   * Si enable est true, valide toutes les cases et surligne les erreurs. Sinon, réinitialise les styles.
   * @param enable Indique si les erreurs doivent être surlignées ou non
   */
  private def highlightErrors(enable: Boolean): Unit = {
    if (enable) {
      validateAll()
    } else {
      updateBoardDisplay()
    }
  }
  
  /* 
   * Affiche une boîte de dialogue pour permettre au joueur de saisir un chiffre.
   * Si le chiffre est valide, surligne toutes les occurrences de ce chiffre sur la grille.
   * Si le chiffre est invalide, ne fait rien.
   */
  private def showDigitDialog(): Unit = {

    val digit = new TextInputDialog() {
      title = "Afficher les cases"
      headerText = "Entrez un chiffre pour voir toutes ses occurrences"
      contentText = "Chiffre:"
    }.showAndWait()
    
    digit.foreach { d =>
      if (d.matches("[1-9]")) {
        // Réinitialiser les styles
        for (row <- 0 until 9; col <- 0 until 9) {
          textFields(row)(col).style = createBorderStyle(row, col)
        }
        
        // Surligner les occurrences
        for (row <- 0 until 9; col <- 0 until 9) {
          if (board.get(row, col) == d.toInt) {
            textFields(row)(col).style = createBorderStyle(row, col) + " -fx-background-color: #99ccff;"
          }
        }
      }
    }
  }
  
  /* 
   * Affiche une boîte de dialogue de victoire lorsque le joueur complète la grille.
   * Affiche le temps écoulé et le nombre d'erreurs commises pendant la partie.
   */
  private def showVictory(): Unit = {
    val elapsed = gameState.getElapsedTime
    val minutes = elapsed.getSeconds / 60
    val seconds = elapsed.getSeconds % 60
    
    statusLabel.text = "FÉLICITATIONS ! Vous avez gagné !"
    statusLabel.style = "-fx-text-fill: green; -fx-font-weight: bold;"
    
    new Alert(Alert.AlertType.Information) {
      title = "Victoire !"
      headerText = "Bravo !"
      contentText = f"Vous avez résolu le Sudoku en $minutes%d:${seconds}%02d\nErreurs: ${gameState.errors}"
    }.showAndWait()
  }
  
  // Affiche les règles du jeu dans une boîte de dialogue.
  private def showRules(): Unit = {
    new Alert(Alert.AlertType.Information) {
      title = "Règles du Sudoku"
      headerText = "Comment jouer"
      contentText = """
        |Règles du Sudoku :
        |
        |• Remplissez la grille avec des chiffres de 1 à 9
        |• Chaque ligne doit contenir tous les chiffres de 1 à 9
        |• Chaque colonne doit contenir tous les chiffres de 1 à 9
        |• Chaque région 3x3 doit contenir tous les chiffres de 1 à 9
        |
        |Les cases grises sont pré-remplies et ne peuvent pas être modifiées.
        |Les cases rouges indiquent une erreur.
      """.stripMargin
    }.showAndWait()
  }
  /* 
   * Sauvegarde la partie en cours.
   */
  private def saveGame(): Unit = {

    if (board.saved) {
      val dialog = new Alert(Alert.AlertType.Confirmation) {
        title = "Sauvegarde"
        headerText = "Une partie est déjà sauvegardée"
        contentText = "Voulez-vous écraser la sauvegarde existante ?"
      }.showAndWait() match {

        case Some(ButtonType.OK) =>
          try {
            board.saveToFile(s"src/saves/${board.nameSaved}.txt", "current_game")
            statusLabel.text = "Sauvegarde mise à jour"
          } catch {
            case e: Exception =>
              statusLabel.text = "Erreur lors de la sauvegarde"
          }
        case _ =>
          statusLabel.text = "Sauvegarde annulée"
      }
    } else {
    val dialog = new TextInputDialog("ma_partie") {
      title = "Sauvegarder la partie"
      headerText = "Entrer le nom du fichier"
      contentText = "Nom :"
    }

    val result = dialog.showAndWait()

    result match {
      case Some(filename) =>

        val file = new File(s"src/saves/$filename.txt")

        if (file.exists()) {
          statusLabel.text = "Ce nom de fichier existe déjà"
          return
        }

        try {
          board.saveToFile(s"src/saves/$filename.txt", "current_game")
          originalBoard.saveToFile(s"src/saves/${filename}_original.txt", "original")
          statusLabel.text = "Partie sauvegardée"
        } catch {
          case e: Exception =>
            statusLabel.text = "Erreur lors de la sauvegarde"
        }

      case None =>
        statusLabel.text = "Sauvegarde annulée"
    }
   }
  }
    
  /* 
   * Affiche une boîte de dialogue pour permettre au joueur de choisir une sauvegarde à charger.
   * Charge la partie sélectionnée et met à jour l'affichage et les labels d'information.
   */
  private def loadGame(): Unit = {

    val saveDir = new File("src/saves")

    if (!saveDir.exists() || !saveDir.isDirectory) {
      statusLabel.text = "Aucune sauvegarde trouvée"
      return
    }

    val saves = saveDir.listFiles()
      .filter(_.getName.endsWith(".txt"))
      .map(_.getName.replace(".txt", ""))
      .filter(!_.contains("_original"))

    if (saves.isEmpty) {
      statusLabel.text = "Aucune sauvegarde trouvée"
      return
    }

    val dialog = new ChoiceDialog(saves.head, saves.toSeq) {
      title = "Charger une partie"
      headerText = "Choisir une sauvegarde"
      contentText = "Sauvegarde :"
    }

    val result = dialog.showAndWait()

    result match {
      case Some(filename) =>
        try {

          board.loadFromFile(s"src/saves/$filename.txt")
          originalBoard.loadFromFile(s"src/saves/${filename}_original.txt")

          updateBoardDisplay()
          statusLabel.text = "Partie chargée"
          board.saved = true
          board.nameSaved = filename

        } catch {
          case e: Exception =>
            statusLabel.text = "Erreur lors du chargement"
        }

      case None =>
        statusLabel.text = "Chargement annulé"
    }
  }
  
  /* 
   * Charge un puzzle pré-généré à partir de la liste des puzzles disponibles.
   * Met à jour le plateau de jeu, l'affichage et les labels d'information en conséquence.
   *
   * @param index L'index du puzzle à charger dans la liste des puzzles pré-générés
   */
  private def loadPreGeneratedPuzzle(index: Int): Unit = {
    if (index >= 0 && index < preGeneratedPuzzles.length) {
      val puzzle = preGeneratedPuzzles(index)
      
      for (i <- 0 until 9; j <- 0 until 9) {
        board.grid(i)(j) = puzzle.grid(i)(j)
        originalBoard.grid(i)(j) = puzzle.grid(i)(j)
      }
      
      gameState = GameState(
        Board = board,
        originalBoard = originalBoard.copy(),
        startTime = Instant.now(),
        errors = 0,
        moveHistory = mutable.Stack()
      )
      
      updateBoardDisplay()
      errorsLabel.text = "Erreurs: 0"
      statusLabel.text = s"Puzzle ${index + 1} chargé"
    }
  }
  
  /* 
   * Met à jour l'affichage de la grille en fonction de l'état actuel du plateau de jeu.
   * Les cases pré-remplies sont affichées en gris et ne sont pas éditables. Les cases vides sont affichées comme des TextFields vides.
   * Les styles des cellules sont mis à jour pour refléter les bordures et les erreurs éventuelles.
   */
  private def updateBoardDisplay(): Unit = {

    for (row <- 0 until 9; col <- 0 until 9) {
      val field = textFields(row)(col)
      val value = board.get(row, col)
      
      field.text = if (value == 0) "" else value.toString
      field.editable = originalBoard.get(row, col) == 0
      field.style = createBorderStyle(row, col)
      
      if (!field.editable.value) {  // CORRECTION : ajouter .value
        field.style = field.style.value + " -fx-background-color: #f0f0f0;"
      }
    }
  }
}