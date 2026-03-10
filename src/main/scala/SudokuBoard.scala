class SudokuBoard {

  val grid: Array[Array[Int]] = Array.ofDim[Int](9,9)

  def get(row:Int,col:Int):Int = grid(row)(col)

  def set(row:Int,col:Int,value:Int):Unit = {
    grid(row)(col) = value
  }

  def clear(row:Int,col:Int):Unit = {
    grid(row)(col) = 0
  }

  def reset():Unit = {
    for(i <- 0 until 9; j <- 0 until 9){
      grid(i)(j) = 0
    }
  }

  def isComplete: Boolean = {
    for (i <- 0 until 9; j <- 0 until 9) {
      if (grid(i)(j) == 0) return false
    }
    true
  }

  def saveToFile(filename: String , name: String = "sudoku_" + System.nanoTime()): Unit = {
    val pw = new java.io.PrintWriter(new java.io.File(filename))
    pw.println(s"sukodu numero : ${name}") // Ligne d'en-tête pour la grille actuelle
    for (i <- 0 until 9) {
      pw.println(grid(i).mkString(","))
    } 
    pw.close()
  }

  def loadFromFile(filename: String): Unit = {
    val lines = scala.io.Source.fromFile(filename).getLines().toArray
    for (i <- 1 until lines.length) { // Commence à 1 pour sauter la ligne d'en-tête
      val values = lines(i).split(",").map(_.toInt)
      for (j <- 0 until 9) {
        grid(i-1)(j) = values(j)
      }
    }
  }

  def copy(): SudokuBoard = {
    val newBoard = new SudokuBoard
    for (i <- 0 until 9; j <- 0 until 9) {
      newBoard.grid(i)(j) = this.grid(i)(j)
    }
    newBoard
  }

}