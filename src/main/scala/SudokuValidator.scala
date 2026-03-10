object SudokuValidator {

  /**
   * Vérifie si un chiffre peut être placé (contraintes immédiates uniquement)
   */
  def isValid(board: SudokuBoard, row: Int, col: Int, num: Int): Boolean = {
    // Vérifier la ligne
    for (c <- 0 until 9) {
      if (board.grid(row)(c) == num) return false
    }

    for (r <- 0 until 9) {
      if (board.grid(r)(col) == num) return false
    }

    // Vérifier le bloc 3x3
    val startRow = (row / 3) * 3
    val startCol = (col / 3) * 3

    for (r <- startRow until startRow + 3) {
      for (c <- startCol until startCol + 3) {
        if (board.grid(r)(c) == num) return false
      }
    }

    true
  }

  def Unicite(board: SudokuBoard): Boolean = {
    
    var solutions = 0
   
    def recursiveUnicite(board: SudokuBoard): Boolean = {
      
      if (solutions >= 2) {
        println(s"Nombre de solutions trouvées: $solutions")
        return false
      }

      SudokuGenerator.findBestEmptyCell(board) match {

        case None =>
          solutions += 1
          return solutions < 2

        case Some((row, col)) =>

          val candidates = SudokuGenerator.getCandidates(board, row, col)

          for (num <- candidates) {

            board.grid(row)(col) = num

            if (!recursiveUnicite(board)) {
              board.grid(row)(col) = 0
              return false
            }

            board.grid(row)(col) = 0
          }

          true
      }
    }

    recursiveUnicite(board)

    solutions == 1
  }

  /**
   * Vérifie toute la grille et retourne les erreurs
   */
  def validateBoard(board: SudokuBoard): Array[Array[Boolean]] = {
    val errors = Array.ofDim[Boolean](9, 9)
    
    for (row <- 0 until 9; col <- 0 until 9) {
      val num = board.grid(row)(col)
      if (num != 0) {
        board.grid(row)(col) = 0
        if (!isValid(board, row, col, num)) {
          errors(row)(col) = true
        }
        board.grid(row)(col) = num
      }
    }
    
    errors
  }

def findConflicts(board: SudokuBoard, num: Int): List[(Int, Int)] = {

  val conflicts = scala.collection.mutable.ListBuffer[(Int, Int)]()
  
  for (row <- 0 until 9; col <- 0 until 9) {
    if (board.grid(row)(col) == num) {
      board.grid(row)(col) = 0
      if (!isValid(board, row, col, num)) {
        conflicts += ((row, col))
      }
      board.grid(row)(col) = num
    }
  }
  conflicts.toList
}

def solveInPlace(board: SudokuBoard): Boolean = {
    // Trouver la première case vide
    var row = -1
    var col = -1
    var found = false
    
    for (r <- 0 until 9 if !found) {
      for (c <- 0 until 9 if !found) {
        if (board.grid(r)(c) == 0) {
          row = r
          col = c
          found = true
        }
      }
    }
    
    // Si plus de case vide, on a trouvé une solution
    if (!found) return true
    
    // Essayer chaque chiffre possible
    for (num <- 1 to 9) {
      if (isValid(board, row, col, num)) {
        board.grid(row)(col) = num
        
        if (solveInPlace(board)) {
          return true
        }
        
        board.grid(row)(col) = 0
      }
    }
    
    false
  }
  
}