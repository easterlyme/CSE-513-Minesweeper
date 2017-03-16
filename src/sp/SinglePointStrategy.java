package sp;

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

import map.*;
import set.*;

/**
 * The class SinglePointStrategy implements a PGMS strategy.
 * The Single Point Strategy makes a decision based on information
 * available from a single probed point in the mine map. <p>
 * The strategy looks at a probed point.  If the number of mines
 * near the point equals the number of marks near the point, the
 * strategy infers that near points whose status is unknown do not
 * contain mines.  Similarly, if the number of mines near the point
 * equals the number of marks near the point plus the number of
 * unknowns near, the strategy infers that the near points whose status
 * is unknown contain mines.
 * <p> The implementation makes extensive use of sets.
 * @see Strategy
 * @see set.Set
 * @version October 1995
 * @author John D. Ramsdell
 */
public final class SinglePointStrategy implements Strategy {

  /**
   * Invoke the Single Point Strategy.
   * @see Strategy
   */
  public void play(Map m) {
    for (;;) {
      int y = m.pick(m.rows());
      int x = m.pick(m.columns());
      int q = m.probe(x, y);	// Guess a point to be probed
      if (Map.BOOM == q)	// Opps! Bad guess
	return;
      else if (q >= 0) {
	apply(m, x, y);		// Try strategy at this point
	if (m.done())
	  return;		// We win!
      }
    }
  }

  /*
   * This routine applies the Single Point Strategy.
   */
  private static void apply(Map m, int x, int y) {
    Set s = new Set().adjoin(new Point(x, y));
				// Set s is a set of probed points
    while (!s.empty()) {
      Point p = (Point)s.first();
      s = s.but_first();
      int i = p.x();
      int j = p.y();
      int q = m.look(i, j);
      if (q >= 0) {		// Then point p is probed and not a mine
	int u = unknowns_near(m, i, j);
	if (u > 0) {		// Then some near points are unknown
	  int k = q - marks_near(m, i, j);
	  if (k == 0)		// Then all near unknowns do not contain mines
	    s = probe_around(m, i, j, s);
	  else if (k == u)	// Then all near unknowns contain mines
	    s = mark_around(m, i, j, s);
	}
      }
    }
  }

  private static int unknowns_near(Map m, int x, int y) {
    int u = 0;
    for (int j = y - 1; j < y + 2; j++)
      for (int i = x - 1; i < x + 2; i++)
	if (Map.UNPROBED == m.look(i, j)) u++;
    return u;
  }

  private static int marks_near(Map m, int x, int y) {
    int u = 0;
    for (int j = y - 1; j < y + 2; j++)
      for (int i = x - 1; i < x + 2; i++)
	if (Map.MARKED == m.look(i, j)) u++;
    return u;
  }

  /*
   * After probing, adjoin all unprobed points near the newly
   * probed point for future consideration by the strategy.
   */
  private static Set probe_around(Map m, int x, int y, Set s) {
    for (int j = y - 1; j < y + 2; j++)
      for (int i = x - 1; i < x + 2; i++)
	if (Map.UNPROBED == m.look(i, j))
	  m.probe(i, j);
    return adjoin_around(m, x, y, s);
  }

  /*
   * After marking, adjoin all unprobed points near the mark
   * for future consideration by the strategy.
   */
  private static Set mark_around(Map m, int x, int y, Set s) {
    for (int j = y - 1; j < y + 2; j++)
      for (int i = x - 1; i < x + 2; i++)
	if (Map.UNPROBED == m.look(i, j))
	  m.mark(i, j);
    return adjoin_around(m, x, y, s);
  }

  private static Set adjoin_around(Map m, int x, int y, Set s) {
    for (int j = y - 2; j < y + 3; j++)
      for (int i = x - 2; i < x + 3; i++)
	if (m.look(i, j) >= 0)
	  s = s.adjoin(new Point(i, j));
    return s;
  }
}
