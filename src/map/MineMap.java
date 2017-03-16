package map;

/* Copyright (C) 1995 and 1997 John D. Ramsdell

This file is part of Programmer's Minesweeper (PGMS).

PGMS is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

PGMS is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PGMS; see the file COPYING.  If not, write to
the Free Software Foundation, 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.
*/

/**
 * The class Map implements a mine map.  A strategy operates on a mine
 * map. By invoking the operations of probing and marking, the strategy
 * attempts to place the mine map in a state in which every cell that does
 * not contain a mine has been probed, without probing a cell that does
 * contain a mine.
 * @see Strategy
 * @version February 1997
 * @author John D. Ramsdell
 */
public class MineMap implements Map {

  int mmm;			// Mines minus marks
  int r;			// Rows
  int c;			// Columns

  /* mine_map[y][x] = -1, if cell (x, y) contains a mine or
   *                   n, where n is the number of mines in adjacent cells.
   */
  int mine_map[][];

  /* mark_map[y][x] = true when cell (x, y) is marked */
  boolean mark_map[][];

  /* unprobed_map[y][x] = true when cell (x, y) is not probed
   * The code maintains the following relation:
   * mark_map[y][x] == true implies unprobed_map[y][x] == true
   */
  boolean unprobed_map[][];

  private MineMap() { }

  /**
   * Create a mine map.
   * @param mines	number of mines in mine map
   * @param rows        rows in map
   * @param columns     columns in map
   * @return 		a mine map
   */
  MineMap(int mines, int rows, int columns) {
    mmm = mines;
    r = rows;
    c = columns;

    mine_map = new int[r][c];
    mark_map = new boolean[r][c];
    unprobed_map = new boolean[r][c];

    for (int y = 0; y < r; y++)
      for (int x = 0; x < c; x++) {
	mine_map[y][x] = 0;
	mark_map[y][x] = false;
	unprobed_map[y][x] = true;
      }

    if (mines / 2 >= r * c)	// Odd parameters
      finished = true;		// Just punt
    else {
      for (int k = mines; k > 0;) { // Place mines randomly
	int x = pick(c);
	int y = pick(r);
	if (mine_map[y][x] >= 0) {
	  mine_map[y][x] = BOOM;
	  k--;
	}
      }

      for (int y = 0; y < r; y++) // Compute weights
	for (int x = 0; x < c; x++)
	  if (mine_map[y][x] >= 0) {
	    int w = 0;
	    int y0 = Math.max(0, y - 1);
	    int y1 = Math.min(r, y + 2);
	    int x0 = Math.max(0, x - 1);
	    int x1 = Math.min(c, x + 2);
	    for (int yw = y0; yw < y1; yw++)
	      for (int xw = x0; xw < x1; xw++)
		if (mine_map[yw][xw] < 0) w++;
	    mine_map[y][x] = w;
	  }
    }
  }

  /**
   * Pick a number at random.
   * @param n           a positive number
   * @return            a nonnegative number less than n
   */
  public int pick(int n) {
    if (n <= 0)
      throw new IllegalArgumentException("n must be positive");
    int p = (int)Math.floor((double)n * Math.random());
    return p >= n ? p - n : p;
  }

  private boolean victory = false;
  private boolean finished = false;
  private boolean probed = false;

  /**
   * Has this game been won?
   * A game is won if every cell which does not contain a mine has
   * been probed, but no cell with a mine has been probed.
   */
  public boolean won() {
    return victory;
  }

  /**
   * Is this game finished?
   * The game is finished if it has been won or if a cell with a
   * mine has been probed.
   */
  public boolean done() {
    if (finished)
      return true;
    for (int y = 0; y < r; y++)
      for (int x = 0; x < c; x++)
	if (mine_map[y][x] < 0 != unprobed_map[y][x])
	  return false;
    finished = true;
    victory = true;
    return true;
  }

  /**
   * Has this game had at least one successful probe.
   */
  public boolean probed() {
    return probed;
  }

  /**
   * Probe a cell for a mine.
   * <ul>
   * <li> If the game is finished, probe behaves like look.
   * <li> If the cell does not exist, <code>OUT_OF_BOUNDS</code>
   * is returned.
   * <li>If the cell is marked, <code>MARKED</code> is returned.
   * <li>If the cell has a mine, <code>BOOM</code> is returned
   * and the game is lost.
   * <li>Otherwise, the number of adjacent mines is returned.
   * </ul>
   * @param x        x coordinate of cell
   * @param y        y coordinate of cell
   */
  public int probe(int x, int y) {
    if (finished)
      return look(x, y);
    else if (x < 0 || x >= c || y < 0 || y >= r)
      return OUT_OF_BOUNDS;
    else if (mark_map[y][x])
      return MARKED;
    unprobed_map[y][x] = false;
    if (mine_map[y][x] < 0)
      finished = true;
    else
      probed = true;
    return mine_map[y][x];
  }

  /**
   * Look at a cell.
   * <ul>
   * <li> If the cell does not exist, <code>OUT_OF_BOUNDS</code>
   * is returned.
   * <li>If the cell is marked, <code>MARKED</code> is returned.
   * <li>If the cell has not been probed, <code>UNPROBED</code>
   * is returned.
   * <li>If the cell has a probed mine, <code>BOOM</code> is returned.
   * <li>Otherwise, the number of adjacent mines is returned.
   * </ul>
   * @param x        x coordinate of cell
   * @param y        y coordinate of cell
   */
  public int look(int x, int y) {
    if (x < 0 || x >= c || y < 0 || y >= r)
      return OUT_OF_BOUNDS;
    else if (mark_map[y][x])
      return MARKED;
    else if (unprobed_map[y][x])
      return UNPROBED;
    else
      return mine_map[y][x];
  }

  /**
   * Mark a cell.
   * <ul>
   * <li> If the game is finished, mark behaves like look.
   * <li> If the cell does not exist, <code>OUT_OF_BOUNDS</code>
   * is returned.
   * <li>If the cell is marked, <code>MARKED</code> is returned.
   * <li>If the cell has not been probed, the cell is marked
   * and <code>MARKED</code> is returned.
   * <li>Otherwise, the number of adjacent mines is returned.
   * </ul>
   * @param x        x coordinate of cell
   * @param y        y coordinate of cell
   */
  public int mark(int x, int y) {
    if (finished)
      return look(x, y);
    else if (x < 0 || x >= c || y < 0 || y >= r)
      return OUT_OF_BOUNDS;
    else if (mark_map[y][x])
      return MARKED;
    else if (unprobed_map[y][x]) {
      mmm--;
      mark_map[y][x] = true;
      return MARKED;
    }
    else
      return mine_map[y][x];
  }

  /**
   * Unmark a cell.
   * <ul>
   * <li> If the game is finished, unmark behaves like look.
   * <li> If the cell does not exist, <code>OUT_OF_BOUNDS</code>
   * is returned.
   * <li>If the cell is marked, the cell is unmarked
   * and <code>UNPROBED</code> is returned.
   * <li>If the cell has not been probed,
  * <code>UNPROBED</code> is returned.
   * <li>Otherwise, the number of adjacent mines is returned.
   * </ul>
   * @param x        x coordinate of cell
   * @param y        y coordinate of cell
   */
  public int unmark(int x, int y) {
    if (finished)
      return look(x, y);
    else if (x < 0 || x >= c || y < 0 || y >= r)
      return OUT_OF_BOUNDS;
    else if (mark_map[y][x]) {
      mmm++;
      mark_map[y][x] = false;
      return UNPROBED;
    }
    else if (unprobed_map[y][x])
      return UNPROBED;
    else
      return mine_map[y][x];
  }

  /**
   * Provide the number of mines minus the
   * number of marks in this mine map.
   */
  public int mines_minus_marks() {
    return mmm;
  }

  /**
   * Provide the number of rows in this mine map.
   */
  public int rows() {
    return r;
  }

  /**
   * Provide the number of columns in this mine map.
   */
  public int columns() {
    return c;
  }

  /**
   * Display the mine map on the standard output stream.
   * Used only for debugging.
   */
  public void display() {
    for (int y = 0; y < r; y++) {
      int z = r - 1 - y;
      System.out.print(z % 10 + ":");
      for (int x = 0; x < c; x++)
	if (mark_map[z][x])
	  if (mine_map[z][x] < 0)
	    System.out.print("-");
	  else
	    System.out.print("?");
	else if (mine_map[z][x] < 0)
	  System.out.print("X");
	else if (unprobed_map[z][x])
	  System.out.print(" ");
	else
	  System.out.print(mine_map[z][x]);
      System.out.println();
    }
    System.out.print("  ");
    for (int x = 0; x < c; x++)
      System.out.print(x % 10);
  }
}
