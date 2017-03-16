package map;

/* Copyright (C) 1997 John D. Ramsdell

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
 * The inteface Map describes a mine map.  A strategy operates on a mine
 * map. By invoking the operations of probing and marking, the strategy
 * attempts to place the mine map in a state in which every cell that does
 * not contain a mine has been probed, without probing a cell that does
 * contain a mine.
 * @see Strategy
 * @version October 1995
 * @author John D. Ramsdell
 */
public interface Map {

  /**
   * Has this game been won?
   * A game is won if every cell which does not contain a mine has
   * been probed, but no cell with a mine has been probed.
   */
  boolean won();

  /**
   * Is this game finished?
   * The game is finished if it has been won or if a cell with a
   * mine has been probed.
   */
  boolean done();

  /**
   * Has this game had at least one successful probe.
   */
  boolean probed();

  /**
   * Out of bounds return code.
   * @see Map#look
   */
  int OUT_OF_BOUNDS = -4;

  /**
   * Marked return code.
   * @see Map#look
   */
  int MARKED = -3;

  /**
   * Unprobed return code.
   * @see Map#look
   */
  int UNPROBED = -2;

  /**
   * Boom return code.
   * @see Map#look
   */
  int BOOM = -1;

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
  int probe(int x, int y);

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
  int look(int x, int y);

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
  int mark(int x, int y);

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
  int unmark(int x, int y);

  /**
   * Provide the number of mines minus the
   * number of marks in this mine map.
   */
  int mines_minus_marks();

  /**
   * Provide the number of rows in this mine map.
   */
  int rows();

  /**
   * Provide the number of columns in this mine map.
   */
  int columns();

  /**
   * Display the mine map on the standard output stream.
   * Used only for debugging.
   */
  void display();

  /**
   * Pick a number at random.
   * @param n           a positive number (not checked)
   * @return            a nonnegative number less than n
   */
  int pick(int n);

}
