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

    public int marked = 0;
    public int bombed = 0;
    public int empty = 0;

    public ActionResult(Map m, int x, int y, int size){
        if(size % 2 == 0){
            throw new IllegalArgumentException("size cannot be even");
        }

        this.x = x;
        this.y = y;
        this.size = size;
        this.center = (size - 1) / 2;
        this.localState = new int[size][size];

        int i2 = 0, j2 = 0;
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
    }

    public double getBombProbability(){
        return ((double) bombed) / ((double) count);
    }

    public boolean equals(ActionResult localState){
        for(int j = 0; j < size; j++){
            for(int i = 0; i < size; i++){
                if(i == center && j == center){
                    continue;
                }

                if(this.localState[j][i] != localState.localState[j][i]){
                    return false;
                }
            }
        }
        return true;
    }

    public void print(){
        for(int j = 0; j < size; j++){
            for(int i = 0; i < size; i++){
                if(i == center && j == center){
                    System.out.print(" \t");
                } else {
                    // y coords start at 0 on bottom
                    System.out.print(localState[size - j - 1][i] + "\t");
                }

            }
            System.out.println();
        }
    }
}
