package eqn;

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

import set.*;
import sp.Point;

/**
 * The class Eqn implements immutable equations.
 * The equations are linear.  A point is used as a variable.
 * The coefficent of each variable in an equation is one.
 * These equations can be used as elements of sets.
 * @see set.Set
 * @see sp.Point
 * @version October 1995
 * @author John D. Ramsdell
 */
final class Eqn implements Element {
  private int c;		// Constant term.
  private Set ps;		// Set of points.

  private Eqn() {}

  /**
   * Construct an equation from a constant and a set of points.
   * the equation is
   * <pre>
   * c = p0 + p1 + ... + pn
   * </pre>
   * @param constant        constant term c
   * @param points          set of points
   * @return                an equation
   */
  Eqn(int constant, Set points) {
    c = constant;
    ps = points;
  }

  /**
   * Access the constant term of an equation.
   * @return 		the constant term
   */
  int constant() {
    return c;
  }

  /**
   * Access the set of points an equation.
   * @return 		the set of points
   */
  Set points() {
    return ps;
  }

  /**
   * Is this element equal to some other element?
   * @see Element#same
   */
  public boolean same(Element e) {
    return e != null
      && e instanceof Eqn
	&& c == ((Eqn)e).c
	  && ps.same(((Eqn)e).ps);
  }

  /**
   * Is this element less than some other element?
   * @see Element#less
   */
  public boolean less(Element e) {
    if (e == null || !(e instanceof Eqn))
      return false;
    else
      return
	c < ((Eqn)e).c
	  || c == ((Eqn)e).c
	    && ps.less(((Eqn)e).ps);
  }

  /**
   * Display the equation on the standard output stream.
   * Used only for debugging.
   */
  void display() {
    System.out.print(c + " = ");
    if (ps.empty())
      System.out.print("0");
    else {
      ((Point)ps.first()).display();
      for (Set ps0 = ps.but_first(); !ps0.empty(); ps0 = ps0.but_first()) {
	System.out.print(" + ");
	((Point)ps0.first()).display();
      }
    }
  }
}
