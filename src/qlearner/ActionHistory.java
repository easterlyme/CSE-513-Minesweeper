package qlearner;

import map.Map;
import state.LocalState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by chewb on 3/23/2017.
 */
public class ActionHistory {

    public List<ActionResult> actionResultList = new ArrayList<>();
    private List<ActionResult> newActionsSinceLastSave = new ArrayList<>();
    public int localStateSize;
    public boolean local;

    public ActionHistory(int localStateSize, boolean local){
        this.localStateSize = localStateSize;
        this.local = local;
    }

    public ActionResult getExistingAction(Map m, int x, int y){
        ActionResult actionResult = new ActionResult(m, x, y, localStateSize, local);

        for(ActionResult a : actionResultList){
            if(a.equals(actionResult)){
                return a;
            }
        }

        return null;
    }

    public void saveAction(Map m, int x, int y, int result, boolean debugLog){

        ActionResult actionResult = new ActionResult(m, x, y, localStateSize, local);

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
            System.out.print("Total=" + actionResultList.size());
            System.out.print(" | ");
            System.out.print("Current=" + actionResult.count);
            System.out.print(" | ");
            System.out.print("Bombed=" + actionResult.bombed);
            System.out.print(" | ");
            System.out.print("Empty=" + actionResult.empty);
            System.out.print(" | ");
            System.out.print("QValue=" + actionResult.getQValue());
            System.out.println();
        }

        if(!foundExisiting){
            actionResultList.add(actionResult);
            newActionsSinceLastSave.add(actionResult);
        }
    }

    public void saveToCsv(String filename){
        try (PrintStream out = new PrintStream(new FileOutputStream(filename))) {
            String headerStr = "x,y,bombed,empty,";
            for(int j = 0; j < localStateSize; j++){
                for(int i = 0; i < localStateSize; i++){
                    headerStr += j + " " + i + ",";
                }
            }
            out.println(headerStr);

            for(ActionResult a : actionResultList){
                String rowStr = a.x + "," + a.y + "," + a.bombed + "," + a.empty + ",";
                for(int j = 0; j < localStateSize; j++){
                    for(int i = 0; i < localStateSize; i++){
                        rowStr += a.localState[j][i] + ",";
                    }
                }
                out.println(rowStr);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void loadFromCsv(String filename){
        try  {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            try {
                String line = br.readLine();
                line = br.readLine();

                while (line != null) {
                    String[] lineSplit = line.split(",");
                    int x = Integer.parseInt(lineSplit[0]);
                    int y = Integer.parseInt(lineSplit[1]);
                    int bombed = Integer.parseInt(lineSplit[2]);
                    int empty = Integer.parseInt(lineSplit[3]);
                    int[][] states = new int[localStateSize][localStateSize];

                    int count = 0;
                    for (int j = 0; j < localStateSize; j++){
                        for (int i = 0; i < localStateSize; i++){
                            states[j][i] = Integer.parseInt(lineSplit[4 + count]);
                            count++;
                        }
                    }

                    ActionResult actionResult = new ActionResult(states, x, y, localStateSize);

                    actionResult.bombed = bombed;
                    actionResult.empty = empty;
                    actionResult.count = bombed + empty;

                    actionResultList.add(actionResult);

                    line = br.readLine();
                }
            } finally {
                br.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
