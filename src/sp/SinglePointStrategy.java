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
import qlearner.ActionHistory;
import set.*;
import state.GameState;
import state.LocalState;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public List<GameState> gameStates = new ArrayList<GameState>();
    public List<LocalState> localStates = new ArrayList<LocalState>();

    private ActionHistory actionHistory;

  /**
   * Invoke the Single Point Strategy.
   * @see Strategy
   */
    public void play(Map m) {
        actionHistory = PGMS.actionHistory3x3;

        for (;;) {
            int y = m.pick(m.rows());
            int x = m.pick(m.columns());
            int q = m.probe(x, y);    // Guess a point to be probed
            actionHistory.saveAction(m, x, y, q, false);

            // Opps! Bad guess
            if (Map.BOOM == q) {
                break;
            } else if (q >= 0) {
	            apply(m, x, y);		// Try strategy at this point
	            if (m.done())
	                break;		// We win!
            }
        }
    }

  /*
   * This routine applies the Single Point Strategy.
   */
    private void apply(Map m, int x, int y) {
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

    private int unknowns_near(Map m, int x, int y) {
        int u = 0;
        for (int j = y - 1; j < y + 2; j++)
            for (int i = x - 1; i < x + 2; i++)
	            if (Map.UNPROBED == m.look(i, j))
	                u++;
        return u;
    }

    private int marks_near(Map m, int x, int y) {
        int u = 0;
        for (int j = y - 1; j < y + 2; j++)
            for (int i = x - 1; i < x + 2; i++)
	            if (Map.MARKED == m.look(i, j))
	                u++;
        return u;
    }

  /*
   * After probing, adjoin all unprobed points near the newly
   * probed point for future consideration by the strategy.
   */
    private Set probe_around(Map m, int x, int y, Set s) {
        for (int j = y - 1; j < y + 2; j++)
            for (int i = x - 1; i < x + 2; i++)
	            if (Map.UNPROBED == m.look(i, j)){
                    m.probe(i, j);
                    actionHistory.saveAction(m, x, y, 0, false);
                }

        return adjoin_around(m, x, y, s);
    }

  /*
   * After marking, adjoin all unprobed points near the mark
   * for future consideration by the strategy.
   */
    private Set mark_around(Map m, int x, int y, Set s) {
        for (int j = y - 1; j < y + 2; j++)
            for (int i = x - 1; i < x + 2; i++)
	            if (Map.UNPROBED == m.look(i, j)){
                    m.mark(i, j);
                    actionHistory.saveAction(m, x, y, Map.MARKED, false);
                }

        return adjoin_around(m, x, y, s);
    }

    private Set adjoin_around(Map m, int x, int y, Set s) {
        for (int j = y - 2; j < y + 3; j++)
            for (int i = x - 2; i < x + 3; i++)
	            if (m.look(i, j) >= 0)
	                s = s.adjoin(new Point(i, j));
        return s;
    }

    private void saveLocalState(Map m, int x, int y, int result){
        LocalState state = new LocalState(m, x, y, 3);

        boolean foundExisiting = false;
        for(LocalState s : localStates){
            if(s.equals(state)){
                state = s;
                state.count++;
                foundExisiting = true;
                break;
            }
        }

        if(result == Map.BOOM){
            state.bombed++;
        } else if(result == Map.MARKED){
            state.marked++;
        } else {
            state.empty++;
        }
        // state.print();

        System.out.print("Saving State after selecting (" + x + ", " + y + ") with result (" + result + ")...");
        System.out.print("Current=" + state.count);
        System.out.print(" | ");
        System.out.print("Total=" + localStates.size());
        System.out.print(" | ");
        System.out.print("Bombed=" + state.bombed);
        System.out.print(" | ");
        System.out.print("Marked=" + state.marked);
        System.out.print(" | ");
        System.out.print("Empty=" + state.empty);
        System.out.println();

        if(!foundExisiting){
            localStates.add(state);
        }
    }

    private void saveLocalStatesToFile(){
        try (PrintStream out = new PrintStream(new FileOutputStream("data.csv"))) {
            String headerStr = "";
            for(int j = 0; j < 3; j++){
                for(int i = 0; i < 3; i++){
                    headerStr += j + " " + i + ",";
                }
            }
            headerStr += "bombed,marked,empty,bombprob";
            out.println(headerStr);

            for(LocalState state : localStates){
                String rowStr = "";
                for(int j = 0; j < 3; j++){
                    for(int i = 0; i < 3; i++){
                        rowStr += state.state[j][i] + ",";
                    }
                }

                rowStr += state.bombed + "," + state.marked + "," + state.empty + "," + state.getBombProbability();
                out.println(rowStr);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveState(Map m, int x, int y){
        System.out.print("");

        GameState state = new GameState(m);

        GameState cachedState = null;
        for(GameState s : gameStates){
            if(s.equals(state)){
                cachedState = s;
                break;
            }
        }

        if(cachedState != null){
            cachedState.count++;
            //System.out.println(x + ", " + y);
            // cachedState.print();
            //while(true);
        } else {
            cachedState = state;
            gameStates.add(cachedState);

        }
        if(gameStates.size() % 10000 == 0){
            System.out.println("Saving State after selecting (" + x + ", " + y + ")...[Current=" + cachedState.count + " | Total=" + gameStates.size() + "]");
        }
    }
}
