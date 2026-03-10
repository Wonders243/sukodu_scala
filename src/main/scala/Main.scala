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
  
  override def start(): Unit = {
    initializeGame()
    createUI()
  }
  
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
  
  private def createTextField(row: Int, col: Int): TextField = {
  new TextField {
    prefWidth = 60.0
    prefHeight = 60.0
    font = Font(24)
    alignment = Pos.Center
    
    // Style pour les blocs 3x3
    style = createBorderStyle(row, col)
    
    // Valeur initiale
    text = if (board.grid(row)(col) == 0) "" else board.grid(row)(col).toString
    
    // Les cases pré-remplies ne sont pas modifiables
    editable = board.grid(row)(col) == 0
    if (!editable.value) { 
      style = style.value + " -fx-background-color: #f0f0f0;"
    }
    
    // Gestionnaire d'événements
    text.onChange { (_, _, newValue) =>
      handleTextFieldChange(row, col, newValue)
    }
    
    // Focus perdu : nettoyer le style
    focused.onChange { (_, _, newFocus) =>
      if (!newFocus) {
        style = createBorderStyle(row, col)
        if (!editable.value) {  // CORRECTION : ajouter .value
          style = style.value + " -fx-background-color: #f0f0f0;"
        }
      }
    }
  }
  }
  
  private def createBorderStyle(row: Int, col: Int): String = {
    var style = "-fx-border-width: 1; -fx-border-color: black;"
    
    // Bordures épaisses entre les blocs
    if (col % 3 == 2 && col != 8) style += " -fx-border-width: 1 3 1 1;"
    if (col % 3 == 0 && col != 0) style += " -fx-border-width: 1 1 1 3;"
    if (row % 3 == 2 && row != 8) style += " -fx-border-width: 1 1 3 1;"
    if (row % 3 == 0 && row != 0) style += " -fx-border-width: 3 1 1 1;"
    
    style
  }
  
  private def handleTextFieldChange(row: Int, col: Int, newValue: String): Unit = {
    // Effacer le style
    textFields(row)(col).style = createBorderStyle(row, col)
    
    if (newValue.nonEmpty && !newValue.matches("[1-9]?")) {
      textFields(row)(col).text = ""
      return
    }
    
    if (newValue.matches("[1-9]")) {
      val num = newValue.toInt
      val previousValue = board.grid(row)(col)
      
      if (SudokuValidator.isValid(board, row, col, num)) {
        // Mouvement valide
        board.grid(row)(col) = num
        gameState.saveMove(row, col, previousValue)
        
        // Vérifier si la grille est complète
        if (board.isComplete) {
          showVictory()
        }
      } else {
        // Erreur
        textFields(row)(col).style = createBorderStyle(row, col) + " -fx-background-color: #ffcccc;"
        gameState.errors += 1
        errorsLabel.text = s"Erreurs: ${gameState.errors}"
      }
    } else {
      // Effacer la cellule
      val previousValue = board.grid(row)(col)
      if (previousValue != 0) {
        board.grid(row)(col) = 0
        gameState.saveMove(row, col, previousValue)
      }
    }
  }

  private def showAbout(): Unit = {
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
          onAction = _ => showHelp()
        }
      )
    }
  }
  
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
              onAction = _ => showAbout()
            }
          )
        }
      )
    }
  }
  
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
  
  private def undo(): Unit = {
    gameState.undo() match {
      case Some((row, col, previousValue)) =>
        board.grid(row)(col) = previousValue
        updateBoardDisplay()
        statusLabel.text = "Annulation effectuée"
      case None =>
        statusLabel.text = "Plus rien à annuler"
    }
  }
  
  private def validateAll(): Unit = {
    val errors = SudokuValidator.validateBoard(board)
    var errorCount = 0
    
    for (row <- 0 until 9; col <- 0 until 9) {
      if (errors(row)(col)) {
        textFields(row)(col).style =  (row, col) + " -fx-background-color: #ffcccc;"
        errorCount += 1
      } else if (board.grid(row)(col) != 0) {
        textFields(row)(col).style = createBorderStyle(row, col) + " -fx-background-color: #ccffcc;"
      }
    }
    
    if (errorCount == 0) {
      statusLabel.text = "Tout est correct !"
    } else {
      statusLabel.text = s"$errorCount erreur(s) trouvée(s)"
    }
  }
  
  private def showHelp(): Unit = {
    val digit = new TextInputDialog() {
      title = "Aide"
      headerText = "Entrez un chiffre (1-9)"
      contentText = "Chiffre:"
    }.showAndWait()
    
    digit.foreach { d =>
      if (d.matches("[1-9]")) {
        val conflicts = SudokuValidator.findConflicts(board, d.toInt)
        
        // Réinitialiser les styles
        for (row <- 0 until 9; col <- 0 until 9) {
          textFields(row)(col).style = createBorderStyle(row, col)
        }
        
        // Surligner les conflits
        for ((row, col) <- conflicts) {
          textFields(row)(col).style = createBorderStyle(row, col) + " -fx-background-color: #ffff99;"
        }
        
        statusLabel.text = s"${conflicts.length} conflit(s) pour le chiffre $d"
      }
    }
  }
  
  private def highlightErrors(enable: Boolean): Unit = {
    if (enable) {
      validateAll()
    } else {
      updateBoardDisplay()
    }
  }
  
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
          if (board.grid(row)(col) == d.toInt) {
            textFields(row)(col).style = createBorderStyle(row, col) + " -fx-background-color: #99ccff;"
          }
        }
      }
    }
  }
  
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

  private def saveGame(): Unit = {

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

        } catch {
          case e: Exception =>
            statusLabel.text = "Erreur lors du chargement"
        }

      case None =>
        statusLabel.text = "Chargement annulé"
    }
  }
  
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
  
  private def updateBoardDisplay(): Unit = {
    for (row <- 0 until 9; col <- 0 until 9) {
      val field = textFields(row)(col)
      val value = board.grid(row)(col)
      
      field.text = if (value == 0) "" else value.toString
      field.editable = originalBoard.grid(row)(col) == 0
      field.style = createBorderStyle(row, col)
      
      if (!field.editable.value) {  // CORRECTION : ajouter .value
        field.style = field.style.value + " -fx-background-color: #f0f0f0;"
      }
    }
  }
}