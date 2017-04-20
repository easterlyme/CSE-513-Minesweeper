package qlearner;

import map.Map;

/**
 * Created by chewb on 3/23/2017.
 */
public class ActionResult {

    public int x;
    public int y;
    public int size;
    public int center;
    public int[][] localState;
    public int count = 1;
    public int result;

    public int marked = 0;
    public int bombed = 0;
    public int empty = 0;

    public ActionResult(int[][] state, int x, int y, int size){
        this.x = x;
        this.y = y;
        this.size = size;
        this.center = (size - 1) / 2;
        this.localState = state;
    }

    public ActionResult(Map m, int x, int y, int size){
        if(size % 2 == 0){
            // throw new IllegalArgumentException("size cannot be even");
        }

        this.x = x;
        this.y = y;
        this.size = size;
        this.center = (size - 1) / 2;
        this.localState = new int[size][size];

        for (int j = 0; j < size; j++){
            for (int i = 0; i < size; i++){
                this.localState[j][i] = m.look(i, size - j - 1);
            }
        }

        /*
        for (int j = y - center; j <= y + center; j++){
            for (int i = x - center; i <= x + center; i++){
                if(i == x && j == y){
                    // we don't care what this value is because it is ignored in equality check
                    this.localState[j2][i2] = 0;
                } else {
                    this.localState[j2][i2] = m.look(i, j);
                }
                i2++;
            }
            i2 = 0;
            j2++;
        }
        */
    }

    public double getQValue(){
        return ((double) empty) / ((double) count);
    }

    public double getBombCertainty(){
        return ((double) bombed) / ((double) count);
    }

    public boolean equals(ActionResult localState){
        for(int j = 0; j < size; j++){
            for(int i = 0; i < size; i++){
                if(i == center && j == center){
                    // continue;
                }

                if(localState.x == i && localState.y == size - j - 1){
                    continue;
                }

                if(this.localState[j][i] != localState.localState[j][i]){
                    return false;
                }
            }
        }
        return true;
    }

    public void printLocalState(){
        for(int j = 0; j < size; j++){
            for(int i = 0; i < size; i++){
                System.out.print(localState[j][i] + "\t");
            }
            System.out.println();
        }
    }

    public void printDebug(){
        System.out.print("Count=" + count);
        System.out.print(" | ");
        System.out.print("Bombed=" + bombed);
        System.out.print(" | ");
        System.out.print("Marked=" + marked);
        System.out.print(" | ");
        System.out.print("Empty=" + empty);
        System.out.println();
    }
}
