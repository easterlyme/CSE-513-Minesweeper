package qlearner;

import map.Map;
import map.PGMS;
import map.Strategy;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by chewb on 4/20/2017.
 */
public class QLearnerStrategy implements Strategy {

    private ActionHistory actionHistory;

    @Override
    public void play(Map m) {
        actionHistory = PGMS.actionHistory;
        //System.out.println(actionHistory.actionResultList.size());
        ArrayList<Tile> fringeTiles = new ArrayList<>();

        Queue<Tile> corners = new LinkedList<>();
        corners.add(new Tile(0, m.rows() - 1, Map.UNPROBED));
        corners.add(new Tile(m.columns() - 1, m.rows() - 1, Map.UNPROBED));
        corners.add(new Tile(0, 0, Map.UNPROBED));
        corners.add(new Tile(m.columns() - 1, 0, Map.UNPROBED));


        Tile currentTile = corners.remove();

        m.probe(currentTile.x, currentTile.y);
        while(!m.done()){
            GetFringeTiles(m, currentTile.x, currentTile.y, fringeTiles);

            currentTile = SelectTileFromFringe(m, fringeTiles);

            if(currentTile == null){
                if(!corners.isEmpty()){
                    currentTile = corners.remove();
                } else {
                    do {
                        int x = m.pick(m.columns() - 1);
                        int y = m.pick(m.rows() - 1);
                        currentTile = new Tile(x, y, m.look(x, y));
                    } while(currentTile.state != Map.UNPROBED);
                }
            }

            int result = m.probe(currentTile.x, currentTile.y);
            actionHistory.saveAction(m, currentTile.x, currentTile.y, result, false);
            fringeTiles.remove(currentTile);
        }
    }

    public Tile SelectTileFromFringe(Map m, ArrayList<Tile> list){
        Tile bestTile = null;
        double threshold = 0.95;
        boolean allBelowThreshold = true;

        for(Tile t : list){
            ActionResult exisitingState = actionHistory.getExistingAction(m, t.x, t.y);
            if(exisitingState == null || exisitingState.count < 10){
                allBelowThreshold = false;
                bestTile = t;
                break;
            }

            t.qValue = exisitingState.getQValue();

            if(t.qValue == 0){
                m.mark(t.x, t.y);
                // System.out.println("Marking");
            }

            if(t.qValue < threshold){
                continue;
            }

            allBelowThreshold = false;

            if(bestTile == null || t.qValue > bestTile.qValue){
                bestTile = t;
            }
        }

        if(allBelowThreshold){
            return null;
        }

        // System.out.println("Select best tile with q value: " + bestTile.qValue);

        return bestTile;
    }

    public void GetFringeTiles(Map m, int x, int y, ArrayList<Tile> list){
        //System.out.println("Getting fringe tiles for " + x + ", " + y);
        for(int j = 1 + y; j >= -1 + y; j--){
            for(int i = -1 + x; i <= 1 + x; i++){
                if(i == y && j == x){
                    continue;
                }

                int state = m.look(i, j);
                // System.out.println(i + ", " + j + " : " + state);
                if(state == Map.UNPROBED){
                    Tile tile = new Tile(i, j, state);

                    boolean contains = false;
                    for(Tile t : list){
                        // System.out.println("Fringe: " + t.x + ", " + t.y);
                        if(t.x == tile.x && t.y == tile.y){
                            contains = true;
                            break;
                        }
                    }

                    if(!contains){
                        list.add(tile);
                    }
                }
            }
        }
        //System.out.println("Tiles in fringe: " + list.size());
    }

    class Tile {
        private int x;
        private int y;
        private int state;
        private double qValue = 1.0;

        public Tile(int x, int y, int state){
            this.x = x;
            this.y = y;
            this.state = state;
        }

        @Override
        public boolean equals(Object obj) {
            Tile tile = (Tile) obj;
            return tile.x == x && tile.y == y;
        }
    }
}
