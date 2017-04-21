package qlearner;

import map.Map;
import map.PGMS;
import map.Strategy;

import java.util.ArrayList;

/**
 * Created by chewb on 4/20/2017.
 */
public class QLearnerStrategy implements Strategy {

    private ActionHistory actionHistory;

    @Override
    public void play(Map m) {
        actionHistory = PGMS.actionHistory3x3;

        double probeThreshold = 0.7;
        double markThreshold = 0;
        ArrayList<Tile> fringeTiles = new ArrayList<>();

        Tile currentTile = new Tile(m.pick(m.columns()), m.pick(m.rows()), Map.UNPROBED);
        m.probe(currentTile.x, currentTile.y);

        while(!m.done()){
            GetFringeTiles(m, currentTile.x, currentTile.y, fringeTiles);

            // if tiles left are only in fringe, then select tiles in order of best q value
            if(fringeTiles.size() == ((m.columns() * m.rows()) - m.Revealed())){
                probeThreshold = 0;
            }

            currentTile = SelectTileFromFringe(m, fringeTiles, probeThreshold, markThreshold);

            // select random tile if no good ones came from fringe
            if(currentTile == null){
                do {
                    int x = m.pick(m.columns());
                    int y = m.pick(m.rows());
                    currentTile = new Tile(x, y, m.look(x, y));
                } while(currentTile.state != Map.UNPROBED || fringeTiles.contains(currentTile));
            }

            int result = m.probe(currentTile.x, currentTile.y);
            PGMS.actionHistory3x3.saveAction(m, currentTile.x, currentTile.y, result,  false);
            PGMS.actionHistory5x5.saveAction(m, currentTile.x, currentTile.y, result, false);
            fringeTiles.remove(currentTile);
        }
    }

    public Tile SelectTileFromFringe(Map m, ArrayList<Tile> list, double probeThreshold, double markThreshold){

        Tile bestTile = null;

        ArrayList<Tile> unknownTileStates = new ArrayList<>();
        ArrayList<Tile> removeTiles = new ArrayList<>();

        for(Tile t : list){
            ActionResult exisitingState = actionHistory.getExistingAction(m, t.x, t.y);

            // only check states with some data
            if(exisitingState == null || exisitingState.count < 5){
                unknownTileStates.add(t);
                continue;
            }

            t.qValue = exisitingState.getQValue();

            // check if should mark
            if(t.qValue <= markThreshold){
                if(!m.HasMine(t.x, t.y)){
                    // don't cheat. we failed the mark so lets teach learner and fail.
                    m.Finish();
                    return t;
                } else {
                    // mark then remove tile from fringe
                    m.mark(t.x, t.y);
                    PGMS.actionHistory3x3.saveAction(m, t.x, t.y, Map.BOOM, false);
                    PGMS.actionHistory5x5.saveAction(m, t.x, t.y, Map.BOOM, false);
                    removeTiles.add(t);
                    break;
                }
            }

            if(bestTile == null || t.qValue > bestTile.qValue){
                bestTile = t;
            }
        }

        if(removeTiles.size() > 0){
            list.removeAll(removeTiles);
            return SelectTileFromFringe(m, list, probeThreshold, markThreshold);
        }

        // if highest q value isn't guaranteed probed, then prioritize unknown states for sake of learning
        if((bestTile == null || bestTile.qValue < 1) && unknownTileStates.size() > 0){
            return unknownTileStates.get(0);
        }

        // return no tile if below threshold so that random tile can be picked
        if(bestTile != null && bestTile.qValue < probeThreshold){
            return null;
        }

        return bestTile;
    }

    public void GetFringeTiles(Map m, int x, int y, ArrayList<Tile> list){
        for(int j = 1 + y; j >= -1 + y; j--){
            for(int i = -1 + x; i <= 1 + x; i++){
                if(i == y && j == x){
                    continue;
                }

                int state = m.look(i, j);

                if(state == Map.UNPROBED){
                    Tile tile = new Tile(i, j, state);

                    boolean contains = false;
                    for(Tile t : list){
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
