package qlearner;

import map.Map;
import map.PGMS;
import map.Strategy;

/**
 * Created by chewb on 3/23/2017.
 */
public class LocalQLearnerStrategy implements Strategy {

    private ActionHistory actionHistory;

    @Override
    public void play(Map m) {
        actionHistory = PGMS.actionHistory;

        while(!m.done()){
            int y = m.pick(m.rows());
            int x = m.pick(m.columns());
            int q = m.probe(x, y);
            actionHistory.saveAction(m, x, y, q, false);
        }
    }

    private boolean probeGuaranteedEmpty(Map m){
        for(int y = 0; y < m.rows(); y++){
            for(int x = 0; x < m.columns(); x++){
                if(m.look(x, y) == Map.UNPROBED){
                    ActionResult tempAction = actionHistory.getExistingAction(m, x, y);
                    if(tempAction != null && tempAction.count == tempAction.empty){
                        int q = m.probe(x, y);
                        actionHistory.saveAction(m, x, y, q, false);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean probeUncertain(Map m){
        for(int y = 0; y < m.rows(); y++){
            for(int x = 0; x < m.columns(); x++){
                if(m.look(x, y) == Map.UNPROBED){
                    int q = m.probe(x, y);
                    actionHistory.saveAction(m, x, y, q, false);
                    return true;
                }
            }
        }

        return false;
    }
}
