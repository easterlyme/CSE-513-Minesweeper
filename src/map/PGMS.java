package map;

import qlearner.ActionHistory;

import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;

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

/**
 * The class PGMS creates and displays minesweeper games played by
 * Java programs.  A user contributes a Java class that conforms to
 * the interface called Strategy.  The strategy plays a game of
 * minesweeper using the methods provided by the Map class. <p>
 * This class provides both an applet for graphical presentations
 * using browsers, and a main routine for applications.
 *
 * @author John D. Ramsdell
 * @version February 1997
 * @see Strategy
 * @see map.Map
 */
public class PGMS extends Applet {
    // The default strategy
    private static String default_strategy_name = "sp.SinglePointStrategy";
    Strategy s;            // The selected strategy
    int mines = 10;        // Beginner game
    int rows = 8;
    int columns = 8;
    DisplayMap m;            // Panel for map display

    public static ActionHistory actionHistory3x3 = new ActionHistory(3, true);
    public static ActionHistory actionHistory5x5 = new ActionHistory(5, true);
    public static ActionHistory actionHistory8x8 = new ActionHistory(8, false);

    public static boolean isSingleRun;

    /**
     * Application entry point.
     *
     * @param args program arguments
     *             <dl>
     *             <dt> <code>-b</code>
     *             <dd> play a beginner game
     *             <dt> <code>-i</code>
     *             <dd> play an intermediate game
     *             <dt> <code>-e</code>
     *             <dd> play an expert game
     *             <dt> <code>-s</code> <var>strategy class name</var>
     *             <dd> play with given strategy
     *             <dt> <code>-n</code> <var>number of games</var>
     *             <dd> play multiple games - graphics will be disabled
     *             with more than one game
     *             </dl>
     */
    public static void main(String args[]) {
        String strategy_name = default_strategy_name;
        String game_name = "beginner";
        int mines = 10;        // Beginner game
        int rows = 8;
        int columns = 8;
        int tries = 1;
        int wins = 0;
        int SumRevealed = 0;
        int boardSizeSum = 0;
        int probed = 0;

        for (int i = 0; i < args.length; i++) // Process args
            if (args[i].equals("-i")) {
                game_name = "intermediate";
                mines = 40;            // Intermediate game
                rows = 13;
                columns = 15;
            } else if (args[i].equals("-e")) {
                game_name = "expert";
                mines = 99;            // Expert game
                rows = 16;
                columns = 30;
            } else if (args[i].equals("-b")) {
                game_name = "beginner";
                mines = 10;            // Beginner game
                rows = 8;
                columns = 8;
            } else if (args[i].equals("-s"))
                if (++i >= args.length) { // User supplied strategy
                    usage();
                    return;
                } else
                    strategy_name = args[i];
            else if (args[i].equals("-n"))
                if (++i >= args.length) { // Game count supplied
                    usage();
                    return;
                } else
                    try {
                        tries = Math.max(1, Integer.parseInt(args[i]));
                    } catch (NumberFormatException e) {
                        System.out.println("Bad number of games");
                        usage();
                        return;
                    }
            else {
                usage();
                return;
            }

        Strategy s;
        try {
            s = (Strategy) Class.forName(strategy_name).newInstance();
        } catch (Exception e) {
            System.out.println("Cannot create strategy " + strategy_name);
            usage();
            return;
        }

        actionHistory3x3.loadFromCsv("action_history_3x3.csv");
        //actionHistory5x5.loadFromCsv("action_history_5x5.csv");
        //actionHistory8x8.loadFromCsv("action_history_8x8.csv");

        if (tries == 1) {
            isSingleRun = true;
            Frame f = new Frame("PGMS");
            f.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

            PGMS p = new PGMS(s, mines, rows, columns);

            MenuBar mb = new MenuBar();
            f.setMenuBar(mb);
            Menu m = new Menu("File");
            mb.add(m);
            MenuItem mi = new MenuItem("Exit", new MenuShortcut(KeyEvent.VK_X));
            mi.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    System.exit(0);
                }
            });
            m.add(mi);

            p.init_display(strategy_name);

            f.add(p);
            f.pack();
            f.setVisible(true);

            p.start();
            return;
        }

        System.out.print("Playing " + tries + " " + game_name + " games");
        System.out.println(" using strategy " + strategy_name);

        //int states3x3 = actionHistory3x3.actionResultList.size();
        //int states5x5 = actionHistory5x5.actionResultList.size();

        isSingleRun = false;

        for (int n = 1; n <= tries; n++) {
            Map m = new MineMap(mines, rows, columns); // Create mine map
            try {
                s.play(m);            // Play game
            } catch (Exception e) {
                System.out.println(e.toString());
            }
            if (m.won()) wins++;    // Record results
            if (m.probed()) probed++;

            if (!m.won() && m.probed()) { //Ignore wins and games where first click fails
                SumRevealed += m.Revealed();
                boardSizeSum += m.rows() * m.columns();
            }

            if(n % 100 == 0){
                System.out.printf("%d wins in %d tries -- %.2f%%", wins, n, percent(wins, n));
                if (probed > 0) {
                    System.out.printf(", with %d standard tries -- %.2f%%", probed, percent(wins, probed));
                    if (SumRevealed > 0 && boardSizeSum > 0)
                        System.out.printf(", with Average Board reveal: %.2f%%", percent(SumRevealed, boardSizeSum));
                }
                System.out.println(".");
                //System.out.print("New 3x3 states: " + (actionHistory3x3.actionResultList.size() - states3x3));
                //System.out.println(" -- New 5x5 states: " + (actionHistory5x5.actionResultList.size() - states5x5));
                //states3x3 = actionHistory3x3.actionResultList.size();
                //states5x5 = actionHistory5x5.actionResultList.size();
            }

            if(n % 1000 == 0){
                actionHistory3x3.saveToCsv("action_history_3x3.csv");
                // actionHistory5x5.saveToCsv("action_history_5x5.csv");
            }
        }

        actionHistory3x3.saveToCsv("action_history_3x3.csv");
        // actionHistory5x5.saveToCsv("action_history_5x5.csv");
    }

    /**
     * Create a PGMS instance for an application
     */
    PGMS(Strategy s, int mines, int rows, int columns) {
        this.s = s;
        this.mines = mines;
        this.rows = rows;
        this.columns = columns;
    }

    private static float percent(int n, int d) {
        return (float) (200 * n + d) / (2 * d);
    }

    private static void usage() {
        System.out.println("Usage: java PGMS [-b] [-i] [-e]"
                + " [-s strategy_name] [-n number_of_games]");
        System.out.println("Beginner:     -b");
        System.out.println("Intermediate: -i");
        System.out.println("Expert:       -e");
    }

    /**
     * Create applet PGMS
     */
    public PGMS() {
    }

    /**
     * Initialize applet by processing the attributes.
     * <dl>
     * <dt> <code>strategy</code>
     * <dd> class name of user supplied strategy
     * <dt> <code>game</code>
     * <dd> level of game, one of
     * <ul>
     * <li> <code>beginner</code>
     * <li> <code>intemediate</code>
     * <li> <code>expert</code>
     * </ul>
     * </dl>
     * <p>
     * Sample:
     * <pre>
     * &lt;applet codebase="classes" code="map/PGMS.class"
     *       width=302 height=262&gt;
     * &lt;param name="strategy" value="eqn.EqnStrategy"&gt;
     * &lt;param name="game" value="intermediate"&gt;
     * &lt;/applet&gt;
     * </pre>
     */
    public void init() {
        String strategy_name = getParameter("strategy");
        String game = getParameter("game");

        if (strategy_name == null)
            strategy_name = default_strategy_name;

        // Create strategy
        try {
            s = (Strategy) Class.forName(strategy_name).newInstance();
        } catch (Exception e) {
            return;
        }

        if (game != null) {        // Set game level
            if (game.equals("intermediate")) {
                mines = 40;            // Intermediate game
                rows = 13;
                columns = 15;
            } else if (game.equals("expert")) {
                mines = 99;            // Expert game
                rows = 16;
                columns = 30;
            }
        }
        init_display(strategy_name);
    }

    /**
     * This routine creates the panels that make up the display.
     */
    private void init_display(String strategy_name) {
        final int gap = 5;
        setLayout(new BorderLayout());
        setBackground(Color.lightGray);

    /* The status panel displays status information  */
        final Label status = new Label("Strategy: " + strategy_name,
                Label.CENTER);
        add(status, "South");

    /* The tally panel displays the number of
       mines minus the number of marks. */
        Label tally = new Label("999", Label.CENTER);
        tally.setBackground(Color.white);

    /* This panel displays the map. */
        Panel map_panel = new Panel(new FlowLayout(FlowLayout.CENTER, gap, 0));
        final DisplayMap display = new DisplayMap(s, mines, rows, columns,
                status, tally);
        display.setBackground(Color.white);
        display.init();
        m = display;        // Save DisplayMap for the start and stop method
        map_panel.add(display);

    /* This panel contains the tally and the control buttons. */
        Panel button_panel = new Panel(new FlowLayout(FlowLayout.LEFT, gap, 0));

        button_panel.add(tally);

        Button b = new Button("Start");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display.start();
            }
        });
        button_panel.add(b);

        b = new Button("Stop");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display.stop();
            }
        });
        button_panel.add(b);

        Panel p = new Panel(new ColumnLayout(0, gap));
        p.add(button_panel);
        p.add(map_panel);
        add(p, "Center");
    }

    public void start() {
        //while(true){
            m.start();
        //}

    }

    public void stop() {
        m.stop();
    }

}
