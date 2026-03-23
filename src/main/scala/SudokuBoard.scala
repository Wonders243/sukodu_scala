class SudokuBoard {

  val grid: Array[Array[Int]] = Array.ofDim[Int](9,9)
  var saved: Boolean = _
  var nameSaved: String = _

  /* 
   * Récupère la valeur d'une cellule donnée.
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   * @return La valeur de la cellule
   */
  def get(row:Int,col:Int):Int = grid(row)(col)

  /* 
   * Définit la valeur d'une cellule donnée.
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   * @param value La valeur à définir dans la cellule
   */
  def set(row:Int,col:Int,value:Int):Unit = {
    grid(row)(col) = value
  }

  /* 
   * Efface la valeur d'une cellule donnée.
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   */
  def clear(row:Int,col:Int):Unit = {
    grid(row)(col) = 0
  }

  /* 
   * Réinitialise le plateau de Sudoku en effaçant toutes les cellules.
   */
  def reset():Unit = {
    for(i <- 0 until 9; j <- 0 until 9){
      grid(i)(j) = 0
    }
  }

  /* 
   * Vérifie si le plateau de Sudoku est complet.
   * @return true si le plateau est complet, false sinon
   */
  def isComplete: Boolean = {
    for (i <- 0 until 9; j <- 0 until 9) {
      if (grid(i)(j) == 0) return false
    }
    true
  }

  /* 
   * Enregistre le plateau de Sudoku dans un fichier.
   * @param filename Le nom du fichier
   * @param name Le nom du puzzle
   */
  def saveToFile(filename: String , name: String = "sudoku_" + System.nanoTime()): Unit = {

    saved = true
    nameSaved = name
    val pw = new java.io.PrintWriter(new java.io.File(filename))
    pw.println(s"sukodu numero : ${name}") // Ligne d'en-tête pour la grille actuelle
    for (i <- 0 until 9) {
      pw.println(grid(i).mkString(","))
    } 
  
    pw.close()
  }

  /* 
   * Charge un plateau de Sudoku à partir d'un fichier.
   * @param filename Le nom du fichier
   */
  def loadFromFile(filename: String): Unit = {
    val lines = scala.io.Source.fromFile(filename).getLines().toArray
    for (i <- 1 until lines.length) { 
      val values = lines(i).split(",").map(_.toInt)
      for (j <- 0 until 9) {
        grid(i-1)(j) = values(j)
      }
    }
  }

  /* 
   * Crée une copie du plateau de Sudoku.
   * @return Une nouvelle instance de SudokuBoard avec les mêmes valeurs
   */
  def copy(): SudokuBoard = {
    val newBoard = new SudokuBoard()
    for (i <- 0 until 9; j <- 0 until 9) {
      newBoard.grid(i)(j) = this.grid(i)(j)
    }
    newBoard
  }

}