import scala.util.Random
import scala.collection.mutable.ListBuffer

object SudokuGenerator {
  
  // Générer une grille complète aléatoire
  def generateCompleteBoard(board: SudokuBoard, reset: Boolean = true): Unit = {
    if (reset) board.reset()
    solveWithBacktracking(board, shuffle = true)
  }
  

  /* 
   * Résoudre le Sudoku avec backtracking
   * L'option shuffle permet de mélanger les candidats à chaque étape pour générer des grilles différentes à chaque exécution.
   * @param board Le plateau de Sudoku
   * @param shuffle Mélanger les candidats
   * @return true si la grille est résolue, false sinon
   */
  private def solveWithBacktracking(board: SudokuBoard, shuffle: Boolean): Boolean = {

    findBestEmptyCell(board) match {

      case None => return true

      case Some((row, col)) =>
        // Obtenir les candidats possibles pour cette cellule
        val candidates = getCandidates(board, row, col)
        
        val candidatesToTry = if (shuffle) scala.util.Random.shuffle(candidates) else candidates 
        
        for (num <- candidatesToTry) {
          board.set(row, col, num)
          
          if (solveWithBacktracking(board, shuffle)) {
            return true
          }
          
          board.set(row, col, 0)
        }
        
        false 
    }
  }
  
  /* 
    * Trouver la cellule vide avec le moins de candidats possibles
    * Retourne None si aucune cellule vide n'est trouvée
    * Cette méthode est cruciale pour optimiser le backtracking, car elle réduit le nombre de choix à explorer à chaque étape, ce qui accélère considérablement la génération de grilles complètes.
    * @param board Le plateau de Sudoku
    * @return Option[(Int, Int)] La position de la cellule vide avec le moins de candidats, ou None si aucune cellule vide n'est trouvée
   */
  def findBestEmptyCell(board: SudokuBoard): Option[(Int, Int)] = {
    var bestRow = -1
    var bestCol = -1
    var maxCandidates = 10 
    
    for (row <- 0 until 9; col <- 0 until 9 if board.get(row, col) == 0) {

      val candidatesCount = countCandidates(board, row, col)
      
      // Si on trouve une cellule avec 0 candidats, on peut arrêter immédiatement
      if (candidatesCount == 0) {
        return Some((row, col)) 
      }
      
      if (candidatesCount < maxCandidates) {
        maxCandidates = candidatesCount
        bestRow = row
        bestCol = col
        
        // Si on trouve une cellule avec 1 seul candidat, c'est le meilleur cas possible
        if (maxCandidates == 1) {
          return Some((bestRow, bestCol))
        }
      }
    }
    
    if (bestRow == -1) None else Some((bestRow, bestCol))
  }
  

  /* 
    * Compter le nombre de candidats possibles pour une cellule donnée
    * Cette méthode est utilisée pour trouver la cellule avec le moins de candidats possibles, ce qui optimise le processus de backtracking.
    * @param board Le plateau de Sudoku
    * @param row La ligne de la cellule
    * @param col La colonne de la cellule
    * @return Le nombre de candidats possibles pour cette cellule
   */
  private def countCandidates(board: SudokuBoard, row: Int, col: Int): Int = {
    var count = 0
    for (num <- 1 to 9) {
      if (SudokuValidator.isValid(board, row, col, num)) {
        count += 1
      }
    }
    count
  }

  /* 
   * Obtenir la liste des candidats possibles pour une cellule donnée
   * @param board Le plateau de Sudoku
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   * @return La liste des candidats possibles pour cette cellule
   */
  def getCandidates(board: SudokuBoard, row: Int, col: Int): List[Int] = {
    (1 to 9).filter(num => SudokuValidator.isValid(board, row, col, num)).toList
  }

  /* 
   * Créer un puzzle en supprimant des cellules d'une grille complète
   * L'option emptyCells permet de contrôler le nombre de cellules vides dans le puzzle final, ce qui influence la difficulté du puzzle.
   * @param board Le plateau de Sudoku complet
   * @param emptyCells Le nombre de cellules à vider pour créer le puzzle
   */
  def createPuzzle(board: SudokuBoard, emptyCells: Int): Unit = {
    
    // liste des cellules 
    val cells = for {
      i <- 0 until 9
      j <- 0 until 9
    } yield (i, j)
    
    // Mélangé pour plus d'aléatoire
    val shuffled = Random.shuffle(Random.shuffle(cells.toList))
    
    var removed = 0
    val maxEmpty = math.min(emptyCells, 70) // Limite pour éviter les puzzles trop vides
    
    for ((row, col) <- shuffled if removed < maxEmpty) {

      val temp = board.get(row, col)
      board.set(row, col, 0)
      
      // Vérifier que la solution reste UNIQUE
      if (!SudokuValidator.Unicite(board)) {
        board.set(row, col, temp)
      } else {
        removed += 1
      }
    }
    
    println(s"Puzzle créé avec $removed cases vides")
  }

  /* 
   * Générer une liste de puzzles pré-générés
   * Cette méthode peut être utilisée pour fournir des puzzles prêts à l'emploi sans avoir à les générer à la volée, ce qui utile pour offrir une sélection de puzzles à l'utilisateur.
   * @param numberOfPuzzles Le nombre de puzzles à générer
   * @param difficulty Le nombre de cellules vides dans chaque puzzle, ce qui influence la difficulté (par défaut 40)
   * @return La liste des puzzles générés
   */
  def getPreGeneratedPuzzles(numberOfPuzzles: Int, difficulty: Int = 40): List[SudokuBoard] = {

  val puzzles = ListBuffer[SudokuBoard]()

  for (_ <- 1 to numberOfPuzzles) {
    
    val board = new SudokuBoard()
    generatePuzzle(board, difficulty)
    puzzles += board.copy()
  }
  puzzles.toList
  }

  /* 
   * Générer un puzzle de Sudoku avec un nombre spécifique de cellules vides
   * Cette méthode permet de créer des puzzles de différentes difficultés en contrôlant le nombre de cellules vides.
   * @param emptyCells Le nombre de cellules vides dans le puzzle
   * @return Un puzzle de Sudoku pré-généré avec le nombre spécifié de cellules vides
   */
  def generatePuzzle(board: SudokuBoard, emptyCells: Int): Unit = {
    generateCompleteBoard(board)
    createPuzzle(board, emptyCells )
  }

  /* 
   * Remplir toutes les occurrences d'un nombre donné dans une grille de Sudoku.
   * @param board Le plateau de Sudoku
   * @param num Le nombre à remplir dans la cellule
   */
  def fillNumber(board: SudokuBoard, num: Int): Unit = {

    val copyboard = board.copy()
    solveWithBacktracking(copyboard, shuffle = false)
    for (i <- 0 until 9; j <- 0 until 9) {
      if (copyboard.grid(i)(j) == num && board.grid(i)(j) == 0) {
        board.grid(i)(j) = num
      }
    }
  }
}