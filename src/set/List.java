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
 * Lists used in the implementation of sets.
 * @see Set
 * @version October 1995
 * @author John D. Ramsdell
 */
final class List {
  private Element e;
  private List l;

  private List() { }

  /**
   * Construct a list from an element and another list.
   * @param e		the element
   * @param l		the list
   * @return 		the new list augment with the elment
   */
  List(Element e, List l) {
    this.e = e;
    this.l = l;
  }

  /**
   * Access the first element of a list.
   * @return 		the element
   */
  Element first() {
    return e;
  }

  /**
   * Access the rest of the list.
   * @return 		the list without the first element
   */
  List but_first() {
    return l;
  }
}
