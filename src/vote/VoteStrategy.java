package vote;

/**
 * This is the Voting Strategy implementation.
 * @author Jacques Breaux
 */


import java.util.ArrayList;
import java.util.Iterator;
import map.Strategy;
import map.Map;

public final class VoteStrategy implements Strategy {

	private int width;
	private int height;

	private ArrayList<Tile> fringe;
	private ArrayList<Tile> voteFringe;
	private Tile[][] board;

	void initialize(Map m) {
		width = m.columns();
		height = m.rows();
		fringe = new ArrayList<Tile>();
		voteFringe = new ArrayList<Tile>();
		board = new Tile[width][height];
		for (int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				board[i][j] = new Tile(i,j);
			}
		}
	}

	void ChooseRandom(Map m) {
		int x = m.pick(width);
		int y = m.pick(height);
		Reveal(x,y,m);
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
					Reveal(i,j,m);
				}
			}
		}
	}

	void Reveal(int x, int y, Map m) {
		voteFringe.remove(board[x][y]);
		fringe.add(board[x][y]);
		int q = m.probe(x,y);
		if(q == Map.BOOM)
			return;
		if(q == 0) {
			RevealSurrounding(x,y,m);
		} else {
			for(int i = x - 1; i <= x + 1; i++) {
				for(int j = y - 1; j <= y+1; j++) {
					if(m.look(i,j) == Map.UNPROBED && !voteFringe.contains(board[i][j])) {
						voteFringe.add(board[i][j]);
					}
				}
			}
		}
	}

	void VotePhase(Map m) {
		for(Tile t: voteFringe) {
			t.Reset();//Reset for votes
		}
		Iterator<Tile> iFringe = fringe.iterator();
		int unprobed = 0;
		while (iFringe.hasNext()) {
			Tile t = iFringe.next();
			unprobed = 0;
			for(int i = t.x - 1;i <= t.x + 1; i++) {
				for(int j = t.y - 1;j <= t.y + 1; j++) {
					if(m.look(i,j) == Map.UNPROBED || m.look(i,j) == Map.MARKED){
						unprobed++;//counting unprobed neighbors
					}
				}
			}
			if(unprobed == 0) { //remove from fringe, it's done
				iFringe.remove();
				continue;
			}
			double score = 1.0 - (m.look(t.x,t.y) / (double)unprobed);
			for(int x = t.x - 1;x <= t.x + 1; x++) {
				for(int y = t.y - 1;y <= t.y + 1; y++) {
					if(m.look(x,y) == Map.UNPROBED){
						if(score <= 5e-5) {//around me is bombs
							m.mark(x,y);
							voteFringe.remove(board[x][y]);//Clean up the vote fringe since i marked it as a bomb
						} else {
							board[x][y].Vote(score);
						}
					}
				}
			}
		}
	}

	Tile ChooseBest() {
		if(voteFringe.size() == 0) {
			return null;
		}
		Tile out = voteFringe.get(0);
		for(Tile t: voteFringe) {
			if(out.getScore() < t.getScore()) {
				out = t;
			}
		}
		return out;
	}

	/**
	 * Invoke the Vote Strategy.
	 * @see Strategy
	 */
	public void play(Map m) {
		initialize(m);
		ChooseRandom(m);
		while(!m.done()) {
			VotePhase(m);
			Tile guess = ChooseBest();
			if(guess == null || guess.getScore() < 0.3) {
				ChooseRandom(m);
			} else {
				Reveal(guess.x, guess.y, m);
			}
		}
		for(int y = height -1 ; y >= 0; y --) {
			for(int x = 0; x < width; x ++) {
				if(voteFringe.contains(board[x][y])) {
					System.out.printf("%.1f ",board[x][y].getScore());
				} else if (fringe.contains(board[x][y])) {
					System.out.print("!!! ");
				} else {
					System.out.print("??? ");
				}
			}
			System.out.println();
		}
	}

	class Tile {
		int x;
		int y;
		private int votes;
		private double score;
		Tile(int xPos, int yPos) {
			x = xPos;
			y = yPos;
			votes = 0;
			score = 0;
		}

		void Vote(double vote) {
			this.votes += 1;
			score += vote;
		}
		
		public double getScore() {
			return score/votes;
		}

		void Reset() {
			votes = 0;
			score = 0;
		}
	}
}
