import scala.util.Random
import scala.collection.mutable.ListBuffer

object SudokuGenerator {
  
  // Générer une grille complète aléatoire
  def generateCompleteBoard(board: SudokuBoard): Unit = {
    board.reset()
    solveWithBacktracking(board, shuffle = true)
  }
  

  // Solveur avec backtracking 
  private def solveWithBacktracking(board: SudokuBoard, shuffle: Boolean): Boolean = {

    findBestEmptyCell(board) match {

      case None => return true

      case Some((row, col)) =>
        // Obtenir les candidats possibles pour cette cellule
        val candidates = getCandidates(board, row, col)
        
        val candidatesToTry = if (shuffle) scala.util.Random.shuffle(candidates) else candidates 
        
        for (num <- candidatesToTry) {
          board.grid(row)(col) = num
          
          if (solveWithBacktracking(board, shuffle)) {
            return true
          }
          
          board.grid(row)(col) = 0 
        }
        
        false 
    }
  }
  
  def findBestEmptyCell(board: SudokuBoard): Option[(Int, Int)] = {
    var bestRow = -1
    var bestCol = -1
    var maxCandidates = 10 
    
    for (row <- 0 until 9; col <- 0 until 9 if board.grid(row)(col) == 0) {

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
  

   // Compte combien de chiffres peuvent être placés dans une cellule
  private def countCandidates(board: SudokuBoard, row: Int, col: Int): Int = {
    var count = 0
    for (num <- 1 to 9) {
      if (SudokuValidator.isValid(board, row, col, num)) {
        count += 1
      }
    }
    count
  }

   // Récupère la liste des candidats valides
  def getCandidates(board: SudokuBoard, row: Int, col: Int): List[Int] = {
    (1 to 9).filter(num => SudokuValidator.isValid(board, row, col, num)).toList
  }

  /**
   * Créer un puzzle à partir d'une grille complète
   * Garantit que la solution est UNIQUE
   */
  def createPuzzle(board: SudokuBoard, emptyCells: Int): Unit = {
    // Sauvegarder la solution complète
    val solution = board.copy()
    
    // Créer liste des cellules et mélanger
    val cells = for {
      i <- 0 until 9
      j <- 0 until 9
    } yield (i, j)
    
    // Mélanger plusieurs fois pour plus d'aléatoire
    val shuffled = Random.shuffle(Random.shuffle(cells.toList))
    
    var removed = 0
    val maxEmpty = math.min(emptyCells, 70) // Limite pour éviter les puzzles trop vides
    
    for ((row, col) <- shuffled if removed < maxEmpty) {
      val temp = board.grid(row)(col)
      board.grid(row)(col) = 0
      
      // Vérifier que la solution reste UNIQUE
      if (!SudokuValidator.Unicite(board)) {
        board.grid(row)(col) = temp
      } else {
        removed += 1
      }
    }
    
    println(s"Puzzle cree avec $removed cases vides")
  }
  
  /**
   * Générer un puzzle complet avec vérification d'unicité
   */
  
  
  def getSolution(puzzle: SudokuBoard): Option[SudokuBoard] = {
    val solution = puzzle.copy()
    if (SudokuValidator.solveInPlace(solution)) {
      Some(solution)
    } else {
      None
    }
  }
  def getPreGeneratedPuzzles(numberOfPuzzles: Int, difficulty: Int = 40): List[SudokuBoard] = {

  val puzzles = ListBuffer[SudokuBoard]()

  for (_ <- 1 to numberOfPuzzles) {
    
    val board = new SudokuBoard()
    
    // Générer un puzzle
    generatePuzzle(board, difficulty)
    
    // Ajouter une copie pour éviter les modifications futures
    puzzles += board.copy()
  }

  puzzles.toList
}

  def generetaDefaultPuzzle(): SudokuBoard = {
    val board = new SudokuBoard()
    getPreGeneratedPuzzles(1, 40).head // Difficulté par défaut
  }

  def generatePuzzle(board: SudokuBoard, emptyCells: Int): Unit = {
    generateCompleteBoard(board)
    createPuzzle(board, emptyCells )
  }
}