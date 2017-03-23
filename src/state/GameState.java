package state;

import map.Map;

/**
 * Created by chewb on 3/22/2017.
 */
public class GameState {
    public int rows;
    public int columns;
    public int[][] state;
    public int count = 1;

    public GameState(Map m){
        rows = m.rows();
        columns = m.columns();
        state = new int[rows][columns];

        for(int x = 0; x < rows; x++){
            for(int y = 0; y < columns; y++){
                state[y][x] = m.look(y, x);
            }
        }
    }

    public boolean equals(GameState gameState){
        for(int x = 0; x < rows; x++){
            for(int y = 0; y < columns; y++){
                if(state[y][x] != gameState.state[y][x]){
                    return false;
                }
            }
        }
        return true;
    }

    public void print(){
        for(int x = 0; x < rows; x++){
            for(int y = 0; y < columns; y++){
                System.out.print(state[y][rows - x - 1] + " ");
            }
            System.out.println();
        }
    }
}
