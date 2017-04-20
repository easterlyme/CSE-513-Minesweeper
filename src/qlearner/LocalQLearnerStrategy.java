package qlearner;

import map.Map;
import map.PGMS;
import map.Strategy;

import javax.swing.*;

/**
 * Created by chewb on 3/23/2017.
 */
public class LocalQLearnerStrategy implements Strategy {

    private ActionHistory actionHistory;

    @Override
    public void play(Map m) {
        actionHistory = PGMS.actionHistory;

        while(!m.done()){
            ActionResult bestResult = null;

            for(int y = 0; y < m.rows(); y++){
                for(int x = 0; x < m.columns(); x++){
                    ActionResult existingAction = actionHistory.getExistingAction(m, x, y);
                    if(existingAction != null && (bestResult == null || existingAction.getQValue() > bestResult.getQValue())){
                        bestResult = existingAction;
                    }
                }
            }

            int q;

            if(bestResult != null && m.look(bestResult.x, bestResult.y) == Map.UNPROBED && bestResult.getQValue() > 0){
                q = m.probe(bestResult.x, bestResult.y);
                System.out.println("Probing tile " + bestResult.x + ", " + bestResult.y + " with Q value of: " + bestResult.getQValue());
                actionHistory.saveAction(m, bestResult.x, bestResult.y, q, false);
            } else {
                int x = m.pick(m.columns());
                int y = m.pick(m.rows());
                q = m.probe(x, y);
                actionHistory.saveAction(m, x, y, q, false);
            }
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
