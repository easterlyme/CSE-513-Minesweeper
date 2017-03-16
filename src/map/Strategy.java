package map;

/* Copyright (C) 1995 John D. Ramsdell

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
 * Strategies for Programmer's Minesweeper.
 * A strategy is an object that plays a game of minesweeper
 * using the methods provided by the Map class.
 * @see PGMS
 * @see Map
 * @version October 1995
 * @author John D. Ramsdell
 */
public interface Strategy {

  /**
   * Plays a game of mine sweeper.
   * @param m		the map of mines
   */
  void play(Map m);
}
