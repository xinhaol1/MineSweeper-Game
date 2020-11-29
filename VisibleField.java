// Name: Xinhao Li
// USC NetID: 6820975319
// CS 455 PA3
// Fall 2020

import java.util.ArrayList;
/**
  VisibleField class
  This is the data that's being displayed at any one point in the game (i.e., visible field, because it's what the
  user can see about the minefield), Client can call getStatus(row, col) for any square.
  It actually has data about the whole current state of the game, including
  the underlying mine field (getMineField()).  Other accessors related to game status: numMinesLeft(), isGameOver().
  It also has mutators related to actions the player could do (resetGameDisplay(), cycleGuess(), uncover()),
  and changes the game state accordingly.

  It, along with the MineField (accessible in mineField instance variable), forms
  the Model for the game application, whereas GameBoardPanel is the View and Controller, in the MVC design pattern.
  It contains the MineField that it's partially displaying.  That MineField can be accessed (or modified) from
  outside this class via the getMineField accessor.
 */
public class VisibleField {
   // ----------------------------------------------------------
   // The following public constants (plus numbers mentioned in comments below) are the possible states of one
   // location (a "square") in the visible field (all are values that can be returned by public method
   // getStatus(row, col)).

   // Covered states (all negative values):
   public static final int COVERED = -1;   // initial value of all squares
   public static final int MINE_GUESS = -2;
   public static final int QUESTION = -3;

   // Uncovered states (all non-negative values):

   // values in the range [0,8] corresponds to number of mines adjacent to this square

   public static final int MINE = 9;      // this loc is a mine that hasn't been guessed already (end of losing game)
   public static final int INCORRECT_GUESS = 10;  // is displayed a specific way at the end of losing game
   public static final int EXPLODED_MINE = 11;   // the one you uncovered by mistake (that caused you to lose)
   // ----------------------------------------------------------

   // Rep Invariant:
   //   visibleField has the same dimensions as MineField
   //   visibleField[i][j] can take any value between COVERED to EXPLODED_MINE for i,j in inRange
   private MineField mineField;
   private int[][] visibleField;
   private boolean gameOver;
   private int numMinesLeft;

   /**
      Create a visible field that has the given underlying mineField.
      The initial state will have all the mines covered up, no mines guessed, and the game
      not over.
      @param mineField  the minefield to use for for this VisibleField
    */
   public VisibleField(MineField mineField) {
      this.mineField = mineField;
      visibleField = new int[mineField.numRows()][mineField.numCols()];
      resetGameDisplay();
   }


   /**
      Reset the object to its initial state (see constructor comments), using the same underlying
      MineField.
   */
   public void resetGameDisplay() {
      for (int i = 0; i < mineField.numRows(); i++) {
         for (int j = 0; j < mineField.numCols(); j++) {
            visibleField[i][j] = COVERED;
         }
      }
      gameOver = false;
      numMinesLeft = mineField.numMines();
   }


   /**
      Returns a reference to the mineField that this VisibleField "covers"
      @return the minefield
    */
   public MineField getMineField() {
      return this.mineField;
   }


   /**
      Returns the visible status of the square indicated.
      @param row  row of the square
      @param col  col of the square
      @return the status of the square at location (row, col).  See the public constants at the beginning of the class
      for the possible values that may be returned, and their meanings.
      PRE: getMineField().inRange(row, col)
    */
   public int getStatus(int row, int col) {
      assert getMineField().inRange(row, col);
      return visibleField[row][col];
   }


   /**
      Returns the the number of mines left to guess.  This has nothing to do with whether the mines guessed are correct
      or not.  Just gives the user an indication of how many more mines the user might want to guess.  This value can
      be negative, if they have guessed more than the number of mines in the minefield.
      @return the number of mines left to guess.
    */
   public int numMinesLeft() {
      return numMinesLeft;
   }


   /**
      Cycles through covered states for a square, updating number of guesses as necessary.  Call on a COVERED square
      changes its status to MINE_GUESS; call on a MINE_GUESS square changes it to QUESTION;  call on a QUESTION square
      changes it to COVERED again; call on an uncovered square has no effect.
      @param row  row of the square
      @param col  col of the square
      PRE: getMineField().inRange(row, col)
    */
   public void cycleGuess(int row, int col) {
      assert !isGameOver();
      assert getMineField().inRange(row, col);
      if (visibleField[row][col] == COVERED) {
         visibleField[row][col] = MINE_GUESS;
         numMinesLeft --;
      }
      else if (visibleField[row][col] == MINE_GUESS) {
         visibleField[row][col] = QUESTION;
         numMinesLeft ++;
      }
      else if (visibleField[row][col] == QUESTION) {visibleField[row][col] = COVERED;}
   }


   /**
      Uncovers this square and returns false iff you uncover a mine here.
      If the square wasn't a mine or adjacent to a mine it also uncovers all the squares in
      the neighboring area that are also not next to any mines, possibly uncovering a large region.
      Any mine-adjacent squares you reach will also be uncovered, and form
      (possibly along with parts of the edge of the whole field) the boundary of this region.
      Does not uncover, or keep searching through, squares that have the status MINE_GUESS.
      Note: this action may cause the game to end: either in a win (opened all the non-mine squares)
      or a loss (opened a mine).
      @param row  of the square
      @param col  of the square
      @return false   iff you uncover a mine at (row, col)
      PRE: getMineField().inRange(row, col)
    */
   public boolean uncover(int row, int col) {
      assert !isGameOver();
      assert getMineField().inRange(row, col);
      if (mineField.hasMine(row, col)) {
         visibleField[row][col] = EXPLODED_MINE;
         numMinesLeft --;
         gameOver = true;
         updateFailStatus();
         return false;
      }
      visibleField[row][col] = mineField.numAdjacentMines(row, col);
      ArrayList<Integer> rowList = new ArrayList<Integer>();
      ArrayList<Integer> colList = new ArrayList<Integer>();
      rowList.add(row);
      colList.add(col);
      chainUncover(rowList, colList);
      gameOverCheck();
      if (isGameOver()) {updateWinStatus();}
      return true;
   }


   /**
      Returns whether the game is over.
      (Note: This is not a mutator.)
      @return whether game over
    */
   public boolean isGameOver() {
      return gameOver;
   }


   /**
      Returns whether this square has been uncovered.  (i.e., is in any one of the uncovered states,
      vs. any one of the covered states).
      @param row of the square
      @param col of the square
      @return whether the square is uncovered
      PRE: getMineField().inRange(row, col)
    */
   public boolean isUncovered(int row, int col) {
      assert getMineField().inRange(row, col);
      return (visibleField[row][col] >= 0) ;
   }


   // <put private methods here>\

   /**
      Checks if every non-mine location has been uncovered.
      If yes, then the game is over and gameOver = true; if not, then does not do anything.
    */
   private void gameOverCheck() {
      for (int i = 0; i < mineField.numRows(); i++) {
         for (int j = 0; j < mineField.numCols(); j++) {
            if (!mineField.hasMine(i, j) && !isUncovered(i, j)) {return;}
         }
      }
      gameOver = true;
   }

   /**
      Change the look of some squares when the game ended with failure.
      Change mines that have not been uncovered to MINE, and non-mines that have been labeled as MINE_GUESS to INCORRECT_GUESS.
    */
   private void updateFailStatus() {
      for (int i = 0; i < mineField.numRows(); i++) {
         for (int j = 0; j < mineField.numCols(); j++) {
            if (mineField.hasMine(i, j) && visibleField[i][j] != MINE_GUESS && !isUncovered(i, j)) {visibleField[i][j] = MINE;}
            else if (!mineField.hasMine(i, j) && visibleField[i][j] == MINE_GUESS) {visibleField[i][j] = INCORRECT_GUESS;}
         }
      }
   }

   /**
      Change the look of some squares when the game ended with success.
      Change all mines that have not been uncovered to MINE_GUESS.
    */
   private void updateWinStatus() {
      for (int i = 0; i < mineField.numRows(); i++) {
         for (int j = 0; j < mineField.numCols(); j++) {
            if (mineField.hasMine(i, j) && !isUncovered(i, j)) {visibleField[i][j] = MINE_GUESS;}
         }
      }
   }

   /**
      if the square currently being uncovered is non-mine, and it does not have neighboring mines, open all squares connected to this square that do not have neighboring mines recursively.
      @param rowList is the queue that keeps track of the row values of the unchecked neighboring squares
      @param colList is the queue that keeps track of the column values of the unchecked neighboring squares
    */
   private void chainUncover(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      if (rowList.isEmpty() && colList.isEmpty()) {return;}
      else {
         int currentRow = rowList.get(0);
         int currentCol = colList.get(0);
         if (visibleField[currentRow][currentCol] == 0) {
            if (currentRow == 0) {// Top
               if (currentCol == 0) {addNearbySquaresToQueueForTopLeft(rowList, colList);} // TopLeft corner
               else if (currentCol == mineField.numCols() - 1) {addNearbySquaresToQueueForTopRight(rowList, colList);} // TopRight corner
               else {addNearbySquaresToQueueForTop(rowList, colList);} // Top
            }
            else if (currentRow == mineField.numCols() - 1) { // Bottom
               if (currentCol == 0) {addNearbySquaresToQueueForBotLeft(rowList, colList);} // BotLeft corner
               else if (currentCol == mineField.numCols() - 1) {addNearbySquaresToQueueForBotRight(rowList, colList);} // BotRight corner
               else {addNearbySquaresToQueueForBot(rowList, colList);} // Bot
            }
            else if (currentCol == 0) {addNearbySquaresToQueueForLeft(rowList, colList);} // Left
            else if (currentCol == mineField.numCols() - 1) {addNearbySquaresToQueueForRight(rowList, colList);} // Right
            else {addNearbySquaresToQueueForMain(rowList, colList);} // main body
         }
         else {
            rowList.remove(0);
            colList.remove(0);
         }
         chainUncover(rowList, colList);
      }
   }

   /**
      The helper methods below are a series of methods that apply flood fill to squares in different locations (in the corner, on the edge, etc)
    */
   private void addNearbySquaresToQueueForTopLeft(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int j = 0; j <= 1; j++) {
         if (!mineField.hasMine(currentRow + 1, currentCol + j) && !isUncovered(currentRow + 1, currentCol + j)) {
            visibleField[currentRow + 1][currentCol + j] = mineField.numAdjacentMines(currentRow + 1, currentCol + j);
            rowList.add(currentRow + 1);
            colList.add(currentCol + j);
         }
      }
      if (!mineField.hasMine(currentRow, currentCol + 1) && !isUncovered(currentRow, currentCol + 1)) {
         visibleField[currentRow][currentCol + 1] = mineField.numAdjacentMines(currentRow, currentCol + 1);
         rowList.add(currentRow);
         colList.add(currentCol + 1);
      }
   }

   private void addNearbySquaresToQueueForTopRight(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int j = -1; j <= 0; j++) {
         if (!mineField.hasMine(currentRow + 1, currentCol + j) && !isUncovered(currentRow + 1, currentCol + j)) {
            visibleField[currentRow + 1][currentCol + j] = mineField.numAdjacentMines(currentRow + 1, currentCol + j);
            rowList.add(currentRow + 1);
            colList.add(currentCol + j);
         }
      }
      if (!mineField.hasMine(currentRow, currentCol - 1) && !isUncovered(currentRow, currentCol - 1)) {
         visibleField[currentRow][currentCol - 1] = mineField.numAdjacentMines(currentRow, currentCol - 1);
         rowList.add(currentRow);
         colList.add(currentCol - 1);
      }
   }

   private void addNearbySquaresToQueueForTop(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int j = -1; j <= 1; j++) {
         if (!mineField.hasMine(currentRow + 1, currentCol + j) && !isUncovered(currentRow + 1, currentCol + j)) {
            visibleField[currentRow + 1][currentCol + j] = mineField.numAdjacentMines(currentRow + 1, currentCol + j);
            rowList.add(currentRow + 1);
            colList.add(currentCol + j);
         }
      }
      for (int j = -1; j <= 1; j+=2) {
         if (!mineField.hasMine(currentRow, currentCol + j) && !isUncovered(currentRow, currentCol + j)) {
            visibleField[currentRow][currentCol + j] = mineField.numAdjacentMines(currentRow, currentCol + j);
            rowList.add(currentRow);
            colList.add(currentCol + j);
         }
      }
   }

   private void addNearbySquaresToQueueForBotLeft(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int j = 0; j <= 1; j++) {
         if (!mineField.hasMine(currentRow - 1, currentCol + j) && !isUncovered(currentRow - 1, currentCol + j)) {
            visibleField[currentRow - 1][currentCol + j] = mineField.numAdjacentMines(currentRow - 1, currentCol + j);
            rowList.add(currentRow - 1);
            colList.add(currentCol + j);
         }
      }
      if (!mineField.hasMine(currentRow, currentCol + 1) && !isUncovered(currentRow, currentCol + 1)) {
         visibleField[currentRow][currentCol + 1] = mineField.numAdjacentMines(currentRow, currentCol + 1);
         rowList.add(currentRow);
         colList.add(currentCol + 1);
      }
   }

   private void addNearbySquaresToQueueForBotRight(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int j = -1; j <= 0; j++) {
         if (!mineField.hasMine(currentRow - 1, currentCol + j) && !isUncovered(currentRow - 1, currentCol + j)) {
            visibleField[currentRow - 1][currentCol + j] = mineField.numAdjacentMines(currentRow - 1, currentCol + j);
            rowList.add(currentRow - 1);
            colList.add(currentCol + j);
         }
      }
      if (!mineField.hasMine(currentRow, currentCol - 1) && !isUncovered(currentRow, currentCol - 1)) {
         visibleField[currentRow][currentCol - 1] = mineField.numAdjacentMines(currentRow, currentCol - 1);
         rowList.add(currentRow);
         colList.add(currentCol - 1);
      }
   }

   private void addNearbySquaresToQueueForBot(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int j = -1; j <= 1; j++) {
         if (!mineField.hasMine(currentRow - 1, currentCol + j) && !isUncovered(currentRow - 1, currentCol + j)) {
            visibleField[currentRow - 1][currentCol + j] = mineField.numAdjacentMines(currentRow - 1, currentCol + j);
            rowList.add(currentRow - 1);
            colList.add(currentCol + j);
         }
      }
      for (int j = -1; j <= 1; j+=2) {
         if (!mineField.hasMine(currentRow, currentCol + j) && !isUncovered(currentRow, currentCol + j)) {
            visibleField[currentRow][currentCol + j] = mineField.numAdjacentMines(currentRow, currentCol + j);
            rowList.add(currentRow);
            colList.add(currentCol + j);
         }
      }
   }

   private void addNearbySquaresToQueueForLeft(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int i = -1; i <= 1; i++) {
         if (!mineField.hasMine(currentRow + i, currentCol + 1) && !isUncovered(currentRow + i, currentCol + 1)) {
            visibleField[currentRow + i][currentCol + 1] = mineField.numAdjacentMines(currentRow + i, currentCol + 1);
            rowList.add(currentRow + i);
            colList.add(currentCol + 1);
         }
      }
      for (int i = -1; i <= 1; i+=2) {
         if (!mineField.hasMine(currentRow + i, currentCol) && !isUncovered(currentRow + i, currentCol)) {
            visibleField[currentRow + i][currentCol] = mineField.numAdjacentMines(currentRow + i, currentCol);
            rowList.add(currentRow + i);
            colList.add(currentCol);
         }
      }
   }

   private void addNearbySquaresToQueueForRight(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int i = -1; i <= 1; i++) {
         if (!mineField.hasMine(currentRow + i, currentCol - 1) && !isUncovered(currentRow + i, currentCol - 1)) {
            visibleField[currentRow + i][currentCol - 1] = mineField.numAdjacentMines(currentRow + i, currentCol - 1);
            rowList.add(currentRow + i);
            colList.add(currentCol - 1);
         }
      }
      for (int i = -1; i <= 1; i+=2) {
         if (!mineField.hasMine(currentRow + i, currentCol) && !isUncovered(currentRow + i, currentCol)) {
            visibleField[currentRow + i][currentCol] = mineField.numAdjacentMines(currentRow + i, currentCol);
            rowList.add(currentRow + i);
            colList.add(currentCol);
         }
      }
   }

   private void addNearbySquaresToQueueForMain(ArrayList<Integer> rowList, ArrayList<Integer> colList) {
      int currentRow = rowList.remove(0);
      int currentCol = colList.remove(0);
      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 1; j+=2) {
            if (!mineField.hasMine(currentRow + i, currentCol + j) && !isUncovered(currentRow + i, currentCol + j)) {
               visibleField[currentRow + i][currentCol + j] = mineField.numAdjacentMines(currentRow + i, currentCol + j);
               rowList.add(currentRow + i);
               colList.add(currentCol + j);
            }
         }
      }
      for (int i = -1; i <= 1; i+=2) {
         if (!mineField.hasMine(currentRow + i, currentCol) && !isUncovered(currentRow + i, currentCol)) {
            visibleField[currentRow + i][currentCol] = mineField.numAdjacentMines(currentRow + i, currentCol);
            rowList.add(currentRow + i);
            colList.add(currentCol);
         }
      }
   }


}
