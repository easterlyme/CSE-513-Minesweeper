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

import set.*;

/**
 * The class Point implements immutable two dimensional points.
 * These points can be used as elements of sets.
 * @see set.Set
 * @version October 1995
 * @author John D. Ramsdell
 */
public final class Point implements Element {
  private int xval, yval;

  private Point() { }

  /**
   * Construct a point from an x coordinate and a y coordinate.
   * @param x		the x coordinate
   * @param y		the y coordinate
   * @return 		the new point
   */
  public Point(int x, int y) {
    xval = x;
    yval = y;
  }

  /**
   * Access the x coordinate of a point.
   * @return 		the coordinate
   */
  public int x() {
    return xval;
  }

  /**
   * Access the y coordinate of a point.
   * @return 		the coordinate
   */
  public int y() {
    return yval;
  }

  /**
   * Is this element equal to some other element?
   * @see Element#same
   */
  public boolean same(Element e) {
    return e != null
      && e instanceof Point
	&& xval == ((Point)e).xval
	  && yval == ((Point)e).yval;
  }

  /**
   * Is this element less than some other element?
   * @see Element#less
   */
  public boolean less(Element e) {
    if (e == null || !(e instanceof Point))
      return false;
    else
      return
	yval < ((Point)e).yval
	  || yval == ((Point)e).yval
	    && xval < ((Point)e).xval;
  }

  /**
   * Display the point on the standard output stream.
   * Used only for debugging.
   */
  public void display() {
    System.out.print("(" + xval + ", " + yval + ")");
  }
}
