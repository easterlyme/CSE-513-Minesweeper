package qlearner;

import map.Map;
import state.LocalState;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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

    public ActionResult getExistingAction(Map m, int x, int y){
        ActionResult actionResult = new ActionResult(m, x, y, localStateSize);

        for(ActionResult a : actionResultList){
            if(a.equals(actionResult)){
                return a;
            }
        }

        return null;
    }

    public void saveAction(Map m, int x, int y, int result, boolean debugLog){

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

        if(debugLog){
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
        }

        if(!foundExisiting){
            actionResultList.add(actionResult);
        }
    }

    public void saveToCsv(String filename){
        try (PrintStream out = new PrintStream(new FileOutputStream(filename))) {
            String headerStr = "";
            for(int j = 0; j < localStateSize; j++){
                for(int i = 0; i < localStateSize; i++){
                    headerStr += j + " " + i + ",";
                }
            }
            headerStr += "bombed,empty,bomb_certainty";
            out.println(headerStr);

            for(ActionResult a : actionResultList){
                String rowStr = "";
                for(int j = 0; j < localStateSize; j++){
                    for(int i = 0; i < localStateSize; i++){
                        rowStr += a.localState[j][i] + ",";
                    }
                }

                rowStr += a.bombed + "," + a.empty + "," + a.getBombCertainty();
                out.println(rowStr);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sortBombsDescending(){
        // multiply by -1 to sort by descending
        Collections.sort(actionResultList, (o1, o2) -> -1 * (o1.bombed > o2.bombed ? 1 :(o1.bombed < o2.bombed ? -1 : 0)));
    }

    public void sortBombCertaintyDescending(){
        // multiply by -1 to sort by descending
        Collections.sort(actionResultList, (o1, o2) -> -1 * (o1.getBombCertainty() > o2.getBombCertainty() ? 1 :(o1.getBombCertainty() < o2.getBombCertainty() ? -1 : 0)));
    }
}
