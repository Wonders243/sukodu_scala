import java.time.{Duration, Instant}
import scala.collection.mutable

/// Représente l'état actuel du jeu, y compris le plateau de jeu, le plateau original, le temps écoulé, les erreurs et l'historique des mouvements.
case class GameState(
  var Board: SudokuBoard,
  var originalBoard: SudokuBoard,
  var startTime: Instant,
  var errors: Int,
  var moveHistory: mutable.Stack[(Int, Int, Int)] // (row, col, previous value)
){
  // Calcule le temps écoulé depuis le début de la partie.
  def getElapsedTime: Duration = Duration.between(startTime, Instant.now())
  
  /* 
   * Enregistre un mouvement dans l'historique.
   * @param row La ligne de la cellule
   * @param col La colonne de la cellule
   * @param previousValue La valeur précédente
   */
  def saveMove(row: Int, col: Int, previousValue: Int): Unit = {
    moveHistory.push((row, col, previousValue))
  }
  
  /* 
   * Annule le dernier mouvement.
   * @return L'élément annulé ou None s'il n'y a pas de mouvements à annuler
   */
  def undo(): Option[(Int, Int, Int)] = {
    if (moveHistory.nonEmpty) {
      Some(moveHistory.pop())
    } else None
  }
  
  /* 
   * Réinitialise le jeu en copiant les valeurs du plateau original vers le plateau de jeu, en effaçant l'historique des mouvements, en réinitialisant les erreurs et en redémarrant le chronomètre.
   */
  def reset(): Unit = {

    for (i <- 0 until 9; j <- 0 until 9) {
      Board.grid(i)(j) = originalBoard.grid(i)(j)
    }
    moveHistory.clear()
    errors = 0
    startTime = Instant.now()
  }
}