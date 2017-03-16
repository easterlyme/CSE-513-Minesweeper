package set;

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
 * Elements for sets.
 * Elements of sets must be linearly ordered.
 * In addition, the implementation of sets assumes that an
 * element is immutable.  Repeated applications of a predicate
 * to a pair of elements, must result is the same value.
 * @see Set
 * @version October 1995
 * @author John D. Ramsdell
 */
public interface Element {

  /**
   * Is this element equal to some other element?
   * @param e		the element to compare with
   * @return 		true if the elements are the same
   */
  boolean same(Element e);

  /**
   * Is this element less than some other element?
   * @param e		the element to compare with
   * @return 		true if this element is the less than the other
   */
  boolean less(Element e);
}
