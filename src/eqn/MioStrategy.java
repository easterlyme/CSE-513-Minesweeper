package eqn;

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

import map.*;
import set.*;
import sp.Point;

/**
 * The class MioStrategy implements a PGMS strategy.
 * The Mio Equation Strategy makes a decision based on a set of equations.
 * <p> The implementation makes extensive use of sets.
 * <p> PGMS players are strongly discouraged from reading the source
 * file that implements the Equation Strategy.  It's much more fun
 * to create your own strategy rather than copy someone else's.
 * @see Strategy
 * @see set.Set
 * @version April 2003
 * @author John D. Ramsdell
 */
public final class MioStrategy implements Strategy {

  /*
   * The Mio Equation Strategy associates a value with each point
   * in the mine map.  A point has the value one if it contains a mine,
   * or the value zero if it does not.  When the value at a point
   * is unknown, its value is represented as a variable in equations.
   *
   * Suppose point (0, 3) does not contain a mine, and there are
   * two mines near.  Suppose point (0, 2) is known to contain a mine
   * and point (1, 2) is known not to contain a mine, but the status
   * of the other three near points is unknown.  The equation derived at
   * point (0, 3) is:
   *
   *             1 = (1, 3) + (0, 4) + (1, 4)
   */

  /* Mine map to which this strategy is applied. */
  private Map m;

  /* em[y][x] contains equation e if point (x, y) is in the equation. */
  private Set em[][];               // em is short for an equation map

  /* ps contains a set of points that are candidates for the rules. */
  private Set ps;

  /* When the number of unknown locations falls to this limit,
   * a global equation is added. */
  private final int global_eqn_limit = 8;
  private boolean global_eqn_added; // Has global equation been added?

  private final boolean messages = false; // Print debugging messages

  /**
   * Invoke the Equation Strategy.
   * @see Strategy
   */
  public void play(Map m) {
    this.m = m;
    global_eqn_added = false;
    init_eqn_map();		// Create initial em array
    ps = new Set();

    while (!m.done()) {
      if (Map.BOOM == choose())	// Guess a location and probe it
	return;			// Choose modifies the point set ps
      while (!ps.empty()) {
	Point p = (Point)ps.first();
	ps = ps.but_first();

	/* Try applying the rules to point p.
	 * The rules modify the point set ps.*/
	single_equation_rule(p);
	subset_rule(p);
	eqn_diff_rule(p);

	/* Add global equation if appropriate. */
	if (!global_eqn_added) {
	  int nps0 = unknown_count();
	  if (nps0 <= global_eqn_limit) {
	    global_eqn_added = true;
	    ps = unknown_points();
	    /* The number of mines minus the number of marks
	     * equals the sum of the values at the unknown points. */
	    adjoin(new Eqn(m.mines_minus_marks(), ps));
	  }
	}
      }
    }
    if (messages) {
      if (m.won())
	System.out.println("Game won.");
      else
	System.out.println("Game lost.");
    }
    em = null;
  }

  private Set unknown_points() { // This routine returns the
    int r = m.rows();		 // set of points which have
    int c = m.columns();	 // values that are not known.
    Set ps = new Set();

    for (int y = r - 1; y >= 0; y--)
      for (int x = c - 1; x >= 0; x--)
        if (Map.UNPROBED == m.look(x, y))
	  ps = ps.adjoin(new Point(x, y));
    return ps;
  }

  private int unknown_count() {  // This routine returns the
    int r = m.rows();		 // number of points which have
    int c = m.columns();	 // values that are not known.
    int n = 0;

    for (int y = r - 1; y >= 0; y--)
      for (int x = c - 1; x >= 0; x--)
        if (Map.UNPROBED == m.look(x, y))
	  n++;
    return n;
  }

  /* Single Equation Rule
   *
   * (1) For any equation of the form
   *     0 = p[0] + p[1] + ... + p[n]
   *     all points must have value zero and therefore should be probed.
   *
   * (2) For any equation of the form
   *     c = p[0] + p[1] + ... + p[n]
   *     where c is the number of points in the equation,
   *     all points must have value one and therefore should be marked.
   */
  private void single_equation_rule(Point p) {

    for (Set es = at(p); !es.empty(); es = es.but_first()) {
      Eqn e = (Eqn)es.first();
      int c = e.constant();
      Set ps = e.points();

      if (c == 0) {		// Case 1 above
	probe_points(ps);
	return;
      }
      else if (c == ps.card()) { // Case 2 above
	mark_points(ps);
	return;
      }
    }
  }

  /* Subset Rule
   *
   * Consider two equations,
   *     c[0] = p[0,0] + p[0,1] + ... + p[0,n[0]]
   *     c[1] = p[1,0] + p[1,1] + ... + p[1,n[1]]
   * If the points in equation 0 are a proper subset of the points
   * in equation 1, the following equation should be added:
   *     c[1] - c[0] = p[1,0] + p[1,1] + ... + p[1,n[1]]
   *                   - p[0,0] - p[0,1] - ... - p[0,n[0]]
   */
  private void subset_rule(Point p) {
    Set es = at(p);

    for (Set es0 = es; !es0.empty(); es0 = es0.but_first()) {
      Eqn e0 = (Eqn)es0.first();

      for (Set es1 = es; !es1.empty(); es1 = es1.but_first()) {
	Eqn e1 = (Eqn)es1.first();
	int c_diff = e1.constant() - e0.constant();

	if (c_diff >= 0 && e0.points().proper_subset(e1.points())) {
	  Set ps0 = e1.points().set_difference(e0.points());
	  Eqn e = new Eqn(c_diff, ps0);

	  if (!member(e)) {
	    adjoin(e);
	    ps = ps.union(ps0);
	  }
	}
      }
    }
  }

  /* Equation Difference Rules
   *
   * Consider two equations,
   *     c[0] = p[0,0] + p[0,1] + ... + p[0,n[0]]
   *     c[1] = p[1,0] + p[1,1] + ... + p[1,n[1]]
   * Substract equation 0 from equation 1.
   * If the number of points with a positive coefficient is the same
   * as the difference between the constants, the points with a positive
   * coefficient must have value one and therefore should be marked.
   * The points with a negative coefficient must have value zero and
   * therefore should be probed.
   *
   * Consider three equations,
   *     c[0] = p[0,0] + p[0,1] + ... + p[0,n[0]]
   *     c[1] = p[1,0] + p[1,1] + ... + p[1,n[1]]
   *     c[2] = p[2,0] + p[2,1] + ... + p[2,n[1]]
   * Substract equations 0 and 2 from equation 1.
   * If the number of points with a positive coefficient is the same
   * as the difference between the constants, the points with a positive
   * coefficient must have value one and therefore should be marked.
   * The points with a negative coefficient must have value zero and
   * therefore should be probed.
   *
   * Equation difference with three equations is the Mio inspired rule.
   */
  private void eqn_diff_rule(Point p) {
    Set es = at(p);

    for (Set es0 = es; !es0.empty(); es0 = es0.but_first()) {
      Eqn e0 = (Eqn)es0.first();

      for (Set es1 = es; !es1.empty(); es1 = es1.but_first()) {
	Eqn e1 = (Eqn)es1.first();
	int c_diff = e1.constant() - e0.constant();

	if (c_diff > 0) {
	  Set ps = e1.points().set_difference(e0.points());

	  if (ps.card() == c_diff) {
	    mark_points(ps);
	    probe_points(e0.points().set_difference(e1.points()));
	  }
	  else if (c_diff > 1) { // Try double elimination -- the Mio trick
	    Set es2 = new Set();

	    for (Set ps0 = ps; !ps0.empty(); ps0 = ps0.but_first()) {
	      Point p1 = (Point)ps0.first();
	      es2 = es2.union(at(p1));
	    }
	    // es2 is the equations that contain a point in ps.
	    for (; !es2.empty(); es2 = es2.but_first()) {
              Eqn e2 = (Eqn)es2.first();
	      int c_diff2 = c_diff - e2.constant();
	      if (c_diff2 > 0) {
		Set ps2 = ps.set_difference(e2.points());
		if (ps2.card() == c_diff2) {
		  mark_points(ps2);
		  Set ps3 = e0.points().union(e2.points());
		  ps3 = ps3.set_difference(e1.points());
		  ps3 = ps3.union(e0.points().intersect(e2.points()));
		  probe_points(ps3);
		}
	      }
	    }
	  }
	}
      }
    }
  }

  private void probe_points(Set ps0) { // ps0 is a set of point known
				       // not to contain mines
    for (; !ps0.empty(); ps0 = ps0.but_first()) {
      Point p = (Point)ps0.first();
      int q = m.probe(p.x(), p.y()); // assert(q >= 0)
      if (messages && q < 0)
	System.out.println("Probe kills");

      /* Propagate assertion that point p does not contain a mine
       * by adjoining the equation 0 = p, and applying the subset rule. */
      adjoin(new Eqn(0, new Set().adjoin(p)));
      subset_rule(p);
      remove_at(p);		// Dump equations at p

      /* Add an equation using the information found by the probe. */
      Set ps1 = unknowns_near(p.x(), p.y());
      adjoin(new Eqn(q - marks_near(p.x(), p.y()), ps1));
      ps = ps.union(ps1);
    }
  }

  private void mark_points(Set ps) { // ps0 is a set of point known
				     // to contain mines
    for (; !ps.empty(); ps = ps.but_first()) {
      Point p = (Point)ps.first();
      int q = m.mark(p.x(), p.y()); // assert(q == Map.MARKED)
      if (messages && q != Map.MARKED)
	System.out.println("Mark misses");

      /* Propagate assertion that point p contains a mine
       * by adjoin the equation 1 = p, and applying the subset rule. */
      adjoin(new Eqn(1, new Set().adjoin(p)));
      subset_rule(p);
      remove_at(p);		// Dump equations at p
    }
  }

  /* Remove all equations that contain point p. */
  private void remove_at(Point p) {
    for (Set es = at(p); !es.empty(); es = es.but_first()) {
      Eqn e = (Eqn)es.first();
      remove(e);
    }
  }

  private void init_eqn_map() {	// Initialize the equation map em
    Set mt = new Set();
    int r = m.rows();
    int c = m.columns();

    em = new Set[r][c];

    for (int y = 0; y < r; y++)
      for (int x = 0; x < c; x++)
        em[y][x] = mt;

    for (int y = 0; y < r; y++)
      for (int x = 0; x < c; x++) {
	int q = m.look(x, y);

	if (q >= 0)
	  adjoin(new Eqn(q - marks_near(x, y), unknowns_near(x, y)));
      }
  }

  private Set unknowns_near(int x, int y) { // Returns the set of points
    Set ps = new Set();			    // near the point (x, y) that
					    // have values which are
    for (int y0 = y - 1; y0 < y + 2; y0++)  // not known
      for (int x0 = x - 1; x0 < x + 2; x0++)
        if (Map.UNPROBED == m.look(x0, y0))
          ps = ps.adjoin(new Point(x0, y0));
    return ps;
  }

  private int marks_near(int x, int y) { // Returns the number of marks
    int q = 0;				 // near the point (x, y)

    for (int y0 = y - 1; y0 < y + 2; y0++)
      for (int x0 = x - 1; x0 < x + 2; x0++)
        if (Map.MARKED == m.look(x0, y0))
          q++;
    return q;
  }

  /* Operations on equation maps. */

  private Set at(Point p) {	// Get the equations that use point p
    return em[p.y()][p.x()];
  }

  private boolean member(Eqn e) { // Is e in the equation map?
    Set ps = e.points();

    return !ps.empty() && at((Point)ps.first()).member(e);
  }

  private void adjoin(Eqn e) { // Add e to the equation map
    if (!member(e))
      for (Set ps0 = e.points(); !ps0.empty(); ps0 = ps0.but_first()) {
	Point p = (Point)ps0.first();
	int y = p.y();
	int x = p.x();
	em[y][x] = em[y][x].adjoin(e);
      }
  }

  private void remove(Eqn e) { // Remove e from the equation map
    if (member(e))
      for (Set ps0 = e.points(); !ps0.empty(); ps0 = ps0.but_first()) {
	Point p = (Point)ps0.first();
	int y = p.y();
	int x = p.x();
	em[y][x] = em[y][x].remove(e);
      }
  }

  /*
   * When none of the other rules in the Equation Strategy are applicable,
   * choose guesses a point at which to probe.  Let p be a point in equation
   * e.  Assuming nothing else in know about point p, the single equation
   * probability that p contains a mine based on equation e is
   *
   *      P(e) = e.constant() / e.points().card().
   *
   * Let es(p) be the set of equations that contain point p.  Let M(p)
   * be the maximum of the single equation probabilities that p contains
   * a mine based on the equations in es(p).  That is
   *
   *      M(p) = max P(e) for e in es(p).
   *
   * This routine picks a point p which minimizes M(p).  A random choice
   * is made when there are many points that minimize M(p).
   *
   * Last updated: February 1997
   */
  private int choose() {
    int r = m.rows();
    int c = m.columns();

    Set ps0 = unknown_points();
    int nps0 = ps0.card();
    if (nps0 == 0)		// Just in case...
      return 0;
    float prob0 = (float)m.mines_minus_marks() / (float)nps0;

    for (int n = m.pick(nps0); n > 0; n--) // Pick starting point
      ps0 = ps0.but_first();

    Point best_point = (Point)ps0.first();
    int y_orig = best_point.y();
    int x_orig = best_point.x();

    float best_prob = prob(x_orig, y_orig, prob0);

    for (int j = 0; j < r; j++) { // Try the rest of the points.
      int y = (j + y_orig) % r;
      for (int i = 0; i < c; i++) {
	int x = (i + x_orig) % c;
	float p = prob(x, y, prob0);

	if (p < best_prob) {
	  best_prob = p;
	  best_point = new Point(x, y);
	}
      }
    }

    return tap(best_point);
  }

  // Find max prop of all the equations
  private float prob(int x, int y, float prob0) {
    Set es = em[y][x];
    if (es.empty()) {
      if (Map.UNPROBED == m.look(x, y))
	return prob0;
      else
	return 1.0f;
    }
    else {
      float p = 0.0f;
      for (; !es.empty(); es = es.but_first()) {
        Eqn e = (Eqn)es.first();
	float e_prob = (float)e.constant() / (float)e.points().card();
	p = Math.max(p, e_prob);
      }
      return p;
    }
  }

  private int tap(Point p) {	// Try probing

    if (messages) {
      System.out.print("Picked ");
      p.display();
      System.out.println();
    }

    int q = m.probe(p.x(), p.y());

    if (q >= 0)			// Probe point if a mine was not found.
      probe_points(new Set().adjoin(p));
    return q;
  }

  /* Routines for debugging.  None are used during a normal run. */

  private Set all_eqns() {	// Collects all equations
    Set es = new Set();
    int r = m.rows();
    int c = m.columns();

    for (int y = r - 1; y >= 0; y--)
      for (int x = c - 1; x >= 0; x--)
        es = es.union(em[y][x]);
    return es;
  }

  private void display() {
    display_eqn_set(all_eqns());
  }

  private void display_eqn_set(Set es) {
    System.out.print(" {");
    if (!es.empty()) {
      ((Eqn)es.first()).display();
      for (es = es.but_first(); !es.empty(); es = es.but_first()) {
	System.out.println(",");
	System.out.print("  ");
	((Eqn)es.first()).display();
      }
      System.out.println("}");
    }
  }
}
