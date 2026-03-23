object SudokuValidator {

  /* 
   * Vérifie si un nombre peut être placé dans une cellule donnée sans violer les règles du Sudoku.
   * Cette méthode est utilisée pour valider les mouvements de l'utilisateur et pour trouver les candidats possibles lors de la génération et de la résolution des puzzles.
   * @param board Le plateau de Sudoku
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   * @param num Le nombre à vérifier
   * @return true si le nombre peut être placé, false sinon
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

  /* 
   * Vérifie si le plateau de Sudoku a une solution unique.
   * Cette méthode est utilisée pour garantir que les puzzles générés ont une solution unique, ce qui est une caractéristique importante pour un bon puzzle de Sudoku.
   * @param board Le plateau de Sudoku à vérifier
   * @return true si le plateau a une solution unique, false sinon
   */
  def Unicite(board: SudokuBoard): Boolean = {
    
    var solutions = 0
   
    /* 
     * Fonction récursive pour compter le nombre de solutions d'un plateau de Sudoku.
     * Cette fonction utilise un algorithme de backtracking similaire à celui utilisé pour résoudre les puzzles, mais elle continue à chercher des solutions même après en avoir trouvé une, afin de déterminer s'il y en a plus d'une.
     * @param board Le plateau de Sudoku à vérifier
     * @return true si le nombre de solutions trouvées est inférieur à 2, false sinon
     */
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

            board.set(row, col, num)

            if (!recursiveUnicite(board)) {
              board.set(row, col, 0)
              return false
            }

            board.set(row, col, 0)
          }

          true
      }
    }

    recursiveUnicite(board)

    solutions == 1
  }

  /* 
   * Valide un plateau de Sudoku en vérifiant chaque cellule pour les conflits potentiels.
   * Cette méthode est utilisée pour fournir un retour d'information à l'utilisateur sur les erreurs dans son plateau, en mettant en évidence les cellules qui contiennent des conflits.
   * @param board Le plateau de Sudoku à valider
   * @return Un tableau 2D de booléens indiquant les cellules en conflit (true) et les cellules valides (false)
   */
  def validateBoard(board: SudokuBoard): Array[Array[Boolean]] = {
    val errors = Array.ofDim[Boolean](9, 9)
    
    for (row <- 0 until 9; col <- 0 until 9) {
      val num = board.get(row, col)
      if (num != 0) {
        board.set(row, col, 0)
        if (!isValid(board, row, col, num)) {
          errors(row)(col) = true
        }
        board.set(row, col, num)
      }
    }
    
    errors
  }

}