package qlearner;

import map.Map;
import state.LocalState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chewb on 3/23/2017.
 */
public class ActionHistory {

    public List<ActionResult> actionResultList = new ArrayList<>();
    public int localStateSize;

    public ActionHistory(int localStateSize){
        this.localStateSize = localStateSize;
    }

    public void saveAction(Map m, int x, int y, int result){

        ActionResult actionResult = new ActionResult(m, x, y, localStateSize);

        boolean foundExisiting = false;

        for(ActionResult a : actionResultList){
            if(a.equals(actionResult)){
                actionResult = a;
                actionResult.count++;
                foundExisiting = true;
                break;
            }
        }


        if(result == m.BOOM){
            actionResult.bombed++;
        } else if(result == m.MARKED){
            actionResult.marked++;
        } else {
            actionResult.empty++;
        }

        System.out.print("Saving State after selecting (" + x + ", " + y + ") with result (" + result + ")...");
        System.out.print("Current=" + actionResult.count);
        System.out.print(" | ");
        System.out.print("Total=" + actionResultList.size());
        System.out.print(" | ");
        System.out.print("Bombed=" + actionResult.bombed);
        System.out.print(" | ");
        System.out.print("Marked=" + actionResult.marked);
        System.out.print(" | ");
        System.out.print("Empty=" + actionResult.empty);
        System.out.println();

        if(!foundExisiting){
            actionResultList.add(actionResult);
        }
    }
}
