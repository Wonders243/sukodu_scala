import java.time.{Duration, Instant}
import scala.collection.mutable

// Note: tous les paramètres sont maintenant des var pour pouvoir les modifier
case class GameState(
  var Board: SudokuBoard,
  var originalBoard: SudokuBoard,
  var startTime: Instant,
  var errors: Int,
  var moveHistory: mutable.Stack[(Int, Int, Int)] // (row, col, previous value)
){
  def getElapsedTime: Duration = Duration.between(startTime, Instant.now())
  
  def saveMove(row: Int, col: Int, previousValue: Int): Unit = {
    moveHistory.push((row, col, previousValue))
  }
  
  def undo(): Option[(Int, Int, Int)] = {
    if (moveHistory.nonEmpty) {
      Some(moveHistory.pop())
    } else None
  }
  
  def reset(): Unit = {
    // Copier les valeurs de originalPlateau vers Board

    for (i <- 0 until 9; j <- 0 until 9) {
      Board.grid(i)(j) = originalBoard.grid(i)(j)
    }
    moveHistory.clear()
    errors = 0
    startTime = Instant.now()
  }
}