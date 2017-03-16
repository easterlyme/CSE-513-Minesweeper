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
 * The class Set implements set operations on linearly ordered elements.
 * This implementation of sets assumes elements are immutable.
 * Note that a set may be an element of a set or a component of an
 * element of a set. <p>
 * The elements of a set are stored in a sorted list.  An alternative
 * might use hash tables instead of lists, but hash tables were thought
 * to use too much space. <p>
 * Declaring this class final seems to expose a compiler error when
 * using the -O switch.  Code running as an applet in the appletviewer
 * produce
 *<pre>
 *    java.lang.IllegalAccessError: set.Set.t
 *</pre>
 * @see Element
 * @version October 1995
 * @author John D. Ramsdell
 */
public class Set implements Element {
  private List t;		// List of elements in the set.

  public Set() {
    t = null;
  }

  private Set(List t) {
    this.t = t;
  }

  /**
   * Is this the empty set?
   * @return 		true if this is the empty set
   */
  public boolean empty() {
    return t == null;
  }

  /**
   * Adjoin an element to a set.
   * @param e		the element to add
   * @return 		the new set
   */
  public Set adjoin(Element e) {
    if (list_member(e, t))
      return this;
    else
      return new Set(list_adjoin(e, t));
  }

  private static List list_adjoin(Element e, List t) {
    if (t == null)
      return new List(e, t);
    else {
      Element e0 = t.first();

      if (e.same(e0))
	return t;		// e already in the list
      else if (e.less(e0))
	return new List(e, t);
      else
	return new List(e0, list_adjoin(e, t.but_first()));
    }
  }

  /**
   * Remove an element from a set.
   * @param e		the element to remove
   * @return 		the new set
   */
  public Set remove(Element e) {
    if (!list_member(e, t))
      return this;
    else
      return new Set(list_remove(e, t));
  }

  private static List list_remove(Element e, List t) {
    if (t == null)
      return t;
    else {
      Element e0 = t.first();

      if (e.same(e0))
	return t.but_first();
      else if (e.less(e0))
	return t;		// e not in the list
      else
	return new List(e0, list_remove(e, t.but_first()));
    }
  }

  /**
   * Is an element a member of this set?
   * @param e		the element
   * @return 		true if element is in the set
   */
  public boolean member(Element e) {
    return list_member(e, t);
  }

  private static boolean list_member(Element e, List t) {
    for (; t != null; t = t.but_first()) {
      Element e0 = t.first();

      if (e.same(e0))
	return true;
      else if (e.less(e0))
	return false;
    }
    return false;
  }

  /* When implementations are known to be tail recursive,
     you can write list_member in this straightforward way.
  private static boolean list_member(Element e, List t) {
    if (t == null)
      return false;
    else {
      Element e0 = t.first();

      if (e.same(e0))
	return true;
      else if (e.less(e0))
	return false;
      else
	return list_member(e, t.but_first());
    }
  }
  */

  /**
   * Is this set a proper subset of another set?
   * @param s		the other set
   * @return 		true if this set is a proper subset of the other
   */
  public boolean proper_subset(Set s) {
    return list_subset(t, s.t) && !list_subset(s.t, t);
  }

  private static boolean list_subset(List t0, List t1) {
    for (; t0 != null; t0 = t0.but_first())
      if (!list_member(t0.first(), t1)) return false;
    return true;
  }

  /**
   * Set union.
   * @param s		the other set
   * @return 		the union of this set and the other
   */
  public Set union(Set s) {
    List t1 = t;
    for (List t0 = s.t; t0 != null; t0 = t0.but_first())
      t1 = list_adjoin(t0.first(), t1);
    return new Set(t1);
  }

  /**
   * Set intersection.
   * @param s		the other set
   * @return 		the intersection of this set and the other
   */
  public Set intersect(Set s) {
    return new Set(list_intersect(t, s.t));
  }

  private static List list_intersect(List t0, List t1) {
    for (; t0 != null; t0 = t0.but_first()) {
      Element e = t0.first();

      if (list_member(e, t1))
	return new List(e, list_intersect(t0.but_first(), t1));
    }
    return null;
  }

  /* When implementations are known to be tail recursive,
     you can write list_intersect in this straightforward way.
  private static List list_intersect(List t0, List t1) {
    if (t0 == null)
      return t0;
    else {
      Element e = t0.first();

      if (list_member(e, t1))
	return new List(e, list_intersect(t0.but_first(), t1));
      else
	return list_intersect(t0.but_first(), t1);
    }
  }
  */

  /**
   * Set difference.
   * @param s		the other set
   * @return 		the set difference of this set and the other
   */
  public Set set_difference(Set s) {
    List t1 = t;
    for (List t0 = s.t; t0 != null; t0 = t0.but_first())
      t1 = list_remove(t0.first(), t1);
    return new Set(t1);
  }

  /**
   * Pick an element from the set.
   * @return 		some element
   */
  public Element first() {
    if (t == null)
      return null;
    else
      return t.first();
  }

  /**
   * The set minus its first element.
   * @return 		the set with the first element removed
   */
  public Set but_first() {
    if (t == null)
      return null;
    else
      return new Set(t.but_first());
  }

  /**
   * Set Cardinality.
   * @return 		the number of elments that are in the set
   */
  public int card() {
    int c = 0;
    for (List t0 = t; t0 != null; t0 = t0.but_first())
      c++;
    return c;
  }

  /**
   * Is this element equal to some other element?
   * @see Element#same
   */
  public boolean same(Element e) {
    return e != null
      && e instanceof Set
	&& list_same(t, ((Set)e).t);
  }

  private static boolean list_same(List t0, List t1) {
    for (;;)
      if (t0 == null)
	return t1 == null;
      else
	if (t1 == null || !t0.first().same(t1.first()))
	  return false;
	else {
	  t0 = t0.but_first();
	  t1 = t1.but_first();
	}
  }

  /* When implementations are known to be tail recursive,
     you can write list_same in this straightforward way.
  private static boolean list_same(List t0, List t1) {
    if (t0 == null)
      return t1 == null;
    else
      return t1 != null
	&& t0.first().same(t1.first())
	  && list_same(t0.but_first(), t1.but_first());
  }
  */

  /**
   * Is this element less than some other element?
   * @see Element#less
   */
  public boolean less(Element e) {
    if (e == null || !(e instanceof Set))
      return false;
    else
      return list_less(t, ((Set)e).t);
  }

  private static boolean list_less(List t0, List t1) {
    for (;;)
      if (t0 == null)
	return t1 != null;
      else if (t1 == null)
	return false;
      else {
	Element e0 = t0.first();
	Element e1 = t1.first();

	if (e0.less(e1))
	  return true;
	else if (!e0.same(e1))
	  return false;
	else {
	  t0 = t0.but_first();
	  t1 = t1.but_first();
	}
      }
  }

  /* When implementations are known to be tail recursive,
     you can write list_intersect in this straightforward way.
  private static boolean list_less(List t0, List t1) {
    if (t0 == null)
      return t1 != null;
    else if (t1 == null)
      return false;
    else {
      Element e0 = t0.first();
      Element e1 = t1.first();

      return e0.less(e1)
	|| e0.same(e1)
	  && list_less(t0.but_first(), t1.but_first());
    }
  }
  */
}
