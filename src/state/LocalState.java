package state;

import map.Map;

/**
 * Created by chewb on 3/22/2017.
 */
public class LocalState {

    public int x;
    public int y;
    public int size;
    public int[][] state;
    public int count = 1;

    public int marked = 0;
    public int bombed = 0;
    public int empty = 0;

    public LocalState(Map m, int x, int y, int size){
        this.x = x;
        this.y = y;
        this.size = size;
        this.state = new int[size][size];

        int offset = (size - 1) / 2;
        int i2 = 0, j2 = 0;
        for (int j = y - offset; j <= y + offset; j++){
            for (int i = x - offset; i <= x + offset; i++){
                if(i == x && j == y){
                    // we don't care what this value is because it is ignore in equality check
                    this.state[j2][i2] = 0;
                } else {
                    this.state[j2][i2] = m.look(i, j);
                }
                i2++;
            }
            i2 = 0;
            j2++;
        }
    }

    public boolean equals(LocalState localState){
        int center = (size - 1) / 2;
        for(int j = 0; j < size; j++){
            for(int i = 0; i < size; i++){
                if(i == center && j == center){
                    continue;
                }

                if(state[j][i] != localState.state[j][i]){
                    return false;
                }
            }
        }
        return true;
    }

    public double getBombProbability(){
        return ((double) bombed) / ((double) count);
    }

    public void print(){
        int center = (size - 1) / 2;
        for(int j = 0; j < size; j++){
            for(int i = 0; i < size; i++){
                if(i == center && j == center){
                    System.out.print("  ");
                } else {
                    System.out.print(state[size - j - 1][i] + " ");
                }

            }
            System.out.println();
        }
    }
}
