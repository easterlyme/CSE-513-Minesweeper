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

import java.awt.*;

/**
 * The class DisplayMap provides graphics for an applet which is running
 * a game of minesweeper.
 * @version February 1997
 * @author John D. Ramsdell
 */
class DisplayMap extends Canvas implements Map, Runnable {
  private Strategy s;
  private int mines;
  private int r;
  private int c;
  private Label status;
  private Label tally;
  private MineMap m;
  private Thread t;

  private final int unit = 20;	// Size of a unit square
  private final int boxsize = unit - 1;	// Size of a painted square
  private final int neighbors = 8;	// Max number of neighbors of a square
  private final int left = 7;		// Center text by moving left
  private final int up = -5;		// Center text by moving up
  private final long delay = 200;	// Sleep time after a display change
  private String digit[];		// Translates numbers to strings
  private boolean map_needs_update;	// Does entire map need updating?
  private boolean needs_update[][];	// Does square need updating?
  private int width;
  private int height;
  private boolean die = false;          // Should thread die?

  /**
   * Create a displayable mine map.
   * @param s           strategy
   * @param parent	applet running the game
   * @param mines	number of mines in mine map
   * @param rows        rows in map
   * @param columns     columns in map
   * @param status      label for status reports
   * @param tally       label for tally of mines minus marks
   * @return 		a displayable mine map
   */
  public DisplayMap(Strategy s, int mines, int rows, int columns,
		    Label status, Label tally) {
    this.s = s;
    this.mines = mines;
    r = rows;
    c = columns;
    this.status = status;
    this.tally = tally;
    m = new MineMap(mines, rows, columns);
    width = unit*c + 1;
    height =  unit*r + 1;
    digit = new String[neighbors + 1];
    for (int d = 0; d <= neighbors; d++)
      digit[d] = Integer.toString(d);
    needs_update = new boolean[r][c];
  }

  void init() {
    show_tally();
    map_needs_update = true;
    repaint();
  }

  public Dimension getMinimumSize() {
    return new Dimension(width, height);
  }

  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  public void paint(Graphics g) {
    map_needs_update = true;
    update(g);
  }

  public void update(Graphics g) {
    if (map_needs_update) {
      map_needs_update = false;
      g.clearRect(1, 1, unit*c, unit*r);
      for (int y = 0; y < r; y++)
	for (int x = 0; x < c; x++)
	  needs_update[y][x] = !m.unprobed_map[y][x]
	    || m.mine_map[y][x] < 0
	      || m.mark_map[y][x];
    }

    /* First class function would allow the capture of the repeated
       pattern below.  Making a separate class seems to be too much
       of a pain. */
    g.setColor(Color.black);	// Draw border
    g.drawRect(0, 0, unit*c, unit*r);
    for (int y = 0; y < r; y++) { // Draw digits for probed squares
      int z = unit * (r - y) + 1 + up; // Use right handed coodinates
      for (int x = 0; x < c; x++)
	if (needs_update[y][x]
	    && !m.unprobed_map[y][x]
	    && m.mine_map[y][x] >= 0) {
	  g.drawString(digit[m.mine_map[y][x]], unit*x + 1 + left, z);
	  needs_update[y][x] = false;
	}
    }

    g.setColor(Color.blue);	// Draw correctly marked squares
    for (int y = 0; y < r; y++) {
      int z = unit * (r - 1 - y) + 1;
      for (int x = 0; x < c; x++)
	if (needs_update[y][x]
	    && m.mark_map[y][x]
	    && m.mine_map[y][x] < 0) {
	  g.fillRect(unit*x + 1, z, boxsize, boxsize);
	  needs_update[y][x] = false;
	}
    }

    g.setColor(Color.black);	// Draw incorrectly marked squares
    for (int y = 0; y < r; y++) {
      int z = unit * (r - 1 - y) + 1;
      for (int x = 0; x < c; x++)
	if (needs_update[y][x]
	    && m.mark_map[y][x]
	    && m.mine_map[y][x] >= 0) {
	  g.fillRect(unit*x + 1, z, boxsize, boxsize);
	  needs_update[y][x] = false;
	}
    }

    g.setColor(Color.yellow);	// Draw unmarked squares that contain mines
    for (int y = 0; y < r; y++) {
      int z = unit * (r - 1 - y) + 1;
      for (int x = 0; x < c; x++)
	if (needs_update[y][x]
	    && m.mine_map[y][x] < 0
	    && m.unprobed_map[y][x]
	    && !m.mark_map[y][x]) {
	  g.fillRect(unit*x + 1, z, boxsize, boxsize);
	  needs_update[y][x] = false;
	}
    }

    g.setColor(Color.red);	// Draw probed squares that contain mines
    for (int y = 0; y < r; y++) {
      int z = unit * (r - 1 - y) + 1;
      for (int x = 0; x < c; x++)
	if (needs_update[y][x]
	    && m.mine_map[y][x] < 0
	    && !m.unprobed_map[y][x]) {
	  g.fillRect(unit*x + 1, z, boxsize, boxsize);
	  needs_update[y][x] = false;
	}
    }
  }

  public int probe(int x, int y) {
    int q0 = m.look(x, y);
    int q1 = m.probe(x, y);
    if (q0 != q1) {
      needs_update[y][x] = true;
      repaint();			// When a change to the display is made
      pause();				// pause so it can be seen
    }
    return q1;
  }

  public int mark(int x, int y) {
    int q0 = m.look(x, y);
    int q1 = m.mark(x, y);
    if (q0 != q1) {
      needs_update[y][x] = true;
      repaint();			// When a change to the display is made
      pause();			        // pause so it can be seen
    }
    show_tally();
    return q1;
  }

  public int unmark(int x, int y) {
    int q0 = m.look(x, y);
    int q1 = m.unmark(x, y);
    if (q0 != q1) {
      needs_update[y][x] = true;
      repaint();			// When a change to the display is made
      pause();			        // pause so it can be seen
    }
    show_tally();
    return q1;
  }

  private void pause() {
    if (die)	                        // Kill thread if stop was called
      throw new ThreadDeath();
    try {
      Thread.sleep(delay);
    } catch (InterruptedException e){}
  }

  private int tally_state = -1;

  private void show_tally() {
    int current_tally = mines_minus_marks();
    if (current_tally != tally_state) {
      tally_state = current_tally;
      tally.setText(Integer.toString(current_tally));
    }
  }

  /* proxy routines so that DisplayMap implements a Map */

  public int mines_minus_marks() {
    return m.mines_minus_marks();
  }

  public int look(int x, int y) {
    return m.look(x, y);
  }

  public boolean won() {
    return m.won();
  }

  public boolean done() {
    return m.done();
  }

  public boolean probed() {
    return m.probed();
  }

  public int rows() {
    return m.rows();
  }

  public int columns() {
    return m.columns();
  }

  public int pick(int n) {
    return m.pick(n);
  }

  public void display() {
    m.display();
  }

  /**
   * Runs a strategy.
   * @see Strategy#play
   */
  public void run() {
    s.play(this);
    show_result();
  }

  private void show_result() {
    if (m.done())
      if (m.won())
	show_status("PGMS: play ended--game won");
      else
	show_status("PGMS: play ended--game lost");
    else
      show_status("PGMS: play ended");
  }

  private void show_status(String message) {
    status.setText(message);
  }

  synchronized void start() {
    if (t != null && t.isAlive())
      return;
    m = new MineMap(mines, r, c);
    show_tally();
    map_needs_update = true;
    repaint();
    die = false;
    t = new Thread(this);
    t.start();
    show_status("PGMS: playing...");
  }

  synchronized void stop() {
    if (t == null)
      return;
    if (t.isAlive()) {
      die = true;
      show_status("PGMS: play stopped");
    }
    else
      t = null;
  }

}
