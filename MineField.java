// Name: Xinhao Li
// USC NetID: 6820975319
// CS 455 PA3
// Fall 2020

import java.util.Random;
import java.util.Arrays;
/**
   MineField
      class with locations of mines for a game.
      This class is mutable, because we sometimes need to change it once it's created.
      mutators: populateMineField, resetEmpty
      includes convenience method to tell the number of mines adjacent to a location.
 */
public class MineField {

   // Rep invariant:
   //   numMinesOnField = the number of true locations in hasMine() <= numMines
   //   numMinesOnField != numMines only when MineField is constructed with the 3-argument constructor
   //   and has not been called populateMineField()
   private boolean[][] mineField;
   private int numMines;
   private int numMinesOnField;


   /**
      Create a minefield with same dimensions as the given array, and populate it with the mines in the array
      such that if mineData[row][col] is true, then hasMine(row,col) will be true and vice versa.  numMines() for
      this minefield will corresponds to the number of 'true' values in mineData.
      @param mineData  the data for the mines; must have at least one row and one col,
                       and must be rectangular (i.e., every row is the same length)
    */
   public MineField(boolean[][] mineData) {
      mineField = new boolean[mineData.length][mineData[0].length];
      for (int i = 0; i < mineData.length; i++) {
         for (int j = 0; j < mineData[0].length; j++) {
            mineField[i][j] = mineData[i][j];
         }
      }
      numMinesOnField = getNumMinesOnField();
      numMines = numMinesOnField;
   }


   /**
      Create an empty minefield (i.e. no mines anywhere), that may later have numMines mines (once
      populateMineField is called on this object).  Until populateMineField is called on such a MineField,
      numMines() will not correspond to the number of mines currently in the MineField. (will correspond to
      the expected number of mines in the MineField)
      @param numRows  number of rows this minefield will have, must be positive
      @param numCols  number of columns this minefield will have, must be positive
      @param numMines   number of mines this minefield will have,  once we populate it.
      PRE: numRows > 0 and numCols > 0 and 0 <= numMines < (1/3 of total number of field locations).
    */
   public MineField(int numRows, int numCols, int numMines) {
      mineField = new boolean[numRows][numCols];
      this.numMines = numMines;
      numMinesOnField = 0;
   }


   /**
      Removes any current mines on the minefield, and puts numMines() mines in random locations on the minefield,
      ensuring that no mine is placed at (row, col).
      @param row the row of the location to avoid placing a mine
      @param col the column of the location to avoid placing a mine
      PRE: inRange(row, col)
    */
   public void populateMineField(int row, int col) {
      resetEmpty();
      while(numMinesOnField < numMines){
         numMinesOnField = randomSetMine(numRows(), numCols(), numMinesOnField);
         if (mineField[row][col] == true){
            mineField[row][col] = false;
            numMinesOnField--;
         }
      }
   }


   /**
      Reset the minefield to all empty squares.  This does not affect numMines(), numRows() or numCols()
      Thus, after this call, the actual number of mines in the minefield does not match numMines().
      Note: This is the state a minefield created with the three-arg constructor is in
         at the beginning of a game.
    */
   public void resetEmpty() {
      mineField = new boolean[numRows()][numCols()];
      numMinesOnField = 0;
   }


  /**
     Returns the number of mines adjacent to the specified mine location (not counting a possible
     mine at (row, col) itself).
     Diagonals are also considered adjacent, so the return value will be in the range [0,8]
     @param row  row of the location to check
     @param col  column of the location to check
     @return  the number of mines adjacent to the square at (row, col)
     PRE: inRange(row, col)
   */
   public int numAdjacentMines(int row, int col) {
      if (row == 0) {
         if (col == 0) {return numAdjacentMinesTopLeft(row, col);}
         else if (col == numCols() - 1) {return numAdjacentMinesTopRight(row, col);}
         return numAdjacentMinesTop(row, col);
      }
      else if (row == numRows() - 1) {
         if (col == 0) {return numAdjacentMinesBotLeft(row, col);}
         else if (col == numCols() - 1) {return numAdjacentMinesBotRight(row, col);}
         return numAdjacentMinesBot(row, col);
      }
      else if (col == 0) {return numAdjacentMinesLeft(row, col);}
      else if (col == numCols() - 1) {return numAdjacentMinesRight(row, col);}
      return numAdjacentMinesMain(row,col);
   }


   /**
      Returns true iff (row,col) is a valid field location.  Row numbers and column numbers
      start from 0.
      @param row  row of the location to consider
      @param col  column of the location to consider
      @return whether (row, col) is a valid field location
   */
   public boolean inRange(int row, int col) {
      return (row < numRows() && row >= 0 && col < numCols() && col >= 0);
   }


   /**
      Returns the number of rows in the field.
      @return number of rows in the field
   */
   public int numRows() {
      return mineField.length;
   }


   /**
      Returns the number of columns in the field.
      @return number of columns in the field
   */
   public int numCols() {
      return mineField[0].length;
   }


   /**
      Returns whether there is a mine in this square
      @param row  row of the location to check
      @param col  column of the location to check
      @return whether there is a mine in this square
      PRE: inRange(row, col)
   */
   public boolean hasMine(int row, int col) {
      return mineField[row][col];
   }


   /**
      Returns the number of mines you can have in this minefield.  For mines created with the 3-arg constructor,
      some of the time this value does not match the actual number of mines currently on the field.  See doc for that
      constructor, resetEmpty, and populateMineField for more details.
    * @return
    */
   public int numMines() {
      return numMines;
   }


   // <put private methods here>

   /**
      this method randomly finds a location in mineField and put a mine there by setting it to true in the mineField[][].
      @param row  row of the mineField.
      @param col  column of the mineField.
      @param currentNumMines  the number of mines currently on field
    */
   private int randomSetMine(int row, int col, int currentNumMines){
      Random rand = new Random();
      int randRow = rand.nextInt(row);
      int randCol = rand.nextInt(col);
      if (mineField[randRow][randCol] == true) {return currentNumMines;}
      mineField[randRow][randCol] = true;
      return ++currentNumMines;
   }

   /**
      Returns the actual number of mines on the mineField.
      @return
    */
   private int getNumMinesOnField(){
      int numMinesOnField = 0;
      for (int i = 0; i < numRows(); i++) {
         for (int j = 0; j < numCols(); j++) {
            if (mineField[i][j]) { numMinesOnField ++;}
         }
      }
      return numMinesOnField;
   }

   /**
    */
   private int numAdjacentMinesTopLeft(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = 0; i <= 1; i++) {
         for (int j = 0; j <= 1; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }

   private int numAdjacentMinesTopRight(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = 0; i <= 1; i++) {
         for (int j = -1; j <= 0; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }

   private int numAdjacentMinesTop(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = 0; i <= 1; i++) {
         for (int j = -1; j <= 1; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }

   private int numAdjacentMinesBotLeft(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = -1; i <= 0; i++) {
         for (int j = 0; j <= 1; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }

   private int numAdjacentMinesBotRight(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = -1; i <= 0; i++) {
         for (int j = -1; j <= 0; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }

   private int numAdjacentMinesBot(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = -1; i <= 0; i++) {
         for (int j = -1; j <= 1; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }

   private int numAdjacentMinesLeft(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = -1; i <= 1; i++) {
         for (int j = 0; j <= 1; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }

   private int numAdjacentMinesRight(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 0; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }

   private int numAdjacentMinesMain(int row, int col) {
      int numAdjacentMines = 0;
      for (int i = -1; i <= 1; i++) {
         for (int j = -1; j <= 1; j++) {
            if (mineField[row + i][col + j]) {numAdjacentMines ++;}
         }
      }
      if (mineField[row][col]) {numAdjacentMines --;}
      return numAdjacentMines;
   }
}
