package vote;

/**
 * This is the Voting Strategy implementation.
 * @author Jacques Breaux
 */


import java.util.ArrayList;
import map.Strategy;
import map.Map;

public final class VoteStrategy implements Strategy {

	private int width;
	private int height;

	private ArrayList<Tile> fringe;
	private int[] votes;

	void initialize(Map m) {
		width = m.columns();
		height = m.rows();
		fringe = new ArrayList<Tile>();
		votes = new int[height * width];
		for (int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				votes[i * height + j] = 1000;
			}
		}
	}

	void ChooseRandom(Map m) {
		int x = m.pick(width);
		int y = m.pick(height);
		int q = m.probe(x,y);
		if(q == 0) {
			RevealSurrounding(x,y,m);	
		} else {
			fringe.add(new Tile(x,y));
		}
	}

	void RevealSurrounding(int x,int y, Map m) {
		int q;
		for(int i = x - 1; i <= x + 1 ; i++) {
			for(int j = y - 1; j <= y + 1 ; j++) {
				if(i == x && j == y) {
					continue;
				}
				q = m.look(i,j);
				if(q == Map.UNPROBED) {
					q = m.probe(i,j);
					if(q == 0) {
						RevealSurrounding(i,j,m);
					} else {
						fringe.add(new Tile(i,j));
					}
				}
			}
		}
	}

	void Vote() {
	}

	/**
	 * Invoke the Vote Strategy.
	 * @see Strategy
	 */
	public void play(Map m) {
		initialize(m);
		ChooseRandom(m);
		while(!m.done()) {
			
			ChooseRandom(m);
		}
	}

	class Tile {
		int x;
		int y;
		Tile(int xPos, int yPos) {
			x = xPos;
			y = yPos;
		}
	}
}
